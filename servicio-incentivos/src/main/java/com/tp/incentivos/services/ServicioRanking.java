package com.tp.incentivos.services;

import com.tp.incentivos.dtos.RankingItemDTO;
import com.tp.incentivos.clients.DonacionesRestClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Servicio encargado de calcular el ranking global de donantes.
 */
@Service
public class ServicioRanking {
    private final DonacionesRestClient donacionesRestClient;

    public ServicioRanking(DonacionesRestClient donacionesRestClient) {
        this.donacionesRestClient = donacionesRestClient;
    }

    public List<RankingItemDTO> obtenerRankingCompleto() {
        List<Map<String, Object>> donantes = donacionesRestClient.obtenerTodosDonantes();
        
        List<RankingItemDTO> ranking = new ArrayList<>();
        
        for (Map<String, Object> donanteMap : donantes) {
            Map<String, Object> persona = (Map<String, Object>) donanteMap.get("persona");
            Integer donanteId = persona != null ? (Integer) persona.get("id") : null;
            if (donanteId == null) continue;
            
            Map<String, Object> perfil = (Map<String, Object>) donanteMap.get("perfil");
            
            String categoriaDonante = "COLABORADOR";
            int totalMisionesCompletadas = 0;
            
            if (perfil != null) {
                categoriaDonante = (String) perfil.getOrDefault("nivelDonante", "COLABORADOR");
                
                Object misionActualIdObj = perfil.get("misionActualId");
                if (misionActualIdObj != null) {
                    int misionActualId = (int) (misionActualIdObj instanceof Number ? ((Number) misionActualIdObj).intValue() : Integer.parseInt(misionActualIdObj.toString()));
                    totalMisionesCompletadas = misionActualId - 1;
                } else if (categoriaDonante.equals("TRANSFORMADOR")) {
                    // Si es transformador y ya no tiene mision actual, cumplio las 4.
                    totalMisionesCompletadas = 4;
                }
            }
            
            ranking.add(RankingItemDTO.builder()
                    .donanteId(donanteId)
                    .categoriaDonante(categoriaDonante)
                    .totalMisionesCompletadas(totalMisionesCompletadas)
                    .build());
        }

        ranking.sort(Comparator.comparingInt(RankingItemDTO::getTotalMisionesCompletadas).reversed());

        AtomicInteger posicion = new AtomicInteger(1);
        for (RankingItemDTO item : ranking) {
            item.setPosicion(posicion.getAndIncrement());
        }

        return ranking;
    }
}
