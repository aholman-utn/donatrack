package com.tp.donatrack.domain.donacion;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import static org.junit.jupiter.api.Assertions.fail;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.tp.donatrack.domain.necesidad.NecesidadRecurrente;
import org.junit.jupiter.api.BeforeEach;

import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.bien.BienPerecedero;
import com.tp.donatrack.domain.bien.CategoriaBien;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.commons.domain.donaciones.Unidad;

import com.tp.donatrack.domain.donante.Donante;
import org.junit.jupiter.api.Test;

public class DonacionSegmentadaTest { 
    private EntidadBeneficiaria unaEntidadBeneficiaria;
    private SubCategoria unaSubCategoria;
    private CategoriaBien unaCategoria;
    private Donacion donacion;
    private NecesidadRecurrente unaNecesidadRecurrente;
    private Date unaFecha;

    @BeforeEach
    void setUp() {
        com.tp.donatrack.domain.persona.PersonaJuridica datos = new com.tp.donatrack.domain.persona.PersonaJuridica();
        datos.setId(10L);
        unaEntidadBeneficiaria = new EntidadBeneficiaria(datos);
        unaCategoria = CategoriaBien.ALIMENTOS;
        unaSubCategoria = new SubCategoria(
            unaCategoria, 
            "Arroz", 
            Unidad.KG
        );
        
        Donante donante = new Donante();
        com.tp.donatrack.domain.persona.PersonaHumana persona = new com.tp.donatrack.domain.persona.PersonaHumana();
        persona.setId(1L);
        donante.setPersona(persona);

        Date fechaVenc = new Date();
        BienPerecedero arroz = new BienPerecedero(
            "Arroz Gallo", 
            "Arroz blanco largo fino", 
            "arroz.jpg", 
            unaSubCategoria, 
            fechaVenc
        );

        List<Bien> bienes = Arrays.asList(arroz);

        donacion = new Donacion(
            donante, 
            "Donación de campaña invernal", 
            new Date(),
            bienes
        );

        unaFecha = new Date();

        unaNecesidadRecurrente = new NecesidadRecurrente(unaSubCategoria, 1, unaFecha, 1);
        unaEntidadBeneficiaria.agregarNecesidad(unaNecesidadRecurrente);
    }

    @Test
    void testImplementarDonacionSegmentada(){
        int cantNecesidadesAntes = unaEntidadBeneficiaria.getCantNececidadesActivas(); 
        
        List<DonacionSegmentada> donacionesSegmentadas = donacion.getDonacionesSegmentadas();

        donacionesSegmentadas.get(0).donar(unaEntidadBeneficiaria);
        
        int cantNecesidadesDespues = unaEntidadBeneficiaria.getCantNececidadesActivas();

        assertTrue(cantNecesidadesDespues < cantNecesidadesAntes, 
            "La cantidad de necesidades activas debería haber disminuido tras la donación");
    }

    @Test
    void testFlujoDeEstadosDeLaDonacionSegmentada() {
        List<DonacionSegmentada> donacionesSegmentadas = donacion.getDonacionesSegmentadas();
        DonacionSegmentada donacionSegmentada = donacionesSegmentadas.get(0);

        assertEquals(EstadoDonacionSegmentada.EN_DEPOSITO, donacionSegmentada.getEstado(),
            "La donación segmentada debería iniciar en estado EN_DEPOSITO");

        donacionSegmentada.donar(unaEntidadBeneficiaria);

        assertEquals(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, donacionSegmentada.getEstado(),
            "El estado debería cambiar a ASIGNACION_REALIZADA luego de ser procesada por la entidad");
    }

    @Test
    void testFlujoCompletoHastaEntrega() {
        DonacionSegmentada segmento = donacion.getDonacionesSegmentadas().get(0);
        assertEquals(EstadoDonacionSegmentada.EN_DEPOSITO, segmento.getEstado());

        segmento.asignar(unaEntidadBeneficiaria, "Admin");
        assertEquals(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, segmento.getEstado());
        assertTrue(segmento.getEstado().isAsignada());

        segmento.listarParaEntrega("Operador");
        assertEquals(EstadoDonacionSegmentada.LISTA_PARA_ENTREGAR, segmento.getEstado());
        assertTrue(segmento.getEstado().isListaParaEntregar());

        segmento.iniciarTraslado("Chofer");
        assertEquals(EstadoDonacionSegmentada.EN_TRASLADO, segmento.getEstado());
        assertTrue(segmento.getEstado().isEnTraslado());

        segmento.confirmarEntrega(10L);
        assertEquals(EstadoDonacionSegmentada.ENTREGADA, segmento.getEstado());
        assertTrue(segmento.getEstado().isFinalizada());
    }

    @Test
    void testTransicionesInvalidasLanzanTransicionNoPermitidaException() {
        DonacionSegmentada segmento = donacion.getDonacionesSegmentadas().get(0);
        assertEquals(EstadoDonacionSegmentada.EN_DEPOSITO, segmento.getEstado());

        // No se puede iniciar traslado directamente desde depósito
        org.junit.jupiter.api.Assertions.assertThrows(
                com.tp.donatrack.domain.donacion.exception.TransicionNoPermitidaException.class,
                () -> segmento.iniciarTraslado("Chofer"),
                "Debe fallar al intentar iniciar traslado desde EN_DEPOSITO"
        );

        // No se puede confirmar entrega directamente desde depósito
        org.junit.jupiter.api.Assertions.assertThrows(
                com.tp.donatrack.domain.donacion.exception.TransicionNoPermitidaException.class,
                () -> segmento.confirmarEntrega(10L)
        );

        // Avanzar a ENTREGADA
        segmento.asignar(unaEntidadBeneficiaria, "Admin");
        segmento.listarParaEntrega("Admin");
        segmento.iniciarTraslado("Chofer");
        segmento.confirmarEntrega(10L);

        // En estado ENTREGADA (terminal), ninguna acción debe permitirse
        org.junit.jupiter.api.Assertions.assertThrows(
                com.tp.donatrack.domain.donacion.exception.TransicionNoPermitidaException.class,
                () -> segmento.asignar(unaEntidadBeneficiaria, "Admin")
        );
        org.junit.jupiter.api.Assertions.assertThrows(
                com.tp.donatrack.domain.donacion.exception.TransicionNoPermitidaException.class,
                () -> segmento.listarParaEntrega("Admin")
        );
    }

    @Test
    void testSerializacionYDeserializacionJackson() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

        // Serialización
        EstadoDonacionSegmentada estado = EstadoDonacionSegmentada.EN_DEPOSITO;
        String json = mapper.writeValueAsString(estado);
        assertEquals("\"EN_DEPOSITO\"", json);

        // Deserialización
        EstadoDonacionSegmentada deserializado = mapper.readValue("\"ASIGNACION_REALIZADA\"", EstadoDonacionSegmentada.class);
        assertEquals(EstadoDonacionSegmentada.ASIGNACION_REALIZADA, deserializado);
        assertTrue(deserializado.isAsignada());
    }
}