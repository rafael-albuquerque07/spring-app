package com.java_avanade.spring_app.config.jwt;

import com.java_avanade.spring_app.services.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI();
        final String authHeader = request.getHeader("Authorization");

        logger.debug("Processando requisição para: {}", requestURI);
        logger.debug("Authorization header: {}", authHeader);

        // Extrair token do header
        Optional.ofNullable(authHeader)
                .filter(header -> header.startsWith("Bearer "))
                .map(header -> header.substring(7))
                .ifPresent(jwt -> {
                    logger.debug("Token JWT encontrado no header");

                    try {
                        String username = jwtTokenProvider.extractUsername(jwt);
                        logger.debug("Username extraído do token: {}", username);

                        // Validar token e configurar autenticação
                        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                            logger.debug("Token contém username e contexto de segurança está vazio");

                            UserDetails userDetails = this.customUserDetailsService.loadUserByUsername(username);
                            logger.debug("UserDetails carregado para username: {}", username);

                            if (jwtTokenProvider.validateToken(jwt, userDetails)) {
                                logger.debug("Token JWT válido para usuário: {}", username);

                                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());

                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authentication);

                                logger.debug("Autenticação configurada no SecurityContextHolder");
                            } else {
                                logger.warn("Token JWT inválido para usuário: {}", username);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Erro ao processar token JWT: {}", e.getMessage());
                    }
                });

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("Nenhum token JWT encontrado no header");
        }

        filterChain.doFilter(request, response);
    }
}