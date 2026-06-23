package com.tp.domain.importador;

import com.tp.domain.donante.Donante;
import com.tp.domain.donante.persona.*;
import com.tp.dtos.input.Registro;
import com.tp.repositories.DonanteRepository;
import com.tp.utils.CryptoUtils;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Component
public class Importador {

    private final DonanteRepository donanteRepository;

    public Importador(DonanteRepository repo){
        this.donanteRepository=repo;
    }

    public List<Donante> iniciar_migracion(List<Registro> registros) {
        System.out.println("Iniciando migracion.....");
        List<Donante> nuevosDonantes = new ArrayList<>();

        for (Registro registro : registros) {

            Donante existe = donanteRepository.findByEmail(registro.getEmail());

            if (existe == null) {
                Donante nuevo_donante = crearDonanteSegunTipoPersona(registro);
                String password = generarPassword();
                String password_hash = CryptoUtils.hashear(password);
                nuevo_donante.setPassword(password_hash);
                donanteRepository.create(nuevo_donante);
                nuevo_donante.setPassword(password);
                nuevosDonantes.add(nuevo_donante);
            } else {
                // Si ya EXISTE, aplicamos la lógica de negocio de actualización en memoria
                actualizarCamposDonante(existe, registro);
            }
        }

        return nuevosDonantes;
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

    private Donante crearDonanteSegunTipoPersona(Registro registro) {
        Donante donante = new Donante();
        Persona persona;

        if (registro.getTipoPersona().equals(TipoPersona.HUMANA.name())) {
            String[] nombreSplit = registro.getNombre().split(" ", 2);
            String nombrePersona = nombreSplit[0];
            String apellido = nombreSplit.length > 1 ? nombreSplit[1] : "";
            String documentoLimpio = registro.getDocumento().replaceAll("[^0-9]", "");

            persona = new PersonaHumana(nombrePersona, Genero.SIN_ESPECIFICAR, apellido, null, 0, documentoLimpio);
        } else {
            PersonaJuridica pj = new PersonaJuridica();
            pj.setRazonSocial(registro.getNombre());
            pj.setTipo(detectarTipo(registro.getNombre()));
            pj.setRubro("Sin especificar");
            persona = pj;
        }

        persona.agregarMedioDeContacto("email", registro.getEmail());
        persona.agregarMedioDeContacto("telefono", registro.getTelefono());
        donante.setPersona(persona);

        return donante;
    }

    private void actualizarCamposDonante(Donante donante, Registro dto) {

        System.out.println("El donante " + donante.getPersona().getMedioDeContacto().get("email").get(0) + " ya se encuentra registrado. Acutalizando...");
        this.donanteRepository.update(donante);
    }

    private TipoOrganizacion detectarTipo(String razonSocial) {
        String n = razonSocial.toLowerCase();

        if (n.contains("s.a") || n.contains("s.a.") || n.contains("s.r.l") ||
                n.contains("s.r.l.") || n.contains("srl") || n.contains("sa") || n.contains("sas")) {
            return TipoOrganizacion.EMPRESA;
        }
        if (n.contains("ong")) {
            return TipoOrganizacion.ONG;
        }
        if (n.contains("ministerio") || n.contains("municipalidad") || n.contains("gobierno")) {
            return TipoOrganizacion.GUBERNAMENTAL;
        }
        return TipoOrganizacion.INSTITUCION;
    }

}
