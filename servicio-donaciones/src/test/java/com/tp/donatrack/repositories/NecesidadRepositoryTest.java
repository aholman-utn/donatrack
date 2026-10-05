package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.necesidad.EstadoNecesidad;
import com.tp.donatrack.domain.necesidad.NecesidadExtraordinaria;
import com.tp.donatrack.domain.necesidad.NecesidadMaterial;
import com.tp.donatrack.domain.necesidad.NecesidadRecurrente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de persistencia del agregado {@link NecesidadMaterial}: guardado,
 * recuperación, método de búsqueda derivado, borrado y mapeo de herencia JOINED
 * de sus subtipos.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.test.context.ActiveProfiles("test")
class NecesidadRepositoryTest {

    @Autowired
    private NecesidadRepository necesidadRepository;

    private SubCategoria subCategoria(String descripcion) {
        return new SubCategoria(null, descripcion, null);
    }

    @Test
    void guardaYRecuperaUnaNecesidadRecurrenteAsignandoId() {
        NecesidadRecurrente necesidad = new NecesidadRecurrente(subCategoria("Alimentos"), 100, new Date(), 30);
        necesidad.setEntidadBeneficiariaId(1L);

        NecesidadMaterial guardada = necesidadRepository.create(necesidad);

        assertThat(guardada.getId()).isNotNull();

        Optional<NecesidadMaterial> recuperada = necesidadRepository.findById(guardada.getId());
        assertThat(recuperada).isPresent();
        assertThat(recuperada.get()).isInstanceOf(NecesidadRecurrente.class);
        assertThat(recuperada.get().getSubCategoria().getDescripcion()).isEqualTo("Alimentos");
        assertThat(recuperada.get().getCantidadObjetivo()).isEqualTo(100);
        assertThat(recuperada.get().getEstado()).isEqualTo(EstadoNecesidad.ACTIVO);
        assertThat(((NecesidadRecurrente) recuperada.get()).getDias()).isEqualTo(30);
    }

    @Test
    void persisteLosSubtiposConSuDiscriminadorEnLaMismaTabla() {
        NecesidadExtraordinaria extraordinaria =
                new NecesidadExtraordinaria(subCategoria("Mantas"), 50, new Date(), "Temporal de frío");
        extraordinaria.setEntidadBeneficiariaId(2L);

        NecesidadMaterial guardada = necesidadRepository.create(extraordinaria);

        Optional<NecesidadMaterial> recuperada = necesidadRepository.findById(guardada.getId());
        assertThat(recuperada).isPresent();
        assertThat(recuperada.get()).isInstanceOf(NecesidadExtraordinaria.class);
        assertThat(((NecesidadExtraordinaria) recuperada.get()).getCausa()).isEqualTo("Temporal de frío");
    }

    @Test
    void findByEntidadBeneficiariaIdDevuelveSoloLasDeEsaEntidad() {
        NecesidadRecurrente deEntidad10 = new NecesidadRecurrente(subCategoria("Agua"), 10, new Date(), 15);
        deEntidad10.setEntidadBeneficiariaId(10L);
        necesidadRepository.create(deEntidad10);

        NecesidadExtraordinaria otraDeEntidad10 =
                new NecesidadExtraordinaria(subCategoria("Ropa"), 5, new Date(), "Inundación");
        otraDeEntidad10.setEntidadBeneficiariaId(10L);
        necesidadRepository.create(otraDeEntidad10);

        NecesidadRecurrente deEntidad20 = new NecesidadRecurrente(subCategoria("Pan"), 20, new Date(), 7);
        deEntidad20.setEntidadBeneficiariaId(20L);
        necesidadRepository.create(deEntidad20);

        List<NecesidadMaterial> resultado = necesidadRepository.findByEntidadBeneficiariaId(10L);

        assertThat(resultado).hasSize(2);
        assertThat(resultado)
                .allMatch(necesidad -> necesidad.getEntidadBeneficiariaId().equals(10L));
    }

    @Test
    void deleteByIdEliminaLaNecesidad() {
        NecesidadRecurrente necesidad = new NecesidadRecurrente(subCategoria("Leche"), 40, new Date(), 10);
        necesidad.setEntidadBeneficiariaId(3L);
        NecesidadMaterial guardada = necesidadRepository.create(necesidad);
        Long id = guardada.getId();

        necesidadRepository.deleteById(id);

        assertThat(necesidadRepository.findById(id)).isEmpty();
    }
}
