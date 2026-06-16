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
    private PersonaJuridica datosDeEntidad;
    private List<NecesidadMaterial> nececidades = new ArrayList<>();
    private Integer id;

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
        List<NecesidadMaterial> necesidadesActivas = this.necesidadesActivas();
        if (necesidadesActivas.isEmpty()) {
            throw new RuntimeException("No existe esa necesidad en la lista de requerimientos");
        } else {
            for (NecesidadMaterial necesidad : necesidadesActivas) {
                if (necesidad.getSubCategoria().equals(donacion.getSubCategoria())) {
                    necesidad.recibirDonacion(donacion);
                    break;
                }
            }
        }
    }
}
