package com.tp.donatrack.importacion;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LectorCSV {

    public void imprimirLinea(String[] partes) {
        for (int i = 0; i < partes.length; i++) {
            System.out.print(partes[i] + " | ");
        }
        System.out.println();
    }

    public void leerArchivo(MultipartFile file) {

        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(file.getInputStream()))) {

            String linea;

            // opcional: si hay cabecera
            br.readLine();

            while ((linea = br.readLine()) != null) {

                String[] partes = linea.split(",");

                imprimirLinea(partes);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

