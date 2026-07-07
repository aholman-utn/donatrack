package com.tp.donatrack.notificaciones.domain.notificadores.email;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailProviderTest {

    @Test
    @DisplayName("EmailProvider simulado no lanza excepción al enviar email")
    void enviarEmailSimuladoNoFalla() {
        EmailProvider provider = new EmailProvider();
        assertDoesNotThrow(() ->
                provider.enviarEmail("test@mail.com", "Mensaje de prueba", "Asunto de prueba")
        );
    }
}
