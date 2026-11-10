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
import java.util.regex.Pattern;
import java.io.IOException;

@Component
public class LectorCSV implements iLectorArchivo {

    private static final Pattern COLUMNA = Pattern.compile(
            "\\G(?:\"((?:[^\"]|\"\")*)\"|([^,\"]*))(?:,|$)");

    public List<RegistroDonanteDTO> leerArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        List<RegistroDonanteDTO> registros = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8)
        )) {
            if (br.readLine() == null) {
                throw new IllegalArgumentException("El CSV requiere una cabecera.");
            }
            int fila = 1;
            String linea;

            while ((linea = br.readLine()) != null) {
                fila++;
                if (linea.isBlank()) continue;

                var matcher = COLUMNA.matcher(linea);
                var columnas = new ArrayList<String>(6);
                int fin = 0;
                while (matcher.find()) {
                    fin = matcher.end();
                    String valor = matcher.group(1);
                    columnas.add((valor == null ? matcher.group(2) : valor.replace("\"\"", "\"")).trim());
                    if (fin == linea.length()) {
                        if (linea.endsWith(",")) columnas.add("");
                        break;
                    }
                }
                if (fin != linea.length() || columnas.size() != 6) {
                    throw new IllegalArgumentException("La fila " + fila + " del CSV debe tener seis columnas.");
                }
                registros.add(new RegistroDonanteDTO(columnas.get(0), columnas.get(1), columnas.get(2),
                        columnas.get(3), columnas.get(4), columnas.get(5)));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("No se pudo leer el archivo CSV.", e);
        }
        if (registros.isEmpty()) {
            throw new IllegalArgumentException("El CSV no contiene donantes.");
        }

        return registros;
    }

    @Override
    public Boolean soportaExtension(String extension) {
        return "csv".equalsIgnoreCase(extension);
    }
}

