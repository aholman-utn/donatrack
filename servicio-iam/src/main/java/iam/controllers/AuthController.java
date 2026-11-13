package iam.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import iam.domain.User;
import iam.enums.AuthProvider;
import iam.enums.Role;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;
import java.util.Locale;
import iam.repositories.UserRepository;
import iam.services.JwtService;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Optional;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import org.springframework.dao.DataIntegrityViolationException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(AuthController.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${google.client.id}")
    private String googleClientId;

    private final RestClient donaciones;

    @Value("${donaciones.base-url:http://localhost:8080}")
    private String donacionesBaseUrl;

    public AuthController(
            JwtService jwtService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(3000);
        this.donaciones = RestClient.builder().requestFactory(factory).build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(request.email() == null ? "" : request.email().trim());

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (user.getProvider() == AuthProvider.GOOGLE) {
                return ResponseEntity.status(401).body("Esta cuenta utiliza Google para ingresar.");
            }

            if (request.password() != null && user.getPassword() != null && passwordEncoder.matches(request.password(), user.getPassword())
                    && (user.getRole() != Role.DONANTE || user.getDonanteId() != null)
                    && (user.getRole() != Role.ENTIDAD_BENEFICIARIA || user.getEntidadBeneficiariaId() != null)) {
                String token = jwtService.generarToken(user.getEmail(), user.getRole(), user.getDonanteId(), user.getEntidadBeneficiariaId());

                UserDTO userDTO = new UserDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getName(),
                        user.getPicture()
                );

                return ResponseEntity.ok(new AuthResponse(token, userDTO));
            }
        }

        return ResponseEntity.status(401).body("Credenciales incorrectas");
    }

    @PostMapping("/registro/donante")
    public ResponseEntity<?> registroDonante(@RequestBody RegistroDonanteLocal request) {
        if (request == null || request.tipo() == null || request.datos() == null) return ResponseEntity.badRequest().build();
        String email = correo(request.datos());
        if (email == null) return ResponseEntity.badRequest().body("Ingresá un correo válido.");
        return registrarLocal(email, request.password(), Role.DONANTE, request.tipo(), request.datos());
    }

    @PostMapping("/registro/entidad")
    public ResponseEntity<?> registroEntidad(@RequestBody RegistroEntidadLocal request) {
        if (request == null || request.datos() == null) return ResponseEntity.badRequest().build();
        String email = correo(request.datos());
        if (email == null) return ResponseEntity.badRequest().body("Ingresá un correo válido.");
        return registrarLocal(email, request.password(), Role.ENTIDAD_BENEFICIARIA, null, request.datos());
    }

    private String correo(JsonNode datos) {
        JsonNode contactos = datos.path("mediosDeContacto");
        if (!contactos.isArray()) return null;
        for (JsonNode contacto : contactos) {
            if ("EMAIL".equals(contacto.path("medio").asText())) {
                String email = contacto.path("valor").asText().trim().toLowerCase(Locale.ROOT);
                if (email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) return email;
            }
        }
        return null;
    }

    private synchronized ResponseEntity<?> registrarLocal(String email, String password, Role role, String tipo, JsonNode datos) {
        if (password == null || password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            return ResponseEntity.badRequest().body("La contraseña debe tener al menos 8 caracteres y hasta 72 bytes.");
        }
        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (user != null && (user.getRole() != role || user.getProvider() != AuthProvider.LOCAL ||
                !passwordEncoder.matches(password, user.getPassword()))) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya tiene una cuenta.");
        }
        if (user != null && (user.getDonanteId() != null || user.getEntidadBeneficiariaId() != null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya tiene una cuenta.");
        }
        if (role == Role.ENTIDAD_BENEFICIARIA) return registrarEntidadLocal(email, password, datos, user);
        if (user == null) {
            try {
                donaciones.get().uri(donacionesBaseUrl + "/donantes/identidad")
                        .headers(h -> h.setBearerAuth(jwtService.tokenConsultaDonante(email))).retrieve().body(Long.class);
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe un perfil con ese correo.");
            } catch (HttpClientErrorException.NotFound ignored) { }
        }
        if (user == null) {
            try {
                user = userRepository.saveAndFlush(User.builder().email(email).password(passwordEncoder.encode(password))
                        .role(role).provider(AuthProvider.LOCAL).build());
            } catch (DataIntegrityViolationException duplicate) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya tiene una cuenta.");
            }
        }
        try {
            Long perfilId;
            try {
                perfilId = donaciones.get().uri(donacionesBaseUrl + "/donantes/identidad")
                        .headers(h -> h.setBearerAuth(jwtService.tokenConsultaDonante(email))).retrieve().body(Long.class);
            } catch (HttpClientErrorException.NotFound ignored) {
                perfilId = null;
            }
            if (perfilId == null) perfilId = donaciones.post().uri(donacionesBaseUrl + "/donantes/registro-publico")
                    .headers(h -> h.setBearerAuth(jwtService.tokenRegistroDonante(email)))
                    .body(Map.of("tipo", tipo, "datos", datos)).retrieve().body(Long.class);
            user.setDonanteId(perfilId);
            if (perfilId == null) return ResponseEntity.status(503).body("No se pudo completar el registro.");
            userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("email", email));
        } catch (HttpClientErrorException error) {
            return ResponseEntity.status(error.getStatusCode()).body(error.getResponseBodyAsString());
        } catch (Exception error) {
            return ResponseEntity.status(503).body("No se pudo completar el registro. Intentá nuevamente.");
        }
    }

    private ResponseEntity<?> registrarEntidadLocal(String email, String password, JsonNode datos, User user) {
        String etapa = "consultar identidad en Donaciones";
        try {
            Long entidadId;
            try {
                entidadId = donaciones.get().uri(donacionesBaseUrl + "/entidadBeneficiaria/identidad")
                        .headers(h -> h.setBearerAuth(jwtService.tokenRegistroEntidad(email))).retrieve().body(Long.class);
            } catch (HttpClientErrorException.NotFound ignored) {
                entidadId = null;
            }
            if (user == null && entidadId != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe una entidad con ese correo.");
            }
            if (user == null) {
                etapa = "guardar cuenta pendiente en IAM";
                try {
                    user = userRepository.saveAndFlush(User.builder().email(email).password(passwordEncoder.encode(password))
                            .role(Role.ENTIDAD_BENEFICIARIA).provider(AuthProvider.LOCAL).build());
                } catch (DataIntegrityViolationException duplicate) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body("El correo ya tiene una cuenta.");
                }
            }
            if (entidadId == null) {
                etapa = "crear entidad en Donaciones";
                entidadId = donaciones.post().uri(donacionesBaseUrl + "/entidadBeneficiaria/registro-publico")
                        .headers(h -> h.setBearerAuth(jwtService.tokenRegistroEntidad(email)))
                        .body(datos).retrieve().body(Long.class);
            }
            if (entidadId == null) throw new IllegalStateException("Donaciones no devolvió el ID de la entidad");
            etapa = "vincular entidad en IAM";
            user.setEntidadBeneficiariaId(entidadId);
            userRepository.saveAndFlush(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("email", email));
        } catch (HttpClientErrorException error) {
            logger.warn("Alta de entidad: etapa '{}', Donaciones respondió {}", etapa, error.getStatusCode().value());
            String motivo = error.getResponseBodyAsString();
            if (motivo.isBlank() && error.getStatusCode().value() == 409) {
                motivo = "Ya existe una entidad con ese correo o CUIT. Pedí a administración que revise el acceso.";
            }
            return ResponseEntity.status(error.getStatusCode()).body(motivo);
        } catch (RuntimeException error) {
            logger.warn("Alta de entidad: falló la etapa '{}' ({})", etapa, error.getClass().getSimpleName());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("No se pudo completar el registro. Reintentá con el mismo correo y contraseña.");
        }
    }

    @PostMapping("/social/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) {
        String idTokenString = request.token();

        if (idTokenString == null || idTokenString.isEmpty()) {
            return ResponseEntity.badRequest().body("Token de Google no proporcionado");
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null || !Boolean.TRUE.equals(idToken.getPayload().getEmailVerified())) {
                return ResponseEntity.status(401).body("Google no pudo verificar el correo.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail().trim().toLowerCase(Locale.ROOT);
            User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
            if (user != null && user.getProvider() == AuthProvider.LOCAL &&
                    (user.getRole() == Role.DONANTE || user.getRole() == Role.ENTIDAD_BENEFICIARIA)) {
                return ResponseEntity.status(403).body("Ingresá con correo y contraseña.");
            }
            Long donanteId = null;

            if (user == null || user.getRole() == Role.DONANTE) {
                try {
                    donanteId = donaciones.get().uri(donacionesBaseUrl + "/donantes/identidad")
                            .headers(headers -> headers.setBearerAuth(jwtService.tokenConsultaDonante(email)))
                            .retrieve().body(Long.class);
                } catch (HttpClientErrorException.NotFound e) {
                    return ResponseEntity.status(403).body("Tu correo todavía no está registrado en DonaTrack.");
                }
                if (donanteId == null) {
                    return ResponseEntity.status(503).body("No se pudo validar el registro del donante.");
                }
            }

            if (user == null) {
                user = User.builder().email(email).role(Role.DONANTE).provider(AuthProvider.GOOGLE).build();
            }

            if (user.getId() == null || user.getName() == null || user.getPicture() == null) {
                if (user.getName() == null) user.setName((String) payload.get("name"));
                if (user.getPicture() == null) user.setPicture((String) payload.get("picture"));
                user = userRepository.save(user);
            }

            String token = jwtService.generarToken(user.getEmail(), user.getRole(), donanteId, user.getEntidadBeneficiariaId());
            return ResponseEntity.ok(new AuthResponse(token,
                    new UserDTO(user.getId(), user.getEmail(), user.getRole().name(), user.getName(), user.getPicture())));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("No se pudo completar el ingreso con Google.");
        }
    }

    @PostMapping("/social/google/registro-entidad")
    public ResponseEntity<?> registroEntidadGoogle(@RequestBody GoogleBeneficiaryRegistrationRequest request) {
        if (request == null || request.token() == null || request.datos() == null) return ResponseEntity.badRequest().build();
        try {
            var verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId)).build();
            GoogleIdToken token = verifier.verify(request.token());
            if (token == null || !Boolean.TRUE.equals(token.getPayload().getEmailVerified())) {
                return ResponseEntity.status(401).body("Google no pudo verificar el correo.");
            }
            String email = token.getPayload().getEmail().trim().toLowerCase(Locale.ROOT);
            if (!email.equals(correo(request.datos()))) return ResponseEntity.badRequest().body("El correo institucional debe coincidir con Google.");
            if (userRepository.findByEmailIgnoreCase(email).isPresent()) return ResponseEntity.status(409).body("El correo ya tiene una cuenta.");
            Long entidadId = donaciones.post().uri(donacionesBaseUrl + "/entidadBeneficiaria/registro-publico")
                    .headers(h -> h.setBearerAuth(jwtService.tokenRegistroEntidad(email)))
                    .body(request.datos()).retrieve().body(Long.class);
            if (entidadId == null) return ResponseEntity.status(503).body("No se pudo completar el registro.");
            User user = userRepository.save(User.builder().email(email).role(Role.ENTIDAD_BENEFICIARIA)
                    .provider(AuthProvider.GOOGLE).entidadBeneficiariaId(entidadId).build());
            return ResponseEntity.status(201).body(new AuthResponse(
                    jwtService.generarToken(email, user.getRole(), null, entidadId),
                    new UserDTO(user.getId(), email, user.getRole().name(), user.getName(), user.getPicture())));
        } catch (HttpClientErrorException error) {
            return ResponseEntity.status(error.getStatusCode()).body(error.getResponseBodyAsString());
        } catch (Exception error) {
            return ResponseEntity.status(503).body("No se pudo completar el registro.");
        }
    }

    @PostMapping("/social/google/registro-donante")
    public ResponseEntity<?> registrarDonanteConGoogle(@RequestBody GoogleDonorRegistrationRequest request) {
        if (request.token() == null || request.token().isBlank() || request.tipo() == null || request.datos() == null) {
            return ResponseEntity.badRequest().body("Completá los datos del registro y continuá con Google.");
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            GoogleIdToken idToken = verifier.verify(request.token());
            if (idToken == null || !Boolean.TRUE.equals(idToken.getPayload().getEmailVerified())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Google no pudo verificar el correo.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail().trim().toLowerCase(Locale.ROOT);
            User user = userRepository.findByEmailIgnoreCase(email).orElse(null);
            if (user != null && user.getRole() != Role.DONANTE) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Este correo ya pertenece a otra cuenta de DonaTrack. Iniciá sesión.");
            }

            Long donanteId;
            try {
                donanteId = donaciones.post()
                        .uri(donacionesBaseUrl + "/donantes/registro-publico")
                        .headers(headers -> headers.setBearerAuth(jwtService.tokenRegistroDonante(email)))
                        .body(Map.of("tipo", request.tipo(), "datos", request.datos()))
                        .retrieve()
                        .body(Long.class);
            } catch (HttpClientErrorException error) {
                return ResponseEntity.status(error.getStatusCode()).body(error.getResponseBodyAsString());
            }
            if (donanteId == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body("No se pudo confirmar el registro del donante.");
            }

            if (user == null) {
                user = User.builder().email(email).role(Role.DONANTE).provider(AuthProvider.GOOGLE).build();
            }
            if (user.getName() == null) user.setName((String) payload.get("name"));
            if (user.getPicture() == null) user.setPicture((String) payload.get("picture"));
            user = userRepository.save(user);

            String token = jwtService.generarToken(user.getEmail(), user.getRole(), donanteId, user.getEntidadBeneficiariaId());
            return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token,
                    new UserDTO(user.getId(), user.getEmail(), user.getRole().name(), user.getName(), user.getPicture())));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("No se pudo completar el registro con Google.");
        }
    }
}

record RegistroDonanteLocal(String tipo, JsonNode datos, String password) {}
record RegistroEntidadLocal(JsonNode datos, String password) {}
record LoginRequest(String email, String password) {}

record GoogleLoginRequest(String token) {}

record GoogleDonorRegistrationRequest(String token, String tipo, JsonNode datos) {}
record GoogleBeneficiaryRegistrationRequest(String token, JsonNode datos) {}

record UserDTO(Long id, String email, String role, String name, String picture) {}

record AuthResponse(String token, UserDTO user) {}
