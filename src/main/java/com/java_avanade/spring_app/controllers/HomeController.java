package com.java_avanade.spring_app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.java_avanade.spring_app.repositories.UserRepository;
import com.java_avanade.spring_app.repositories.ClientRepository;
import com.java_avanade.spring_app.repositories.AffiliateRepository;
import com.java_avanade.spring_app.repositories.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AffiliateRepository affiliateRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/")
    public String homePage(Model model) {
        logger.info("Acessando a página inicial");

        // Adicionando estatísticas ao modelo
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalClients", clientRepository.count());
        model.addAttribute("totalAffiliates", affiliateRepository.count());
        model.addAttribute("totalProducts", productRepository.count());

        // Definindo endpoints para exibição na página
        model.addAttribute("endpoints", getAPIEndpoints());

        return "index";
    }

    private EndpointInfo[] getAPIEndpoints() {
        return new EndpointInfo[] {
                new EndpointInfo("GET", "/clients", "Listar Clientes",
                        "Retorna uma lista de todos os clientes cadastrados no sistema.",
                        "/swagger-ui/index.html#/client-controller/getAllClients"),

                new EndpointInfo("POST", "/auth/register", "Registrar Usuário",
                        "Registra um novo usuário no sistema (cliente ou afiliado).",
                        "/swagger-ui/index.html#/auth-controller/registerUser"),

                new EndpointInfo("POST", "/auth/login", "Autenticação",
                        "Autentica o usuário e retorna um token JWT válido.",
                        "/swagger-ui/index.html#/auth-controller/authenticateUser"),

                new EndpointInfo("GET", "/products", "Listar Produtos",
                        "Retorna uma lista de todos os produtos disponíveis.",
                        "/swagger-ui/index.html#/product-controller/getAllProducts"),

                new EndpointInfo("POST", "/products", "Criar Produto",
                        "Cria um novo produto com os dados fornecidos.",
                        "/swagger-ui/index.html#/product-controller/createProduct"),

                new EndpointInfo("GET", "/affiliates", "Listar Afiliados",
                        "Retorna uma lista de todos os afiliados cadastrados no sistema.",
                        "/swagger-ui/index.html#/affiliate-controller/getAllAffiliates")
        };
    }

    // Classe interna para representar informações do endpoint
    public static class EndpointInfo {
        private String method;
        private String path;
        private String title;
        private String description;
        private String swaggerLink;

        public EndpointInfo(String method, String path, String title, String description, String swaggerLink) {
            this.method = method;
            this.path = path;
            this.title = title;
            this.description = description;
            this.swaggerLink = swaggerLink;
        }

        public String getMethod() { return method; }
        public String getPath() { return path; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getSwaggerLink() { return swaggerLink; }

        public String getBadgeClass() {
            switch (method) {
                case "GET": return "bg-success";
                case "POST": return "bg-primary";
                case "PUT": return "bg-warning text-dark";
                case "PATCH": return "bg-info text-dark";
                case "DELETE": return "bg-danger";
                default: return "bg-secondary";
            }
        }
    }
}