package com.tp.donatrack;

//import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.bien.BienDuradero;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.entidad.Categoria;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.entidad.EstadoBien;
import com.tp.donatrack.domain.entidad.SubCategoria;
import com.tp.donatrack.domain.entidad.Unidad;

public class donacionSegmentadaTest{
    
        private EntidadBeneficiaria unaEntidadBeneficiaria;
        private SubCategoria unaSubCategoria;
        private Categoria unaCategoria;
        private DonacionSegmentada donacion;

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
            
            EntidadBeneficiaria.Necesidad necesidadExistente = 
            new EntidadBeneficiaria.Necesidad(1, unaSubCategoria, bienes);

            unaEntidadBeneficiaria.getNecesidades().add(necesidadExistente);        
    }
        @Test
        void testImplementarDonacionSegmentada(){
            Integer cantNecesidadesAntes = unaEntidadBeneficiaria.getCantNecesidades();
            donacion.donar(unaEntidadBeneficiaria);
            assertTrue(unaEntidadBeneficiaria.getCantNecesidades() < cantNecesidadesAntes);
        }
}