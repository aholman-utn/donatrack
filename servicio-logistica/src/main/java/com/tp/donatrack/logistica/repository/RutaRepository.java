package com.tp.donatrack.logistica.repository;

import com.tp.donatrack.logistica.domain.Ruta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RutaRepository extends MongoRepository<Ruta, String> {
}