package com.tp.donatrack.services;

import com.tp.commons.domain.notificador.TipoNotificador;
import com.tp.commons.services.notificador.NotificacionQueueClient;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;

import com.tp.donatrack.domain.donante.PerfilDonante;
import com.tp.donatrack.domain.donante.Donante;

import com.tp.donatrack.domain.persona.Persona;

import com.tp.donatrack.domain.importador.ImportadorCargaMasiva;
import com.tp.donatrack.domain.lectoresDeArchivos.iLectorArchivo;
import com.tp.donatrack.dtos.input.importacionCSV.RegistroDonanteDTO;
import com.tp.donatrack.repositories.DonacionRepository;
import com.tp.donatrack.repositories.DonanteRepository;

import com.tp.donatrack.dtos.*;
import com.tp.commons.dtos.incentivos.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class DonanteService {
    private final DonanteRepository donanteRepository;
    private final NotificacionQueueClient notificacionQueueClient;
    private static final Logger logger = LoggerFactory.getLogger(DonanteService.class);

    @Autowired
    private ImportadorCargaMasiva importadorCargaMasiva;

    @Autowired
    private List<iLectorArchivo> lectoresDeArchivos;

    public DonanteService(
            DonanteRepository donanteRepository,
            NotificacionQueueClient notificacionQueueClient,
            List<iLectorArchivo> lectores) {
        this.donanteRepository = donanteRepository;
        this.notificacionQueueClient = notificacionQueueClient;
        this.lectoresDeArchivos = lectores;
    }

    // CREATE
    public Donante registrar(Persona persona) {
        Donante donante = new Donante(persona);
        return donanteRepository.create(donante);
    }

    public synchronized Donante registrarConAcceso(Persona persona) {
        String email = persona.getMedioDeContacto().getOrDefault("EMAIL", List.of()).stream()
                .findFirst().orElse("").trim().toLowerCase(Locale.ROOT);
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Ingresá un correo válido.");
        }
        if (!donanteRepository.findByEmail(email).isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Ya hay un donante con ese correo.");
        }
        persona.setId(null);
        persona.reemplazarMedioDeContacto("EMAIL", email);
        if (persona.getMedioPredeterminado() == null || persona.getTipoNotificadorPreferido() == TipoNotificador.EMAIL) {
            persona.setMedioPredeterminado(Map.of("medio", "EMAIL", "valor", email));
        }
        persona.setFechaUltimaInteraccion(LocalDateTime.now());
        Donante donante = registrar(persona);
        enviarInvitacion(donante, email);
        return donante;
    }

    public synchronized Donante registrarPublicoVerificado(Persona persona, String emailVerificado) {
        String email = emailVerificado == null ? "" : emailVerificado.trim().toLowerCase(Locale.ROOT);
        String emailDeclarado = persona.getMedioDeContacto() == null ? "" : persona.getMedioDeContacto()
                .getOrDefault("EMAIL", List.of()).stream().findFirst().orElse("").trim().toLowerCase(Locale.ROOT);
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Ingresá un correo válido.");
        }
        if (!email.equals(emailDeclarado)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "El correo del formulario debe coincidir con el correo verificado por Google.");
        }

        List<Donante> existentes = donanteRepository.findByEmail(email);
        if (!existentes.isEmpty()) {
            if (existentes.size() != 1) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.CONFLICT, "El correo está asociado a más de un donante.");
            }
            return existentes.getFirst();
        }

        persona.setId(null);
        persona.reemplazarMedioDeContacto("EMAIL", email);
        if (persona.getMedioPredeterminado() == null || persona.getTipoNotificadorPreferido() == TipoNotificador.EMAIL) {
            persona.setMedioPredeterminado(Map.of("medio", "EMAIL", "valor", email));
        }
        persona.setFechaUltimaInteraccion(LocalDateTime.now());
        return registrar(persona);
    }

    @org.springframework.beans.factory.annotation.Value("${donatrack.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    private boolean enviarInvitacion(Donante donante, String email) {
        return notificacionQueueClient.notificar(TipoNotificador.EMAIL, email,
                "Ingresá con Google usando este mismo correo en " + frontendUrl + "/login",
                "Acceso a DonaTrack", donante.getId());
    }

    // READ
    public List<Donante> listarTodos() {
        // Limita la consulta en la base de datos para no cargar toda la tabla.
        return donanteRepository.findAll(PageRequest.of(0, 100, Sort.by("id")))
                .getContent();
    }

    // UPDATE TODO: aca actualizo la persona completa, el problema es que en un
    // futuro esto me va a cambiar el ID
    public Donante actualizar(String email, Persona personaActualizada) {
        Donante donante = buscarDonante(email);
        donante.setPersona(personaActualizada);
        return donanteRepository.create(donante);
    }

    // DELETE
    public void eliminar(String email) {
        Donante donanteAEliminar = buscarDonante(email);
        if (donanteAEliminar != null) {
            donanteRepository.delete(donanteAEliminar);
        }
    }

    public Donante buscarDonante(String email) {
        return this.donanteRepository.find(email);
    }

    private static final PageRequest RESULTADOS_BUSQUEDA = PageRequest.of(0, 20);

    public List<Donante> buscarPorCorreo(String email) {
        return donanteRepository.findByEmailFragment(email, RESULTADOS_BUSQUEDA);
    }

    public List<Donante> buscarPorDni(String dni) {
        return donanteRepository.findByDniFragment(dni, RESULTADOS_BUSQUEDA);
    }

    public Donante buscarDonantePorId(Long id) {
        return this.donanteRepository.findByIdOrNull(id);
    }

    public synchronized ImportacionResponseDTO importarDonantes(MultipartFile archivo) {
        try {
            // 1. Extraer la extensión del archivo (ej: "csv" o "xlsx")
            String nombreArchivo = Objects.toString(archivo.getOriginalFilename(), "");
            String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1);

            // 2. BUSCAR EL LECTOR ADECUADO (Principio de Sustitución de Liskov)
            iLectorArchivo lectorAdecuado = lectoresDeArchivos.stream()
                    .filter(lector -> lector.soportaExtension(extension))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Formato de archivo no soportado: " + extension));

            // 3. Capa Técnica: Leemos usando el polimorfismo
            List<RegistroDonanteDTO> registros = lectorAdecuado.leerArchivo(archivo);

            // 4. Capa de Negocio: Procesamos las reglas de negocio sobre esos objetos
            List<Donante> nuevos_donantes = importadorCargaMasiva.iniciar_migracion(registros);

            long invitacionesEncoladas = nuevos_donantes.stream()
                    .filter(d -> enviarInvitacion(d, d.getPersona().getMedioDeContacto().get("EMAIL").getFirst()))
                    .count();
            return new ImportacionResponseDTO(true, String.format(
                    "%d altas, %d filas actualizadas, %d invitaciones encoladas.",
                    nuevos_donantes.size(), registros.size() - nuevos_donantes.size(), invitacionesEncoladas));

        } catch (IllegalArgumentException e) {
            return new ImportacionResponseDTO(false, e.getMessage());
        } catch (RuntimeException e) {
            logger.error("Error al importar donantes", e);
            return new ImportacionResponseDTO(false, "No se pudo completar la importación.");
        }
    }

    public List<DonanteInactivoDTO> obtenerDonantesSinInteraccionMasDeDias(int dias) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);

        return donanteRepository.findAll().stream()
                .filter(donante -> donante.getPersona() != null &&
                        donante.getPersona().getMedioPredeterminado() != null &&
                        !donante.getPersona().getMedioPredeterminado().isEmpty() &&
                        donante.getPersona().getFechaUltimaInteraccion() != null &&
                        donante.getPersona().getFechaUltimaInteraccion().isBefore(fechaLimite) &&
                        (donante.getPersona().getFechaAvisoInactividad() == null ||
                                donante.getPersona().getFechaAvisoInactividad().isBefore(
                                        donante.getPersona().getFechaUltimaInteraccion())))
                .map(donante -> {
                    DonanteInactivoDTO dto = new DonanteInactivoDTO();
                    dto.setId(donante.getPersona().getId());
                    dto.setUltimaInteraccion(donante.getPersona().getFechaUltimaInteraccion());

                    Map<String, String> medioMap = donante.getPersona().getMedioPredeterminado();

                    if (medioMap.containsKey("medio")) {
                        dto.setContacto(medioMap.get("valor"));
                        try {
                            dto.setTipoNotificadorPreferido(
                                    TipoNotificador.valueOf(medioMap.get("medio").toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            System.err.println("Advertencia: El medio '" + medioMap.get("medio")
                                    + "' no coincide con ningún TipoNotificador.");
                        }
                    } else {
                        Map.Entry<String, String> entry = medioMap.entrySet().iterator().next();
                        dto.setContacto(entry.getValue());
                        try {
                            dto.setTipoNotificadorPreferido(TipoNotificador.valueOf(entry.getKey().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            System.err.println("Advertencia: El medio '" + entry.getKey()
                                    + "' no coincide con ningún TipoNotificador.");
                        }
                    }

                    return dto;
                })
                .filter(dto -> dto.getTipoNotificadorPreferido() != null)
                .collect(Collectors.toList());
    }

    @org.springframework.transaction.annotation.Transactional
    public boolean reservarAvisoInactividad(DonanteInactivoDTO aviso, LocalDateTime fecha) {
        return donanteRepository.reservarAvisoInactividad(aviso.getId(), aviso.getUltimaInteraccion(), fecha) == 1;
    }

    @org.springframework.transaction.annotation.Transactional
    public void liberarAvisoInactividad(Long id, LocalDateTime fecha) {
        donanteRepository.liberarAvisoInactividad(id, fecha);
    }

    public void notificarEntrega(Long donanteId) {
        try {
            Donante donante = donanteRepository.findByIdOrNull(donanteId);

            if (donante == null || donante.getPersona() == null) {
                logger.warn("No se encontró el donante o la persona para el ID: {}", donanteId);
                return;
            }

            Persona persona = donante.getPersona();

            boolean tieneMedioConfigurado = persona.getMedioPredeterminado() != null &&
                    !persona.getMedioPredeterminado().isEmpty();

            if (tieneMedioConfigurado) {
                java.util.Map<String, String> mapaMedio = persona.getMedioPredeterminado();

                String tipoString = mapaMedio.get("medio");
                String contacto = mapaMedio.get("valor");

                if (tipoString != null && contacto != null) {
                    TipoNotificador tipoNotificador = TipoNotificador.valueOf(tipoString.toUpperCase());

                    logger.info("Notificando a donante ID: {} vía {}", persona.getId(), tipoNotificador);

                    notificacionQueueClient.notificar(
                            tipoNotificador,
                            contacto,
                            "Se ha confirmado la recepción de la donación que ha realizado.",
                            "Confirmación de Entrega a Entidad Beneficiaria",
                            persona.getId());
                    logger.info("Notificación de donante {} enviada con éxito.", persona.getId());
                } else {
                    logger.warn("El JSON del medio predeterminado está incompleto para el donante {}", persona.getId());
                }

            } else {
                logger.warn("El donante ID {} no tiene un medio predeterminado configurado. No se envió notificación.",
                        persona.getId());
            }

        } catch (IllegalArgumentException e) {
            logger.error("ERROR DE ENUM: La clave en la base de datos no existe en TipoNotificador para el donante {}.",
                    donanteId, e);
        } catch (NullPointerException e) {
            logger.error("ERROR DE REFERENCIA NULA: Chequeá que notifService esté inicializado. Falló en donante {}.",
                    donanteId, e);
        } catch (Exception e) {
            logger.error("ERROR INESPERADO procesando el donante {}.", donanteId, e);
        }
    }

    public IndicadoresDonanteDTO calcularIndicadores(Long donanteId, DonacionSegmentada segmentada,
            List<String> indicadores) {
        Donante donante = donanteRepository.findByIdOrNull(donanteId);

        if (donante == null) {
            throw new RuntimeException("Donante no encontrado");
        }

        IndicadoresDonanteDTO.IndicadoresDonanteDTOBuilder builder = IndicadoresDonanteDTO.builder();
        var entregadas = donacionRepository.findByDonanteId(donanteId).stream()
                .flatMap(d -> d.getDonacionesSegmentadas().stream())
                .filter(s -> com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada.ENTREGADA.equals(s.getEstado()))
                .toList();

        if (indicadores.contains("CANTIDAD_BIENES")) {
            builder.cantidadBienesTotal(segmentada.getCantidad());
        }
        if (indicadores.contains("CATEGORIAS_DISTINTAS")) {
            builder.cantidadCategoriasUnicas((int) entregadas.stream()
                    .map(DonacionSegmentada::getCategoriaBien).filter(Objects::nonNull).distinct().count());
        }
        if (indicadores.contains("MESES_CONSECUTIVOS")) {
            Set<java.time.YearMonth> meses = entregadas.stream()
                    .flatMap(s -> s.getHistorial().stream())
                    .filter(e -> com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada.ENTREGADA.equals(e.getEstadoNuevo()))
                    .map(e -> java.time.YearMonth.from(e.getFecha()))
                    .collect(Collectors.toSet());
            java.time.YearMonth mes = java.time.YearMonth.now();
            if (!meses.contains(mes)) mes = mes.minusMonths(1);
            int racha = 0;
            while (meses.contains(mes)) {
                racha++;
                mes = mes.minusMonths(1);
            }
            builder.mesesConsecutivosRacha(racha);
        }
        if (indicadores.contains("ENTREGAS_EXITOSAS_TOTALES")) {
            builder.cantidadDonacionesEntregadas(entregadas.size());
        }

        return builder.build();
    }

    @Autowired
    private DonacionRepository donacionRepository;

    public void registrarEntregaEnPerfil(Long donanteId, DonacionSegmentada segmentada) {
        Donante donante = this.buscarDonantePorId(donanteId);
        donante.getPerfil().registrarEntrega(segmentada);
    }

    public MetricasActividadDTO obtenerMetricas(Long donanteId) {
        Donante donante = buscarDonantePorId(donanteId);
        if (donante == null)
            throw new RuntimeException("Donante no encontrado");
        PerfilDonante perfil = donante.getPerfil();

        var donaciones = donacionRepository.findByDonanteId(donanteId);
        List<DonacionSegmentada> segmentadas = donaciones.stream()
                .flatMap(d -> d.getDonacionesSegmentadas().stream())
                .collect(Collectors.toList());

        List<Long> entidadesIds = segmentadas.stream()
                .filter(ds -> com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada.ENTREGADA.equals(ds.getEstado()))
                .map(DonacionSegmentada::getEntidadBeneficiariaAsignadaId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        int totalExitosas = (int) segmentadas.stream()
                .filter(ds -> com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada.ENTREGADA.equals(ds.getEstado()))
                .count();
        java.time.YearMonth periodo = java.time.YearMonth.now();
        Map<String, Integer> agrupado = donaciones.stream()
                .filter(d -> d.getFechaIngreso() != null)
                .map(d -> d.getFechaIngreso().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate())
                .collect(Collectors.groupingBy(fecha -> fecha.getYear() + "-" + fecha.getMonthValue(),
                        Collectors.summingInt(fecha -> 1)));
        int donacionesMesActual = agrupado.getOrDefault(periodo.getYear() + "-" + periodo.getMonthValue(), 0);

        List<RegistroDonacionMensualDTO> comparaciones = agrupado.entrySet().stream()
                .map(e -> {
                    String[] parts = e.getKey().split("-");
                    return RegistroDonacionMensualDTO.builder()
                            .anio(Integer.parseInt(parts[0]))
                            .mes(Integer.parseInt(parts[1]))
                            .totalDonaciones(e.getValue())
                            .build();
                })
                .collect(Collectors.toList());

        int misionesCompletadas = perfil.getTotalMisionesCompletadas();

        return MetricasActividadDTO.builder()
                .donanteId(donanteId.intValue())
                .categoriaDonante(perfil.getNivelDonante().getNombre())
                .totalDonacionesExitosas(totalExitosas)
                .entidadesAyudadasCount(entidadesIds.size())
                .entidadesAyudadasIds(entidadesIds.stream().map(Long::intValue).collect(Collectors.toList()))
                .donacionesMesActual(donacionesMesActual)
                .mesPeriodoActual(java.time.LocalDate.now().getMonthValue())
                .anioPeriodoActual(java.time.LocalDate.now().getYear())
                .comparacionesMensuales(comparaciones)
                .misionesCompletadasCount(misionesCompletadas)
                .build();
    }

    public PerfilDonanteDTO obtenerPerfilDonante(Long donanteId) {
        Donante donante = buscarDonantePorId(donanteId);
        if (donante == null)
            throw new RuntimeException("Donante no encontrado");
        PerfilDonante perfil = donante.getPerfil();

        int misionesCompletadas = perfil.getTotalMisionesCompletadas();

        return PerfilDonanteDTO.builder()
                .visibilidadInsignia(perfil.isVisibilidadInsignia())
                .categoriaDonante(perfil.getNivelDonante())
                .misionActualId(perfil.getMisionActualId())
                .progreso(perfil.getProgreso())
                .insigniasGanadas(perfil.getInsigniasGanadas())
                .totalMisionesCompletadas(misionesCompletadas)
                .build();
    }

    public List<com.tp.commons.dtos.incentivos.DonanteRachaDTO> obtenerDonantesConMisionActual() {
        return donanteRepository.findAll().stream()
                .filter(d -> d.getPerfil() != null && d.getPerfil().getMisionActualId() != null)
                .map(d -> com.tp.commons.dtos.incentivos.DonanteRachaDTO.builder()
                        .donanteId(d.getPersona().getId())
                        .misionActualId(d.getPerfil().getMisionActualId())
                        .nivel(d.getPerfil().getNivelDonante())
                        .build())
                .collect(Collectors.toList());
    }

    public java.time.LocalDate obtenerFechaUltimaDonacion(Long donanteId) {
        Donante donante = buscarDonantePorId(donanteId);
        if (donante == null || donante.getPerfil() == null
                || donante.getPerfil().getHistorialDonaciones() == null
                || donante.getPerfil().getHistorialDonaciones().isEmpty()) {
            return null;
        }

        return donante.getPerfil().getHistorialDonaciones().stream()
                .map(item -> item.getFecha())
                .max(java.time.LocalDate::compareTo)
                .orElse(null);
    }

    public void resetearProgresoRacha(Long donanteId) {
        Donante donante = buscarDonantePorId(donanteId);
        if (donante == null) {
            throw new RuntimeException("Donante no encontrado con ID: " + donanteId);
        }
        donante.getPerfil().setProgreso(0.0);
        donanteRepository.update(donante);
        logger.info("Progreso de racha reseteado para donante ID: {}", donanteId);
    }
}
