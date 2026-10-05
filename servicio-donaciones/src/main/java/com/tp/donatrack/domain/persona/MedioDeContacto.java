package com.tp.donatrack.domain.persona;

import com.tp.commons.domain.notificador.TipoNotificador;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Medio de contacto de una Persona, modelado como entidad/tabla según el DER
 * (idMedioContacto, idPersona FK, valor, esPredeterminado, tipoNotificador).
 */
@Getter
@Setter
@Entity
@Table(name = "medios_de_contacto")
public class MedioDeContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medio_contacto")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_notificador")
    private TipoNotificador tipoNotificador;

    @Column(name = "valor")
    private String valor;

    @Column(name = "es_predeterminado")
    private boolean esPredeterminado;

    public MedioDeContacto() {
    }

    public MedioDeContacto(TipoNotificador tipoNotificador, String valor, boolean esPredeterminado) {
        this.tipoNotificador = tipoNotificador;
        this.valor = valor;
        this.esPredeterminado = esPredeterminado;
    }
}
