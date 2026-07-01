package com.tp.donatrack.services;

import com.tp.commons.domain.notificador.TipoNotificador;
import com.tp.commons.services.notificador.NotificacionRestClient;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;

import com.tp.donatrack.domain.donante.PerfilDonante;
import com.tp.donatrack.domain.donante.Donante;

import com.tp.donatrack.domain.persona.Persona;

import com.tp.donatrack.domain.importador.ImportadorCargaMasiva;
import com.tp.donatrack.domain.lectoresDeArchivos.iLectorArchivo;
import com.tp.donatrack.repositories.DonacionRepository;
import com.tp.donatrack.repositories.DonanteRepository;

import com.tp.donatrack.dtos.*;
import com.tp.commons.dtos.incentivos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DonanteService {
    private final DonanteRepository donanteRepository;
    private final NotificacionRestClient notificacionRestClient;
    private static final Logger logger = LoggerFactory.getLogger(DonanteService.class);

    @Autowired
    private ImportadorCargaMasiva importadorCargaMasiva;

    @Autowired
    private List<iLectorArchivo> lectoresDeArchivos;

    public DonanteService(
            DonanteRepository donanteRepository,
            NotificacionRestClient notifService,
            List<iLectorArchivo> lectores) {
        this.donanteRepository = donanteRepository;
        this.notificacionRestClient = notifService;
        this.lectoresDeArchivos = lectores;
    }

    // CREATE
    public Donante registrar(Persona persona) {
        Donante donante = new Donante(persona);
        return donanteRepository.create(donante);
    }

    // READ
    public List<Donante> listarTodos() {
        return donanteRepository.findAll();
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

    public Donante buscarDonantePorId(Long id) {
        return this.donanteRepository.findById(id);
    }

    public ImportacionResponseDTO importarDonantes(MultipartFile archivo) {
        try {
            // 1. Extraer la extensión del archivo (ej: "csv" o "xlsx")
            String nombreArchivo = archivo.getOriginalFilename();
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

            for (Donante donante : nuevos_donantes) {
                Persona persona = donante.getPersona();
                String email = persona.getMedioDeContacto().get("email").getFirst();
                notificacionRestClient.notificar(
                        TipoNotificador.EMAIL,
                        email,
                        "Gracias por sumarte como donante...",
                        "¡Bienvenido a Donatrack!",
                        persona.getId());
            }
            // 5. Respuesta de la aplicación
            String mensaje = "Importados " + registros.size() + " registros exitosamente";
            ImportacionResponseDTO response = new ImportacionResponseDTO(true, mensaje);
            response.setData(this.donanteRepository.findAll());
            return response;

        } catch (Exception e) {
            return new ImportacionResponseDTO(false, "Error al importar: " + e.getMessage());
        }
    }

    public List<DonanteInactivoDTO> obtenerDonantesSinInteraccionMasDeDias(int dias) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);

        return donanteRepository.findAll().stream()
                .filter(donante -> donante.getPersona() != null &&
                        donante.getPersona().getMedioPredeterminado() != null &&
                        !donante.getPersona().getMedioPredeterminado().isEmpty() &&
                        donante.getPersona().getFechaUltimaInteraccion() != null &&
                        donante.getPersona().getFechaUltimaInteraccion().isBefore(fechaLimite))
                .map(donante -> {
                    DonanteInactivoDTO dto = new DonanteInactivoDTO();
                    dto.setId(donante.getPersona().getId());

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

    public void notificarEntrega(Long donanteId) {
        try {
            Donante donante = donanteRepository.findById(donanteId);

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

                    notificacionRestClient.notificar(
                            tipoNotificador,
                            contacto,
                            "Se ha confirmado la recepción de la donación.",
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
        Donante donante = donanteRepository.findById(donanteId);

        if (donante == null) {
            throw new RuntimeException("Donante no encontrado");
        }

        PerfilDonante perfil = donante.getPerfil();

        IndicadoresDonanteDTO.IndicadoresDonanteDTOBuilder builder = IndicadoresDonanteDTO.builder();

        if (indicadores.contains("CANTIDAD_BIENES")) {
            builder.cantidadBienesTotal(segmentada.getCantidad());
        }

        if (indicadores.contains("CATEGORIAS_DISTINTAS")) {
            builder.cantidadCategoriasUnicas(perfil.contarCategoriasUnicas());
        }

        if (indicadores.contains("MESES_CONSECUTIVOS")) {
            builder.mesesConsecutivosRacha(perfil.calcularRachaMeses());
        }

        if (indicadores.contains("ENTREGAS_EXITOSAS_TOTALES")) {
            builder.cantidadDonacionesEntregadas(perfil.calcularDonacionesAEntidadesBeneficiarias());
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

        List<DonacionSegmentada> segmentadas = donacionRepository.findByDonanteId(donanteId).stream()
                .flatMap(d -> d.getDonacionesSegmentadas().stream())
                .collect(Collectors.toList());

        List<Long> entidadesIds = segmentadas.stream()
                .filter(ds -> ds.getEstado() == com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada.ENTREGADA)
                .map(DonacionSegmentada::getEntidadBeneficiariaAsignadaId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        int totalExitosas = perfil.calcularCantidadDonacionesEntregadas();

        int donacionesMesActual = (int) perfil.getHistorialDonaciones().stream()
                .filter(item -> item.getFecha().getMonthValue() == java.time.LocalDate.now().getMonthValue() &&
                        item.getFecha().getYear() == java.time.LocalDate.now().getYear())
                .count();

        Map<String, Integer> agrupado = perfil.getHistorialDonaciones().stream()
                .collect(Collectors.groupingBy(
                        item -> item.getFecha().getYear() + "-" + item.getFecha().getMonthValue(),
                        Collectors.summingInt(x -> 1)));

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

        int misionesCompletadas = perfil.getMetricasPerfil() != null
                && perfil.getMetricasPerfil().getMisionesCompletadas() != null
                        ? perfil.getMetricasPerfil().getMisionesCompletadas().size()
                        : 0;

        return MetricasActividadDTO.builder()
                .donanteId(donanteId.intValue())
                .categoriaDonante(perfil.getNivelDonante().name())
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

        return PerfilDonanteDTO.builder()
                .visibilidadInsignia(perfil.isVisibilidadInsignia())
                .categoriaDonante(perfil.getNivelDonante())
                .misionActualId(perfil.getMisionActualId())
                .progreso(perfil.getProgreso())
                .insigniasGanadas(perfil.getInsigniasGanadas())
                .misionesCompletadasIds(perfil.getMetricasPerfil() != null
                        && perfil.getMetricasPerfil().getMisionesCompletadas() != null
                                ? perfil.getMetricasPerfil().getMisionesCompletadas()
                                : new ArrayList<>())
                .build();
    }
}