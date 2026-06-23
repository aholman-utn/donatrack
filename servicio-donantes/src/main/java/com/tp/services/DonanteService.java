package com.tp.services;

import com.tp.domain.donante.Donante;
import com.tp.domain.donante.Perfil;
import com.tp.domain.donante.persona.*;
import com.tp.domain.importador.Importador;
import com.tp.domain.lectoresDeArchivos.iLectorArchivo;
import com.tp.dtos.input.ActualizarDonanteInputDTO;
import com.tp.dtos.input.CrearDonanteInputDTO;
import com.tp.dtos.input.DonanteFiltroDTO;
import com.tp.dtos.input.Registro;
import com.tp.dtos.output.DonanteInactivoDTO;
import com.tp.dtos.output.DonanteOutputDTO;
import com.tp.dtos.output.ImportacionOutputDTO;
import com.tp.repositories.DonanteRepository;
import com.tp.utils.CryptoUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DonanteService {
    private final DonanteRepository donanteRepository;
    private final Importador importador;
    private final List<iLectorArchivo> lectores;

    public DonanteService(DonanteRepository repo, List<iLectorArchivo> lectores, Importador importador) {
        this.donanteRepository = repo;
        this.lectores=lectores;
        this.importador = importador;
    }

    public ImportacionOutputDTO importar(MultipartFile archivo){

        String nombreArchivo = archivo.getOriginalFilename();
        String extension = "";

        // 2. Validar que no sea nulo y que contenga un punto
        if (nombreArchivo != null && nombreArchivo.contains(".")) {
            extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
        }

        iLectorArchivo lector_correspondiente = this.seleccionar_lector(extension);
        ImportacionOutputDTO resultado = new ImportacionOutputDTO();
        if(lector_correspondiente==null){
            System.out.println("Error al importar los donantes: No existe lector para archivos con extension " + extension);
            resultado.setSuccess(false);
            resultado.setMessage("No existe lector para archivos con extension " + extension);
        } else {
            List<Registro> registros = lector_correspondiente.leerArchivo(archivo);
            List<Donante> donantes_importados = importador.iniciar_migracion(registros);
            System.out.println("Donantes importados correctamente");

            resultado.setSuccess(true);
            resultado.setMessage("Donantes importados correctamente");
        }
        return resultado;
    }

    private iLectorArchivo seleccionar_lector(String extension){
        return this.lectores.stream().filter(lector -> lector.soportaExtension(extension)).findFirst().orElse(null);
    }

    public List<DonanteInactivoDTO> obtenerDonantesSinInteraccionMasDeDias(int dias) {
        LocalDateTime fechaLimite = LocalDateTime.now().minusDays(dias);

        return donanteRepository.findAll(null).stream()
                .filter(donante -> donante.getPersona() != null &&
                        donante.getPersona().getMedioPredeterminado() != null &&
                        !donante.getPersona().getMedioPredeterminado().isEmpty() &&
                        donante.getPersona().getFechaUltimaInteraccion() != null &&
                        donante.getPersona().getFechaUltimaInteraccion().isBefore(fechaLimite))
                .map(donante -> {
                    DonanteInactivoDTO dto = new DonanteInactivoDTO();
                    dto.setId(donante.getPersona().getId());

                    Map<String, String> medioMap = donante.getPersona().getMedioPredeterminado();

                    if (medioMap.containsKey("medio")) {
                        dto.setContacto(medioMap.get("valor"));
                        try {
                            dto.setTipoNotificadorPreferido(TipoNotificador.valueOf(medioMap.get("medio").toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            System.err.println("Advertencia: El medio '" + medioMap.get("medio") + "' no coincide con ningún TipoNotificador.");
                        }
                    } else {
                        Map.Entry<String, String> entry = medioMap.entrySet().iterator().next();
                        dto.setContacto(entry.getValue());
                        try {
                            dto.setTipoNotificadorPreferido(TipoNotificador.valueOf(entry.getKey().toUpperCase()));
                        } catch (IllegalArgumentException e) {
                            System.err.println("Advertencia: El medio '" + entry.getKey() + "' no coincide con ningún TipoNotificador.");
                        }
                    }

                    return dto;
                })
                .filter(dto -> dto.getTipoNotificadorPreferido() != null)
                .collect(Collectors.toList());
    }

    public List<DonanteOutputDTO> findAll(DonanteFiltroDTO filtro){
        List<Donante> donantes = this.donanteRepository.findAll(filtro);
        return donantes.stream().map(this::donanteToOutput).toList();
    }

    public DonanteOutputDTO find(Long id){
        Donante donante = this.donanteRepository.findById(id);
        return donanteToOutput(donante);
    }

    public DonanteOutputDTO create(CrearDonanteInputDTO dto){
        TipoPersona tipoPersona = dto.getTipoPersona();
        Persona persona = crearPersonaSegunTipo(dto);
        Perfil perfil = crearPerfil();
        Donante nuevo_donante = new Donante();
        nuevo_donante.setPersona(persona);
        nuevo_donante.setPerfil(perfil);
        nuevo_donante.setFechaUltimaInteraccion(LocalDateTime.now());
        String password_hasheado = CryptoUtils.hashear(dto.getPassword());
        nuevo_donante.setPassword(password_hasheado);
        Donante response = this.donanteRepository.create(nuevo_donante);
        return donanteToOutput(response);
    }

    public DonanteOutputDTO update(ActualizarDonanteInputDTO dto){
        return null;
    }

    public DonanteOutputDTO delete(Long id){
        Donante eliminado = this.donanteRepository.delete(id);
        DonanteOutputDTO output = new DonanteOutputDTO();
        output.setId(eliminado.getId());
        output.setPersona(eliminado.getPersona());
        output.setPerfil(eliminado.getPerfil());
        output.setFechaUltimaInteraccion(eliminado.getFechaUltimaInteraccion());
        return output;
    }

    public DonanteOutputDTO upsert(DonanteFiltroDTO dto){
        return null;
    }

    //------------------------------ FUNCIONES UTILITARIAS --------------------------------------
    //esta funcion es para mapear la salida, porque no quiero exponer TODOS los atributos (por ejemplo el password)
    private DonanteOutputDTO donanteToOutput(Donante donante){
        DonanteOutputDTO output = new DonanteOutputDTO();
        output.setId(donante.getId());
        output.setPerfil(donante.getPerfil());
        output.setFechaUltimaInteraccion(donante.getFechaUltimaInteraccion());
        output.setPersona(donante.getPersona());
        return output;
    }

    private Persona crearPersonaSegunTipo(CrearDonanteInputDTO dto){
        Persona persona;
        if(dto.getTipoPersona().equals(TipoPersona.HUMANA)){
            persona= new PersonaHumana(
                    dto.getNombre(),
                    dto.getGenero(),
                    dto.getApellido(),
                    dto.getFechaNacimiento(),
                    null,
                    dto.getNroDocumento());
        } else {
            PersonaJuridica personaJuridica = new PersonaJuridica();
            personaJuridica.setRazonSocial(dto.getRazonSocial());
            personaJuridica.setTipo(dto.getTipoOrganizacion());
            personaJuridica.setCuit(dto.getCuit());
            persona = personaJuridica;
        }
        Map<String, String> medioPredeterminado = new HashMap<>();
        Map<String, List<String>> mediosDeContacto = new HashMap<>();
        List<String> emails = new ArrayList<>();
        emails.add(dto.getEmail());
        mediosDeContacto.put("email", emails);
        medioPredeterminado.put("email", dto.getEmail());
        persona.setMedioPredeterminado(medioPredeterminado);
        persona.setMedioDeContacto(mediosDeContacto);
        return persona;
    }

    private Perfil crearPerfil(){
        Perfil perfil = new Perfil();
        //TODO: falta setearle los atributos, deberia inicializar en la ultima posicion del ranking, pero me la tendria que pasar el servicio de incentivos?
        return perfil;
    }

    public Donante buscarDonante(String email) {
        return this.donanteRepository.findByEmail(email);
    }

}
