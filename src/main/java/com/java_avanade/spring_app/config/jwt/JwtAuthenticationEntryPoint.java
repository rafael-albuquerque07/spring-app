package com.java_avanade.spring_app.config.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        logger.error("Acesso não autorizado: {} para URI: {}", authException.getMessage(), request.getRequestURI());

        // Log detalhado de todos os cabeçalhos para debug
        Optional.ofNullable(request.getHeaderNames())
                .ifPresent(headerNames -> {
                    logger.debug("Headers da requisição:");
                    Collections.list(headerNames).forEach(headerName ->
                            logger.debug("{}={}", headerName, request.getHeader(headerName))
                    );
                });

        // Log do Authorization header especificamente
        Optional.ofNullable(request.getHeader("Authorization"))
                .ifPresentOrElse(
                        authHeader -> {
                            if (authHeader.startsWith("Bearer ")) {
                                logger.debug("Authorization header presente e começa com Bearer");
                                if (authHeader.length() > 10) {
                                    // Mostrar apenas os primeiros caracteres do token para segurança
                                    logger.debug("Token: {}...", authHeader.substring(7, 15));
                                } else {
                                    logger.debug("Token muito curto ou malformado");
                                }
                            } else {
                                logger.debug("Authorization header não começa com Bearer: {}", authHeader);
                            }
                        },
                        () -> logger.debug("Authorization header ausente")
                );

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", HttpStatus.UNAUTHORIZED.value());
        errorDetails.put("error", "Não autorizado");
        errorDetails.put("message", Optional.ofNullable(authException.getMessage())
                .orElse("Acesso não autorizado. Faça login para continuar."));
        errorDetails.put("path", request.getRequestURI());

        new ObjectMapper().writeValue(response.getOutputStream(), errorDetails);
    }
}