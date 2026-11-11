package com.tp.donatrack.controllers;

import com.tp.donatrack.dtos.CrearPersonaJuridicaRequest;
import com.tp.donatrack.repositories.EntidadBeneficiariaRepository;
import com.tp.donatrack.services.EntidadBeneficiariaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/entidadBeneficiaria")
public class RegistroPublicoEntidadController {
    private final EntidadBeneficiariaService service;
    private final EntidadBeneficiariaRepository repository;

    public RegistroPublicoEntidadController(EntidadBeneficiariaService service, EntidadBeneficiariaRepository repository) {
        this.service = service;
        this.repository = repository;
    }

    @GetMapping("/identidad")
    public Long identidad(@AuthenticationPrincipal Jwt jwt) {
        var vinculada = repository.findByEmailAccesoIgnoreCase(jwt.getSubject());
        if (vinculada.isPresent()) return vinculada.get().getIdEntidadBeneficiaria();
        var existentes = repository.findByCorreoContacto(jwt.getSubject());
        if (!existentes.isEmpty()) throw new ResponseStatusException(HttpStatus.CONFLICT);
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/registro-publico")
    public Long registrar(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CrearPersonaJuridicaRequest request) {
        String email = jwt.getSubject();
        if (request.getCuit() == null || request.getCuit().isBlank() || request.getMediosDeContacto() == null ||
                request.getMediosDeContacto().stream().noneMatch(c -> c.getMedio() != null &&
                        c.getMedio().name().equals("EMAIL") && email.equalsIgnoreCase(c.getValor()))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Revisá el CUIT y el correo institucional.");
        }
        if (repository.existsByDatosDeEntidadCuit(request.getCuit())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una entidad con ese CUIT.");
        }
        if (repository.findByEmailAccesoIgnoreCase(email).isPresent() || !repository.findByCorreoContacto(email).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una entidad con ese correo.");
        }
        var persona = request.toDomain();
        persona.setId(null);
        var entidad = service.registrar(persona);
        entidad.setEmailAcceso(email);
        return repository.save(entidad).getIdEntidadBeneficiaria();
    }
}
