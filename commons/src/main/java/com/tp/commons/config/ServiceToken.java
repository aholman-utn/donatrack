package com.tp.commons.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Component
public class ServiceToken {
    private final NimbusJwtEncoder encoder;

    public ServiceToken(@Value("${jwt.secret}") String secret) {
        encoder = new NimbusJwtEncoder(new ImmutableSecret<>(secret.getBytes(StandardCharsets.UTF_8)));
    }

    public String emitir(String scope) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder().subject(scope).claim("scope", scope)
                .issuedAt(ahora).expiresAt(ahora.plusSeconds(60)).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
