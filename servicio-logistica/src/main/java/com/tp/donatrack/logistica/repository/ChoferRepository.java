package com.tp.donatrack.logistica.repository;

import com.tp.donatrack.logistica.domain.Chofer;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChoferRepository extends MongoRepository<Chofer, String> {
    Optional<Chofer> findByDni(String dni);
}
