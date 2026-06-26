package com.tp.donatrack.domain.importador;

import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.persona.*;
import com.tp.donatrack.dtos.input.importacionCSV.RegistroDonanteDTO;
import com.tp.donatrack.repositories.DonanteRepository;
import com.tp.donatrack.utils.security.CryptoUtils;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

@Component
public class ImportadorCargaMasiva {
    DonanteRepository donanteRepository;

    public ImportadorCargaMasiva(DonanteRepository donanteRepo){
        this.donanteRepository=donanteRepo;
    }

    public List<Donante> iniciar_migracion(List<RegistroDonanteDTO> registros) {
        List<Donante> nuevosDonantes = new ArrayList<>();

        for (RegistroDonanteDTO registro : registros) {

            Donante existe = donanteRepository.find(registro.getEmail());

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

    private Donante crearDonanteSegunTipoPersona(RegistroDonanteDTO registro) {
        Donante donante = new Donante();
        Persona persona;

        if (registro.getTipoPersona().equals(TipoPersona.HUMANA.name())) {
            String[] nombreSplit = registro.getNombre().split(" ", 2);
            String nombrePersona = nombreSplit[0];
            String apellido = nombreSplit.length > 1 ? nombreSplit[1] : "";
            String documentoLimpio = registro.getDocumento().replaceAll("[^0-9]", "");

            persona = new PersonaHumana(nombrePersona, "Sin especificar", apellido, null, 0, documentoLimpio);
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

    private void actualizarCamposDonante(Donante donante, RegistroDonanteDTO dto) {
        Persona personaActual = donante.getPersona();
        String tipoActual = personaActual instanceof PersonaHumana ? TipoPersona.HUMANA.name() : TipoPersona.JURIDICA.name();
        boolean cambioTipoPersona = !tipoActual.equalsIgnoreCase(dto.getTipoPersona());

        if (cambioTipoPersona) {
            // Si cambió el tipo de persona, se reemplaza la persona entera
            Persona nuevaPersona;

            if (dto.getTipoPersona().equalsIgnoreCase(TipoPersona.HUMANA.name())) {
                String[] nombreSplit = dto.getNombre().split(" ", 2);
                String nombrePersona = nombreSplit[0];
                String apellido = nombreSplit.length > 1 ? nombreSplit[1] : "";
                String documentoLimpio = dto.getDocumento().replaceAll("[^0-9]", "");

                nuevaPersona = new PersonaHumana(nombrePersona, "Sin especificar", apellido, null, 0, documentoLimpio);
            } else {
                PersonaJuridica pj = new PersonaJuridica();
                pj.setRazonSocial(dto.getNombre());
                pj.setTipo(detectarTipo(dto.getNombre()));
                pj.setRubro("Sin especificar");
                nuevaPersona = pj;
            }

            // Preservar el ID y datos que no vienen del CSV
            nuevaPersona.setId(personaActual.getId());
            nuevaPersona.setDireccion(personaActual.getDireccion());
            nuevaPersona.setMedioPredeterminado(personaActual.getMedioPredeterminado());
            nuevaPersona.setFechaUltimaInteraccion(personaActual.getFechaUltimaInteraccion());

            // Setear los medios de contacto del CSV
            nuevaPersona.agregarMedioDeContacto("email", dto.getEmail());
            nuevaPersona.agregarMedioDeContacto("telefono", dto.getTelefono());

            donante.setPersona(nuevaPersona);
        } else {
            // Mismo tipo de persona: actualizar campos individuales
            if (personaActual instanceof PersonaHumana ph) {
                String[] nombreSplit = dto.getNombre().split(" ", 2);
                ph.setNombre(nombreSplit[0]);
                ph.setApellido(nombreSplit.length > 1 ? nombreSplit[1] : "");
                ph.setNroDocumento(dto.getDocumento().replaceAll("[^0-9]", ""));
            } else if (personaActual instanceof PersonaJuridica pj) {
                pj.setRazonSocial(dto.getNombre());
                pj.setTipo(detectarTipo(dto.getNombre()));
            }

            // Actualizar teléfono: reemplazar la lista existente
            personaActual.getMedioDeContacto().put("telefono", List.of(dto.getTelefono()));
        }

        System.out.println("El donante " + dto.getEmail() + " ya se encontraba registrado. Actualizado correctamente.");
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
