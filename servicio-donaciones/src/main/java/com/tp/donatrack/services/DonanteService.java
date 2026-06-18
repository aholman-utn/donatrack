package com.tp.donatrack.services;

import com.tp.donatrack.domain.importador.ImportadorCargaMasiva;
import com.tp.donatrack.dtos.DonanteInactivoDTO;
import com.tp.donatrack.domain.lectoresDeArchivos.iLectorArchivo;
import com.tp.donatrack.dtos.ImportacionResponseDTO;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.bien.SubCategoria;
import com.tp.donatrack.domain.notificacion.Notificacion;
import com.tp.donatrack.domain.notificacion.TipoNotificacion;
import com.tp.donatrack.domain.notificador.TipoNotificador;
import com.tp.donatrack.domain.persona.*;
import com.tp.donatrack.dtos.input.importacionCSV.RegistroDonanteDTO;
import com.tp.donatrack.repositories.DonanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
public class DonanteService {

    private DonanteRepository donanteRepository;
    private NotificacionService notifService;
    private PersonaService personaService;

    @Autowired
    private ImportadorCargaMasiva importadorCargaMasiva;

    @Autowired
    private List<iLectorArchivo> lectoresDeArchivos;

    public DonanteService(
        DonanteRepository donanteRepository, 
        NotificacionService notifService, 
        PersonaService personaService,
        List<iLectorArchivo> lectores
    ) {
        this.donanteRepository = donanteRepository;
        this.notifService = notifService;
        this.personaService = personaService;
        this.lectoresDeArchivos=lectores;
    }

    //CREATE
    public Donante registrar(Persona persona) {
        Donante donante = new Donante(persona);
        return donanteRepository.create(donante);
    }

    //READ
    public List<Donante> listarTodos() {
        return donanteRepository.findAll();
    }

    //UPDATE TODO: aca actualizo la persona completa, el problema es que en un futuro esto me va a cambiar el ID
    public Donante actualizar(String email, Persona personaActualizada) {
        Donante donante = buscarDonante(email);
        donante.setPersona(personaActualizada);
        return donanteRepository.create(donante);
    }

    //DELETE
    public void eliminar(String email){
        Donante donanteAEliminar = buscarDonante(email);
        if ( donanteAEliminar != null ) {
            donanteRepository.delete(donanteAEliminar);
        }
    }

    public Donante buscarDonante(String email){
        return this.donanteRepository.find(email);
    }

    public Donante buscarDonantePorId(Integer id) {
        return this.donanteRepository.findById(id);
    }

    public ImportacionResponseDTO importarDonantes(MultipartFile archivo) {
        try {
            // 1. Extraer la extensión del archivo (ej: "csv" o "xlsx")
            String nombreArchivo = archivo.getOriginalFilename();
            String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1);

            // 2. BUSCAR EL LECTOR ADECUADO (Principio de Sustitución de Liskov)
            iLectorArchivo lectorAdecuado = lectoresDeArchivos.stream()
                    .filter(lector -> lector.soportaExtension(extension))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Formato de archivo no soportado: " + extension));

            // 3. Capa Técnica: Leemos usando el polimorfismo
            List<RegistroDonanteDTO> registros = lectorAdecuado.leerArchivo(archivo);

            // 4. Capa de Negocio: Procesamos las reglas de negocio sobre esos objetos
            List<Donante> nuevos_donantes = importadorCargaMasiva.iniciar_migracion(registros);

            for(Donante donante: nuevos_donantes){
                Notificacion notif_bienvenida = new Notificacion(
                    "¡Bienvenido a Donatrack!",
                    "Gracias por sumarte como donante...",
                    "Registro exitoso",
                    TipoNotificacion.BIENVENIDA
                );
                String email = donante.getPersona().getMedioDeContacto().get("email").getFirst();
                String password = donante.getPassword();
                this.notifService.notificar(notif_bienvenida, TipoNotificador.EMAIL, email);
            }
            // 5. Respuesta de la aplicación
            String mensaje = "Importados " + registros.size() + " registros exitosamente";
            ImportacionResponseDTO response = new ImportacionResponseDTO(true, mensaje);
            response.setData(this.donanteRepository.findAll());
            return response;

        } catch (Exception e) {
            return new ImportacionResponseDTO(false, "Error al importar: " + e.getMessage());
        }
    }

    private Donante darDeAlta(Donante donante){
        Donante nuevo_donante = this.donanteRepository.create(donante);
        if(nuevo_donante!= null) {
            //Notifico
            Notificacion notif_bienvenida = new Notificacion(
                "¡Bienvenido a Donatrack!",
                "Gracias por sumarte como donante...",
                "Registro exitoso",
                TipoNotificacion.BIENVENIDA
            );
            String email = donante.getPersona().getMedioDeContacto().get("email").getFirst();
            String password = nuevo_donante.getPassword();
            this.notifService.notificar(notif_bienvenida, TipoNotificador.EMAIL, email);
        }
        return nuevo_donante;
    }

    public void guardarNotificacionEnHistorial(Integer donanteId, Notificacion notificacion) {
        Donante donante = donanteRepository.findById(donanteId);
        
        if (donante != null && donante.getPersona() != null) {
            donante.getPersona().agregarNotificacion(notificacion);
        }
    }

    public List<DonanteInactivoDTO> obtenerDonantesSinInteraccionMasDeDias(int dias) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);

        return donanteRepository.findAll().stream()
                .filter(donante -> 
                    donante.getPersona() != null &&
                    donante.getPersona().getMedioPredeterminado() != null &&
                    !donante.getPersona().getMedioPredeterminado().isEmpty() &&
                    donante.getPersona().getFechaUltimaInteraccion() != null && 
                    donante.getPersona().getFechaUltimaInteraccion().isBefore(fechaLimite)
                )
                .map(donante -> {
                    DonanteInactivoDTO dto = new DonanteInactivoDTO();
                    dto.setId(donante.getId()); 
                    
                    Map.Entry<String, String> medio = donante.getPersona().getMedioPredeterminado().entrySet().iterator().next();
                    
                    dto.setContacto(medio.getValue());
                    
                    try {
                        dto.setTipoNotificadorPreferido(TipoNotificador.valueOf(medio.getKey().toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Advertencia: El medio '" + medio.getKey() + "' no coincide con ningún TipoNotificador.");
                    }
                    
                    return dto;
                })
                .filter(dto -> dto.getTipoNotificadorPreferido() != null)
                .collect(Collectors.toList());
    }

    public void notificarDonacionAsignada(Integer donanteId, SubCategoria subCategoria) {
        Donante donante = donanteRepository.findById(donanteId);
        
        if (donante != null) {
            com.tp.donatrack.domain.persona.Persona persona = donante.getPersona();
            
            if (persona != null && persona.getMedioPredeterminado() != null && !persona.getMedioPredeterminado().isEmpty()) {
                java.util.Map.Entry<String, String> medio = persona.getMedioPredeterminado().entrySet().iterator().next();
                com.tp.donatrack.domain.notificador.TipoNotificador tipoNotificador = com.tp.donatrack.domain.notificador.TipoNotificador.valueOf(medio.getKey().toUpperCase());
                String contacto = medio.getValue();

                Notificacion aviso = new Notificacion(
                    "¡Tu donación llegó a destino!",
                    "Queríamos avisarte que tu donación de la categoría '" + subCategoria + "' acaba de ser asignada a una entidad. ¡Muchas gracias por tu aporte!",
                    "Donación Asignada",
                    TipoNotificacion.ASIGNACION
                );

                notifService.notificar(aviso, tipoNotificador, contacto);
                personaService.guardarNotificacion(persona, aviso);
            }
        }
    }
}