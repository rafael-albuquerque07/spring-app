package com.java_avanade.spring_app.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.java_avanade.spring_app.services.CustomUserDetailsService;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final Key key;

    @Value("${jwt.expiration:10800000}")
    private long jwtExpirationInMs;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    public JwtTokenProvider(@Value("${jwt.secret:}") String jwtSecret) {
        logger.debug("Inicializando JwtTokenProvider com expiração de {} ms", jwtExpirationInMs);

        // Cria uma chave segura a partir da string do segredo
        this.key = Optional.ofNullable(jwtSecret)
                .filter(secret -> !secret.isEmpty())
                .map(secret -> {
                    logger.debug("Chave JWT criada a partir do segredo configurado");
                    return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
                })
                .orElseGet(() -> {
                    logger.warn("Nenhum segredo JWT configurado, gerando chave aleatória");
                    return Keys.secretKeyFor(SignatureAlgorithm.HS256);
                });
    }

    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return generateToken(userDetails);
    }

    public String generateToken(UserDetails userDetails) {
        logger.debug("Gerando token para o usuário: {}", userDetails.getUsername());

        // Obter informações adicionais para o token
        String username = userDetails.getUsername();
        Long userId = customUserDetailsService.getRealUserIdByUsername(username);
        String userType = customUserDetailsService.getUserTypeByUsername(username);

        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);
        claims.put("type", userType);

        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        logger.debug("Criando token para subject: {} com expiração: {}", subject, expiryDate);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            String username = extractClaim(token, Claims::getSubject);
            logger.debug("Username extraído do token: {}", username);
            return username;
        } catch (Exception e) {
            logger.error("Erro ao extrair username do token: {}", e.getMessage());
            throw e;
        }
    }

    public Long extractUserId(String token) {
        try {
            Long userId = extractClaim(token, claims -> claims.get("id", Long.class));
            logger.debug("UserId extraído do token: {}", userId);
            return userId;
        } catch (Exception e) {
            logger.error("Erro ao extrair userId do token: {}", e.getMessage());
            throw e;
        }
    }

    public String extractUserType(String token) {
        try {
            String userType = extractClaim(token, claims -> claims.get("type", String.class));
            logger.debug("UserType extraído do token: {}", userType);
            return userType;
        } catch (Exception e) {
            logger.error("Erro ao extrair userType do token: {}", e.getMessage());
            throw e;
        }
    }

    public Date extractExpiration(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            logger.debug("Data de expiração extraída do token: {}", expiration);
            return expiration;
        } catch (Exception e) {
            logger.error("Erro ao extrair data de expiração do token: {}", e.getMessage());
            throw e;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            logger.debug("Claims extraídos com sucesso do token");
            return claims;
        } catch (Exception e) {
            // Usar pattern matching para identificar o tipo específico de exceção
            String errorMessage = switch (e) {
                case ExpiredJwtException ex -> "Token JWT expirado: " + ex.getMessage();
                case UnsupportedJwtException ex -> "Token JWT não suportado: " + ex.getMessage();
                case MalformedJwtException ex -> "Token JWT malformado: " + ex.getMessage();
                case SignatureException ex -> "Assinatura do token JWT inválida: " + ex.getMessage();
                default -> "Erro ao extrair claims do token: " + e.getMessage();
            };

            logger.error(errorMessage);
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            boolean isExpired = extractExpiration(token).before(new Date());
            logger.debug("Verificação de expiração do token: {}", isExpired ? "expirado" : "válido");
            return isExpired;
        } catch (Exception e) {
            logger.error("Erro ao verificar expiração do token: {}", e.getMessage());
            return true; // Se houver um erro, considera o token como expirado
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);

            if (isValid) {
                logger.debug("Token JWT válido para o usuário: {}", username);
            } else {
                if (!username.equals(userDetails.getUsername())) {
                    logger.warn("Token JWT inválido: username do token ({}) não corresponde ao userDetails ({})",
                            username, userDetails.getUsername());
                } else {
                    logger.warn("Token JWT inválido: token expirado");
                }
            }

            return isValid;
        } catch (Exception e) {
            logger.error("Erro ao validar token JWT: {}", e.getMessage());
            return false;
        }
    }
}