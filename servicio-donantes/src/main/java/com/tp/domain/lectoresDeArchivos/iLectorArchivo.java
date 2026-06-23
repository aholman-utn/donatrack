package com.tp.domain.lectoresDeArchivos;

import com.tp.dtos.input.Registro;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface iLectorArchivo {
    List<Registro> leerArchivo(MultipartFile file);
    Boolean soportaExtension(String extension);
}
