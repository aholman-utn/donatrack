package com.tp.donatrack.services;

import com.tp.commons.domain.notificador.TipoNotificador;
import com.tp.commons.services.notificador.NotificacionRestClient;

import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.persona.Persona;
import com.tp.donatrack.domain.persona.PersonaJuridica;
import com.tp.donatrack.repositories.EntidadBeneficiariaRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Service
public class EntidadBeneficiariaService {
    private final EntidadBeneficiariaRepository entidadBeneficiariaRepository;
    private final NotificacionRestClient notificacionRestClient;
    private static final Logger logger = LoggerFactory.getLogger(EntidadBeneficiariaService.class);

    public EntidadBeneficiariaService(
        EntidadBeneficiariaRepository entidadBeneficiariaRepository,
        NotificacionRestClient notificacionRestClient
    ) {
        this.entidadBeneficiariaRepository = entidadBeneficiariaRepository;
        this.notificacionRestClient = notificacionRestClient;
    }

    // CREATE
    public EntidadBeneficiaria registrar(PersonaJuridica personaJuridica) {
        EntidadBeneficiaria entidadBeneficiaria = new EntidadBeneficiaria(personaJuridica);
        return entidadBeneficiariaRepository.create(entidadBeneficiaria);
    }

    // READ
    public List<EntidadBeneficiaria> listarTodos() {
        return entidadBeneficiariaRepository.findAll();
    }

    public EntidadBeneficiaria buscarEntidad(Long id) {
        return entidadBeneficiariaRepository.find(id);
    }

    // UPDATE
    public EntidadBeneficiaria actualizarDatosPerosnales(Long id, PersonaJuridica personaJuridica) {
        EntidadBeneficiaria entidad = buscarEntidad(id);
        entidad.setDatosDeEntidad(personaJuridica);
        return entidadBeneficiariaRepository.create(entidad);
    }

    // DELETE
    public void eliminar(Long id) {
        EntidadBeneficiaria entidadAeliminar = buscarEntidad(id);
        if (entidadAeliminar != null) {
            entidadBeneficiariaRepository.delete(entidadAeliminar);
        }
    }

    public void notificarEntrega(Long entidadId) {
        try {
            EntidadBeneficiaria entidad = this.buscarEntidad(entidadId);

            if (entidad == null || entidad.getDatosDeEntidad() == null) {
                logger.warn("No se encontró la entidad o sus datos de persona para el ID: {}", entidadId);
                return;
            }

            Persona personaEntidad = entidad.getDatosDeEntidad();

            boolean tieneMedioConfigurado = personaEntidad.getMedioPredeterminado() != null &&
                    !personaEntidad.getMedioPredeterminado().isEmpty();

            if (tieneMedioConfigurado) {
                java.util.Map<String, String> mapaMedio = personaEntidad.getMedioPredeterminado();

                String tipoString = mapaMedio.get("medio");
                String contacto = mapaMedio.get("valor");

                if (tipoString != null && contacto != null) {
                    TipoNotificador tipoNotificador = TipoNotificador.valueOf(tipoString.toUpperCase());

                    logger.info("Notificando a entidad ID: {} vía {}", personaEntidad.getId(), tipoNotificador);

                    notificacionRestClient.notificar(
                            tipoNotificador,
                            contacto,
                            "Se ha confirmado la recepción de la donación.",
                            "Confirmación de Entrega",
                            personaEntidad.getId()
                    );
                    logger.info("Notificación de entidad {} enviada con éxito.", personaEntidad.getId());
                } else {
                    logger.warn("El JSON del medio predeterminado está incompleto para la entidad {}", personaEntidad.getId());
                }

            } else {
                logger.warn("La entidad ID {} no tiene un medio predeterminado configurado. No se envió notificación.", personaEntidad.getId());
            }

        } catch (IllegalArgumentException e) {
            logger.error("ERROR DE ENUM: La clave en la base de datos no existe en TipoNotificador para la entidad {}.", entidadId, e);
        } catch (NullPointerException e) {
            logger.error("ERROR DE REFERENCIA NULA: Chequeá que notificacionService esté inicializado. Falló en entidad {}.", entidadId, e);
        } catch (Exception e) {
            logger.error("ERROR INESPERADO procesando la entidad {}.", entidadId, e);
        }
    }

}
