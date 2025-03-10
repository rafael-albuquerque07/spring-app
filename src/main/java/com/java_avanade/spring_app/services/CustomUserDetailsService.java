package com.java_avanade.spring_app.services;

import com.java_avanade.spring_app.models.Affiliate;
import com.java_avanade.spring_app.models.Client;
import com.java_avanade.spring_app.models.User;
import com.java_avanade.spring_app.repositories.AffiliateRepository;
import com.java_avanade.spring_app.repositories.ClientRepository;
import com.java_avanade.spring_app.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AffiliateRepository affiliateRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Buscando usuário pelo username: {}", username);

        // Primeiro verifica se é o admin (User)
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            logger.debug("Usuário admin encontrado: {} com ID {}", user.getUsername(), user.getId());
            return user; // User já implementa UserDetails
        }

        // Verifica se é um cliente
        Optional<Client> clientOptional = clientRepository.findByUsername(username);
        if (clientOptional.isPresent()) {
            Client client = clientOptional.get();
            logger.debug("Cliente encontrado: {} com ID {}", client.getUsername(), client.getId());

            // Criar um UserDetails para o cliente
            return new org.springframework.security.core.userdetails.User(
                    client.getUsername(),
                    client.getPassword(),
                    client.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList())
            );
        }

        // Verifica se é um afiliado
        Optional<Affiliate> affiliateOptional = affiliateRepository.findByUsername(username);
        if (affiliateOptional.isPresent()) {
            Affiliate affiliate = affiliateOptional.get();
            logger.debug("Afiliado encontrado: {} com ID {}", affiliate.getUsername(), affiliate.getId());

            // Criar um UserDetails para o afiliado
            return new org.springframework.security.core.userdetails.User(
                    affiliate.getUsername(),
                    affiliate.getPassword(),
                    affiliate.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList())
            );
        }

        logger.warn("Usuário não encontrado com o username: {}", username);
        throw new UsernameNotFoundException("Usuário não encontrado com username: " + username);
    }

    // Método adicional para obter o ID real da entidade pelo username
    public Long getRealUserIdByUsername(String username) {
        // Verifica nos admins (User)
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            return userOptional.get().getId();
        }

        // Verifica nos clientes
        Optional<Client> clientOptional = clientRepository.findByUsername(username);
        if (clientOptional.isPresent()) {
            return clientOptional.get().getId();
        }

        // Verifica nos afiliados
        Optional<Affiliate> affiliateOptional = affiliateRepository.findByUsername(username);
        if (affiliateOptional.isPresent()) {
            return affiliateOptional.get().getId();
        }

        throw new UsernameNotFoundException("Usuário não encontrado com username: " + username);
    }

    // Método para obter o tipo de usuário pelo username
    public String getUserTypeByUsername(String username) {
        // Verifica nos admins (User)
        if (userRepository.existsByUsername(username)) {
            return "ADMIN";
        }

        // Verifica nos clientes
        if (clientRepository.existsByUsername(username)) {
            return "CLIENT";
        }

        // Verifica nos afiliados
        if (affiliateRepository.existsByUsername(username)) {
            return "AFFILIATE";
        }

        throw new UsernameNotFoundException("Usuário não encontrado com username: " + username);
    }
}