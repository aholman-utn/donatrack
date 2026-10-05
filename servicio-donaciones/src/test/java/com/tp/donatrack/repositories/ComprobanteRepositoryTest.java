package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.donacion.ComprobanteRecepcionDonacion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifica el ComprobanteRepository migrado a JPA, con PK de tipo String
 * asignada manualmente vía generarId() (UUID), tal como lo usa el servicio.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.test.context.ActiveProfiles("test")
class ComprobanteRepositoryTest {

    @Autowired
    private ComprobanteRepository comprobanteRepository;

    @Test
    void guardaYRecuperaUnComprobantePorSuIdString() {
        ComprobanteRecepcionDonacion comprobante = ComprobanteRecepcionDonacion.builder()
                .donacionSegmentadaId(10L)
                .nombreDonante("Juan Pérez")
                .nombreEntidad("Comedor Solidario")
                .fechaEntrega(LocalDateTime.now())
                .detallesLogistica("Entregado en mano")
                .build();
        comprobante.generarId();
        String id = comprobante.getId();

        comprobanteRepository.save(comprobante);

        Optional<ComprobanteRecepcionDonacion> recuperado = comprobanteRepository.findById(id);
        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getNombreDonante()).isEqualTo("Juan Pérez");
        assertThat(recuperado.get().getDonacionSegmentadaId()).isEqualTo(10L);
    }

    @Test
    void findByIdInexistenteDevuelveVacio() {
        assertThat(comprobanteRepository.findById("no-existe")).isEmpty();
    }
}
