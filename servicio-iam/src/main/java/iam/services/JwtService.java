package iam.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import iam.enums.Role;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    public String generarToken(String email, Role role, Long donanteId) {
        return firmar(email, role.name(), donanteId, 86_400_000);
    }

    public String tokenConsultaDonante(String email) {
        return firmar(email, "DONANTE_LOOKUP", null, 30_000);
    }

    public String tokenRegistroDonante(String email) {
        return firmar(email, "DONANTE_REGISTRATION", null, 30_000);
    }

    private String firmar(String email, String scope, Long donanteId, long duracion) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        Date ahora = new Date();
        var builder = Jwts.builder()
                .subject(email)
                .claim("scope", scope)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + duracion));
        if (donanteId != null) {
            builder.claim("donante_id", donanteId.toString());
        }
        return builder.signWith(key).compact();
    }
}
