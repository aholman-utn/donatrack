package com.tp.donatrack.domain.entidad;
import java.util.ArrayList;
import java.util.List;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.necesidad.NecesidadMaterial;
import com.tp.donatrack.domain.persona.PersonaJuridica;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "entidades_beneficiarias")
public class EntidadBeneficiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_entidad_beneficiaria")
    private Long idEntidadBeneficiaria;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_persona_juridica")
    private PersonaJuridica datosDeEntidad;

    // Las necesidades se vinculan por entidad_beneficiaria_id (ya presente en
    // NecesidadMaterial). Se mantiene la colección en memoria para la lógica de
    // dominio existente; la fuente de verdad persistida es NecesidadRepository.
    @Transient
    private List<NecesidadMaterial> nececidades = new ArrayList<>();

    private List<NecesidadMaterial> necesidadesActivas() {
        return nececidades.stream().filter(NecesidadMaterial::activo).toList();
    }

    public EntidadBeneficiaria(PersonaJuridica personaJuridica) {
        this.datosDeEntidad = personaJuridica;
        //TODO: aca puedo poner una notificacion de bienvenida o similar
    }

    public void agregarNecesidad(NecesidadMaterial necesidad) {
        this.nececidades.add(necesidad);
    }

    public void removerNecesidad(NecesidadMaterial necesidad) {
        this.nececidades.remove(necesidad);
    }

    public int getCantNecesidades() {
        return this.nececidades.size();
    }

    public int getCantNececidadesActivas() {
        return this.necesidadesActivas().size();
    }

    public void implementarDonacion(DonacionSegmentada donacion) {
        NecesidadMaterial necesidadCorrespondiente = this.necesidadesActivas().stream()
        .filter(necesidad -> necesidad.getSubCategoria().equals(donacion.getSubCategoria()))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("La entidad no tiene una necesidad activa para la categoría: " + donacion.getSubCategoria()));

        necesidadCorrespondiente.recibirDonacion(donacion);
    }
}
