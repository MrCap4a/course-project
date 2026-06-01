package ru.denis.Calculator.Control;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.denis.Calculator.Dto.TokenResponse;
import ru.denis.Calculator.Dto.Request.LoginRequest;
import ru.denis.Calculator.Dto.Request.RefreshRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtDecoder jwtDecoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          JwtDecoder jwtDecoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtDecoder = jwtDecoder;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.login(), request.password())
        );
        String login = request.login();
        return ResponseEntity.ok(TokenResponse.bearer(
                jwtService.generateToken(login),
                jwtService.generateRefreshToken(login)
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request) {
        try {
            Jwt jwt = jwtDecoder.decode(request.refreshToken());
            if (!"refresh".equals(jwt.getClaim("type"))) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String login = jwt.getSubject();
            return ResponseEntity.ok(TokenResponse.bearer(
                    jwtService.generateToken(login),
                    jwtService.generateRefreshToken(login)
            ));
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}
