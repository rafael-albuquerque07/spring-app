package com.java_avanade.spring_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class SpringAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringAppApplication.class, args);
	}

	// Dados iniciais para testes poderiam ser adicionados com um CommandLineRunner
    /*
    @Bean
    public CommandLineRunner dataLoader(UserRepository userRepository,
                                       AffiliateRepository affiliateRepository,
                                       ClientRepository clientRepository,
                                       ProductRepository productRepository,
                                       StockRepository stockRepository,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            // Código para popular o banco de dados com dados iniciais
        };
    }
    */
}