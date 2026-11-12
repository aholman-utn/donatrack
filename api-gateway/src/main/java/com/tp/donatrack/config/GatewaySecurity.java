package com.tp.donatrack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurity {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/api/auth/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/publico/**").permitAll()
                        .pathMatchers("/donantes/identidad", "/donantes/registro-publico").denyAll()
                        .pathMatchers(HttpMethod.GET, "/donantes/me/**", "/incentivos/misiones",
                                "/api/notificaciones/me").hasAuthority("SCOPE_DONANTE")
                        .pathMatchers(HttpMethod.PUT, "/donantes/me/insignias").hasAuthority("SCOPE_DONANTE")
                        .pathMatchers("/donantes/me/**").denyAll()
                        .pathMatchers(HttpMethod.GET, "/entidades/me/**").hasAuthority("SCOPE_ENTIDAD_BENEFICIARIA")
                        .pathMatchers(HttpMethod.POST, "/entidades/me/donaciones/*/segmentos/*/recepcion",
                                "/entidades/me/donaciones/*/segmentos/*/no-recibida").hasAuthority("SCOPE_ENTIDAD_BENEFICIARIA")
                        .pathMatchers("/entidades/me/**").denyAll()
                        .pathMatchers("/api/users/administradores-logistica").hasAuthority("SCOPE_ADMIN")
                        .pathMatchers("/api/logistica/panel/**").hasAuthority("SCOPE_ADMIN_LOGISTICA")
                        .anyExchange().access((authentication, context) -> authentication
                                .map(auth -> new org.springframework.security.authorization.AuthorizationDecision(
                                        auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)
                                                && auth.getAuthorities().stream().noneMatch(
                                                authority -> authority.getAuthority().equals("SCOPE_DONANTE")
                                                        || authority.getAuthority().equals("SCOPE_DONANTE_LOOKUP")
                                                        || authority.getAuthority().equals("SCOPE_DONANTE_REGISTRATION")
                                                        || authority.getAuthority().equals("SCOPE_DONACIONES_INCENTIVOS")
                                                        || authority.getAuthority().equals("SCOPE_INCENTIVOS_DONACIONES")
                                                        || authority.getAuthority().equals("SCOPE_ADMIN_LOGISTICA")
                                                        || authority.getAuthority().equals("SCOPE_LOGISTICA_DONACIONES"))))
                                .defaultIfEmpty(new org.springframework.security.authorization.AuthorizationDecision(false)))
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder())));

        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        SecretKeySpec secretKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
        return NimbusReactiveJwtDecoder.withSecretKey(secretKey).build();
    }
}
