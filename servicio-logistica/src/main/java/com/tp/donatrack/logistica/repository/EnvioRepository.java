package com.tp.donatrack.logistica.repository;

import com.tp.donatrack.logistica.domain.Envio;
import com.tp.donatrack.logistica.domain.EstadoEnvio;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvioRepository extends MongoRepository<Envio, String> {
    List<Envio> findByEstado(EstadoEnvio estado);
    List<Envio> findByRutaId(Long rutaId);
    Optional<Envio> findByDonacionSegmentadaId(Long donacionSegmentadaId);
}
