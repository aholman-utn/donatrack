package com.tp.donatrack.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.dtos.CrearPersonaHumanaRequest;
import com.tp.donatrack.dtos.CrearPersonaJuridicaRequest;
import com.tp.donatrack.dtos.ContactoValorRequest;
import com.tp.donatrack.services.DonanteService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping("/donantes")
public class RegistroPublicoDonanteController {
    private final DonanteService donantes;
    private final ObjectMapper mapper;
    private final Validator validator;

    public RegistroPublicoDonanteController(DonanteService donantes, ObjectMapper mapper, Validator validator) {
        this.donantes = donantes;
        this.mapper = mapper;
        this.validator = validator;
    }

    @PostMapping("/registro-publico")
    public Long registrar(@AuthenticationPrincipal Jwt jwt, @RequestBody SolicitudRegistro request) {
        if (request == null || request.tipo() == null || request.datos() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan datos para registrar el donante.");
        }

        String tipo = request.tipo().trim().toLowerCase(Locale.ROOT);
        Donante donante;
        try {
            if ("humano".equals(tipo)) {
                CrearPersonaHumanaRequest datos = convertir(request.datos(), CrearPersonaHumanaRequest.class);
                if (datos.getEdad() < 1 || datos.getEdad() > 120 || datos.getNroDocumento() == null || datos.getNroDocumento().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Revisá la edad y el DNI ingresados.");
                }
                donante = donantes.registrarPublicoVerificado(datos.toDomain(), jwt.getSubject());
            } else if ("juridico".equals(tipo)) {
                CrearPersonaJuridicaRequest datos = convertir(request.datos(), CrearPersonaJuridicaRequest.class);
                if (datos.getCuit() == null || datos.getCuit().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El CUIT es obligatorio.");
                }
                donante = donantes.registrarPublicoVerificado(datos.toDomain(), jwt.getSubject());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El tipo de donante no es válido.");
            }
        } catch (IllegalArgumentException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos del registro no tienen un formato válido.");
        }
        return donante.getId();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> errorDeRegistro(ResponseStatusException error) {
        return ResponseEntity.status(error.getStatusCode()).body(error.getReason());
    }

    private <T> T convertir(JsonNode json, Class<T> tipo) {
        T datos = mapper.convertValue(json, tipo);
        Set<ConstraintViolation<T>> errores = validator.validate(datos);
        if (!errores.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errores.iterator().next().getMessage());
        }
        List<ContactoValorRequest> contactos = datos instanceof CrearPersonaHumanaRequest humano
                ? humano.getMediosDeContacto()
                : ((CrearPersonaJuridicaRequest) datos).getMediosDeContacto();
        boolean tieneEmail = contactos != null && contactos.stream()
                .anyMatch(contacto -> contacto.getMedio() != null && contacto.getMedio().name().equals("EMAIL")
                        && contacto.getValor() != null && !contacto.getValor().isBlank());
        if (!tieneEmail) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresá un correo de Google.");
        }
        return datos;
    }

    public record SolicitudRegistro(String tipo, JsonNode datos) {}
}
