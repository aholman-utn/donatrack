package com.tp.donatrack.notificaciones.domain.notificadores.email;

import com.tp.commons.domain.notificador.TipoNotificador;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificadorEmailTest {

    @Mock
    private iEmailProvider emailProvider;

    @Test
    @DisplayName("NotificadorEmail delega al provider con los parámetros correctos")
    void delegaAlProvider() {
        NotificadorEmail notificador = new NotificadorEmail(emailProvider);

        notificador.enviarNotificacion("ana@mail.com", "Mensaje", "Asunto");

        verify(emailProvider).enviarEmail("ana@mail.com", "Mensaje", "Asunto");
    }

    @Test
    @DisplayName("NotificadorEmail retorna TipoNotificador.EMAIL como medio")
    void retornaMedioEmail() {
        NotificadorEmail notificador = new NotificadorEmail(emailProvider);
        assertEquals(TipoNotificador.EMAIL, notificador.getMedio());
    }

    @Test
    @DisplayName("Si el provider lanza excepción, NotificadorEmail la propaga como RuntimeException")
    void propagaExcepcionDelProvider() {
        doThrow(new RuntimeException("Error de red")).when(emailProvider)
                .enviarEmail(any(), any(), any());

        NotificadorEmail notificador = new NotificadorEmail(emailProvider);

        assertThrows(RuntimeException.class, () ->
                notificador.enviarNotificacion("fail@mail.com", "Msg", "Asunto")
        );
    }
}
