package com.tp.donatrack.notificaciones.domain.entities;

import com.tp.commons.domain.notificador.TipoNotificador;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long idPersona;
    private boolean enviado;
    @Enumerated(EnumType.STRING)
    private TipoNotificador medio;
    private String asunto;
    private String mensaje;
    private String destinatario;
    private LocalDateTime fecha;
}
