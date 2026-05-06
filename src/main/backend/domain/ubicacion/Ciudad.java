package domain.ubicacion;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ciudad {
    private String nombre;
    private Provincia provincia;
}
