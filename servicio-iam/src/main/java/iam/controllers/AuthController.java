package iam.controllers;

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

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

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
        Optional<User> userOpt = userRepository.findByEmail(request.email());

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (user.getProvider() == AuthProvider.GOOGLE) {
                return ResponseEntity.status(401).body("Esta cuenta utiliza Google para ingresar.");
            }

            if (passwordEncoder.matches(request.password(), user.getPassword())) {
                String token = jwtService.generarToken(user.getEmail(), user.getRole(), null);

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

            String token = jwtService.generarToken(user.getEmail(), user.getRole(), donanteId);
            return ResponseEntity.ok(new AuthResponse(token,
                    new UserDTO(user.getId(), user.getEmail(), user.getRole().name(), user.getName(), user.getPicture())));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("No se pudo completar el ingreso con Google.");
        }
    }
}

record LoginRequest(String email, String password) {}

record GoogleLoginRequest(String token) {}

record UserDTO(Long id, String email, String role, String name, String picture) {}

record AuthResponse(String token, UserDTO user) {}