package com.tp.donatrack.domain.importador;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.persona.*;
import com.tp.donatrack.dtos.input.importacionCSV.RegistroDonanteDTO;
import com.tp.donatrack.repositories.DonanteRepository;
import com.tp.donatrack.utils.security.CryptoUtils;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Component
public class ImportadorCargaMasiva {
    private static final Logger logger = LoggerFactory.getLogger(ImportadorCargaMasiva.class);
    private static final int BATCH_SIZE = 1000;

    DonanteRepository donanteRepository;

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    private EntityManager entityManager;

    private record PasswordPar(String plain, String hash) {}
    private record DonanteExistenteInfo(Long idDonante, String tipoPersona) {}

    private static volatile List<PasswordPar> POOL_PASSWORDS;

    public ImportadorCargaMasiva(DonanteRepository donanteRepo) {
        this.donanteRepository = donanteRepo;
    }

    private List<PasswordPar> obtenerPoolPasswords() {
        if (POOL_PASSWORDS == null) {
            synchronized (ImportadorCargaMasiva.class) {
                if (POOL_PASSWORDS == null) {
                    POOL_PASSWORDS = IntStream.range(0, 64)
                            .parallel()
                            .mapToObj(i -> {
                                String plain = generarPassword();
                                return new PasswordPar(plain, CryptoUtils.hashear(plain));
                            })
                            .toList();
                }
            }
        }
        return POOL_PASSWORDS;
    }

    public List<Donante> iniciar_migracion(List<RegistroDonanteDTO> registros) {
        if (registros == null || registros.isEmpty()) {
            return new ArrayList<>();
        }

        // Si JdbcTemplate está disponible (ejecución en Spring Boot), usamos procesamiento masivo por lotes
        if (jdbcTemplate != null) {
            return iniciarMigracionBatchJdbc(registros);
        }

        // Fallback para tests unitarios sin JdbcTemplate
        List<Donante> nuevosDonantes = new ArrayList<>();
        for (RegistroDonanteDTO registro : registros) {
            Donante existe = donanteRepository.find(registro.getEmail());
            if (existe == null) {
                Donante nuevo_donante = crearDonanteSegunTipoPersona(registro);
                String password = generarPassword();
                String password_hash = CryptoUtils.hashear(password);
                nuevo_donante.setPassword(password_hash);
                donanteRepository.create(nuevo_donante);
                nuevo_donante.setPassword(password);
                nuevosDonantes.add(nuevo_donante);
            } else {
                actualizarCamposDonante(existe, registro);
            }
        }
        return nuevosDonantes;
    }

    private List<Donante> iniciarMigracionBatchJdbc(List<RegistroDonanteDTO> registros) {
        long startMs = System.currentTimeMillis();

        // Sincronizar cambios pendientes del contexto de persistencia JPA antes de consultar por JDBC
        if (entityManager != null) {
            entityManager.flush();
        } else {
            donanteRepository.flush();
        }

        // 1. Deduplicar registros dentro del propio CSV por email en O(N) (conservando la última aparición)
        Map<String, RegistroDonanteDTO> registrosPorEmail = new LinkedHashMap<>(registros.size());
        for (RegistroDonanteDTO reg : registros) {
            if (reg.getEmail() == null || reg.getEmail().isBlank()) {
                continue;
            }
            registrosPorEmail.put(reg.getEmail().trim().toLowerCase(), reg);
        }

        // 2. Consultar todos los emails existentes en la base en UNA SOLA consulta SQL
        Map<String, DonanteExistenteInfo> existentesEnBase = new HashMap<>();
        jdbcTemplate.query(
                "SELECT d.id_donante, LOWER(TRIM(m.valor)) AS email, " +
                "CASE WHEN ph.id_persona IS NOT NULL THEN 'HUMANA' ELSE 'JURIDICA' END AS tipo_persona " +
                "FROM donantes d " +
                "JOIN medios_de_contacto m ON m.id_persona = d.id_donante " +
                "LEFT JOIN personas_humanas ph ON ph.id_persona = d.id_donante " +
                "WHERE m.valor IS NOT NULL",
                rs -> {
                    String em = rs.getString("email");
                    if (em != null && !em.isEmpty()) {
                        existentesEnBase.putIfAbsent(
                                em,
                                new DonanteExistenteInfo(rs.getLong("id_donante"), rs.getString("tipo_persona"))
                        );
                    }
                }
        );

        // 3. Clasificar en memoria cuáles son ALTAS (nuevos) y cuáles son ACTUALIZACIONES (duplicados)
        List<RegistroDonanteDTO> paraInsertar = new ArrayList<>();
        List<RegistroDonanteDTO> paraActualizar = new ArrayList<>();

        for (Map.Entry<String, RegistroDonanteDTO> entry : registrosPorEmail.entrySet()) {
            if (existentesEnBase.containsKey(entry.getKey())) {
                paraActualizar.add(entry.getValue());
            } else {
                paraInsertar.add(entry.getValue());
            }
        }

        logger.info("Carga masiva CSV: {} registros únicos ({} nuevos para insertar, {} existentes para actualizar).",
                registrosPorEmail.size(), paraInsertar.size(), paraActualizar.size());

        List<Donante> nuevosDonantes = new ArrayList<>(paraInsertar.size());

        // 4. Procesar ALTAS masivas en bloques (multi-row INSERT)
        if (!paraInsertar.isEmpty()) {
            List<PasswordPar> poolPasswords = obtenerPoolPasswords();
            for (int i = 0; i < paraInsertar.size(); i += BATCH_SIZE) {
                List<RegistroDonanteDTO> lote = paraInsertar.subList(i, Math.min(i + BATCH_SIZE, paraInsertar.size()));
                insertarLoteJdbc(lote, poolPasswords, nuevosDonantes);
            }
        }

        // 5. Procesar ACTUALIZACIONES masivas en bloques (batch UPDATE)
        if (!paraActualizar.isEmpty()) {
            for (int i = 0; i < paraActualizar.size(); i += BATCH_SIZE) {
                List<RegistroDonanteDTO> lote = paraActualizar.subList(i, Math.min(i + BATCH_SIZE, paraActualizar.size()));
                actualizarLoteJdbc(lote, existentesEnBase);
            }
        }

        // Limpiar la caché de primer nivel de Hibernate para que las lecturas posteriores reflejen los cambios JDBC
        if (entityManager != null) {
            entityManager.flush();
            entityManager.clear();
        }

        long elapsedMs = System.currentTimeMillis() - startMs;
        logger.info("Carga masiva CSV completada en {} ms ({} insertados, {} actualizados).",
                elapsedMs, paraInsertar.size(), paraActualizar.size());

        return nuevosDonantes;
    }

    private void insertarLoteJdbc(List<RegistroDonanteDTO> lote, List<PasswordPar> poolPasswords, List<Donante> nuevosDonantes) {
        int n = lote.size();
        // 1. Insertar las N filas en `personas` en una sola sentencia multi-row y recuperar los IDs autogenerados
        StringBuilder sqlPersonas = new StringBuilder(
                "INSERT INTO personas (fecha_ultima_interaccion, tipo_notificador, id_direccion) VALUES ");
        for (int i = 0; i < n; i++) {
            if (i > 0) sqlPersonas.append(',');
            sqlPersonas.append("(NOW(), 'EMAIL', NULL)");
        }

        List<Long> idsGenerados = jdbcTemplate.execute((java.sql.Connection con) -> {
            try (PreparedStatement ps = con.prepareStatement(sqlPersonas.toString(), Statement.RETURN_GENERATED_KEYS)) {
                ps.executeUpdate();
                List<Long> ids = new ArrayList<>(n);
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    while (rs.next()) {
                        ids.add(rs.getLong(1));
                    }
                }
                return ids;
            }
        });

        if (idsGenerados == null || idsGenerados.size() != n) {
            throw new IllegalStateException("No se pudieron recuperar todos los IDs generados para el lote de personas.");
        }

        // 2. Preparar lotes para personas_humanas, personas_juridicas, donantes y medios_de_contacto
        List<Object[]> batchHumanas = new ArrayList<>();
        List<Object[]> batchJuridicas = new ArrayList<>();
        List<Object[]> batchDonantes = new ArrayList<>();
        List<Object[]> batchMedios = new ArrayList<>(n * 2);

        String nivelInicial = Nivel.COLABORADOR.getNombre();

        for (int i = 0; i < n; i++) {
            Long idPersona = idsGenerados.get(i);
            RegistroDonanteDTO reg = lote.get(i);
            PasswordPar pw = poolPasswords.get(i % poolPasswords.size());

            boolean esHumana = TipoPersona.HUMANA.name().equalsIgnoreCase(reg.getTipoPersona());
            String nombreRaw = reg.getNombre() != null ? reg.getNombre().trim() : "";

            if (esHumana) {
                String[] partes = nombreRaw.split(" ", 2);
                String nombre = partes[0];
                String apellido = partes.length > 1 ? partes[1] : "";
                String docLimpio = reg.getDocumento() != null ? reg.getDocumento().replaceAll("[^0-9]", "") : "";
                batchHumanas.add(new Object[]{idPersona, nombre, "Sin especificar", apellido, docLimpio});
            } else {
                String tipoOrg = detectarTipo(nombreRaw).name();
                batchJuridicas.add(new Object[]{idPersona, nombreRaw, tipoOrg, "Sin especificar"});
            }

            batchDonantes.add(new Object[]{idPersona, pw.hash(), nivelInicial});

            if (reg.getEmail() != null && !reg.getEmail().isBlank()) {
                batchMedios.add(new Object[]{idPersona, "EMAIL", reg.getEmail().trim(), true});
            }
            if (reg.getTelefono() != null && !reg.getTelefono().isBlank()) {
                batchMedios.add(new Object[]{idPersona, "SMS", reg.getTelefono().trim(), false});
            }

            Donante nuevoDonante = crearDonanteSegunTipoPersona(reg);
            nuevoDonante.getPersona().setId(idPersona);
            nuevoDonante.setId(idPersona);
            nuevoDonante.setPassword(pw.plain());
            nuevosDonantes.add(nuevoDonante);
        }

        if (!batchHumanas.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "INSERT INTO personas_humanas (id_persona, nombre, genero, apellido, fecha_nacimiento, edad, nro_documento) " +
                    "VALUES (?, ?, ?, ?, NULL, 0, ?)",
                    batchHumanas
            );
        }

        if (!batchJuridicas.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "INSERT INTO personas_juridicas (id_persona, razon_social, tipo_organizacion, cuit, rubro) " +
                    "VALUES (?, ?, ?, NULL, ?)",
                    batchJuridicas
            );
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO donantes (id_donante, password_hash, visibilidad_insignia, mision_actual_id, " +
                "progreso_mision_actual, nivel_donante, total_donaciones_exitosas, total_misiones_completadas, fecha_ultima_donacion) " +
                "VALUES (?, ?, true, 1, 0.0, ?, 0, 0, NULL)",
                batchDonantes
        );

        if (!batchMedios.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "INSERT INTO medios_de_contacto (id_persona, tipo_notificador, valor, es_predeterminado) " +
                    "VALUES (?, ?, ?, ?)",
                    batchMedios
            );
        }
    }

    private void actualizarLoteJdbc(List<RegistroDonanteDTO> lote, Map<String, DonanteExistenteInfo> existentesEnBase) {
        List<Object[]> updateHumanas = new ArrayList<>();
        List<Object[]> updateJuridicas = new ArrayList<>();
        List<Object[]> updateTelefono = new ArrayList<>();

        for (RegistroDonanteDTO reg : lote) {
            String emailKey = reg.getEmail().trim().toLowerCase();
            DonanteExistenteInfo info = existentesEnBase.get(emailKey);
            if (info == null) continue;

            Long idPersona = info.idDonante();
            boolean nuevoEsHumana = TipoPersona.HUMANA.name().equalsIgnoreCase(reg.getTipoPersona());
            boolean actualEsHumana = TipoPersona.HUMANA.name().equalsIgnoreCase(info.tipoPersona());
            String nombreRaw = reg.getNombre() != null ? reg.getNombre().trim() : "";

            // Si cambió el tipo de persona (HUMANA <-> JURIDICA), usamos la lógica individual de migración de subclase
            if (nuevoEsHumana != actualEsHumana) {
                Donante existente = donanteRepository.findByIdOrNull(idPersona);
                if (existente != null) {
                    actualizarCamposDonante(existente, reg);
                }
                continue;
            }

            if (nuevoEsHumana) {
                String[] partes = nombreRaw.split(" ", 2);
                String nombre = partes[0];
                String apellido = partes.length > 1 ? partes[1] : "";
                String docLimpio = reg.getDocumento() != null ? reg.getDocumento().replaceAll("[^0-9]", "") : "";
                updateHumanas.add(new Object[]{nombre, apellido, docLimpio, idPersona});
            } else {
                String tipoOrg = detectarTipo(nombreRaw).name();
                updateJuridicas.add(new Object[]{nombreRaw, tipoOrg, idPersona});
            }

            if (reg.getTelefono() != null && !reg.getTelefono().isBlank()) {
                updateTelefono.add(new Object[]{reg.getTelefono().trim(), idPersona});
            }
        }

        if (!updateHumanas.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "UPDATE personas_humanas SET nombre = ?, apellido = ?, nro_documento = ? WHERE id_persona = ?",
                    updateHumanas
            );
        }

        if (!updateJuridicas.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "UPDATE personas_juridicas SET razon_social = ?, tipo_organizacion = ? WHERE id_persona = ?",
                    updateJuridicas
            );
        }

        if (!updateTelefono.isEmpty()) {
            jdbcTemplate.batchUpdate(
                    "UPDATE medios_de_contacto SET valor = ? WHERE id_persona = ? AND tipo_notificador = 'SMS'",
                    updateTelefono
            );
        }
    }

    private String generarPassword() {
        String[] palabras = {"luna", "rio", "mate", "sol", "verde", "auto", "nube", "casa", "mar", "flor"};
        SecureRandom random = new SecureRandom();

        String palabra = palabras[random.nextInt(palabras.length)];
        int numero = random.nextInt(90) + 10; // 10 a 99

        // opcional: primera letra mayúscula a veces
        if (random.nextBoolean()) {
            palabra = palabra.substring(0, 1).toUpperCase() + palabra.substring(1);
        }

        return palabra + numero;
    }

    private Donante crearDonanteSegunTipoPersona(RegistroDonanteDTO registro) {
        Donante donante = new Donante();
        Persona persona;

        if (registro.getTipoPersona().equals(TipoPersona.HUMANA.name())) {
            String[] nombreSplit = registro.getNombre().split(" ", 2);
            String nombrePersona = nombreSplit[0];
            String apellido = nombreSplit.length > 1 ? nombreSplit[1] : "";
            String documentoLimpio = registro.getDocumento().replaceAll("[^0-9]", "");

            persona = new PersonaHumana(nombrePersona, "Sin especificar", apellido, null, 0, documentoLimpio);
        } else {
            PersonaJuridica pj = new PersonaJuridica();
            pj.setRazonSocial(registro.getNombre());
            pj.setTipo(detectarTipo(registro.getNombre()));
            pj.setRubro("Sin especificar");
            persona = pj;
        }

        persona.agregarMedioDeContacto("EMAIL", registro.getEmail());
        persona.agregarMedioDeContacto("SMS", registro.getTelefono());
        donante.setPersona(persona);

        return donante;
    }

    private void actualizarCamposDonante(Donante donante, RegistroDonanteDTO dto) {
        Persona personaActual = donante.getPersona();
        String tipoActual = personaActual instanceof PersonaHumana ? TipoPersona.HUMANA.name() : TipoPersona.JURIDICA.name();
        boolean cambioTipoPersona = !tipoActual.equalsIgnoreCase(dto.getTipoPersona());

        if (cambioTipoPersona) {
            // Al cambiar el tipo de persona (humana <-> jurídica) cambia la subclase
            // persistida. Con herencia JOINED no es posible mutar el tipo de una fila
            // existente, por lo que se elimina el donante actual y se recrea con una
            // nueva identidad asignada por la base.
            Persona nuevaPersona;

            if (dto.getTipoPersona().equalsIgnoreCase(TipoPersona.HUMANA.name())) {
                String[] nombreSplit = dto.getNombre().split(" ", 2);
                String nombrePersona = nombreSplit[0];
                String apellido = nombreSplit.length > 1 ? nombreSplit[1] : "";
                String documentoLimpio = dto.getDocumento().replaceAll("[^0-9]", "");

                nuevaPersona = new PersonaHumana(nombrePersona, "Sin especificar", apellido, null, 0, documentoLimpio);
            } else {
                PersonaJuridica pj = new PersonaJuridica();
                pj.setRazonSocial(dto.getNombre());
                pj.setTipo(detectarTipo(dto.getNombre()));
                pj.setRubro("Sin especificar");
                nuevaPersona = pj;
            }

            nuevaPersona.setDireccion(personaActual.getDireccion());
            nuevaPersona.setMedioPredeterminado(personaActual.getMedioPredeterminado());
            nuevaPersona.setFechaUltimaInteraccion(personaActual.getFechaUltimaInteraccion());

            nuevaPersona.agregarMedioDeContacto("EMAIL", dto.getEmail());
            nuevaPersona.agregarMedioDeContacto("SMS", dto.getTelefono());

            donanteRepository.delete(donante);
            donanteRepository.flush();

            Donante nuevoDonante = new Donante();
            nuevoDonante.setPersona(nuevaPersona);
            nuevoDonante.setPassword(donante.getPassword());
            donanteRepository.save(nuevoDonante);
            return;
        } else {
            // Mismo tipo de persona: actualizar campos individuales
            if (personaActual instanceof PersonaHumana ph) {
                String[] nombreSplit = dto.getNombre().split(" ", 2);
                ph.setNombre(nombreSplit[0]);
                ph.setApellido(nombreSplit.length > 1 ? nombreSplit[1] : "");
                ph.setNroDocumento(dto.getDocumento().replaceAll("[^0-9]", ""));
            } else if (personaActual instanceof PersonaJuridica pj) {
                pj.setRazonSocial(dto.getNombre());
                pj.setTipo(detectarTipo(dto.getNombre()));
            }

            personaActual.reemplazarMedioDeContacto("SMS", dto.getTelefono());
        }

        donanteRepository.update(donante);
        System.out.println("El donante " + dto.getEmail() + " ya se encontraba registrado. Actualizado correctamente.");
    }

    private TipoOrganizacion detectarTipo(String razonSocial) {
        String n = razonSocial.toLowerCase();

        if (n.contains("s.a") || n.contains("s.a.") || n.contains("s.r.l") ||
                n.contains("s.r.l.") || n.contains("srl") || n.contains("sa") || n.contains("sas")) {
            return TipoOrganizacion.EMPRESA;
        }
        if (n.contains("ong")) {
            return TipoOrganizacion.ONG;
        }
        if (n.contains("ministerio") || n.contains("municipalidad") || n.contains("gobierno")) {
            return TipoOrganizacion.GUBERNAMENTAL;
        }
        return TipoOrganizacion.INSTITUCION;
    }


}
