package com.tp.donatrack.services;

import com.tp.donatrack.ImportacionResponseDTO;
import com.tp.donatrack.domain.entidad.Donante;
import com.tp.donatrack.domain.entidad.TipoOrganizacion;
import com.tp.donatrack.domain.persona.Persona;
import com.tp.donatrack.domain.persona.PersonaHumana;
import com.tp.donatrack.domain.persona.PersonaJuridica;
import com.tp.donatrack.domain.persona.TipoPersona;
import com.tp.donatrack.repositories.DonanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

            ImportacionResponseDTO response = new ImportacionResponseDTO(true, mensaje);
            response.setData(this.donanteRepository.findAll());

            return response;

        } catch (Exception e) {
            return new ImportacionResponseDTO(false, "Error al importar el CSV" + " " + e.getMessage());
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

    private Donante darDeAlta(Donante donante){
        return this.donanteRepository.darDeAlta(donante);
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

        Donante existe = buscarDonante(email);
        if(existe== null){
            Donante donante = new Donante();
            Persona persona;
            if (tipoPersona.equals(TipoPersona.HUMANA.name())) {

                String[] nombreSplit = nombre.split(" ", 2);

                String nombre_persona= nombreSplit[0];
                String apellido = nombreSplit.length > 1 ? nombreSplit[1] : "";
                String documentoLimpio = documento.replaceAll("[^0-9]", "");
                String genero = "Sin especificar";
                Integer edad = 0;
                persona = new PersonaHumana(nombre_persona, genero, apellido, edad, documentoLimpio);
                persona.agregarMedioDeContacto("email",email);
                persona.agregarMedioDeContacto("telefono",telefono);

            }
            else {
                PersonaJuridica pj = new PersonaJuridica();
                pj.agregarMedioDeContacto("email",email);
                pj.agregarMedioDeContacto("telefono", telefono);
                pj.setRazonSocial(nombre);
                TipoOrganizacion tipo = detectarTipo(nombre);
                pj.setTipo(tipo);
                pj.setRubro("Sin especificar");
                persona = pj;
            }

            donante.setPersona(persona);
            this.darDeAlta(donante);
        }

    }

    public TipoOrganizacion detectarTipo(String razonSocial){

        String n = razonSocial.toLowerCase();

        if (n.contains("s.a") || n.contains("s.a.") ||
                n.contains("s.r.l") || n.contains("s.r.l.") ||
                n.contains("srl") || n.contains("sa") || n.contains("sas")) {
            return TipoOrganizacion.EMPRESA;
        }

        if (n.contains("ong")) {
            return TipoOrganizacion.ONG;
        }

        if (n.contains("ministerio") ||
                n.contains("municipalidad") ||
                n.contains("gobierno")) {
            return TipoOrganizacion.GUBERNAMENTAL;
        }

        return TipoOrganizacion.INSTITUCION;

    }

    public Donante buscarDonante(String email){
        return this.donanteRepository.buscarDonante(email);
    }
}

