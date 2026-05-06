package domain.persona;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class PersonaHumana extends Persona {
    private String nombre;
    private String genero;
    private String apellido;
    private Date fechaNacimiento;
    private int edad;
    private int nroDocumento;
}
