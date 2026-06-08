package com.tp.donatrack.domain.lectoresDeArchivos.csv;

import com.tp.donatrack.domain.lectoresDeArchivos.iLectorArchivo;
import com.tp.donatrack.dtos.input.importacionCSV.RegistroDonanteDTO;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class LectorCSV implements iLectorArchivo {

    public List<RegistroDonanteDTO> leerArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        List<RegistroDonanteDTO> registros = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8)
        )) {
            br.readLine(); // Saltar cabecera
            String linea;

            while ((linea = br.readLine()) != null) {
                if (linea.isBlank()) continue;

                String[] partes = linea.split(",");
                if (partes.length < 6) {
                    throw new IllegalArgumentException("Línea inválida en el CSV: " + linea);
                }

                // Guardamos los strings crudos en el DTO
                registros.add(new RegistroDonanteDTO(
                        partes[0].trim(), partes[1].trim(), partes[2].trim(),
                        partes[3].trim(), partes[4].trim(), partes[5].trim()
                ));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error técnico leyendo el archivo CSV: " + e.getMessage(), e);
        }

        return registros;
    }

    @Override
    public Boolean soportaExtension(String extension) {
        return "csv".equalsIgnoreCase(extension);
    }
}

