package domain.notificacion;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Notificacion {
    private String titulo;
    private String cuerpo;
    private String asunto;
    private Date fecha;
    private TipoNotificacion tipo;
}
