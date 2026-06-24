package com.tp.donatrack.services;

import com.tp.donatrack.domain.asignacion.ResultadoMatchmaking;
import com.tp.donatrack.domain.asignacion.ServicioMatchmaking;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.dtos.EntidadRankingDTO;
import com.tp.donatrack.dtos.ResultadoMatchmakingDTO;
import com.tp.donatrack.repositories.DonacionRepository;
import com.tp.donatrack.repositories.EntidadBeneficiariaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación que orquesta el matchmaking:
 * - Obtiene la donación segmentada y las entidades del repositorio.
 * - Delega al ServicioMatchmaking de dominio la ejecución de los algoritmos.
 * - Mapea el resultado a DTOs para la capa REST.
 * - Permite asignar una donación segmentada a la entidad elegida del ranking.
 */
@Service
public class MatchmakingService {

    private final DonacionRepository donacionRepository;
    private final EntidadBeneficiariaRepository entidadBeneficiariaRepository;
    private final ServicioMatchmaking servicioMatchmaking;

    public MatchmakingService(
            DonacionRepository donacionRepository,
            EntidadBeneficiariaRepository entidadBeneficiariaRepository) {
        this.donacionRepository = donacionRepository;
        this.entidadBeneficiariaRepository = entidadBeneficiariaRepository;
        this.servicioMatchmaking = new ServicioMatchmaking();
    }

    /**
     * Ejecuta el matchmaking para una donación segmentada específica.
     */
    public ResultadoMatchmakingDTO obtenerRanking(Integer donacionSegmentadaId) {
        DonacionSegmentada segmentada = donacionRepository.findSegmentadaById(donacionSegmentadaId);
        if (segmentada == null) {
            throw new IllegalArgumentException("No se encontró la donación segmentada con ID: " + donacionSegmentadaId);
        }
        if (segmentada.getEstado() != EstadoDonacionSegmentada.EN_DEPOSITO) {
            throw new IllegalStateException(
                    "Solo se puede ejecutar el matchmaking para donaciones en estado EN_DEPOSITO");
        }

        List<EntidadBeneficiaria> todasLasEntidades = entidadBeneficiariaRepository.findAll();

        ResultadoMatchmaking resultado = servicioMatchmaking.ejecutar(segmentada, todasLasEntidades);

        return mapearResultado(segmentada, resultado);
    }

    /**
     * Retorna el ranking para todas las donaciones en depósito de un donante.
     */
    public List<ResultadoMatchmakingDTO> obtenerRankingPorDonante(Long donanteId) {
        List<DonacionSegmentada> segmentadasEnDeposito = donacionRepository
                .findSegmentadasEnDepositoByDonanteId(donanteId);
        if (segmentadasEnDeposito.isEmpty()) {
            throw new IllegalArgumentException(
                    "No se encontraron donaciones en depósito para el donante con ID: " + donanteId);
        }

        List<EntidadBeneficiaria> todasLasEntidades = entidadBeneficiariaRepository.findAll();

        return segmentadasEnDeposito.stream()
                .map(segmentada -> {
                    ResultadoMatchmaking resultado = servicioMatchmaking.ejecutar(segmentada, todasLasEntidades);
                    return mapearResultado(segmentada, resultado);
                })
                .collect(Collectors.toList());
    }

    /**
     * Asigna una donación segmentada a la entidad beneficiaria elegida del ranking.
     */
    public void asignarDonacion(Integer donacionSegmentadaId, Long entidadBeneficiariaId) {
        DonacionSegmentada segmentada = donacionRepository.findSegmentadaById(donacionSegmentadaId);
        if (segmentada == null) {
            throw new IllegalArgumentException("No se encontró la donación segmentada con ID: " + donacionSegmentadaId);
        }
        if (segmentada.getEstado() != EstadoDonacionSegmentada.EN_DEPOSITO) {
            throw new IllegalStateException("Solo se pueden asignar donaciones en estado EN_DEPOSITO");
        }

        EntidadBeneficiaria entidad = entidadBeneficiariaRepository.find(entidadBeneficiariaId);
        if (entidad == null) {
            throw new IllegalArgumentException(
                    "No se encontró la entidad beneficiaria con ID: " + entidadBeneficiariaId);
        }

        segmentada.asignar(entidad, "Administrador");
    }

    private ResultadoMatchmakingDTO mapearResultado(DonacionSegmentada segmentada, ResultadoMatchmaking resultado) {
        return ResultadoMatchmakingDTO.builder()
                .donacionSegmentadaId(segmentada.getId())
                .subCategoria(segmentada.getSubCategoria() != null ? segmentada.getSubCategoria().getDescripcion()
                        : "Sin Categoría")
                .cantidadBienes(segmentada.getCantidad())
                .coincidencias(mapearEntidades(resultado.getCoincidencias()))
                .rankingCompatibilidad(mapearEntidades(resultado.getResultadoCompatibilidad()))
                .rankingSubAtendidos(mapearEntidades(resultado.getResultadoSubAtendidos()))
                .entidadesPropuestas(mapearEntidades(resultado.getEntidadesPropuestas()))
                .huboCoincidencias(resultado.isHuboCoincidencias())
                .build();
    }

    private List<EntidadRankingDTO> mapearEntidades(List<EntidadBeneficiaria> entidades) {
        return entidades.stream()
                .map(e -> EntidadRankingDTO.builder()
                        .id(e.getDatosDeEntidad() != null ? e.getDatosDeEntidad().getId() : null)
                        .razonSocial(
                                e.getDatosDeEntidad() != null ? e.getDatosDeEntidad().getRazonSocial() : "Sin nombre")
                        .cantNecesidadesActivas(e.getCantNececidadesActivas())
                        .build())
                .collect(Collectors.toList());
    }
}
