package com.tp.incentivos.services;

import com.tp.incentivos.domain.PerfilIncentivosDonante;
import com.tp.incentivos.dtos.RankingItemDTO;
import com.tp.incentivos.repositories.IncentivosRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Servicio encargado de calcular el ranking global de donantes.
 * El ranking se calcula on-the-fly sobre el repositorio en memoria,
 * ordenando por totalDonacionesExitosas de forma descendente.
 */
@Service
public class ServicioRanking {

    private final IncentivosRepository repository;

    public ServicioRanking(IncentivosRepository repository) {
        this.repository = repository;
    }

    public List<RankingItemDTO> obtenerRankingCompleto() {
        List<PerfilIncentivosDonante> ordenados = new ArrayList<>(repository.findAll());
        ordenados.sort(Comparator.comparingInt(PerfilIncentivosDonante::getTotalDonacionesExitosas).reversed());

        List<RankingItemDTO> ranking = new ArrayList<>();
        AtomicInteger posicion = new AtomicInteger(1);

        for (PerfilIncentivosDonante perfil : ordenados) {
            ranking.add(RankingItemDTO.builder()
                    .posicion(posicion.getAndIncrement())
                    .donanteId(perfil.getDonanteId())
                    .totalDonacionesExitosas(perfil.getTotalDonacionesExitosas())
                    .categoriaDonante(perfil.getCategoriaDonante().name())
                    .build());
        }

        return ranking;
    }

    public RankingItemDTO obtenerPosicionDonante(Integer donanteId) {
        List<RankingItemDTO> rankingCompleto = obtenerRankingCompleto();

        return rankingCompleto.stream()
                .filter(item -> item.getDonanteId().equals(donanteId))
                .findFirst()
                .orElse(RankingItemDTO.builder()
                        .posicion(-1)
                        .donanteId(donanteId)
                        .totalDonacionesExitosas(0)
                        .categoriaDonante("SIN_PERFIL")
                        .build());
    }

    public int calcularPosicion(Integer donanteId) {
        return obtenerPosicionDonante(donanteId).getPosicion();
    }
}
