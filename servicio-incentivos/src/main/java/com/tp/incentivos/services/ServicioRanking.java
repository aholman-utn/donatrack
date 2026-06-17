package com.tp.incentivos.services;

import com.tp.incentivos.domain.Perfil;
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
        return obtenerRankingCompleto(false);
    }

    public List<RankingItemDTO> obtenerRankingCompleto(boolean mesActual) {
        List<Perfil> ordenados = new ArrayList<>(repository.findAll());
        if (mesActual) {
            ordenados.sort(Comparator.comparingLong(Perfil::getMisionesCompletadasCountMesActual).reversed());
        } else {
            ordenados.sort(Comparator.comparingLong(Perfil::getMisionesCompletadasCountHistorico).reversed());
        }

        List<RankingItemDTO> ranking = new ArrayList<>();
        AtomicInteger posicion = new AtomicInteger(1);

        for (Perfil perfil : ordenados) {
            int misionesCount = (int) (mesActual ? perfil.getMisionesCompletadasCountMesActual()
                    : perfil.getMisionesCompletadasCountHistorico());
            ranking.add(RankingItemDTO.builder()
                    .posicion(posicion.getAndIncrement())
                    .donanteId(perfil.getDonanteId())
                    .totalDonacionesExitosas(perfil.getTotalDonacionesExitosas()) // quizas se podria sacar si no me
                                                                                  // importa en las metricas del ranking
                    .categoriaDonante(perfil.getCategoriaDonante().name()) // idem antetior
                    .totalMisionesCompletadas(misionesCount)
                    .build());
        }

        return ranking;
    }

    public RankingItemDTO obtenerPosicionDonante(Integer donanteId) {
        return obtenerPosicionDonante(donanteId, false);
    }

    public RankingItemDTO obtenerPosicionDonante(Integer donanteId, boolean mesActual) {
        List<RankingItemDTO> rankingCompleto = obtenerRankingCompleto(mesActual);

        return rankingCompleto.stream()
                .filter(item -> item.getDonanteId().equals(donanteId))
                .findFirst()
                .orElse(RankingItemDTO.builder()
                        .posicion(-1)
                        .donanteId(donanteId)
                        .totalDonacionesExitosas(0)
                        .categoriaDonante("SIN_PERFIL")
                        .totalMisionesCompletadas(0)
                        .build());
    }

    public int calcularPosicion(Integer donanteId) {
        return calcularPosicion(donanteId, false);
    }

    public int calcularPosicion(Integer donanteId, boolean mesActual) {
        return obtenerPosicionDonante(donanteId, mesActual).getPosicion();
    }
}
