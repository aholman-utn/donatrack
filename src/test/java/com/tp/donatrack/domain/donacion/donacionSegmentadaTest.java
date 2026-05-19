package com.tp.donatrack.domain.donacion;

//import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tp.donatrack.domain.necesidad.NecesidadRecurrente;
import org.junit.jupiter.api.BeforeEach;

import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.bien.BienDuradero;
import com.tp.donatrack.domain.bien.Categoria;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.bien.EstadoBien;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.bien.Unidad;
import org.junit.jupiter.api.Test;

public class donacionSegmentadaTest{
    private EntidadBeneficiaria unaEntidadBeneficiaria;
    private SubCategoria unaSubCategoria;
    private Categoria unaCategoria;
    private DonacionSegmentada donacion;
    private NecesidadRecurrente unaNecesidadRecurrente;
    private Date unaFecha;

    @BeforeEach
    void setUp() {
        unaEntidadBeneficiaria = new EntidadBeneficiaria();
        unaCategoria = new Categoria("Alimento");
        unaSubCategoria = new SubCategoria(unaCategoria, "Alimento no perecedero", Unidad.UNIDADES);

        BienDuradero unArroz = new BienDuradero(
            "Arroz",
            "Arroz Blanco",
            "arroz.png",
            unaSubCategoria,
            EstadoBien.NUEVO
        );
        List<Bien> bienes = Arrays.asList(unArroz);

        donacion = new DonacionSegmentada(1,unaSubCategoria,true,bienes);

        unaFecha = new Date();

        unaNecesidadRecurrente = new NecesidadRecurrente(unaSubCategoria, 1, unaFecha, 15);
        unaEntidadBeneficiaria.agregarNecesidad(unaNecesidadRecurrente);
    }

    @Test
    void testImplementarDonacionSegmentada(){
        int cantNecesidadesAntes = unaEntidadBeneficiaria.getCantNececidadesActivas();
        donacion.donar(unaEntidadBeneficiaria);
        assertTrue(unaEntidadBeneficiaria.getCantNececidadesActivas() < cantNecesidadesAntes);
    }
}