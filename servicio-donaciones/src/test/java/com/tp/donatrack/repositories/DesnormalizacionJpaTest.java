package com.tp.donatrack.repositories;

import com.tp.commons.domain.donaciones.Unidad;
import com.tp.commons.domain.donantes.Nivel;
import com.tp.donatrack.domain.bien.Bien;
import com.tp.donatrack.domain.bien.BienPerecedero;
import com.tp.donatrack.domain.bien.CategoriaBien;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacion;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.necesidad.NecesidadMaterial;
import com.tp.donatrack.domain.necesidad.NecesidadRecurrente;
import com.tp.donatrack.domain.persona.PersonaHumana;
import com.tp.donatrack.domain.persona.PersonaJuridica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class DesnormalizacionJpaTest {

    @Autowired
    private DonacionRepository donacionRepository;

    @Autowired
    private DonacionSegmentadaRepository donacionSegmentadaRepository;

    @Autowired
    private DonanteRepository donanteRepository;

    @Autowired
    private EntidadBeneficiariaRepository entidadBeneficiariaRepository;

    @Test
    @DisplayName("Desnormalización en DonacionSegmentada: persiste categoria_bien y donante_id para consultas sin JOINs")
    void testDesnormalizacionDonacionSegmentada() {
        PersonaHumana ph = new PersonaHumana();
        ph.setNombre("Carlos");
        ph.setApellido("Gómez");

        Donante donante = new Donante(ph);
        donante = donanteRepository.save(donante);
        Long donanteId = donante.getId();

        SubCategoria subAlimentos = new SubCategoria(CategoriaBien.ALIMENTOS, "Arroz", Unidad.KG);
        Bien bien = new BienPerecedero("Arroz 1kg", "Paquete", null, subAlimentos, new Date());

        Donacion donacion = new Donacion(donante, "Donación para comedor", new Date(), List.of(bien));
        donacion = donacionRepository.save(donacion);

        DonacionSegmentada segmentada = donacion.getDonacionesSegmentadas().get(0);

        // Verificamos campos desnormalizados
        assertThat(segmentada.getCategoriaBien()).isEqualTo(CategoriaBien.ALIMENTOS);
        assertThat(segmentada.getDonanteId()).isEqualTo(donanteId);

        // Consulta directa sin JOIN con subcategorias ni donaciones
        List<DonacionSegmentada> filtradas = donacionSegmentadaRepository
                .findByDonanteIdAndCategoriaBien(donanteId, CategoriaBien.ALIMENTOS);
        assertThat(filtradas).hasSize(1);
        assertThat(filtradas.get(0).getId()).isEqualTo(segmentada.getId());
    }

    @Test
    @DisplayName("Desnormalización en Donacion: persiste estado agregado de la donación padre")
    void testDesnormalizacionEstadoDonacionPadre() {
        SubCategoria sub = new SubCategoria(CategoriaBien.ALIMENTOS, "Lentejas", Unidad.KG);
        Bien bien = new BienPerecedero("Lentejas 500g", "Bolsa", null, sub, new Date());

        Donacion donacion = new Donacion(null, "Donación estado inicial", new Date(), List.of(bien));
        donacion = donacionRepository.save(donacion);

        // Nace en PENDIENTE
        assertThat(donacion.getEstado()).isEqualTo(EstadoDonacion.PENDIENTE);

        List<Donacion> pendientes = donacionRepository.findByEstado(EstadoDonacion.PENDIENTE);
        assertThat(pendientes).extracting(Donacion::getId).contains(donacion.getId());
    }

    @Test
    @DisplayName("Desnormalización en EntidadBeneficiaria y Segmentada: persiste nombre de entidad y contador de donaciones")
    void testDesnormalizacionEntidadYAsignacion() {
        SubCategoria sub = new SubCategoria(CategoriaBien.ALIMENTOS, "Fideos", Unidad.KG);
        Bien bien = new BienPerecedero("Fideos", "Paquete", null, sub, new Date());

        Donacion donacion = new Donacion(null, "Donación fideos", new Date(), List.of(bien));
        donacion = donacionRepository.save(donacion);
        DonacionSegmentada segmentada = donacion.getDonacionesSegmentadas().get(0);

        PersonaJuridica pj = new PersonaJuridica();
        pj.setRazonSocial("Comedor Los Piletones");

        EntidadBeneficiaria entidad = new EntidadBeneficiaria(pj);
        NecesidadMaterial necesidad = new NecesidadRecurrente(sub, 50, new Date(), 1);
        entidad.agregarNecesidad(necesidad);
        entidad = entidadBeneficiariaRepository.save(entidad);

        assertThat(entidad.getTotalDonacionesRecibidas()).isEqualTo(0);

        // Asignamos la donación a la entidad
        segmentada.asignar(entidad, "Operador");
        donacionRepository.save(donacion);
        entidadBeneficiariaRepository.save(entidad);

        // Verificamos que se desnormalizó el nombre de la entidad en la donación segmentada
        assertThat(segmentada.getEntidadBeneficiariaNombre()).isEqualTo("Comedor Los Piletones");
        assertThat(segmentada.getEntidadBeneficiariaAsignadaId()).isEqualTo(entidad.getDatosDeEntidad().getId());

        // Verificamos que se incrementó el contador desnormalizado en la entidad
        Optional<EntidadBeneficiaria> entidadActualizada = entidadBeneficiariaRepository.findById(entidad.getIdEntidadBeneficiaria());
        assertThat(entidadActualizada).isPresent();
        assertThat(entidadActualizada.get().getTotalDonacionesRecibidas()).isEqualTo(1);
    }

    @Test
    @DisplayName("Desnormalización en Donante: PerfilDonante embebido en tabla donantes")
    void testDesnormalizacionPerfilDonanteEmbebido() {
        PersonaHumana ph = new PersonaHumana();
        ph.setNombre("Lucía");
        ph.setApellido("Fernández");

        Donante donante = new Donante(ph);
        donante.getPerfil().setNivelDonante(Nivel.SOSTENEDOR);
        donante.getPerfil().setProgreso(75.5);
        donante.getPerfil().setMisionActualId(2L);
        donante.getPerfil().setTotalDonacionesExitosas(5);
        donante.getPerfil().setTotalMisionesCompletadas(3);

        donante = donanteRepository.save(donante);
        Long id = donante.getId();

        Optional<Donante> recuperado = donanteRepository.findById(id);
        assertThat(recuperado).isPresent();

        Donante d = recuperado.get();
        assertThat(d.getPerfil().getNivelDonante()).isEqualTo(Nivel.SOSTENEDOR);
        assertThat(d.getPerfil().getProgreso()).isEqualTo(75.5);
        assertThat(d.getPerfil().getMisionActualId()).isEqualTo(2L);
        assertThat(d.getPerfil().getTotalDonacionesExitosas()).isEqualTo(5);
        assertThat(d.getPerfil().getTotalMisionesCompletadas()).isEqualTo(3);

        // Verificamos consulta de ranking usando el campo desnormalizado
        List<Donante> ranking = donanteRepository.findTopDonantesRanking();
        assertThat(ranking).isNotEmpty();
        assertThat(ranking.get(0).getId()).isEqualTo(id);
    }
}
