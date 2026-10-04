package com.tp.donatrack.services;

import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.necesidad.NecesidadExtraordinaria;
import com.tp.donatrack.domain.necesidad.NecesidadMaterial;
import com.tp.donatrack.domain.necesidad.NecesidadRecurrente;
import com.tp.donatrack.dtos.necesidad.CrearNecesidadDTO;
import com.tp.donatrack.dtos.necesidad.NecesidadResponseDTO;
import com.tp.donatrack.repositories.EntidadBeneficiariaRepository;
import com.tp.donatrack.repositories.NecesidadRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de {@link NecesidadService}. Los repositorios (ahora JPA)
 * se mockean para aislar la lógica de negocio de la capa de persistencia.
 */
@ExtendWith(MockitoExtension.class)
class NecesidadServiceTest {

    @Mock
    private NecesidadRepository necesidadRepository;

    @Mock
    private EntidadBeneficiariaRepository entidadBeneficiariaRepository;

    @InjectMocks
    private NecesidadService necesidadService;

    private EntidadBeneficiaria entidad;

    @BeforeEach
    void setUp() {
        entidad = new EntidadBeneficiaria();
    }

    private CrearNecesidadDTO dtoRecurrente() {
        CrearNecesidadDTO dto = new CrearNecesidadDTO();
        dto.setEntidadBeneficiariaId(1L);
        dto.setSubCategoriaNombre("Alimentos");
        dto.setCantidad(100);
        dto.setFechaLimite(LocalDate.now().plusDays(30));
        dto.setTipoNecesidad("RECURRENTE");
        dto.setDiasRecurrencia(30);
        return dto;
    }

    @Test
    void crearNecesidadRecurrentePersisteYLaVinculaALaEntidad() {
        CrearNecesidadDTO dto = dtoRecurrente();
        when(entidadBeneficiariaRepository.find(1L)).thenReturn(entidad);
        when(necesidadRepository.create(any(NecesidadMaterial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NecesidadResponseDTO response = necesidadService.crearNecesidad(dto);

        ArgumentCaptor<NecesidadMaterial> captor = ArgumentCaptor.forClass(NecesidadMaterial.class);
        verify(necesidadRepository).create(captor.capture());

        NecesidadMaterial persistida = captor.getValue();
        assertThat(persistida).isInstanceOf(NecesidadRecurrente.class);
        assertThat(persistida.getEntidadBeneficiariaId()).isEqualTo(1L);
        assertThat(persistida.getCantidadObjetivo()).isEqualTo(100);

        assertThat(entidad.getCantNecesidades()).isEqualTo(1);
        assertThat(response.getTipoNecesidad()).isEqualTo("RECURRENTE");
        assertThat(response.getDiasRecurrencia()).isEqualTo(30);
    }

    @Test
    void crearNecesidadExtraordinariaMapeaLaCausa() {
        CrearNecesidadDTO dto = dtoRecurrente();
        dto.setTipoNecesidad("EXTRAORDINARIA");
        dto.setCausa("Inundación");
        when(entidadBeneficiariaRepository.find(1L)).thenReturn(entidad);
        when(necesidadRepository.create(any(NecesidadMaterial.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NecesidadResponseDTO response = necesidadService.crearNecesidad(dto);

        assertThat(response.getTipoNecesidad()).isEqualTo("EXTRAORDINARIA");
        assertThat(response.getCausa()).isEqualTo("Inundación");
    }

    @Test
    void crearNecesidadFallaSiLaEntidadNoExiste() {
        CrearNecesidadDTO dto = dtoRecurrente();
        when(entidadBeneficiariaRepository.find(1L)).thenReturn(null);

        assertThatThrownBy(() -> necesidadService.crearNecesidad(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Entidad beneficiaria no encontrada");
    }

    @Test
    void crearNecesidadFallaConTipoInvalido() {
        CrearNecesidadDTO dto = dtoRecurrente();
        dto.setTipoNecesidad("DESCONOCIDO");
        when(entidadBeneficiariaRepository.find(1L)).thenReturn(entidad);

        assertThatThrownBy(() -> necesidadService.crearNecesidad(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Tipo de necesidad inválido");
    }

    @Test
    void obtenerPorIdDevuelveLaNecesidadMapeada() {
        NecesidadExtraordinaria necesidad =
                new NecesidadExtraordinaria(null, 10, new java.util.Date(), "Sequía");
        necesidad.setEntidadBeneficiariaId(5L);
        necesidad.setId(7L);
        when(necesidadRepository.findById(7L)).thenReturn(Optional.of(necesidad));

        NecesidadResponseDTO response = necesidadService.obtenerPorId(7L);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getTipoNecesidad()).isEqualTo("EXTRAORDINARIA");
        assertThat(response.getCausa()).isEqualTo("Sequía");
    }

    @Test
    void obtenerPorIdFallaSiNoExiste() {
        when(necesidadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> necesidadService.obtenerPorId(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Necesidad no encontrada");
    }

    @Test
    void listarPorEntidadMapeaTodasLasNecesidades() {
        NecesidadRecurrente r = new NecesidadRecurrente(null, 10, new java.util.Date(), 15);
        r.setEntidadBeneficiariaId(2L);
        NecesidadExtraordinaria e = new NecesidadExtraordinaria(null, 5, new java.util.Date(), "Causa");
        e.setEntidadBeneficiariaId(2L);
        when(necesidadRepository.findByEntidadBeneficiariaId(2L)).thenReturn(List.of(r, e));

        List<NecesidadResponseDTO> resultado = necesidadService.listarPorEntidad(2L);

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(NecesidadResponseDTO::getEntidadBeneficiariaId)
                .containsOnly(2L);
    }

    @Test
    void eliminarNecesidadLaDesvinculaYLaBorra() {
        NecesidadRecurrente necesidad = new NecesidadRecurrente(null, 10, new java.util.Date(), 15);
        necesidad.setEntidadBeneficiariaId(4L);
        necesidad.setId(8L);
        entidad.agregarNecesidad(necesidad);

        when(necesidadRepository.findById(8L)).thenReturn(Optional.of(necesidad));
        when(entidadBeneficiariaRepository.find(4L)).thenReturn(entidad);

        necesidadService.eliminarNecesidad(8L);

        assertThat(entidad.getCantNecesidades()).isZero();
        verify(necesidadRepository).deleteById(8L);
    }
}
