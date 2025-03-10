package com.java_avanade.spring_app.config;

import com.java_avanade.spring_app.config.jwt.JwtAuthenticationEntryPoint;
import com.java_avanade.spring_app.config.jwt.JwtAuthenticationFilter;
import com.java_avanade.spring_app.services.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService customUserDetailsService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomUserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customUserDetailsService = customUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        logger.info("SecurityConfig inicializado com filtro JWT, entry point e CustomUserDetailsService");
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder);

        logger.debug("AuthenticationManager configurado com CustomUserDetailsService");
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.debug("Configurando SecurityFilterChain");

        // Desabilitar CSRF
        http.csrf(AbstractHttpConfigurer::disable);
        logger.debug("CSRF desabilitado");

        // Configurar CORS
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
        logger.debug("CORS configurado");

        // Configurar autorização de requisições
        http.authorizeHttpRequests(auth -> {
            // Permitir acesso público à página inicial e recursos estáticos
            auth.requestMatchers("/", "/static/**", "/css/**", "/js/**", "/images/**", "/error").permitAll();
            // Permitir acesso à documentação e endpoints de autenticação
            auth.requestMatchers("/auth/**", "/v3/api-docs/**", "/swagger-ui/**",
                    "/swagger-ui.html", "/swagger", "/api-docs", "/h2-console/**").permitAll();
            // Todos os outros endpoints requerem autenticação
            auth.anyRequest().authenticated();
        });
        logger.debug("Autorização de requisições configurada");

        // Configurar tratamento de exceções
        http.exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint));
        logger.debug("Tratamento de exceções configurado");

        // Configurar gerenciamento de sessão
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        logger.debug("Gerenciamento de sessão configurado como STATELESS");

        // Configuração para permitir acesso ao console H2
        http.headers(headers -> headers.frameOptions(frameOption -> frameOption.disable()));
        logger.debug("Frame options desabilitadas para permitir console H2");

        // Adicionar filtro JWT antes do filtro de autenticação padrão
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        logger.debug("Filtro JWT adicionado à cadeia de filtros");

        logger.info("SecurityFilterChain configurado com sucesso");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.debug("Configurando CORS");

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Auth-Token"));
        configuration.setExposedHeaders(Arrays.asList("X-Auth-Token"));

        logger.debug("CORS configurado para permitir todas as origens e métodos HTTP comuns");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}