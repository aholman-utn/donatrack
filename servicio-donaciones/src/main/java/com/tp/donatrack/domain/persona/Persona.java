package com.tp.donatrack.domain.persona;

import com.tp.donatrack.domain.ubicacion.Direccion;
import com.tp.commons.domain.notificador.TipoNotificador;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "personas")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_persona")
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_direccion")
    private Direccion direccion;

    /**
     * Medios de contacto de la persona, persistidos en su propia tabla. La clase
     * expone además una vista en forma de {@link Map} mediante métodos de
     * conveniencia para los consumidores que trabajan con esa representación.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_persona")
    private List<MedioDeContacto> mediosDeContacto = new ArrayList<>();

    @Column(name = "fecha_ultima_interaccion")
    private LocalDateTime fechaUltimaInteraccion;

    @Column(name = "tipo_notificador")
    @Enumerated(EnumType.STRING)
    private TipoNotificador tipoNotificador;

    /**
     * Devuelve los medios de contacto agrupados como {@code tipoNotificador -> valores}.
     */
    @Transient
    public Map<String, List<String>> getMedioDeContacto() {
        Map<String, List<String>> mapa = new HashMap<>();
        for (MedioDeContacto medio : this.mediosDeContacto) {
            if (medio.getTipoNotificador() == null) {
                continue;
            }
            mapa.computeIfAbsent(medio.getTipoNotificador().name(), k -> new ArrayList<>())
                    .add(medio.getValor());
        }
        return mapa;
    }

    /**
     * Reemplaza los medios de contacto a partir de un Map tipo -> valores.
     */
    public void setMedioDeContacto(Map<String, List<String>> medios) {
        this.mediosDeContacto.removeIf(m -> !m.isEsPredeterminado());
        if (medios == null) {
            return;
        }
        medios.forEach((tipo, valores) -> {
            if (valores == null) {
                return;
            }
            TipoNotificador tn = parseTipo(tipo);
            if (tn == null) {
                return;
            }
            for (String valor : valores) {
                this.mediosDeContacto.add(new MedioDeContacto(tn, valor, false));
            }
        });
    }

    /**
     * Devuelve el medio de contacto predeterminado como {@code Map} con las claves
     * {@code "medio"} y {@code "valor"}, o {@code null} si no hay uno marcado como tal.
     */
    @Transient
    public Map<String, String> getMedioPredeterminado() {
        return this.mediosDeContacto.stream()
                .filter(MedioDeContacto::isEsPredeterminado)
                .filter(m -> m.getTipoNotificador() != null)
                .findFirst()
                .map(m -> {
                    Map<String, String> mapa = new HashMap<>();
                    mapa.put("medio", m.getTipoNotificador().name());
                    mapa.put("valor", m.getValor());
                    return mapa;
                })
                .orElse(null);
    }

    /**
     * Fija el medio predeterminado desde un Map {"medio","valor"}.
     */
    public void setMedioPredeterminado(Map<String, String> medioPredeterminado) {
        this.mediosDeContacto.removeIf(MedioDeContacto::isEsPredeterminado);
        if (medioPredeterminado == null || medioPredeterminado.isEmpty()) {
            return;
        }
        String tipo = medioPredeterminado.get("medio");
        String valor = medioPredeterminado.get("valor");
        if (tipo == null && !medioPredeterminado.isEmpty()) {
            Map.Entry<String, String> entry = medioPredeterminado.entrySet().iterator().next();
            tipo = entry.getKey();
            valor = entry.getValue();
        }
        TipoNotificador tn = parseTipo(tipo);
        if (tn != null) {
            this.mediosDeContacto.add(new MedioDeContacto(tn, valor, true));
        }
    }

    public Map<String, List<String>> agregarMedioDeContacto(String key, String value) {
        TipoNotificador tn = parseTipo(key);
        if (tn != null) {
            this.mediosDeContacto.add(new MedioDeContacto(tn, value, false));
        }
        return getMedioDeContacto();
    }

    /**
     * Reemplaza todos los medios de contacto no predeterminados de un tipo dado
     * por un único valor. Opera sobre la colección persistente.
     */
    public void reemplazarMedioDeContacto(String key, String value) {
        TipoNotificador tn = parseTipo(key);
        if (tn == null) {
            return;
        }
        this.mediosDeContacto.removeIf(m -> !m.isEsPredeterminado() && tn.equals(m.getTipoNotificador()));
        this.mediosDeContacto.add(new MedioDeContacto(tn, value, false));
    }

    private static TipoNotificador parseTipo(String clave) {
        if (clave == null) {
            return null;
        }
        try {
            return TipoNotificador.valueOf(clave.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public TipoNotificador getTipoNotificadorPreferido() {
        Map<String, String> pred = getMedioPredeterminado();
        if (pred == null || pred.isEmpty()) {
            return TipoNotificador.EMAIL;
        }
        return TipoNotificador.valueOf(pred.get("medio").toUpperCase());
    }

    public String getContactoPredeterminado() {
        Map<String, String> pred = getMedioPredeterminado();
        if (pred == null || pred.isEmpty()) {
            return null;
        }
        return pred.get("valor");
    }
}
