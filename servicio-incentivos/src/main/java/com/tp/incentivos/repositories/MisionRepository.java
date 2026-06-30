package com.tp.incentivos.repositories;

import com.tp.incentivos.domain.misiones.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MisionRepository {

        private final List<Mision> misiones;

        public MisionRepository() {

                Mision misionExitosas = new MisionDonacionesExitosas(
                    1,
                    "Donacion Exitosa",
                    "Lograr 1 donacion que sean recibidas exitosamente por una entidad beneficiaria."
                );
                misionExitosas.setId(1L);
                misionExitosas.setOrden(1);

                Mision misionCompletitud = new MisionCompletitud(
                    4,
                    "Completitud",
                    "Realizar donaciones de 4 categorías distintas."
                );
                misionCompletitud.setId(2L);
                misionCompletitud.setOrden(2);

                Mision misionHabil = new MisionHabilDonador(
                    20,
                    "Habil Donador",
                    "Realizar una donación que supere los 20 bienes en una sola entrega."
                );
                misionHabil.setId(3L);
                misionHabil.setOrden(3);

                Mision misionRacha = new MisionRacha(
                    3,
                    "Racha",
                    "Realizar una donación durante 3 meses consecutivos."
                );
                misionRacha.setId(4L);
                misionRacha.setOrden(4);

                this.misiones = List.of(misionExitosas, misionCompletitud, misionHabil, misionRacha);
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