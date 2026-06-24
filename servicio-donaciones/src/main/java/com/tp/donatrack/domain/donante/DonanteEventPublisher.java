package com.tp.donatrack.domain.donante;

public interface DonanteEventPublisher {
    void publicar(DonanteCreadoEvent event);
}
