package iam.controllers;

import iam.domain.User;
import iam.enums.AuthProvider;
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

    public AuthController(
            JwtService jwtService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
                String token = jwtService.generarToken(user.getEmail());

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

            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();

                Optional<User> userOpt = userRepository.findByEmail(email);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    String googleName = (String) payload.get("name");
                    String googlePicture = (String) payload.get("picture");

                    boolean updated = false;

                    if (user.getName() == null && googleName != null) {
                        user.setName(googleName);
                        updated = true;
                    }

                    if (user.getPicture() == null && googlePicture != null) {
                        user.setPicture(googlePicture);
                        updated = true;
                    }

                    if (updated) {
                        userRepository.save(user);
                    }

                    String donatrackToken = jwtService.generarToken(user.getEmail());

                    UserDTO userDTO = new UserDTO(
                            user.getId(),
                            user.getEmail(),
                            user.getRole().name(),
                            user.getName(),
                            user.getPicture()
                    );

                    return ResponseEntity.ok(new AuthResponse(donatrackToken, userDTO));
                } else {
                    return ResponseEntity.status(403).body("Usuario no autorizado. Contacte al administrador.");
                }
            } else {
                return ResponseEntity.status(401).body("Token de Google inválido");
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error validando token con Google: " + e.getMessage());
        }
    }
}

record LoginRequest(String email, String password) {}

record GoogleLoginRequest(String token) {}

record UserDTO(Long id, String email, String role, String name, String picture) {}

record AuthResponse(String token, UserDTO user) {}