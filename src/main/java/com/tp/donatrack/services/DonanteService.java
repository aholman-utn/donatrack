package com.tp.donatrack.services;

import com.tp.donatrack.domain.importador.ImportadorCargaMasiva;
import com.tp.donatrack.domain.lectoresDeArchivos.iLectorArchivo;
import com.tp.donatrack.dtos.ImportacionResponseDTO;
import com.tp.donatrack.domain.donante.Donante;
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

@Service
public class DonanteService {

    private DonanteRepository donanteRepository;
    private NotificacionService notifService;

    @Autowired
    private ImportadorCargaMasiva importadorCargaMasiva;

    @Autowired
    private List<iLectorArchivo> lectoresDeArchivos;

    public DonanteService(DonanteRepository donanteRepository, NotificacionService notifService, List<iLectorArchivo> lectores) {
        this.donanteRepository = donanteRepository;
        this.notifService = notifService;
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
                Notificacion notif_bienvenida = new Notificacion();
                notif_bienvenida.setTipo(TipoNotificacion.BIENVENIDA);
                String email = donante.getPersona().getMedioDeContacto().get("email").getFirst();
                String password = donante.getPassword();
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
            Notificacion notif_bienvenida = new Notificacion();
            notif_bienvenida.setTipo(TipoNotificacion.BIENVENIDA);
            String email = donante.getPersona().getMedioDeContacto().get("email").getFirst();
            String password = nuevo_donante.getPassword();
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

}