package com.tp.donatrack.services;

import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import com.tp.donatrack.domain.persona.PersonaHumana;
import com.tp.donatrack.repositories.DonacionSegmentadaRepository;
import com.tp.donatrack.repositories.DonanteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class IncentivosEntregas {
    private static final Logger logger = LoggerFactory.getLogger(IncentivosEntregas.class);

    private final DonacionSegmentadaRepository segmentos;
    private final DonanteRepository donantes;
    private final HttpDonacionEventPublisher incentivos;
    private final TransactionTemplate transaccion;

    public IncentivosEntregas(DonacionSegmentadaRepository segmentos, DonanteRepository donantes,
                             HttpDonacionEventPublisher incentivos, TransactionTemplate transaccion) {
        this.segmentos = segmentos;
        this.donantes = donantes;
        this.incentivos = incentivos;
        this.transaccion = transaccion;
    }

    @Scheduled(fixedDelayString = "${donatrack.incentivos.intervalo:5000}",
            initialDelayString = "${donatrack.incentivos.intervalo:5000}")
    public void procesarPendientes() {
        var pendientes = segmentos.buscarEntregasPendientes(EstadoDonacionSegmentada.ENTREGADA, PageRequest.of(0, 25));
        for (Long id : pendientes) {
            try {
                transaccion.executeWithoutResult(status -> procesar(id));
            } catch (RuntimeException error) {
                logger.warn("Quedó pendiente la evaluación de la entrega del segmento {}: {}", id, error.getMessage());
            }
        }
    }

    private void procesar(Long id) {
        var segmento = segmentos.bloquear(id).orElse(null);
        if (segmento == null || Boolean.TRUE.equals(segmento.getIncentivosProcesados())) return;
        var donante = donantes.bloquear(segmento.getDonanteId()).orElse(null);
        if (donante != null && donante.getPersona() instanceof PersonaHumana) {
            var perfil = donante.getPerfil();
            perfil.setTotalDonacionesExitosas(perfil.getTotalDonacionesExitosas() + 1);
            var fecha = segmento.getComprobanteRecepcionDonacion().getFechaEntrega();
            var entrega = fecha == null ? java.time.LocalDate.now() : fecha.toLocalDate();
            if (perfil.getFechaUltimaDonacion() == null || perfil.getFechaUltimaDonacion().isBefore(entrega)) {
                perfil.setFechaUltimaDonacion(entrega);
            }
            incentivos.evaluarEntrega(donante, id);
        }
        segmento.setIncentivosProcesados(true);
    }
}
