package com.tp.donatrack.domain.lectoresDeArchivos;

import com.tp.donatrack.dtos.input.importacionCSV.RegistroDonanteDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface iLectorArchivo {
    List<RegistroDonanteDTO> leerArchivo(MultipartFile file);
    Boolean soportaExtension(String extension);
}

