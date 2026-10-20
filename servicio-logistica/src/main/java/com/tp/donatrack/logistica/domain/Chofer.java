package com.tp.donatrack.logistica.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "choferes")
public class Chofer {
    @Id
    private String id = null;

    private String nombre;

    private String apellido;

    private String dni;
}
