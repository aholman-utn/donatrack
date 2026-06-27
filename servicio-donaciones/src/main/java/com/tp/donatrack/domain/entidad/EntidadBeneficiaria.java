package com.tp.donatrack.domain.entidad;
import java.util.ArrayList;
import java.util.List;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.necesidad.NecesidadMaterial;
import com.tp.donatrack.domain.persona.PersonaJuridica;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EntidadBeneficiaria {
    private Long entidadBeneficiariaId;
    private PersonaJuridica datosDeEntidad;
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
