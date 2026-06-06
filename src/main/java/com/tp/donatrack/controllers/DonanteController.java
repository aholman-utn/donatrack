package com.tp.donatrack.controllers;

import com.tp.donatrack.dtos.ImportacionResponseDTO;
import com.tp.donatrack.dtos.CrearDonanteHumanoRequest;
import com.tp.donatrack.dtos.CrearDonanteJuridicoRequest;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.routes.DonanteRoutes;
import com.tp.donatrack.services.DonanteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/donantes")
public class DonanteController {

    private final DonanteService donanteService;

    public DonanteController(DonanteService donanteService){
        this.donanteService = donanteService;
    }

    // GET /donantes → listar todos
    @GetMapping
    public ResponseEntity<List<Donante>> listarTodos() { return  ResponseEntity.ok(donanteService.listarTodos());}

    // GET /donantes/{email} → busca un donante por email
    //     http://localhost:8080/donantes/pepe@mail.com
   @GetMapping(DonanteRoutes.POR_EMAIL)
    public ResponseEntity<Donante> buscarDonante(@PathVariable String email) {
        Donante donante = donanteService.buscarDonante(email);
        if (donante == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(donante);
    }

    // POST http://localhost:8080/donantes/humano → crear donante humano
    /* En el BODY -> raw
    {
      "nombre": "pepe",
      "apellido": "pangaro",
      "nroDocumento": 12345678,
      "mediosDeContacto": [
        { "medio": "EMAIL", "valor": "pepe@mail.com" }
      ],
      "medioPredeterminado": {
        "medio": "EMAIL",
        "valor": "pepe@mail.com"
      }
    }
    */
    @PostMapping(DonanteRoutes.HUMANO)
    public ResponseEntity<Donante> crearHumano(
            @Valid @RequestBody CrearDonanteHumanoRequest request) {
        Donante creado = donanteService.registrar(request.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // POST /donantes/juridico → crear donante jurídico
    @PostMapping(DonanteRoutes.JURIDICO)
    public ResponseEntity<Donante> crearJuridico(
            @Valid @RequestBody CrearDonanteJuridicoRequest request) {

        Donante creado = donanteService.registrar(request.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    //POST /donantes/importar → importa un csv
    @PostMapping("/importar")
    public ResponseEntity<ImportacionResponseDTO> importarCSV(@RequestParam("file") MultipartFile csv) {

        ImportacionResponseDTO response = donanteService.importarCSV(csv);

        return ResponseEntity.ok(response);
    }


    // PUT /donantes/{email}/humano → actualizar donante humano
    @PutMapping(DonanteRoutes.ACTUALIZAR_HUMANO)
    public ResponseEntity<Donante> actualizarHumano(
            @PathVariable String email,
            @Valid @RequestBody CrearDonanteHumanoRequest request) {

        Donante actualizado = donanteService.actualizar(email, request.toDomain());
        return ResponseEntity.ok(actualizado);
    }

    // PUT /donantes/{email}/juridico → actualizar donante jurídico
    @PutMapping(DonanteRoutes.ACTUALIZAR_JURIDICO)
    public ResponseEntity<Donante> actualizarJuridico(
            @PathVariable String email,
            @Valid @RequestBody CrearDonanteJuridicoRequest request) {

        Donante actualizado = donanteService.actualizar(email, request.toDomain());
        return ResponseEntity.ok(actualizado);
    }

    // DELETE /donantes/{email} → elimina el donante con ese mail
    @DeleteMapping(DonanteRoutes.POR_EMAIL)
    public ResponseEntity<Donante> eliminar(@PathVariable String email) {
        donanteService.eliminar(email);
        return ResponseEntity.noContent().build();
    }

}