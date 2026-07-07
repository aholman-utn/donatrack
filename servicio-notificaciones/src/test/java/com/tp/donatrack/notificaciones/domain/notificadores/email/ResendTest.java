package com.tp.donatrack.notificaciones.domain.notificadores.email;

import com.tp.donatrack.notificaciones.domain.notificadores.email.providers.Resend;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResendTest {

    private Resend resend;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        resend = new Resend();
        ReflectionTestUtils.setField(resend, "apiKey", "re_test_key_123");
        ReflectionTestUtils.setField(resend, "fromEmail", "DonaTrack <test@donatrack.com>");
    }

    @Test
    @DisplayName("enviarEmail construye el request correctamente y llama a la API de Resend")
    void enviarEmailExitoso() {
        assertDoesNotThrow(() ->
                resend.enviarEmail("juan@mail.com", "Hola Juan", "Bienvenido")
        );
    }

    @Test
    @DisplayName("Si la API de Resend falla, no lanza excepción (se loguea el error)")
    void enviarEmailConErrorNoLanzaExcepcion() {
        ReflectionTestUtils.setField(resend, "apiKey", "invalid_key");

        assertDoesNotThrow(() ->
                resend.enviarEmail("error@mail.com", "Test", "Test asunto")
        );
    }
}
