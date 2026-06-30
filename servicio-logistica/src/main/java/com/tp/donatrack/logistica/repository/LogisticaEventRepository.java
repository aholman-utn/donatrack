package com.tp.donatrack.logistica.repository;

import com.tp.donatrack.logistica.domain.EventoLogistica;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class LogisticaEventRepository {
    private final List<EventoLogistica> eventos = new CopyOnWriteArrayList<>();

    public void registrar(EventoLogistica e) {
        eventos.add(e);
    }

    public List<EventoLogistica> obtenerTodos() {
        return new ArrayList<>(eventos);
    }
}
