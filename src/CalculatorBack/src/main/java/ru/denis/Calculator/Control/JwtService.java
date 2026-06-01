package ru.denis.Calculator.Control;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

@Service
public class JwtService {

    private final NimbusJwtEncoder encoder;
    private final long expiration;
    private final long refreshExpiration;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration}") long expiration,
            @Value("${security.jwt.refresh-expiration}") long refreshExpiration) {
        this.encoder = new NimbusJwtEncoder(
                new ImmutableSecret<>(secret.getBytes(StandardCharsets.UTF_8)));
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateToken(String login) {
        return buildToken(login, expiration, "access");
    }

    public String generateRefreshToken(String login) {
        return buildToken(login, refreshExpiration, "refresh");
    }

    private String buildToken(String login, long expirationSeconds, String type) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("SuperCalculator")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationSeconds))
                .subject(login)
                .claim("type", type)
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
    }
}
