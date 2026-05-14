package com.tp.donatrack.services;

import com.tp.donatrack.ImportacionResponseDTO;
import com.tp.donatrack.repositories.DonanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class DonanteService {

    private DonanteRepository donanteRepository;

    public DonanteService(DonanteRepository donanteRepository) {
        this.donanteRepository = donanteRepository;
    }

    public ImportacionResponseDTO importarCSV(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        int total = 0;

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8)
        )) {

            br.readLine(); // cabecera

            String linea;

            while ((linea = br.readLine()) != null) {
                procesarLinea(linea);
                total++;
            }

            String mensaje = "Importados " + total + " registros exitosamente";

            return new ImportacionResponseDTO(true, mensaje);

        } catch (Exception e) {
            throw new RuntimeException("Error al importar CSV", e);
        }
    }

    private void procesarArchivo(BufferedReader br) throws Exception {

        String linea;

        // si hay cabecera, la saltamos sin asumir contenido fijo
        br.readLine();

        while ((linea = br.readLine()) != null) {
            procesarLinea(linea);
        }
    }

    private void procesarLinea(String linea) {

        if (linea == null || linea.isBlank()) return;

        String[] partes = linea.split(",");

        if (partes.length < 6) {
            throw new IllegalArgumentException("Línea inválida: " + linea);
        }

        String tipoPersona = partes[0];
        String tipoDoc = partes[1];
        String documento = partes[2];
        String nombre = partes[3];
        String email = partes[4];
        String telefono = partes[5];

        System.out.println(
                tipoPersona + " | " +
                        tipoDoc + " | " +
                        documento + " | " +
                        nombre + " | " +
                        email + " | " +
                        telefono
        );
    }
}