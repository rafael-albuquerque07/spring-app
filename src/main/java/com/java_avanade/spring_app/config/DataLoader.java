package com.java_avanade.spring_app.config;

import com.java_avanade.spring_app.models.Affiliate;
import com.java_avanade.spring_app.models.Client;
import com.java_avanade.spring_app.models.Product;
import com.java_avanade.spring_app.models.Stock;
import com.java_avanade.spring_app.models.User;
import com.java_avanade.spring_app.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Classe responsável por carregar dados iniciais no banco de dados
 * durante a inicialização da aplicação.
 */
@Configuration
public class DataLoader {

    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    @Profile({"dev", "prod"}) // Executa nos perfis de desenvolvimento e produção
    public CommandLineRunner loadData(
            UserRepository userRepository,
            ClientRepository clientRepository,
            AffiliateRepository affiliateRepository,
            ProductRepository productRepository,
            StockRepository stockRepository,
            PasswordEncoder passwordEncoder) {

        return new CommandLineRunner() {
            @Override
            @Transactional // Executa tudo em uma única transação
            public void run(String... args) throws Exception {
                // Carrega dados apenas se não houver usuários no banco
                if (userRepository.count() == 0) {
                    logger.info("Carregando dados iniciais...");

                    // Criando usuário administrador (primeiro para garantir ID 1)
                    User admin = createAdminUser(userRepository, passwordEncoder);

                    // Criando cliente com referência ao admin
                    Client client = createClientUser(clientRepository, passwordEncoder, admin);

                    // Criando afiliado com referência ao admin
                    Affiliate affiliate = createAffiliateUser(affiliateRepository, passwordEncoder, admin);

                    // Criando produtos para o afiliado
                    createProducts(productRepository, stockRepository, affiliate);

                    logger.info("Dados iniciais carregados com sucesso!");
                }
            }

            private User createAdminUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
                // Verificar se já existe um admin
                if (userRepository.existsByUsername("admin")) {
                    logger.info("Admin já existe, pulando criação");
                    return userRepository.findByUsername("admin").orElseThrow();
                }

                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@ecofraldas.com");
                admin.setEnabled(true);

                Set<String> roles = new HashSet<>();
                roles.add("ADMIN");
                roles.add("SUPERUSER"); // Papel adicional para indicar domínio total
                admin.setRoles(roles);

                User savedAdmin = userRepository.save(admin);
                logger.info("Usuário administrador criado: admin com ID {}", savedAdmin.getId());
                return savedAdmin;
            }

            private Client createClientUser(ClientRepository clientRepository,
                                            PasswordEncoder passwordEncoder,
                                            User admin) {
                // Criar Client com referência ao admin
                Client client = new Client();
                client.setName("Cliente Exemplo");
                client.setEmail("cliente@ecofraldas.com");
                client.setUsername("cliente");
                client.setPassword(passwordEncoder.encode("cliente123"));
                client.setAdminId(admin.getId()); // Referência explícita ao admin

                Set<String> roles = new HashSet<>();
                roles.add("CLIENT");
                client.setRoles(roles);

                Client savedClient = clientRepository.save(client);
                logger.info("Cliente criado: cliente com ID {} vinculado ao admin ID {}",
                        savedClient.getId(), admin.getId());
                return savedClient;
            }

            private Affiliate createAffiliateUser(AffiliateRepository affiliateRepository,
                                                  PasswordEncoder passwordEncoder,
                                                  User admin) {
                // Criar Affiliate com referência ao admin
                Affiliate affiliate = new Affiliate();
                affiliate.setName("Afiliado Exemplo");
                affiliate.setEmail("afiliado@ecofraldas.com");
                affiliate.setUsername("afiliado");
                affiliate.setPassword(passwordEncoder.encode("afiliado123"));
                affiliate.setAdminId(admin.getId()); // Referência explícita ao admin

                Set<String> roles = new HashSet<>();
                roles.add("AFFILIATE");
                affiliate.setRoles(roles);

                Affiliate savedAffiliate = affiliateRepository.save(affiliate);
                logger.info("Afiliado criado: afiliado com ID {} vinculado ao admin ID {}",
                        savedAffiliate.getId(), admin.getId());
                return savedAffiliate;
            }

            private void createProducts(ProductRepository productRepository,
                                        StockRepository stockRepository,
                                        Affiliate affiliate) {
                // Produto 1: Fralda Ecológica Infantil P
                Product product1 = new Product();
                product1.setName("Fralda Ecológica Infantil P");
                product1.setDescription("Fralda ecológica lavável e sustentável para bebês no tamanho P, com design ergonômico e absorção eficiente");
                product1.setPrice(new BigDecimal("89.90"));
                product1.setProductType("INFANTIL");
                product1.setProductChoice("TAMANHO_P");
                product1.setAffiliate(affiliate);
                product1.setImageUrl("https://example.com/fralda_p.jpg");

                Product savedProduct1 = productRepository.save(product1);

                // Adicionar estoque para o produto 1
                Stock stock1 = new Stock();
                stock1.setProduct(savedProduct1);
                stock1.setQuantity(50);
                stockRepository.save(stock1);

                // Produto 2: Fralda Ecológica Infantil M
                Product product2 = new Product();
                product2.setName("Fralda Ecológica Infantil M");
                product2.setDescription("Fralda ecológica lavável e sustentável para bebês no tamanho M, com camadas extra de proteção e conforto");
                product2.setPrice(new BigDecimal("99.90"));
                product2.setProductType("INFANTIL");
                product2.setProductChoice("TAMANHO_M");
                product2.setAffiliate(affiliate);
                product2.setImageUrl("https://example.com/fralda_m.jpg");

                Product savedProduct2 = productRepository.save(product2);

                // Adicionar estoque para o produto 2
                Stock stock2 = new Stock();
                stock2.setProduct(savedProduct2);
                stock2.setQuantity(40);
                stockRepository.save(stock2);

                // Produto 3: Fralda Ecológica Infantil G
                Product product3 = new Product();
                product3.setName("Fralda Ecológica Infantil G");
                product3.setDescription("Fralda ecológica lavável e sustentável para bebês no tamanho G, com ajuste perfeito e materiais hipoalergênicos");
                product3.setPrice(new BigDecimal("109.90"));
                product3.setProductType("INFANTIL");
                product3.setProductChoice("TAMANHO_G");
                product3.setAffiliate(affiliate);
                product3.setImageUrl("https://example.com/fralda_g.jpg");

                Product savedProduct3 = productRepository.save(product3);

                // Adicionar estoque para o produto 3
                Stock stock3 = new Stock();
                stock3.setProduct(savedProduct3);
                stock3.setQuantity(30);
                stockRepository.save(stock3);

                logger.info("Produtos criados com sucesso: 3 tipos de fraldas ecológicas");
            }
        };
    }
}