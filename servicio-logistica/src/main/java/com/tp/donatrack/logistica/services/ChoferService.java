package com.tp.donatrack.logistica.services;

import com.tp.donatrack.logistica.domain.Chofer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ChoferService {
    private final Map<Long, Chofer> choferes = new ConcurrentHashMap<>();
    private final AtomicLong choferIdSeq = new AtomicLong(1);

    public Chofer registrarChofer(Chofer chofer) {
        Long id = choferIdSeq.getAndIncrement();
        chofer.setId(id);
        choferes.put(id, chofer);
        return chofer;
    }

    public List<Chofer> listarChoferes() {
        return new ArrayList<>(choferes.values());
    }
}
