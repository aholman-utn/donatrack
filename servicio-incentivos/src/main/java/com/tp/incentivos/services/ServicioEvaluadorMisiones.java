package com.tp.incentivos.services;

import com.tp.commons.services.notificador.NotificacionRestClient;

import com.tp.incentivos.domain.misiones.Mision;
import com.tp.incentivos.domain.InfoDonacion;
import org.springframework.stereotype.Service;
/*
 * Servicio encargado de evaluar el progreso de las misiones tras cada entrega.
 * Solo evalúa la misión activa actual; al completarse, desbloquea la siguiente.
 * Si todas las misiones de la categoría se completan, sube al donante de
 * categoría.
 */
@Service
public class ServicioEvaluadorMisiones {
     /*
    private final NotificacionRestClient notificacionRestClient;

    public ServicioEvaluadorMisiones(
            NotificacionRestClient notificacionRestClient
    ) {
        this.notificacionRestClient = notificacionRestClient;
    }

    public void evaluar(Perfil perfil, InfoDonacion infoDonacion) {
        Mision misionActual = perfil.getMisionActual();

        if (misionActual == null) {
            // o es TRANSFORMADOR en su máximo nivel
            return;
        }

        String usuario = misionActual.getInsigniaAsociada().getNombreDonante();
        String nombreMision = misionActual.getInsigniaAsociada().getTitulo();
        String description = misionActual.getInsigniaAsociada().getDescripcion();

        misionActual.actualizarProgreso(infoDonacion);

        if (misionActual.evaluarYMarcarCompletitud(usuario, nombreMision, description)) {
            // Otorgar la insignia de la misión completada
            perfil.getInsigniasGanadas().add(misionActual.getInsigniaAsociada());
            perfil.getMisionesCompletadas().add(misionActual);

            //notifico
            //todo: necesito el id de la persona!
            //notifService.crearNotificacion(Long.valueOf(1),"¡Mision Completada!", "Completaste una mision perrito malvado");

            boolean todasCompletas = perfil.getMisionesActuales().stream()
                    .allMatch(Mision::isCompletada);

            if (todasCompletas && perfil.getCategoriaDonante() != CategoriaDonante.TRANSFORMADOR) {
                //todo: necesito el id de la persona!

                //Puedo hacer get a donaciones preguntando por el id de persona.
                //notificacionRestClient.notificar(Long.valueOf(2),"¡Subiste de categoría", "Gracias por tu compromiso y participación. Nos complace informarte que has sido promovido a una nueva categoría debido a tu valiosa contribución. Este reconocimiento refleja el impacto positivo de tus acciones en nuestra comunidad. ¡Felicitaciones y gracias por seguir colaborando!");
                perfil.subirCategoria();
            }
        }
    }
    */
}
