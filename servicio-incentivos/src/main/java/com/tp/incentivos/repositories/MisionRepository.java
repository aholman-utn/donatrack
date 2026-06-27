package com.tp.incentivos.repositories;

import com.tp.incentivos.domain.misiones.*;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Repository
public class MisionRepository {
    private List<Mision> misiones;

    public MisionRepository(){

        Mision misionRacha = new MisionRacha(
            3,
            "Racha Solidaria",
            "Realizar una donación durante 3 meses consecutivos."
        );

        Mision misionCompletitud = new MisionCompletitud(
            4,
            "Explorador de Categorías",
            "Realizar donaciones de 4 categorías distintas."
        );

        Mision misionHabil = new MisionHabilDonador(
            20,
            "Ayuda Masiva",
            "Realizar una donación que supere los 20 bienes en una sola entrega."
        );

        Mision misionExitosas = new MisionDonacionesExitosas(
            5,
            "Héroe Confirmado",
            "Lograr 5 donaciones que sean recibidas exitosamente por una entidad beneficiaria."
        );

        this.misiones = new ArrayList<>();
    }

    public Optional<Mision> findById(Long misionId) {
        return this.misiones.stream()
                .filter(mision -> mision.getId().equals(misionId))
                .findFirst();
    }
}
