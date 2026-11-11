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
        return firmar(email, role.name(), donanteId, null, 86_400_000);
    }

    public String generarToken(String email, Role role, Long donanteId, Long entidadId) {
        return firmar(email, role.name(), donanteId, entidadId, 86_400_000);
    }

    public String tokenConsultaDonante(String email) {
        return firmar(email, "DONANTE_LOOKUP", null, null, 30_000);
    }

    public String tokenRegistroDonante(String email) {
        return firmar(email, "DONANTE_REGISTRATION", null, null, 30_000);
    }

    public String tokenRegistroEntidad(String email) {
        return firmar(email, "ENTIDAD_REGISTRATION", null, null, 30_000);
    }

    private String firmar(String email, String scope, Long donanteId, Long entidadId, long duracion) {
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
        if (entidadId != null) builder.claim("entidad_beneficiaria_id", entidadId.toString());
        return builder.signWith(key).compact();
    }
}
