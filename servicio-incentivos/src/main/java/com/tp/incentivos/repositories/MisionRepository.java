package com.tp.incentivos.repositories;

import com.tp.incentivos.domain.misiones.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MisionRepository {

    private final List<Mision> misiones;

    public MisionRepository() {

        Mision misionRacha = new MisionRacha(
                3,
                "Racha Solidaria",
                "Realizar una donación durante 3 meses consecutivos."
        );
        misionRacha.setId(1L);
        misionRacha.setOrden(1);

        Mision misionCompletitud = new MisionCompletitud(
                4,
                "Explorador de Categorías",
                "Realizar donaciones de 4 categorías distintas."
        );
        misionCompletitud.setId(2L);
        misionCompletitud.setOrden(2);

        Mision misionHabil = new MisionHabilDonador(
                20,
                "Ayuda Masiva",
                "Realizar una donación que supere los 20 bienes en una sola entrega."
        );
        misionHabil.setId(3L);
        misionHabil.setOrden(3);

        Mision misionExitosas = new MisionDonacionesExitosas(
                5,
                "Héroe Confirmado",
                "Lograr 5 donaciones que sean recibidas exitosamente por una entidad beneficiaria."
        );
        misionExitosas.setId(4L);
        misionExitosas.setOrden(4);

        this.misiones = List.of(misionRacha, misionCompletitud, misionHabil, misionExitosas);
    }

    public Optional<Mision> findById(Long misionId) {
        return this.misiones.stream()
                .filter(mision -> mision.getId().equals(misionId))
                .findFirst();
    }

    public Optional<Mision> findSiguiente(Long misionActualId) {
        Optional<Mision> actual = findById(misionActualId);

        if (actual.isPresent()) {
            int ordenSiguiente = actual.get().getOrden() + 1;
            return this.misiones.stream()
                    .filter(mision -> mision.getOrden() == ordenSiguiente)
                    .findFirst();
        }

        return Optional.empty();
    }
}