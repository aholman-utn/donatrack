package com.tp.incentivos.services;

import com.tp.incentivos.domain.CategoriaDonante;
import com.tp.incentivos.domain.Mision;
import com.tp.incentivos.domain.Perfil;
import com.tp.incentivos.domain.InfoDonacion;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de evaluar el progreso de las misiones tras cada entrega.
 * Solo evalúa la misión activa actual; al completarse, desbloquea la siguiente.
 * Si todas las misiones de la categoría se completan, sube al donante de
 * categoría.
 */
@Service
public class ServicioEvaluadorMisiones {

    public void evaluar(Perfil perfil, InfoDonacion infoDonacion) {
        Mision misionActual = perfil.getMisionActual();

        if (misionActual == null) {
            // o es TRANSFORMADOR en su máximo nivel
            return;
        }

        String usuario = perfil.getNombreUsuario();
        String nombreMision = misionActual.getInsigniaAsociada().getTitulo();
        String description = misionActual.getInsigniaAsociada().getDescripcion();

        misionActual.actualizarProgreso(infoDonacion);

        if (misionActual.evaluarYMarcarCompletitud(usuario, nombreMision, description)) {
            // Otorgar la insignia de la misión completada
            perfil.getInsigniasGanadas().add(misionActual.getInsigniaAsociada());
            perfil.getMisionesCompletadas().add(misionActual);

            boolean todasCompletas = perfil.getMisionesActuales().stream()
                    .allMatch(Mision::isCompletada);

            if (todasCompletas && perfil.getCategoriaDonante() != CategoriaDonante.TRANSFORMADOR) {
                perfil.subirCategoria();
            }
        }
    }
}
