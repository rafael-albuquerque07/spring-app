package com.java_avanade.spring_app.services;

import com.java_avanade.spring_app.config.jwt.JwtTokenProvider;
import com.java_avanade.spring_app.dtos.AuthDTO;
import com.java_avanade.spring_app.exceptions.ResourceAlreadyExistsException;
import com.java_avanade.spring_app.exceptions.ResourceNotFoundException;
import com.java_avanade.spring_app.models.Affiliate;
import com.java_avanade.spring_app.models.Client;
import com.java_avanade.spring_app.models.User;
import com.java_avanade.spring_app.repositories.AffiliateRepository;
import com.java_avanade.spring_app.repositories.ClientRepository;
import com.java_avanade.spring_app.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AffiliateRepository affiliateRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    public AuthDTO.JwtResponse authenticateUser(AuthDTO.LoginRequest loginRequest) {
        logger.info("Tentativa de autenticação para o usuário: {}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtTokenProvider.generateToken(userDetails);

            // Obter informações adicionais do usuário
            Long userId = customUserDetailsService.getRealUserIdByUsername(loginRequest.getUsername());
            String userType = customUserDetailsService.getUserTypeByUsername(loginRequest.getUsername());
            String email = "";

            // Obter email com base no tipo de usuário
            if ("ADMIN".equals(userType)) {
                User user = userRepository.findByUsername(loginRequest.getUsername())
                        .orElseThrow(() -> new ResourceNotFoundException("Admin", "username", loginRequest.getUsername()));
                email = user.getEmail();
            } else if ("CLIENT".equals(userType)) {
                Client client = clientRepository.findByUsername(loginRequest.getUsername())
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente", "username", loginRequest.getUsername()));
                email = client.getEmail();
            } else if ("AFFILIATE".equals(userType)) {
                Affiliate affiliate = affiliateRepository.findByUsername(loginRequest.getUsername())
                        .orElseThrow(() -> new ResourceNotFoundException("Afiliado", "username", loginRequest.getUsername()));
                email = affiliate.getEmail();
            }

            logger.info("Usuário autenticado com sucesso: {}", loginRequest.getUsername());

            return new AuthDTO.JwtResponse(
                    jwt,
                    userId,
                    loginRequest.getUsername(),
                    email,
                    userType
            );
        } catch (Exception e) {
            logger.error("Falha na autenticação para o usuário: {}", loginRequest.getUsername(), e);
            throw e;
        }
    }

    @Transactional
    public AuthDTO.JwtResponse registerUser(AuthDTO.RegisterRequest registerRequest) {
        logger.info("Iniciando registro de novo usuário: {}", registerRequest.getUsername());

        // Verificar se o username já existe em qualquer entidade
        if (userRepository.existsByUsername(registerRequest.getUsername()) ||
                clientRepository.existsByUsername(registerRequest.getUsername()) ||
                affiliateRepository.existsByUsername(registerRequest.getUsername())) {

            logger.warn("Tentativa de registro com username já existente: {}", registerRequest.getUsername());
            throw new ResourceAlreadyExistsException("Usuário", "username", registerRequest.getUsername());
        }

        // Verificar se o email já existe em qualquer entidade
        if (userRepository.existsByEmail(registerRequest.getEmail()) ||
                clientRepository.existsByEmail(registerRequest.getEmail()) ||
                affiliateRepository.existsByEmail(registerRequest.getEmail())) {

            logger.warn("Tentativa de registro com email já existente: {}", registerRequest.getEmail());
            throw new ResourceAlreadyExistsException("Usuário", "email", registerRequest.getEmail());
        }

        try {
            // Obter o admin principal (ID 1)
            User admin = userRepository.findById(1L)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin principal não encontrado"));

            String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());

            // Determinar o tipo de usuário a ser criado
            if ("AFFILIATE".equalsIgnoreCase(registerRequest.getUserType())) {
                // Criar afiliado diretamente
                Affiliate affiliate = new Affiliate();
                affiliate.setName(registerRequest.getName());
                affiliate.setEmail(registerRequest.getEmail());
                affiliate.setUsername(registerRequest.getUsername());
                affiliate.setPassword(encodedPassword);
                affiliate.setAdminId(admin.getId()); // Referência explícita ao admin

                Set<String> roles = new HashSet<>();
                roles.add("AFFILIATE");
                affiliate.setRoles(roles);

                Affiliate savedAffiliate = affiliateRepository.save(affiliate);
                logger.debug("Afiliado criado: {} com ID {}", savedAffiliate.getUsername(), savedAffiliate.getId());

                // Autenticar o afiliado recém-registrado
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(registerRequest.getUsername(), registerRequest.getPassword())
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String jwt = jwtTokenProvider.generateToken(userDetails);

                logger.info("Afiliado registrado e autenticado com sucesso: {}", affiliate.getUsername());

                return new AuthDTO.JwtResponse(
                        jwt,
                        affiliate.getId(),
                        affiliate.getUsername(),
                        affiliate.getEmail(),
                        "AFFILIATE"
                );
            } else {
                // Por padrão, criar cliente
                Client client = new Client();
                client.setName(registerRequest.getName());
                client.setEmail(registerRequest.getEmail());
                client.setUsername(registerRequest.getUsername());
                client.setPassword(encodedPassword);
                client.setAdminId(admin.getId()); // Referência explícita ao admin

                Set<String> roles = new HashSet<>();
                roles.add("CLIENT");
                client.setRoles(roles);

                Client savedClient = clientRepository.save(client);
                logger.debug("Cliente criado: {} com ID {}", savedClient.getUsername(), savedClient.getId());

                // Autenticar o cliente recém-registrado
                Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(registerRequest.getUsername(), registerRequest.getPassword())
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String jwt = jwtTokenProvider.generateToken(userDetails);

                logger.info("Cliente registrado e autenticado com sucesso: {}", client.getUsername());

                return new AuthDTO.JwtResponse(
                        jwt,
                        client.getId(),
                        client.getUsername(),
                        client.getEmail(),
                        "CLIENT"
                );
            }
        } catch (Exception e) {
            logger.error("Erro ao registrar novo usuário: {}", registerRequest.getUsername(), e);
            throw e;
        }
    }
}