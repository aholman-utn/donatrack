package com.tp.incentivos.repositories;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.incentivos.domain.misiones.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class MisionRepository {

        private final Map<Nivel, List<Mision>> misionesPorNivel;

        public MisionRepository() {
                this.misionesPorNivel = new EnumMap<>(Nivel.class);

                // --- COLABORADOR ---
                Mision colExitosas = new MisionDonacionesExitosas(
                    1,
                    "Donacion Exitosa",
                    "Lograr 1 donación que sea recibida exitosamente por una entidad beneficiaria."
                );
                colExitosas.setId(1L);
                colExitosas.setOrden(1);

                Mision colHabil = new MisionHabilDonador(
                    5,
                    "Habil Donador",
                    "Realizar una donación que supere los 5 bienes en una sola entrega."
                );
                colHabil.setId(2L);
                colHabil.setOrden(2);

                Mision colCompletitud = new MisionCompletitud(
                    2,
                    "Completitud",
                    "Realizar donaciones de 2 categorías distintas."
                );
                colCompletitud.setId(3L);
                colCompletitud.setOrden(3);

                this.misionesPorNivel.put(Nivel.COLABORADOR, List.of(colExitosas, colHabil, colCompletitud));

                // --- SOSTENEDOR ---
                Mision sosExitosas = new MisionDonacionesExitosas(
                    5,
                    "Donacion Exitosa",
                    "Lograr 5 donaciones que sean recibidas exitosamente por una entidad beneficiaria."
                );
                sosExitosas.setId(1L);
                sosExitosas.setOrden(1);

                Mision sosCompletitud = new MisionCompletitud(
                    4,
                    "Completitud",
                    "Realizar donaciones de 4 categorías distintas."
                );
                sosCompletitud.setId(2L);
                sosCompletitud.setOrden(2);

                Mision sosHabil = new MisionHabilDonador(
                    10,
                    "Habil Donador",
                    "Realizar una donación que supere los 10 bienes en una sola entrega."
                );
                sosHabil.setId(3L);
                sosHabil.setOrden(3);

                this.misionesPorNivel.put(Nivel.SOSTENEDOR, List.of(sosExitosas, sosCompletitud, sosHabil));

                // --- TRANSFORMADOR ---
                Mision traRacha = new MisionRacha(
                    3,
                    "Racha",
                    "Realizar una donación durante 3 meses consecutivos."
                );
                traRacha.setId(1L);
                traRacha.setOrden(1);

                Mision traExitosas = new MisionDonacionesExitosas(
                    15,
                    "Donacion Exitosa",
                    "Lograr 15 donaciones que sean recibidas exitosamente por una entidad beneficiaria."
                );
                traExitosas.setId(2L);
                traExitosas.setOrden(2);

                Mision traCompletitud = new MisionCompletitud(
                    5,
                    "Completitud",
                    "Realizar donaciones de 5 categorías distintas."
                );
                traCompletitud.setId(3L);
                traCompletitud.setOrden(3);

                Mision traHabil = new MisionHabilDonador(
                    20,
                    "Habil Donador",
                    "Realizar una donación que supere los 20 bienes en una sola entrega."
                );
                traHabil.setId(4L);
                traHabil.setOrden(4);

                this.misionesPorNivel.put(Nivel.TRANSFORMADOR, List.of(traRacha, traExitosas, traCompletitud, traHabil));
        }

        public Optional<Mision> findById(Nivel nivel, Long misionId) {
                List<Mision> misiones = this.misionesPorNivel.getOrDefault(nivel, List.of());
                return misiones.stream()
                                .filter(mision -> mision.getId().equals(misionId))
                                .findFirst();
        }

        public Optional<Mision> findSiguiente(Nivel nivel, Long misionActualId) {
                Optional<Mision> actual = findById(nivel, misionActualId);

                if (actual.isPresent()) {
                        int ordenSiguiente = actual.get().getOrden() + 1;
                        List<Mision> misiones = this.misionesPorNivel.getOrDefault(nivel, List.of());
                        return misiones.stream()
                                        .filter(mision -> mision.getOrden() == ordenSiguiente)
                                        .findFirst();
                }

                return Optional.empty();
        }

        public Optional<Mision> obtenerMisionInicial(Nivel nivel) {
                return findById(nivel, 1L);
        }
}