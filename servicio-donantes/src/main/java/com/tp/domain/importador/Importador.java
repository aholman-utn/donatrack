package com.tp.domain.importador;

import com.tp.commons.domain.notificador.TipoNotificador;
import com.tp.commons.services.notificador.NotificacionRestClient;
import com.tp.domain.donante.Donante;
import com.tp.domain.donante.persona.*;
import com.tp.dtos.input.ActualizarDonanteInputDTO;
import com.tp.dtos.input.Registro;
import com.tp.repositories.DonanteRepository;
import com.tp.utils.CryptoUtils;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Importador {

    private final DonanteRepository donanteRepository;
    private final NotificacionRestClient notificador;
    public Importador(DonanteRepository repo, NotificacionRestClient notificador){
        this.donanteRepository=repo;
        this.notificador=notificador;
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
                //notifico
                notificador.notificar(TipoNotificador.EMAIL,registro.getEmail(),"Bienvenido a Donatrack. Usuario: " + registro.getEmail() + ", password: " + password, "Cuenta creada en Donatrack", nuevo_donante.getPersona().getId());
            } else {
                // Si ya EXISTE, aplicamos la lógica de negocio de actualización en memoria
                actualizarCamposDonante(registro, existe);
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
            persona.setTipoDoc(TipoDoc.DNI);
        } else {
            PersonaJuridica pj = new PersonaJuridica();
            pj.setRazonSocial(registro.getNombre());
            pj.setTipo(detectarTipo(registro.getNombre()));
            pj.setRubro("Sin especificar");
            pj.setTipoDoc(TipoDoc.CUIT);
            persona = pj;
        }

        Map<String,String> medioPredeterminado = new HashMap<>();
        medioPredeterminado.put("email", registro.getEmail());
        persona.agregarMedioDeContacto("email", registro.getEmail());
        persona.agregarMedioDeContacto("telefono", registro.getTelefono());
        persona.setMedioPredeterminado(medioPredeterminado);
        persona.registrarInteraccion();

        donante.setPersona(persona);

        return donante;
    }

    private void actualizarCamposDonante(Registro registro, Donante donante_existente) {
        System.out.println("El donante " + registro.getNombre() + " ya se encuentra registrado. Acutalizando...");
        String[] nombreSplit = registro.getNombre().split(" ", 2);
        String nombrePersona = nombreSplit[0];
        String apellido = nombreSplit.length > 1 ? nombreSplit[1] : "";

        donante_existente.setEmail(registro.getEmail());
        donante_existente.getPersona().setTipoDoc(TipoDoc.valueOf(registro.getTipoDoc()));
        donante_existente.getPersona().agregarMedioDeContacto("telefono",registro.getTelefono());

        if(donante_existente.getPersona() instanceof PersonaHumana){
            ((PersonaHumana) donante_existente.getPersona()).setNombre(nombrePersona);
            ((PersonaHumana) donante_existente.getPersona()).setApellido(apellido);
        } else {
            ((PersonaJuridica) donante_existente.getPersona()).setRazonSocial(registro.getNombre());
            ((PersonaJuridica) donante_existente.getPersona()).setCuit(registro.getDocumento());
        }
        this.donanteRepository.update(donante_existente.getId(),donante_existente);
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
