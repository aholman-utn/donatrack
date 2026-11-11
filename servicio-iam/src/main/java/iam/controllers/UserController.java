package iam.controllers;

import iam.domain.User;
import iam.enums.AuthProvider;
import iam.enums.Role;
import iam.repositories.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/supervisores")
    public ResponseEntity<List<UserDTO>> getSupervisores() {
        List<User> supervisores = userRepository.findByRole(Role.SUPERVISOR);

        List<UserDTO> response = supervisores.stream()
                .map(u -> new UserDTO(u.getId(), u.getEmail(), u.getRole().name(), u.getName(), u.getPicture()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/supervisores")
    public ResponseEntity<?> createSupervisor(@RequestBody CreateSupervisorRequest request) {
        if (request.email() == null || request.email().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("El email es obligatorio");
        }

        Optional<User> existingUser = userRepository.findByEmail(request.email());
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body("El usuario ya se encuentra registrado");
        }

        User newSupervisor = User.builder()
                .email(request.email())
                .role(Role.SUPERVISOR)
                .provider(AuthProvider.GOOGLE)
                .build();

        userRepository.save(newSupervisor);

        UserDTO responseDTO = new UserDTO(
                newSupervisor.getId(),
                newSupervisor.getEmail(),
                newSupervisor.getRole().name(),
                newSupervisor.getName(),
                newSupervisor.getPicture()
        );

        return ResponseEntity.ok(responseDTO);
    }
}

record CreateSupervisorRequest(String email) {}