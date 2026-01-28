// Summary: Erzeugt und prüft JWT für Lehrkraft-Login.
package security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final byte[] secret;
    private final String issuer;
    private final long ttlMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.ttlMinutes}") long ttlMinutes
    ) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.issuer = issuer;
        this.ttlMinutes = ttlMinutes;
    }

    public String issueToken(long teacherId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlMinutes * 60);

        return Jwts.builder()
                .issuer(issuer)
                .subject(Long.toString(teacherId))
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(Keys.hmacShaKeyFor(secret))
                .compact();
    }

    public long parseTeacherId(String token) {
        String sub = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        return Long.parseLong(sub);
    }
}