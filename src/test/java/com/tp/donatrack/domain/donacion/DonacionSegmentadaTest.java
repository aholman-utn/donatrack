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
import com.tp.donatrack.domain.bien.Categoria;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.bien.EstadoBien;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.bien.Unidad;

import com.tp.donatrack.domain.donante.Donante;
import org.junit.jupiter.api.Test;

public class DonacionSegmentadaTest { 
    private EntidadBeneficiaria unaEntidadBeneficiaria;
    private SubCategoria unaSubCategoria;
    private Categoria unaCategoria;
    private Donacion donacion;
    private NecesidadRecurrente unaNecesidadRecurrente;
    private Date unaFecha;

    @BeforeEach
    void setUp() {
        unaEntidadBeneficiaria = new EntidadBeneficiaria();
        unaCategoria = Categoria.ALIMENTOS;
        unaSubCategoria = new SubCategoria(
            unaCategoria, 
            "Arroz", 
            Unidad.KG
        );
        
        Donante donante = new Donante();

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

        assertEquals(EstadoDonacionSegmentada.PENDIENTE, donacionSegmentada.getEstado(),
            "La donación segmentada debería iniciar en estado PENDIENTE");

        donacionSegmentada.donar(unaEntidadBeneficiaria);

        assertEquals(EstadoDonacionSegmentada.ADJUDICADA, donacionSegmentada.getEstado(),
            "El estado debería cambiar a ADJUDICADA luego de ser procesada por la entidad");
    }
}