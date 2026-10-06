package com.tp.donatrack.logistica.repository;

import com.tp.donatrack.logistica.domain.EventoLogistica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogisticaEventRepository extends JpaRepository<EventoLogistica, Long> {

    default void registrar(EventoLogistica e) {
        save(e);
    }

    default List<EventoLogistica> obtenerTodos() {
        return findAll();
    }
}
