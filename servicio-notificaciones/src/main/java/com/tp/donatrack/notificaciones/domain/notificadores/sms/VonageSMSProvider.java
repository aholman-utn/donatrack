package com.tp.donatrack.notificaciones.domain.notificadores.sms;

import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "vonage")
public class VonageSMSProvider implements iSMSProvider {

    private static final Logger logger = LoggerFactory.getLogger(VonageSMSProvider.class);

    @Value("${vonage.api-key}")
    private String apiKey;

    @Value("${vonage.api-secret}")
    private String apiSecret;

    @Value("${vonage.from-number}")
    private String fromNumber;

    private VonageClient vonageClient;

    @PostConstruct
    public void init() {
        logger.info(">>> VonageSMSProvider inicializado. API Key: {}, From: {}", apiKey, fromNumber);
        this.vonageClient = VonageClient.builder()
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .build();
    }

    @Override
    public void enviarSMS(String numero, String mensaje) {
        logger.info(">>> Enviando SMS via Vonage a: {} desde: {}", numero, fromNumber);
        TextMessage textMessage = new TextMessage(fromNumber, numero, mensaje);

        SmsSubmissionResponse response = vonageClient.getSmsClient().submitMessage(textMessage);

        MessageStatus status = response.getMessages().get(0).getStatus();
        String errorText = response.getMessages().get(0).getErrorText();
        logger.info(">>> Respuesta Vonage - Status: {}, Error: {}", status, errorText);

        if (status != MessageStatus.OK) {
            throw new RuntimeException("Error al enviar SMS via Vonage: " + errorText);
        }

        logger.info(">>> SMS enviado exitosamente a {}", numero);
    }
}
