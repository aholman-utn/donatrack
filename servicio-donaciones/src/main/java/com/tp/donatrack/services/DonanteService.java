package com.tp.donatrack.services;

import com.tp.commons.domain.notificador.TipoNotificador;
import com.tp.commons.services.notificador.NotificacionRestClient;

import com.tp.donatrack.domain.importador.ImportadorCargaMasiva;
import com.tp.donatrack.domain.persona.Persona;
import com.tp.donatrack.domain.lectoresDeArchivos.iLectorArchivo;
import com.tp.donatrack.dtos.DonanteInactivoDTO;
import com.tp.donatrack.dtos.ImportacionResponseDTO;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.donante.DonanteCreadoEvent;
import com.tp.donatrack.domain.donante.DonanteEventPublisher;
import com.tp.donatrack.dtos.input.importacionCSV.RegistroDonanteDTO;
import com.tp.donatrack.repositories.DonanteRepository;
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
    private final DonanteEventPublisher eventPublisher;
    private static final Logger logger = LoggerFactory.getLogger(DonanteService.class);

    @Autowired
    private ImportadorCargaMasiva importadorCargaMasiva;

    @Autowired
    private List<iLectorArchivo> lectoresDeArchivos;

    public DonanteService(
            DonanteRepository donanteRepository,
            NotificacionRestClient notifService,
            List<iLectorArchivo> lectores,
            DonanteEventPublisher eventPublisher
    ) {
        this.donanteRepository = donanteRepository;
        this.notificacionRestClient = notifService;
        this.lectoresDeArchivos = lectores;
        this.eventPublisher = eventPublisher;
    }

    // CREATE
    public Donante registrar(Persona persona) {
        Donante donante = new Donante(persona);
        Donante nuevoDonante = donanteRepository.create(donante);
        if (nuevoDonante != null) {
            eventPublisher.publicar(new DonanteCreadoEvent(nuevoDonante.getId(), nuevoDonante.getNombreCompleto()));
        }
        return nuevoDonante;
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

    public Donante buscarDonantePorId(Integer id) {
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
                        persona.getId()
                );
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

    private Donante darDeAlta(Donante donante) {
        Donante nuevo_donante = this.donanteRepository.create(donante);
        if (nuevo_donante != null) {
            Persona persona = donante.getPersona();
            String email = persona.getMedioDeContacto().get("email").getFirst();
            notificacionRestClient.notificar(
                    TipoNotificador.EMAIL,
                    email,
                    "Gracias por sumarte como donante...",
                    "¡Bienvenido a Donatrack!",
                    persona.getId()
            );
            eventPublisher.publicar(new DonanteCreadoEvent(nuevo_donante.getId(), nuevo_donante.getNombreCompleto()));
        }
        return nuevo_donante;
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
                            dto.setTipoNotificadorPreferido(TipoNotificador.valueOf(medioMap.get("medio").toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            System.err.println("Advertencia: El medio '" + medioMap.get("medio") + "' no coincide con ningún TipoNotificador.");
                        }
                    } else {
                        Map.Entry<String, String> entry = medioMap.entrySet().iterator().next();
                        dto.setContacto(entry.getValue());
                        try {
                            dto.setTipoNotificadorPreferido(TipoNotificador.valueOf(entry.getKey().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            System.err.println("Advertencia: El medio '" + entry.getKey() + "' no coincide con ningún TipoNotificador.");
                        }
                    }

                    return dto;
                })
                .filter(dto -> dto.getTipoNotificadorPreferido() != null)
                .collect(Collectors.toList());
    }

    public void notificarEntrega(Integer donanteId) {
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
                            persona.getId()
                    );
                    logger.info("Notificación de donante {} enviada con éxito.", persona.getId());
                } else {
                    logger.warn("El JSON del medio predeterminado está incompleto para el donante {}", persona.getId());
                }

            } else {
                logger.warn("El donante ID {} no tiene un medio predeterminado configurado. No se envió notificación.", persona.getId());
            }

        } catch (IllegalArgumentException e) {
            logger.error("ERROR DE ENUM: La clave en la base de datos no existe en TipoNotificador para el donante {}.", donanteId, e);
        } catch (NullPointerException e) {
            logger.error("ERROR DE REFERENCIA NULA: Chequeá que notifService esté inicializado. Falló en donante {}.", donanteId, e);
        } catch (Exception e) {
            logger.error("ERROR INESPERADO procesando el donante {}.", donanteId, e);
        }
    }
}