package com.tp.donatrack.domain.asignacion;

import com.tp.donatrack.domain.bien.*;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.necesidad.EstadoNecesidad;
import com.tp.donatrack.domain.necesidad.NecesidadExtraordinaria;
import com.tp.donatrack.domain.persona.PersonaJuridica;
import com.tp.donatrack.domain.persona.TipoOrganizacion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ServicioMatchmakingTest {

    private ServicioMatchmaking servicio;
    private SubCategoria sillas;
    private SubCategoria fideos;
    private EntidadBeneficiaria escuela;
    private EntidadBeneficiaria comedor;
    private EntidadBeneficiaria hogar;

    @BeforeEach
    void setUp() {
        servicio = new ServicioMatchmaking();
        sillas = new SubCategoria(Categoria.MOBILIARIO, "Sillas", Unidad.UNIDADES);
        fideos = new SubCategoria(Categoria.ALIMENTOS, "Fideos secos", Unidad.UNIDADES);

        escuela = crearEntidad("Escuela N°10");
        escuela.agregarNecesidad(new NecesidadExtraordinaria(sillas, 30, new Date(), "Inundación"));

        comedor = crearEntidad("Comedor Sonrisas");
        comedor.agregarNecesidad(new NecesidadExtraordinaria(fideos, 100, new Date(), "Stock bajo"));

        hogar = crearEntidad("Hogar San Martín");
        hogar.agregarNecesidad(new NecesidadExtraordinaria(sillas, 10, new Date(), "Ampliación"));
    }

    @Test
    @DisplayName("El algoritmo de compatibilidad semántica retorna entidades con necesidad coincidente")
    void compatibilidadSemanticaFiltraPorSubcategoria() {
        DonacionSegmentada donacion = crearDonacion(sillas, 5);

        AlgoritmoCompatibilidadSemantica algo = new AlgoritmoCompatibilidadSemantica();
        List<EntidadBeneficiaria> ranking = algo.rankear(donacion, List.of(escuela, comedor, hogar));

        assertTrue(ranking.contains(escuela));
        assertTrue(ranking.contains(hogar));
        assertFalse(ranking.contains(comedor)); // comedor necesita fideos, no sillas
    }

    @Test
    @DisplayName("El algoritmo de compatibilidad prioriza a quien más necesita")
    void compatibilidadPriorizaMayorFaltante() {
        DonacionSegmentada donacion = crearDonacion(sillas, 5);

        AlgoritmoCompatibilidadSemantica algo = new AlgoritmoCompatibilidadSemantica();
        List<EntidadBeneficiaria> ranking = algo.rankear(donacion, List.of(escuela, hogar));

        assertEquals(escuela, ranking.get(0)); // escuela necesita 30, hogar necesita 10
    }

    @Test
    @DisplayName("El algoritmo de sub-atendidos prioriza a quien recibió menos donaciones")
    void subAtendidosPriorizaMenosAtendidos() {
        // Simular que el comedor ya recibió donaciones
        DonacionSegmentada donacionPrevia = crearDonacion(fideos, 50);
        comedor.getNececidades().get(0).recibirDonacion(donacionPrevia);

        DonacionSegmentada donacion = crearDonacion(fideos, 20);

        AlgoritmoPrioridadSubAtendidos algo = new AlgoritmoPrioridadSubAtendidos();
        // Crear otra entidad con necesidad de fideos y sin donaciones previas
        EntidadBeneficiaria nuevoComedor = crearEntidad("Comedor Nuevo");
        nuevoComedor.agregarNecesidad(new NecesidadExtraordinaria(fideos, 50, new Date(), "Apertura"));

        List<EntidadBeneficiaria> ranking = algo.rankear(donacion, List.of(comedor, nuevoComedor));

        assertEquals(nuevoComedor, ranking.get(0)); // nuevo comedor tiene 0 donaciones recibidas
    }

    @Test
    @DisplayName("El servicio de matchmaking retorna coincidencias entre ambos algoritmos")
    void matchmakingRetornaCoincidencias() {
        DonacionSegmentada donacion = crearDonacion(sillas, 5);

        ResultadoMatchmaking resultado = servicio.ejecutar(donacion, List.of(escuela, comedor, hogar));

        assertTrue(resultado.isHuboCoincidencias());
        assertTrue(resultado.getCoincidencias().contains(escuela) || resultado.getCoincidencias().contains(hogar));
    }

    @Test
    @DisplayName("Solo se ejecuta matchmaking para donaciones EN_DEPOSITO")
    void soloAsignaDonacionesEnDeposito() {
        DonacionSegmentada donacion = crearDonacion(sillas, 5);
        donacion.setEstado(EstadoDonacionSegmentada.ASIGNACION_REALIZADA);

        assertThrows(IllegalStateException.class, () ->
                servicio.ejecutar(donacion, List.of(escuela)));
    }

    @Test
    @DisplayName("Los algoritmos retornan máximo 10 resultados")
    void maximoDiezResultados() {
        List<EntidadBeneficiaria> muchasEntidades = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            EntidadBeneficiaria e = crearEntidad("Entidad " + i);
            e.agregarNecesidad(new NecesidadExtraordinaria(sillas, 10 + i, new Date(), "Necesidad"));
            muchasEntidades.add(e);
        }

        DonacionSegmentada donacion = crearDonacion(sillas, 5);
        AlgoritmoCompatibilidadSemantica algo = new AlgoritmoCompatibilidadSemantica();
        List<EntidadBeneficiaria> ranking = algo.rankear(donacion, muchasEntidades);

        assertTrue(ranking.size() <= 10);
    }

    // Helpers
    private DonacionSegmentada crearDonacion(SubCategoria sub, int cantidad) {
        Bien bien = new BienDuradero("Item", "Desc", "img.png", sub, EstadoBien.NUEVO);
        List<Bien> bienes = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) bienes.add(bien);
        return new DonacionSegmentada(cantidad, sub, bienes);
    }

    private EntidadBeneficiaria crearEntidad(String nombre) {
        PersonaJuridica pj = new PersonaJuridica();
        pj.setRazonSocial(nombre);
        pj.setTipo(TipoOrganizacion.ONG);
        return new EntidadBeneficiaria(pj);
    }
}
