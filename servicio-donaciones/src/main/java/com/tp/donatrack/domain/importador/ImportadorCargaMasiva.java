package com.tp.donatrack.domain.importador;

import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.persona.*;
import com.tp.donatrack.dtos.input.importacionCSV.RegistroDonanteDTO;
import com.tp.donatrack.repositories.DonanteRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.Map;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ImportadorCargaMasiva {
    DonanteRepository donanteRepository;

    public ImportadorCargaMasiva(DonanteRepository donanteRepo){
        this.donanteRepository=donanteRepo;
    }

    @Transactional
    public List<Donante> iniciar_migracion(List<RegistroDonanteDTO> registros) {
        List<Donante> nuevosDonantes = new ArrayList<>();

        for (RegistroDonanteDTO registro : registros) {

            String email = registro.getEmail().trim().toLowerCase(Locale.ROOT);
            if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
                throw new IllegalArgumentException("Correo inválido en el CSV.");
            }
            registro.setEmail(email);
            registro.setTipoPersona(registro.getTipoPersona().toUpperCase(Locale.ROOT));
            TipoPersona.valueOf(registro.getTipoPersona());
            if (registro.getNombre().isBlank() || registro.getDocumento().isBlank()) {
                throw new IllegalArgumentException("El nombre y el documento son obligatorios.");
            }
            List<Donante> coincidencias = donanteRepository.findByEmail(email);
            if (coincidencias.size() > 1) {
                throw new IllegalArgumentException("Hay registros duplicados para un correo del CSV.");
            }

            if (coincidencias.isEmpty()) {
                Donante nuevo = crearDonanteSegunTipoPersona(registro);
                nuevosDonantes.add(donanteRepository.create(nuevo));
            } else {
                actualizarCamposDonante(coincidencias.getFirst(), registro);
            }
        }

        return nuevosDonantes;
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
            pj.setCuit(registro.getDocumento());
            pj.setTipo(detectarTipo(registro.getNombre()));
            pj.setRubro("Sin especificar");
            persona = pj;
        }

        persona.agregarMedioDeContacto("EMAIL", registro.getEmail());
        if (!registro.getTelefono().isBlank()) {
            persona.agregarMedioDeContacto("SMS", registro.getTelefono());
        }
        persona.setMedioPredeterminado(Map.of("medio", "EMAIL", "valor", registro.getEmail()));
        persona.setFechaUltimaInteraccion(LocalDateTime.now());
        donante.setPersona(persona);

        return donante;
    }

    private void actualizarCamposDonante(Donante donante, RegistroDonanteDTO dto) {
        Persona personaActual = donante.getPersona();
        String tipoActual = personaActual instanceof PersonaHumana ? TipoPersona.HUMANA.name() : TipoPersona.JURIDICA.name();
        if (!tipoActual.equals(dto.getTipoPersona())) {
            throw new IllegalArgumentException("El tipo de persona debe coincidir con el donante registrado.");
        }
        if (personaActual instanceof PersonaHumana ph) {
            String[] nombre = dto.getNombre().split(" ", 2);
            ph.setNombre(nombre[0]);
            ph.setApellido(nombre.length > 1 ? nombre[1] : "");
            ph.setNroDocumento(dto.getDocumento().replaceAll("[^0-9]", ""));
        } else if (personaActual instanceof PersonaJuridica pj) {
            pj.setRazonSocial(dto.getNombre());
            pj.setCuit(dto.getDocumento());
            pj.setTipo(detectarTipo(dto.getNombre()));
        }
        if (!dto.getTelefono().isBlank()) {
            personaActual.reemplazarMedioDeContacto("SMS", dto.getTelefono());
            if (personaActual.getTipoNotificadorPreferido() == com.tp.commons.domain.notificador.TipoNotificador.SMS) {
                personaActual.setMedioPredeterminado(Map.of("medio", "SMS", "valor", dto.getTelefono()));
            }
        }
        donanteRepository.update(donante);
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
