package com.tp.donatrack.services;

import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.dtos.CrearEventoRequest;
import com.tp.donatrack.dtos.TrazaDonacionDTO;
import com.tp.donatrack.dtos.TrazaSegmentoDTO;
import com.tp.donatrack.repositories.DonacionRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TrazabilidadService {
    private final DonacionRepository donacionRepository;

    public TrazabilidadService(
            DonacionRepository donacionRepository
    ) {
        this.donacionRepository = donacionRepository;
    }

    private Donacion buscarDonacionPorId(Integer id){
        Donacion donacion = donacionRepository.findById(id);
        if (donacion == null) {
            throw new IllegalArgumentException("No se encontró la donación con ID: " + id);
        }
        return donacion;
    }

    private DonacionSegmentada buscarDonacionSegmentadaPorId(Integer idDonacion, Integer idSegmentada){
        Donacion donacion = this.buscarDonacionPorId(idDonacion);
        return donacion.getDonacionesSegmentadas().stream()
                .filter(p -> p.getId().equals(idSegmentada))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la el segmento de la donación con ID: " + idSegmentada
                ));
    }

    public TrazaDonacionDTO trazabilizarDonacion(Integer id) {
        Donacion donacion = this.buscarDonacionPorId(id);

        List<DonacionSegmentada> segmentos = donacion.getDonacionesSegmentadas();
        List<TrazaSegmentoDTO> trazabilidades = new ArrayList<>();
        for (DonacionSegmentada segmentada : segmentos) {
            trazabilidades.add(
                    TrazaSegmentoDTO.builder()
                            .id(Math.toIntExact(segmentada.getId()))
                            .eventos(segmentada.getHistorial())
                            .build());
        }
        return TrazaDonacionDTO.builder()
                .id(donacion.getId())
                .estado(donacion.getEstado())
                .segmentos(trazabilidades)
                .build();
    }

    public TrazaSegmentoDTO trazabilizarDonacionSegmentada(Integer idDonacion, Integer idSegmento) {
        DonacionSegmentada segmentada = buscarDonacionSegmentadaPorId(idDonacion, idSegmento);
        return TrazaSegmentoDTO.builder().id(idSegmento).eventos(segmentada.getHistorial()).build();
    }

    public TrazaSegmentoDTO transicionarDonacion(
            Integer idDonacion,
            Integer idSegmento,
            CrearEventoRequest request
    ) {
        DonacionSegmentada segmentada =  buscarDonacionSegmentadaPorId(idDonacion, idSegmento);
        if (!segmentada.transicionPosible(segmentada.getEstado(), request.getNuevoEstado())) {
            throw new IllegalArgumentException("No es posible realizar esta transicion");
        }

        segmentada.transicionar(
                request.getNuevoEstado(),
                request.getActor(),
                request.getDescripcion()
        );

        return TrazaSegmentoDTO.builder().id(idSegmento).eventos(segmentada.getHistorial()).build();
    }

    public TrazaSegmentoDTO transicionListaEntregar(
            Integer idDonacion,
            Integer idSegmento,
            String actor
    ) {
        DonacionSegmentada segmentada =  buscarDonacionSegmentadaPorId(idDonacion, idSegmento);
        if (!segmentada.transicionPosible(segmentada.getEstado(), EstadoDonacionSegmentada.LISTA_PARA_ENTREGAR)) {
            throw new IllegalArgumentException("No es posible realizar esta transicion");
        }
        segmentada.listarParaEntrega(actor);
        return TrazaSegmentoDTO.builder().id(idSegmento).eventos(segmentada.getHistorial()).build();
    }

    public TrazaSegmentoDTO transicionEnTraslado(
            Integer idDonacion,
            Integer idSegmento,
            String actor
    ) {
        DonacionSegmentada segmentada =  buscarDonacionSegmentadaPorId(idDonacion, idSegmento);
        if (!segmentada.transicionPosible(segmentada.getEstado(), EstadoDonacionSegmentada.EN_TRASLADO)) {
            throw new IllegalArgumentException("No es posible realizar esta transicion");
        }
        segmentada.iniciarTraslado(actor);
        return TrazaSegmentoDTO.builder().id(idSegmento).eventos(segmentada.getHistorial()).build();
    }

    public TrazaSegmentoDTO transicionEntregaFallida(
            Integer idDonacion,
            Integer idSegmento,
            String actor,
            String justificacion
    ) {
        DonacionSegmentada segmentada =  buscarDonacionSegmentadaPorId(idDonacion, idSegmento);
        if (!segmentada.transicionPosible(segmentada.getEstado(), EstadoDonacionSegmentada.ENTREGA_FALLIDA)) {
            throw new IllegalArgumentException("No es posible realizar esta transicion");
        }
        segmentada.registrarEntregaFallida(actor, justificacion);
        return TrazaSegmentoDTO.builder().id(idSegmento).eventos(segmentada.getHistorial()).build();
    }

    public TrazaSegmentoDTO transicionMarcarVencida(
            Integer idDonacion,
            Integer idSegmento,
            String actor
    ) {
        DonacionSegmentada segmentada =  buscarDonacionSegmentadaPorId(idDonacion, idSegmento);
        if (!segmentada.transicionPosible(segmentada.getEstado(), EstadoDonacionSegmentada.VENCIDA)) {
            throw new IllegalArgumentException("No es posible realizar esta transicion");
        }
        segmentada.marcarVencida(actor);
        return TrazaSegmentoDTO.builder().id(idSegmento).eventos(segmentada.getHistorial()).build();
    }

}
