package com.tp.donatrack.notificaciones.domain.notificadores.email;

import com.tp.donatrack.notificaciones.domain.notificadores.email.providers.Resend;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResendTest {

    private Resend resend;

    @Mock
    private RestTemplate restTemplateMock;

    @BeforeEach
    void setUp() {
        resend = new Resend(restTemplateMock);
        ReflectionTestUtils.setField(resend, "apiKey", "re_test_key_123");
        ReflectionTestUtils.setField(resend, "fromEmail", "DonaTrack <test@donatrack.com>");
    }

    @Test
    @DisplayName("enviarEmail llama a la API de Resend y no tira error si todo sale bien")
    void enviarEmailExitoso() {
        when(restTemplateMock.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("Email sent", HttpStatus.OK));

        assertDoesNotThrow(() ->
                resend.enviarEmail("juan@mail.com", "Hola Juan", "Bienvenido")
        );
    }

    @Test
    @DisplayName("Si la API de Resend falla, LANZA una RuntimeException para que el Service se entere")
    void enviarEmailConErrorLanzaExcepcion() {
        when(restTemplateMock.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new RuntimeException("Simulacro de error de red"));

         assertThrows(RuntimeException.class, () ->
                resend.enviarEmail("error@mail.com", "Test", "Test asunto")
        );
    }
}