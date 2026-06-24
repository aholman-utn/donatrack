package com.tp.incentivos.services;

import com.tp.incentivos.domain.Insignia;
import com.tp.incentivos.domain.Mision;
import com.tp.incentivos.domain.MisionDonacionesExitosas;
import com.tp.incentivos.domain.Perfil;
import com.tp.incentivos.dtos.RankingItemDTO;
import com.tp.incentivos.dtos.InsigniasDonanteDTO;
import com.tp.incentivos.repositories.IncentivosRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServicioRankingTest {

    private IncentivosRepository repository;
    private ServicioRanking servicioRanking;

    @BeforeEach
    public void setUp() {
        repository = new IncentivosRepository();
        servicioRanking = new ServicioRanking(repository);
    }

    @Test
    public void testRankingHistoricoYMesActual() {
        // Creamos 3 perfiles
        Perfil perfil1 = new Perfil(101); // Donante 101
        Perfil perfil2 = new Perfil(102); // Donante 102
        Perfil perfil3 = new Perfil(103); // Donante 103

        LocalDate ahora = LocalDate.now();
        LocalDate mesPasado = ahora.minusMonths(2);

        // Perfil 1: completa 3 misiones (2 este mes, 1 el mes pasado)
        Mision m1_1 = crearMisionCompletada(ahora);
        Mision m1_2 = crearMisionCompletada(ahora);
        Mision m1_3 = crearMisionCompletada(mesPasado);
        perfil1.getMisionesCompletadas().addAll(List.of(m1_1, m1_2, m1_3));

        // Perfil 2: completa 4 misiones (1 este mes, 3 el mes pasado)
        Mision m2_1 = crearMisionCompletada(ahora);
        Mision m2_2 = crearMisionCompletada(mesPasado);
        Mision m2_3 = crearMisionCompletada(mesPasado);
        Mision m2_4 = crearMisionCompletada(mesPasado);
        perfil2.getMisionesCompletadas().addAll(List.of(m2_1, m2_2, m2_3, m2_4));

        // Perfil 3: completa 2 misiones (0 este mes, 2 el mes pasado)
        Mision m3_1 = crearMisionCompletada(mesPasado);
        Mision m3_2 = crearMisionCompletada(mesPasado);
        perfil3.getMisionesCompletadas().addAll(List.of(m3_1, m3_2));

        repository.create(perfil1);
        repository.create(perfil2);
        repository.create(perfil3);

        // --- VALIDAR RANKING HISTÓRICO ---
        // Esperado histórico: Perfil 2 (4 misiones), Perfil 1 (3 misiones), Perfil 3 (2 misiones)
        List<RankingItemDTO> rankingHistorico = servicioRanking.obtenerRankingCompleto(false);
        assertEquals(3, rankingHistorico.size());

        assertEquals(1, rankingHistorico.get(0).getPosicion());
        assertEquals(102, rankingHistorico.get(0).getDonanteId());
        assertEquals(4, rankingHistorico.get(0).getTotalMisionesCompletadas());

        assertEquals(2, rankingHistorico.get(1).getPosicion());
        assertEquals(101, rankingHistorico.get(1).getDonanteId());
        assertEquals(3, rankingHistorico.get(1).getTotalMisionesCompletadas());

        assertEquals(3, rankingHistorico.get(2).getPosicion());
        assertEquals(103, rankingHistorico.get(2).getDonanteId());
        assertEquals(2, rankingHistorico.get(2).getTotalMisionesCompletadas());

        // --- VALIDAR RANKING MES ACTUAL ---
        // Esperado mes actual: Perfil 1 (2 misiones), Perfil 2 (1 mision), Perfil 3 (0 misiones)
        List<RankingItemDTO> rankingMesActual = servicioRanking.obtenerRankingCompleto(true);
        assertEquals(3, rankingMesActual.size());

        assertEquals(1, rankingMesActual.get(0).getPosicion());
        assertEquals(101, rankingMesActual.get(0).getDonanteId());
        assertEquals(2, rankingMesActual.get(0).getTotalMisionesCompletadas());

        assertEquals(2, rankingMesActual.get(1).getPosicion());
        assertEquals(102, rankingMesActual.get(1).getDonanteId());
        assertEquals(1, rankingMesActual.get(1).getTotalMisionesCompletadas());

        assertEquals(3, rankingMesActual.get(2).getPosicion());
        assertEquals(103, rankingMesActual.get(2).getDonanteId());
        assertEquals(0, rankingMesActual.get(2).getTotalMisionesCompletadas());

        // --- VALIDAR POSICION DE DONANTE ---
        assertEquals(2, servicioRanking.calcularPosicion(101, false)); // Histórico: perfil 1 está 2do
        assertEquals(1, servicioRanking.calcularPosicion(101, true));  // Mes actual: perfil 1 está 1ro

        assertEquals(1, servicioRanking.calcularPosicion(102, false)); // Histórico: perfil 2 está 1ro
        assertEquals(2, servicioRanking.calcularPosicion(102, true));  // Mes actual: perfil 2 está 2do
    }

    private Mision crearMisionCompletada(LocalDate fecha) {
        Insignia insignia = new Insignia("Usuario", "Insignia", "Descripción");
        Mision mision = new MisionDonacionesExitosas(1, "Titulo", "Descripcion", insignia);
        mision.setCompletada(true);
        mision.setFechaObtencion(fecha);
        return mision;
    }
}
