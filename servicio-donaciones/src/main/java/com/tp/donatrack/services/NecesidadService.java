package com.tp.donatrack.services;

import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.necesidad.NecesidadExtraordinaria;
import com.tp.donatrack.domain.necesidad.NecesidadMaterial;
import com.tp.donatrack.domain.necesidad.NecesidadRecurrente;
import com.tp.donatrack.dtos.necesidad.CrearNecesidadDTO;
import com.tp.donatrack.dtos.necesidad.NecesidadResponseDTO;
import com.tp.donatrack.repositories.EntidadBeneficiariaRepository;
import com.tp.donatrack.repositories.NecesidadRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NecesidadService {

    private final NecesidadRepository necesidadRepository;
    private final EntidadBeneficiariaRepository entidadBeneficiariaRepository;

    public NecesidadService(NecesidadRepository necesidadRepository, EntidadBeneficiariaRepository entidadBeneficiariaRepository) {
        this.necesidadRepository = necesidadRepository;
        this.entidadBeneficiariaRepository = entidadBeneficiariaRepository;
    }

    public NecesidadResponseDTO crearNecesidad(CrearNecesidadDTO dto) {
        // Validar Entidad
        EntidadBeneficiaria entidad = entidadBeneficiariaRepository.find(dto.getEntidadBeneficiariaId());
        if (entidad == null) {
            throw new IllegalArgumentException("Entidad beneficiaria no encontrada con ID: " + dto.getEntidadBeneficiariaId());
        }

        SubCategoria subCategoria = new SubCategoria(null, dto.getSubCategoriaNombre(), null);
        Date fechaLimite = Date.from(dto.getFechaLimite().atStartOfDay(ZoneId.systemDefault()).toInstant());

        NecesidadMaterial necesidad;

        if ("RECURRENTE".equalsIgnoreCase(dto.getTipoNecesidad())) {
            int dias = dto.getDiasRecurrencia() != null ? dto.getDiasRecurrencia() : 30;
            necesidad = new NecesidadRecurrente(subCategoria, dto.getCantidad(), fechaLimite, dias);
        } else if ("EXTRAORDINARIA".equalsIgnoreCase(dto.getTipoNecesidad())) {
            String causa = dto.getCausa() != null ? dto.getCausa() : "Causa no especificada";
            necesidad = new NecesidadExtraordinaria(subCategoria, dto.getCantidad(), fechaLimite, causa);
        } else {
            throw new IllegalArgumentException("Tipo de necesidad inválido. Use RECURRENTE o EXTRAORDINARIA");
        }

        necesidad.setEntidadBeneficiariaId(dto.getEntidadBeneficiariaId());
        necesidad = necesidadRepository.create(necesidad);
        
        // Vincular en memoria la necesidad a la entidad beneficiaria
        entidad.agregarNecesidad(necesidad);

        return mapearADTO(necesidad);
    }

    public NecesidadResponseDTO obtenerPorId(Long id) {
        NecesidadMaterial necesidad = necesidadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Necesidad no encontrada con ID: " + id));
        return mapearADTO(necesidad);
    }

    public List<NecesidadResponseDTO> listarPorEntidad(Long entidadBeneficiariaId) {
        List<NecesidadMaterial> necesidades = necesidadRepository.findByEntidadBeneficiariaId(entidadBeneficiariaId);
        return necesidades.stream().map(this::mapearADTO).collect(Collectors.toList());
    }

    public NecesidadResponseDTO actualizarNecesidad(Long id, CrearNecesidadDTO dto) {
        NecesidadMaterial necesidad = necesidadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Necesidad no encontrada con ID: " + id));

        necesidad.setCantidadObjetivo(dto.getCantidad());
        necesidad.setFechaDelPedido(Date.from(dto.getFechaLimite().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        
        SubCategoria subCategoria = new SubCategoria(null, dto.getSubCategoriaNombre(), null);
        necesidad.setSubCategoria(subCategoria);

        if (necesidad instanceof NecesidadRecurrente recurrente) {
            if (dto.getDiasRecurrencia() != null) {
                recurrente.setDias(dto.getDiasRecurrencia());
            }
        } else if (necesidad instanceof NecesidadExtraordinaria extraordinaria) {
            if (dto.getCausa() != null) {
                extraordinaria.setCausa(dto.getCausa());
            }
        }

        necesidad = necesidadRepository.update(necesidad);
        return mapearADTO(necesidad);
    }

    public void eliminarNecesidad(Long id) {
        NecesidadMaterial necesidad = necesidadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Necesidad no encontrada con ID: " + id));

        // Desvincular en memoria de la entidad beneficiaria
        EntidadBeneficiaria entidad = entidadBeneficiariaRepository.find(necesidad.getEntidadBeneficiariaId());
        if (entidad != null) {
            entidad.removerNecesidad(necesidad);
        }

        necesidadRepository.deleteById(id);
    }

    private NecesidadResponseDTO mapearADTO(NecesidadMaterial necesidad) {
        NecesidadResponseDTO dto = NecesidadResponseDTO.builder()
                .id(necesidad.getId())
                .entidadBeneficiariaId(necesidad.getEntidadBeneficiariaId())
                .subCategoriaNombre(necesidad.getSubCategoria() != null ? necesidad.getSubCategoria().getDescripcion() : null)
                .cantidadSolicitada(necesidad.getCantidadObjetivo())
                .cantidadCubierta(necesidad.getCantidadRecibida())
                .fechaDelPedido(necesidad.getFechaDelPedido())
                .estado(necesidad.getEstado().name())
                .activa(necesidad.activo())
                .build();

        if (necesidad instanceof NecesidadRecurrente recurrente) {
            dto.setTipoNecesidad("RECURRENTE");
            dto.setDiasRecurrencia(recurrente.getDias());
        } else if (necesidad instanceof NecesidadExtraordinaria extraordinaria) {
            dto.setTipoNecesidad("EXTRAORDINARIA");
            dto.setCausa(extraordinaria.getCausa());
        }

        return dto;
    }
}
