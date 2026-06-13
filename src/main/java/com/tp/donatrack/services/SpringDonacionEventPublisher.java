package com.tp.donatrack.services;

import com.tp.donatrack.domain.donacion.DonacionEntregadaEvent;
import com.tp.donatrack.domain.donacion.DonacionEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringDonacionEventPublisher implements DonacionEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDonacionEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publicar(DonacionEntregadaEvent event) {
        // Publicamos el evento al bus de Spring
        applicationEventPublisher.publishEvent(event);
    }
}
