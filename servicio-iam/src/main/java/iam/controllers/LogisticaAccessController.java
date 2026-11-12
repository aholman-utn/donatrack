package iam.controllers;

import iam.domain.User;
import iam.enums.AuthProvider;
import iam.enums.Role;
import iam.repositories.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@RestController
public class LogisticaAccessController {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public LogisticaAccessController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping("/api/users/administradores-logistica")
    public List<Cuenta> cuentas() {
        return users.findByRole(Role.ADMIN_LOGISTICA).stream()
                .map(user -> new Cuenta(user.getEmail(), user.getPassword() != null))
                .sorted(java.util.Comparator.comparing(Cuenta::email))
                .toList();
    }

    @PostMapping("/api/users/administradores-logistica")
    @ResponseStatus(HttpStatus.CREATED)
    public Cuenta autorizar(@RequestBody Alta request) {
        String email = emailValido(request.email());
        if (users.findByEmailIgnoreCase(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado o autorizado.");
        }
        try {
            users.saveAndFlush(User.builder().email(email).role(Role.ADMIN_LOGISTICA)
                    .provider(AuthProvider.LOCAL).build());
        } catch (DataIntegrityViolationException duplicate) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado o autorizado.");
        }
        return new Cuenta(email, false);
    }

    @PostMapping("/api/auth/registro/logistica")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void activar(@RequestBody Activacion request) {
        String email = emailValido(request.email());
        String password = request.password();
        if (password == null || password.length() < 8 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usá al menos 8 caracteres y hasta 72 bytes.");
        }
        if (users.activar(email, Role.ADMIN_LOGISTICA, encoder.encode(password)) != 1) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El correo no está habilitado para activar una cuenta.");
        }
    }

    private String emailValido(String valor) {
        String email = valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
        if (email.length() > 254 || !email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ingresá un correo válido.");
        }
        return email;
    }

    @ExceptionHandler(ResponseStatusException.class)
    public org.springframework.http.ProblemDetail error(ResponseStatusException error) {
        return error.getBody();
    }

    public record Alta(String email) {}
    public record Activacion(String email, String password) {}
    public record Cuenta(String email, boolean activa) {}
}
