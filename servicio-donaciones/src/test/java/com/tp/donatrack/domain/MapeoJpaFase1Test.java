package com.tp.donatrack.domain;

import com.tp.commons.domain.donaciones.Unidad;
import com.tp.donatrack.domain.bien.BienDuradero;
import com.tp.donatrack.domain.bien.BienPerecedero;
import com.tp.donatrack.domain.bien.CategoriaBien;
import com.tp.donatrack.domain.bien.EstadoBien;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.ubicacion.Ciudad;
import com.tp.donatrack.domain.ubicacion.Direccion;
import com.tp.donatrack.domain.ubicacion.Pais;
import com.tp.donatrack.domain.ubicacion.Provincia;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica el mapeo JPA de la Fase 1 (fiel al DER): cadena de ubicación con FKs
 * encadenadas, SubCategoria como entidad propia, y jerarquía Bien con herencia
 * JOINED (tabla hija con FK al id del padre).
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.test.context.ActiveProfiles("test")
class MapeoJpaFase1Test {

    @Autowired
    private TestEntityManager em;

    @Test
    void persisteLaCadenaDeUbicacionPaisProvinciaCiudadDireccion() {
        Pais pais = new Pais("Argentina");
        Provincia provincia = new Provincia("Buenos Aires", pais);
        Ciudad ciudad = new Ciudad("La Plata", provincia);

        Direccion direccion = new Direccion();
        direccion.setCalle1("Calle 7");
        direccion.setAltura(1234);
        direccion.setCiudad(ciudad);

        // Cascada ALL: persistir la dirección arrastra ciudad, provincia y país.
        Direccion guardada = em.persistFlushFind(direccion);

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getCiudad().getId()).isNotNull();
        assertThat(guardada.getCiudad().getProvincia().getId()).isNotNull();
        assertThat(guardada.getCiudad().getProvincia().getPais().getId()).isNotNull();
        assertThat(guardada.getCiudad().getProvincia().getPais().getNombre()).isEqualTo("Argentina");
    }

    @Test
    void persisteSubCategoriaComoEntidadConSuId() {
        SubCategoria sub = new SubCategoria(CategoriaBien.ALIMENTOS, "Fideos", Unidad.KG);

        SubCategoria guardada = em.persistFlushFind(sub);

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getCategoria()).isEqualTo(CategoriaBien.ALIMENTOS);
        assertThat(guardada.getUnidad()).isEqualTo(Unidad.KG);
        assertThat(guardada.getDescripcion()).isEqualTo("Fideos");
    }

    @Test
    void persisteBienPerecederoConHerenciaJoined() {
        SubCategoria sub = new SubCategoria(CategoriaBien.ALIMENTOS, "Leche", Unidad.LITROS);
        BienPerecedero bien = new BienPerecedero("Leche entera", "1L", null, sub, new Date());

        BienPerecedero guardado = em.persistFlushFind(bien);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getFechaVencimiento()).isNotNull();
        assertThat(guardado.getSubCategoria().getId()).isNotNull();
    }

    @Test
    void persisteBienDuraderoConHerenciaJoined() {
        SubCategoria sub = new SubCategoria(CategoriaBien.MOBILIARIO, "Sillas", Unidad.UNIDADES);
        BienDuradero bien = new BienDuradero("Silla", "madera", null, sub, EstadoBien.USADO);

        BienDuradero guardado = em.persistFlushFind(bien);

        assertThat(guardado.getId()).isNotNull();
        assertThat(guardado.getEstado()).isEqualTo(EstadoBien.USADO);
        assertThat(guardado.getSubCategoria().getId()).isNotNull();
    }
}
