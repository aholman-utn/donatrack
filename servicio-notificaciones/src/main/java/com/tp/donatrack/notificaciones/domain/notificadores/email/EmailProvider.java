package com.tp.donatrack.notificaciones.domain.notificadores.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Proveedor de email simulado (fallback para desarrollo local).
 * Se activa solo si resend.api.key no está configurada o es "disabled".
 */
@Component
@ConditionalOnProperty(name = "resend.api.key", havingValue = "disabled", matchIfMissing = true)
public class EmailProvider implements iEmailProvider {

    private static final Logger logger = LoggerFactory.getLogger(EmailProvider.class);

    @Override
    public void enviarEmail(String destinatario, String mensaje, String asunto) {
        logger.info("--- SIMULACIÓN: Enviando email a {} ---", destinatario);
        logger.info("Asunto: {}", asunto);
        logger.info("Mensaje: {}", mensaje);
    }
}
