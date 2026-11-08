package iam.config;

import iam.domain.User;
import iam.enums.AuthProvider;
import iam.enums.Role;
import iam.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByEmail("admin@donatrack.com").isEmpty()) {
            User admin = User.builder()
                    .email("admin@donatrack.com")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.ADMIN)
                    .provider(AuthProvider.LOCAL)
                    .build();

            userRepository.save(admin);
        }
    }
}