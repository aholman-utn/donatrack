package com.tp.donatrack.services;

import com.tp.donatrack.ImportacionResponseDTO;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.notificacion.Notificacion;
import com.tp.donatrack.domain.notificacion.TipoNotificacion;
import com.tp.donatrack.domain.notificador.TipoNotificador;
import com.tp.donatrack.domain.persona.*;
import com.tp.donatrack.repositories.DonanteRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

@Service
public class DonanteService {

    private DonanteRepository donanteRepository;
    private NotificacionService notifService;

    public DonanteService(DonanteRepository donanteRepository, NotificacionService notifService) {
        this.donanteRepository = donanteRepository;
        this.notifService = notifService;
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

    private String generarPassword() {
        String[] palabras = {"luna", "rio", "mate", "sol", "verde", "auto", "nube", "casa", "mar", "flor"};
        SecureRandom random = new SecureRandom();

        String palabra = palabras[random.nextInt(palabras.length)];
        int numero = random.nextInt(90) + 10; // 10 a 99

        // opcional: primera letra mayúscula a veces
        if (random.nextBoolean()) {
            palabra = palabra.substring(0, 1).toUpperCase() + palabra.substring(1);
        }

        return palabra + numero;
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
        Donante nuevo_donante = this.donanteRepository.darDeAlta(donante);
        if(nuevo_donante!= null) {
            //Notifico
            Notificacion notif_bienvenida = new Notificacion();
            notif_bienvenida.setTipo(TipoNotificacion.BIENVENIDA);
            String email = donante.getPersona().getMedioDeContacto().get("email").getFirst();
            String password = this.generarPassword();
            notif_bienvenida.setTitulo("Bienvenido a DonaTrack");
            notif_bienvenida.setAsunto("Registro exitoso");
            notif_bienvenida.setCuerpo(
                    "¡Bienvenido a DonaTrack! Gracias por sumarte como donante. A continuación, te compartiremos las credenciales de acceso para iniciar sesion:\n" +
                            "Email: " + donante.getPersona().getMedioDeContacto().get("email").get(0) + "\n" +
                            "Password: " + password
            );
            notif_bienvenida.setFecha(new Date());
            this.notifService.notificar(notif_bienvenida, TipoNotificador.EMAIL, email);
        }
        return nuevo_donante;
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
                persona = new PersonaHumana(nombre_persona, genero, apellido, null, edad, documentoLimpio);
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

        } else {
            //actualizar los campos

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