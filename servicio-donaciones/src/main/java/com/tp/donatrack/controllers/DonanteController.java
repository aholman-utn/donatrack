package com.tp.donatrack.controllers;

import com.tp.commons.services.notificador.NotificacionRestClient;
import com.tp.donatrack.domain.persona.Persona;
import com.tp.donatrack.dtos.ImportacionResponseDTO;
import com.tp.donatrack.dtos.CrearPersonaHumanaRequest;
import com.tp.donatrack.dtos.CrearPersonaJuridicaRequest;
import com.tp.donatrack.dtos.*;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.routes.DonanteRoutes;
import com.tp.donatrack.services.DonanteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

import java.util.List;

@RestController
@RequestMapping(DonanteRoutes.BASE)
public class DonanteController {

    private final DonanteService donanteService;
    private final NotificacionRestClient notificacionRestClient;

    public DonanteController(
            DonanteService donanteService,
            NotificacionRestClient notificacionRestClient
    ) {
        this.donanteService = donanteService;
        this.notificacionRestClient = notificacionRestClient;
    }

    // GET /donantes → listar todos
    @GetMapping
    public ResponseEntity<List<Donante>> listarTodos() {
        return ResponseEntity.ok(donanteService.listarTodos());
    }

    // GET /donantes/{email} → busca un donante por email
    // http://localhost:8080/donantes/pepe@mail.com
    @GetMapping(DonanteRoutes.POR_EMAIL)
    public ResponseEntity<Donante> buscarDonante(@PathVariable String email) {
        Donante donante = donanteService.buscarDonante(email);
        if (donante == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(donante);
    }

    // POST http://localhost:8080/donantes/humano → crear donante humano
    /*
     * En el BODY -> raw
     * {
     * "nombre": "pepe",
     * "apellido": "pangaro",
     * "nroDocumento": 12345678,
     * "mediosDeContacto": [
     * { "medio": "EMAIL", "valor": "pepe@mail.com" }
     * ],
     * "medioPredeterminado": {
     * "medio": "EMAIL",
     * "valor": "pepe@mail.com"
     * }
     * }
     */
    @PostMapping(DonanteRoutes.HUMANO)
    public ResponseEntity<Donante> crearHumano(
            @Valid @RequestBody CrearPersonaHumanaRequest request) {
        Donante donante = donanteService.registrar(request.toDomain());
        Persona persona = donante.getPersona();
        boolean enviada = notificacionRestClient.notificar(
                persona.getTipoNotificadorPreferido(),
                persona.getContactoPredeterminado(),
                "Gracias por registrarte como donante.",
                "¡Bienvenido a Donatrack!",
                persona.getId()
        );
        if (!enviada) {
            System.err.println("La notificación de bienvenida no pudo enviarse.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(donante);
    }

    // POST /donantes/juridico → crear donante jurídico
    @PostMapping(DonanteRoutes.JURIDICO)
    public ResponseEntity<Donante> crearJuridico(
            @Valid @RequestBody CrearPersonaJuridicaRequest request) {

        Donante donante = donanteService.registrar(request.toDomain());
        Persona persona = donante.getPersona();
        System.err.println("Persona: "+persona.getId());
        boolean enviada = notificacionRestClient.notificar(
                persona.getTipoNotificadorPreferido(),
                persona.getContactoPredeterminado(),
                "Gracias por registrarte como donante.",
                "¡Bienvenido a Donatrack!",
                persona.getId()
        );
        if (!enviada) {
            System.err.println("La notificación de bienvenida no pudo enviarse.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(donante);
    }

    // POST /donantes/importar → importa un csv
    @PostMapping("/importar")
    public ResponseEntity<ImportacionResponseDTO> importarCSV(@RequestParam("file") MultipartFile csv) {

        ImportacionResponseDTO response = donanteService.importarDonantes(csv);

        return ResponseEntity.ok(response);
    }

    // PUT /donantes/{email}/humano → actualizar donante humano
    @PutMapping(DonanteRoutes.ACTUALIZAR_HUMANO)
    public ResponseEntity<Donante> actualizarHumano(
            @PathVariable String email,
            @Valid @RequestBody CrearPersonaHumanaRequest request) {

        Donante actualizado = donanteService.actualizar(email, request.toDomain());
        return ResponseEntity.ok(actualizado);
    }

    // PUT /donantes/{email}/juridico → actualizar donante jurídico
    @PutMapping(DonanteRoutes.ACTUALIZAR_JURIDICO)
    public ResponseEntity<Donante> actualizarJuridico(
            @PathVariable String email,
            @Valid @RequestBody CrearPersonaJuridicaRequest request) {

        Donante actualizado = donanteService.actualizar(email, request.toDomain());
        return ResponseEntity.ok(actualizado);
    }

    // DELETE /donantes/{email} → elimina el donante con ese mail
    @DeleteMapping(DonanteRoutes.POR_EMAIL)
    public ResponseEntity<Donante> eliminar(@PathVariable String email) {
        donanteService.eliminar(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/simular-inactividad")
    public void simularInactividad(@PathVariable Long id) {
        Donante donante = donanteService.buscarDonantePorId(id);
        
        if (donante != null) {
            donante.getPersona().setFechaUltimaInteraccion(LocalDateTime.now().minusDays(32));
            System.out.println("Fecha seteada para: " + donante.getPersona().getId());
        } else {
            throw new RuntimeException("Donante no encontrado");
        }
    }

    // GET /donantes/perfil/{donanteId}
    @GetMapping("/perfil/{donanteId}")
    public ResponseEntity<com.tp.donatrack.dtos.PerfilDonanteDTO> obtenerPerfil(@PathVariable Long donanteId) {
        return ResponseEntity.ok(donanteService.obtenerPerfilDonante(donanteId));
    }

    // GET /donantes/metricas/{donanteId}
    @GetMapping("/metricas/{donanteId}")
    public ResponseEntity<com.tp.donatrack.dtos.MetricasActividadDTO> obtenerMetricas(@PathVariable Long donanteId) {
        return ResponseEntity.ok(donanteService.obtenerMetricas(donanteId));
    }
}