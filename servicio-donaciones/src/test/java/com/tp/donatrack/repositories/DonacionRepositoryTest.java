package com.tp.donatrack.repositories;

import com.tp.commons.domain.donaciones.Unidad;
import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.bien.BienPerecedero;
import com.tp.donatrack.domain.bien.CategoriaBien;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de persistencia de {@link Donacion}: id de tipo {@code Long},
 * segmentadas persistidas en cascada, estado almacenado mediante
 * {@code AttributeConverter} e historial de trazabilidad persistido como tabla.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class DonacionRepositoryTest {

    @Autowired
    private DonacionRepository donacionRepository;

    private Donacion nuevaDonacion() {
        SubCategoria sub = new SubCategoria(CategoriaBien.ALIMENTOS, "Fideos", Unidad.KG);
        BienPerecedero bien = new BienPerecedero("Fideos", "500g", null, sub, new Date());
        List<Bien> bienes = List.of(bien);
        return new Donacion(null, "Donación de prueba", new Date(), bienes);
    }

    @Test
    void persisteDonacionConIdLongYSegmentadasEnCascada() {
        Donacion guardada = donacionRepository.save(nuevaDonacion());

        assertThat(guardada.getId()).isNotNull();
        assertThat(guardada.getId()).isInstanceOf(Long.class);
        assertThat(guardada.getDonacionesSegmentadas()).isNotEmpty();
        assertThat(guardada.getDonacionesSegmentadas().get(0).getId()).isNotNull();
    }

    @Test
    void persisteYRecuperaElEstadoViaConverter() {
        Donacion guardada = donacionRepository.save(nuevaDonacion());
        Long id = guardada.getId();

        Optional<Donacion> recuperada = donacionRepository.findById(id);
        assertThat(recuperada).isPresent();

        DonacionSegmentada segmentada = recuperada.get().getDonacionesSegmentadas().get(0);
        assertThat(segmentada.getEstado()).isEqualTo(EstadoDonacionSegmentada.EN_DEPOSITO);
        assertThat(segmentada.getEstado().isEnDeposito()).isTrue();
    }

    @Test
    void persisteElHistorialDeTrazabilidad() {
        Donacion guardada = donacionRepository.save(nuevaDonacion());

        Optional<Donacion> recuperada = donacionRepository.findById(guardada.getId());
        assertThat(recuperada).isPresent();

        DonacionSegmentada segmentada = recuperada.get().getDonacionesSegmentadas().get(0);
        assertThat(segmentada.getHistorial()).isNotEmpty();
        assertThat(segmentada.getHistorial().get(0).getEstadoNuevo())
                .isEqualTo(EstadoDonacionSegmentada.EN_DEPOSITO);
    }

    @Test
    void findSegmentadaByIdLaEncuentra() {
        Donacion guardada = donacionRepository.save(nuevaDonacion());
        Long segId = guardada.getDonacionesSegmentadas().get(0).getId();

        DonacionSegmentada encontrada = donacionRepository.findSegmentadaById(segId);

        assertThat(encontrada).isNotNull();
        assertThat(encontrada.getId()).isEqualTo(segId);
    }
}
