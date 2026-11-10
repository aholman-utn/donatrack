package com.tp.commons.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.http.HttpMethod;
import static org.springframework.security.authorization.AuthorizationManagers.allOf;
import static org.springframework.security.authorization.AuthorizationManagers.not;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import static org.springframework.security.authorization.AuthorityAuthorizationManager.hasAnyAuthority;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class CommonsSecurityConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/donantes/identidad").hasAuthority("SCOPE_DONANTE_LOOKUP")
                        .requestMatchers(HttpMethod.GET, "/donantes/me/**", "/incentivos/misiones",
                                "/api/notificaciones/me").hasAuthority("SCOPE_DONANTE")
                        .requestMatchers(HttpMethod.PUT, "/donantes/me/insignias").hasAuthority("SCOPE_DONANTE")
                        .requestMatchers("/donantes/me/**").denyAll()
                        .requestMatchers(HttpMethod.POST, "/donantes/humano", "/donantes/juridico",
                                "/donantes/importar").hasAnyAuthority("SCOPE_ADMIN", "SCOPE_SUPERVISOR")
                        .requestMatchers("/api/users/supervisores").hasAuthority("SCOPE_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/incentivos/entrega")
                                .hasAnyAuthority("SCOPE_ADMIN", "SCOPE_SUPERVISOR", "SCOPE_DONACIONES_INCENTIVOS")
                        .requestMatchers(HttpMethod.GET, "/donantes", "/donantes/*/contacto-notificacion",
                                "/donantes/ids-mision-actual", "/donantes/*/fecha-ultima-donacion",
                                "/donaciones-segmentadas/indicadores/**")
                                .hasAnyAuthority("SCOPE_ADMIN", "SCOPE_SUPERVISOR", "SCOPE_INCENTIVOS_DONACIONES")
                        .requestMatchers(HttpMethod.PUT, "/donantes/*/resetear-progreso-racha")
                                .hasAnyAuthority("SCOPE_ADMIN", "SCOPE_SUPERVISOR", "SCOPE_INCENTIVOS_DONACIONES")
                        .anyRequest().access(allOf(AuthenticatedAuthorizationManager.authenticated(),
                                not(hasAnyAuthority("SCOPE_DONANTE", "SCOPE_DONANTE_LOOKUP",
                                        "SCOPE_DONACIONES_INCENTIVOS", "SCOPE_INCENTIVOS_DONACIONES"))))
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder())));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKeySpec secretKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}