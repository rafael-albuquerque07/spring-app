package com.java_avanade.spring_app.services;

import com.java_avanade.spring_app.models.*;
import com.java_avanade.spring_app.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AffiliateRepository affiliateRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private CheckoutRepository checkoutRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    // Verifica se o usuário é o dono do recurso, um administrador direto, ou o superadmin
    public boolean isOwnerOrAdmin(Long resourceOwnerId, Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        // Obter o tipo real de usuário e ID usando o CustomUserDetailsService
        String userType = customUserDetailsService.getUserTypeByUsername(username);
        Long userId = customUserDetailsService.getRealUserIdByUsername(username);

        logger.debug("Verificando acesso: usuário {} do tipo {} para recurso {}", username, userType, resourceOwnerId);

        // Se for o superadmin (ID 1), tem acesso total
        if ("ADMIN".equals(userType)) {
            logger.debug("Acesso permitido: usuário é admin com ID {}", userId);
            return true;
        }

        // Verificar permissões de ADMIN via authorities
        if (userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            logger.debug("Acesso permitido: usuário possui ROLE_ADMIN");
            return true;
        }

        // Verificar se é um cliente e se é o dono do recurso
        if ("CLIENT".equals(userType)) {
            Optional<Client> clientOptional = clientRepository.findByUsername(username);
            if (clientOptional.isPresent()) {
                Client client = clientOptional.get();

                // Verificar se é o próprio cliente
                if (client.getId().equals(resourceOwnerId)) {
                    logger.debug("Acesso permitido: cliente {} é o dono do recurso {}",
                            client.getId(), resourceOwnerId);
                    return true;
                }

                // Verificar se o admin do cliente tem acesso a esse recurso
                User clientAdmin = userRepository.findById(client.getAdminId())
                        .orElse(null);
                if (clientAdmin != null && clientAdmin.getRoles().contains("SUPERUSER")) {
                    logger.debug("Acesso permitido: cliente {} tem admin SUPERUSER {}",
                            client.getId(), clientAdmin.getId());
                    return true;
                }
            }
        }

        // Verificar se é um afiliado e se é o dono do recurso
        if ("AFFILIATE".equals(userType)) {
            Optional<Affiliate> affiliateOptional = affiliateRepository.findByUsername(username);
            if (affiliateOptional.isPresent()) {
                Affiliate affiliate = affiliateOptional.get();

                // Verificar se é o próprio afiliado
                if (affiliate.getId().equals(resourceOwnerId)) {
                    logger.debug("Acesso permitido: afiliado {} é o dono do recurso {}",
                            affiliate.getId(), resourceOwnerId);
                    return true;
                }

                // Verificar se o admin do afiliado tem acesso a esse recurso
                User affiliateAdmin = userRepository.findById(affiliate.getAdminId())
                        .orElse(null);
                if (affiliateAdmin != null && affiliateAdmin.getRoles().contains("SUPERUSER")) {
                    logger.debug("Acesso permitido: afiliado {} tem admin SUPERUSER {}",
                            affiliate.getId(), affiliateAdmin.getId());
                    return true;
                }
            }
        }

        logger.debug("Acesso negado: usuário {} ao recurso {}", username, resourceOwnerId);
        return false;
    }

    // Verifica se o ID do usuário corresponde ao usuário autenticado
    public boolean isUserIdMatch(Long userId, Authentication authentication) {
        String username = authentication.getName();
        String userType = customUserDetailsService.getUserTypeByUsername(username);
        Long authenticatedUserId = customUserDetailsService.getRealUserIdByUsername(username);

        logger.debug("Verificando correspondência de ID: usuário {} (tipo: {}, ID: {}) com ID solicitado {}",
                username, userType, authenticatedUserId, userId);

        // Se for admin, tem acesso
        if ("ADMIN".equals(userType)) {
            logger.debug("Correspondência permitida: usuário é admin");
            return true;
        }

        // Verificar se os IDs correspondem
        boolean isMatch = authenticatedUserId.equals(userId);
        if (isMatch) {
            logger.debug("Correspondência permitida: IDs são iguais");
        } else {
            logger.debug("Correspondência negada: IDs são diferentes");
        }

        return isMatch;
    }

    // Verifica se o usuário é dono do pedido
    public boolean isOrderOwner(Long orderId, Authentication authentication) {
        String username = authentication.getName();
        String userType = customUserDetailsService.getUserTypeByUsername(username);

        logger.debug("Verificando propriedade do pedido: usuário {} (tipo: {}) para pedido {}",
                username, userType, orderId);

        // Se for admin, tem acesso
        if ("ADMIN".equals(userType) || hasRole((UserDetails) authentication.getPrincipal(), "ADMIN")) {
            logger.debug("Acesso permitido: usuário é admin");
            return true;
        }

        // Se não for cliente, não tem acesso
        if (!"CLIENT".equals(userType)) {
            logger.debug("Acesso negado: usuário não é cliente");
            return false;
        }

        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            logger.debug("Acesso negado: pedido {} não encontrado", orderId);
            return false;
        }

        Order order = orderOpt.get();
        Client orderClient = order.getClient();

        // Verificar se o cliente autenticado é o dono do pedido
        Optional<Client> clientOpt = clientRepository.findByUsername(username);
        if (clientOpt.isPresent() && clientOpt.get().getId().equals(orderClient.getId())) {
            logger.debug("Acesso permitido: cliente {} é dono do pedido {}",
                    clientOpt.get().getId(), orderId);
            return true;
        }

        logger.debug("Acesso negado: cliente {} não é dono do pedido {}",
                clientOpt.map(Client::getId).orElse(null), orderId);
        return false;
    }

    // Verifica se o pedido contém produtos do afiliado
    public boolean isOrderContainsAffiliateProdut(Long orderId, Authentication authentication) {
        String username = authentication.getName();
        String userType = customUserDetailsService.getUserTypeByUsername(username);

        logger.debug("Verificando se pedido contém produtos do afiliado: usuário {} (tipo: {}) para pedido {}",
                username, userType, orderId);

        // Se for admin, tem acesso
        if ("ADMIN".equals(userType) || hasRole((UserDetails) authentication.getPrincipal(), "ADMIN")) {
            logger.debug("Acesso permitido: usuário é admin");
            return true;
        }

        // Se não for afiliado, não tem acesso
        if (!"AFFILIATE".equals(userType)) {
            logger.debug("Acesso negado: usuário não é afiliado");
            return false;
        }

        // Verificar se é um afiliado
        Optional<Affiliate> affiliateOpt = affiliateRepository.findByUsername(username);
        if (affiliateOpt.isEmpty()) {
            logger.debug("Acesso negado: afiliado não encontrado para username {}", username);
            return false;
        }

        Affiliate affiliate = affiliateOpt.get();

        // Verificar se o pedido contém produtos deste afiliado
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            logger.debug("Acesso negado: pedido {} não encontrado", orderId);
            return false;
        }

        Order order = orderOpt.get();

        // Verificar cada item do carrinho no pedido
        boolean containsAffiliateProduct = order.getCartItems().stream()
                .anyMatch(cartItem -> cartItem.getProduct().getAffiliate().getId().equals(affiliate.getId()));

        if (containsAffiliateProduct) {
            logger.debug("Acesso permitido: pedido {} contém produtos do afiliado {}",
                    orderId, affiliate.getId());
        } else {
            logger.debug("Acesso negado: pedido {} não contém produtos do afiliado {}",
                    orderId, affiliate.getId());
        }

        return containsAffiliateProduct;
    }

    // Verifica se o usuário é dono do item do carrinho
    public boolean isCartItemOwner(Long cartItemId, Authentication authentication) {
        String username = authentication.getName();
        String userType = customUserDetailsService.getUserTypeByUsername(username);

        logger.debug("Verificando propriedade do item do carrinho: usuário {} (tipo: {}) para item {}",
                username, userType, cartItemId);

        // Se for admin, tem acesso
        if ("ADMIN".equals(userType) || hasRole((UserDetails) authentication.getPrincipal(), "ADMIN")) {
            logger.debug("Acesso permitido: usuário é admin");
            return true;
        }

        // Se não for cliente, não tem acesso
        if (!"CLIENT".equals(userType)) {
            logger.debug("Acesso negado: usuário não é cliente");
            return false;
        }

        // Verificar se é um cliente
        Optional<Client> clientOpt = clientRepository.findByUsername(username);
        if (clientOpt.isEmpty()) {
            logger.debug("Acesso negado: cliente não encontrado para username {}", username);
            return false;
        }

        Client client = clientOpt.get();

        // Verificar se o item do carrinho pertence a um pedido deste cliente
        Optional<Cart> cartItemOpt = cartRepository.findById(cartItemId);
        if (cartItemOpt.isEmpty()) {
            logger.debug("Acesso negado: item do carrinho {} não encontrado", cartItemId);
            return false;
        }

        Cart cartItem = cartItemOpt.get();
        boolean isOwner = cartItem.getOrder().getClient().getId().equals(client.getId());

        if (isOwner) {
            logger.debug("Acesso permitido: cliente {} é dono do item do carrinho {}",
                    client.getId(), cartItemId);
        } else {
            logger.debug("Acesso negado: cliente {} não é dono do item do carrinho {}",
                    client.getId(), cartItemId);
        }

        return isOwner;
    }

    // Verifica se o usuário é o dono do produto
    public boolean isProductOwner(Long productId, Authentication authentication) {
        String username = authentication.getName();
        String userType = customUserDetailsService.getUserTypeByUsername(username);

        logger.debug("Verificando propriedade do produto: usuário {} (tipo: {}) para produto {}",
                username, userType, productId);

        // Se for admin, tem acesso
        if ("ADMIN".equals(userType) || hasRole((UserDetails) authentication.getPrincipal(), "ADMIN")) {
            logger.debug("Acesso permitido: usuário é admin");
            return true;
        }

        // Se não for afiliado, não tem acesso
        if (!"AFFILIATE".equals(userType)) {
            logger.debug("Acesso negado: usuário não é afiliado");
            return false;
        }

        // Verificar se é um afiliado
        Optional<Affiliate> affiliateOpt = affiliateRepository.findByUsername(username);
        if (affiliateOpt.isEmpty()) {
            logger.debug("Acesso negado: afiliado não encontrado para username {}", username);
            return false;
        }

        Affiliate affiliate = affiliateOpt.get();

        // Verificar se o produto pertence a este afiliado
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isEmpty()) {
            logger.debug("Acesso negado: produto {} não encontrado", productId);
            return false;
        }

        Product product = productOpt.get();
        boolean isOwner = product.getAffiliate().getId().equals(affiliate.getId());

        if (isOwner) {
            logger.debug("Acesso permitido: afiliado {} é dono do produto {}",
                    affiliate.getId(), productId);
        } else {
            logger.debug("Acesso negado: afiliado {} não é dono do produto {}",
                    affiliate.getId(), productId);
        }

        return isOwner;
    }

    // Verifica se o usuário é o dono do estoque
    public boolean isStockOwner(Long stockId, Authentication authentication) {
        String username = authentication.getName();
        String userType = customUserDetailsService.getUserTypeByUsername(username);

        logger.debug("Verificando propriedade do estoque: usuário {} (tipo: {}) para estoque {}",
                username, userType, stockId);

        // Se for admin, tem acesso
        if ("ADMIN".equals(userType) || hasRole((UserDetails) authentication.getPrincipal(), "ADMIN")) {
            logger.debug("Acesso permitido: usuário é admin");
            return true;
        }

        // Se não for afiliado, não tem acesso
        if (!"AFFILIATE".equals(userType)) {
            logger.debug("Acesso negado: usuário não é afiliado");
            return false;
        }

        // Verificar se é um afiliado
        Optional<Affiliate> affiliateOpt = affiliateRepository.findByUsername(username);
        if (affiliateOpt.isEmpty()) {
            logger.debug("Acesso negado: afiliado não encontrado para username {}", username);
            return false;
        }

        Affiliate affiliate = affiliateOpt.get();

        // Verificar se o estoque pertence a um produto deste afiliado
        Optional<Stock> stockOpt = stockRepository.findById(stockId);
        if (stockOpt.isEmpty()) {
            logger.debug("Acesso negado: estoque {} não encontrado", stockId);
            return false;
        }

        Stock stock = stockOpt.get();
        boolean isOwner = stock.getProduct().getAffiliate().getId().equals(affiliate.getId());

        if (isOwner) {
            logger.debug("Acesso permitido: afiliado {} é dono do estoque {}",
                    affiliate.getId(), stockId);
        } else {
            logger.debug("Acesso negado: afiliado {} não é dono do estoque {}",
                    affiliate.getId(), stockId);
        }

        return isOwner;
    }

    // Verifica se o usuário é o dono do checkout
    public boolean isCheckoutOwner(Long checkoutId, Authentication authentication) {
        String username = authentication.getName();
        String userType = customUserDetailsService.getUserTypeByUsername(username);

        logger.debug("Verificando propriedade do checkout: usuário {} (tipo: {}) para checkout {}",
                username, userType, checkoutId);

        // Se for admin, tem acesso
        if ("ADMIN".equals(userType) || hasRole((UserDetails) authentication.getPrincipal(), "ADMIN")) {
            logger.debug("Acesso permitido: usuário é admin");
            return true;
        }

        // Se não for cliente, não tem acesso
        if (!"CLIENT".equals(userType)) {
            logger.debug("Acesso negado: usuário não é cliente");
            return false;
        }

        // Verificar se é um cliente
        Optional<Client> clientOpt = clientRepository.findByUsername(username);
        if (clientOpt.isEmpty()) {
            logger.debug("Acesso negado: cliente não encontrado para username {}", username);
            return false;
        }

        Client client = clientOpt.get();

        // Verificar se o checkout pertence a um pedido deste cliente
        Optional<Checkout> checkoutOpt = checkoutRepository.findById(checkoutId);
        if (checkoutOpt.isEmpty()) {
            logger.debug("Acesso negado: checkout {} não encontrado", checkoutId);
            return false;
        }

        Checkout checkout = checkoutOpt.get();
        boolean isOwner = checkout.getOrder().getClient().getId().equals(client.getId());

        if (isOwner) {
            logger.debug("Acesso permitido: cliente {} é dono do checkout {}",
                    client.getId(), checkoutId);
        } else {
            logger.debug("Acesso negado: cliente {} não é dono do checkout {}",
                    client.getId(), checkoutId);
        }

        return isOwner;
    }

    // Verifica se o checkout contém produtos do afiliado
    public boolean isCheckoutContainsAffiliateProduct(Long checkoutId, Authentication authentication) {
        String username = authentication.getName();
        String userType = customUserDetailsService.getUserTypeByUsername(username);

        logger.debug("Verificando se checkout contém produtos do afiliado: usuário {} (tipo: {}) para checkout {}",
                username, userType, checkoutId);

        // Se for admin, tem acesso
        if ("ADMIN".equals(userType) || hasRole((UserDetails) authentication.getPrincipal(), "ADMIN")) {
            logger.debug("Acesso permitido: usuário é admin");
            return true;
        }

        // Se não for afiliado, não tem acesso
        if (!"AFFILIATE".equals(userType)) {
            logger.debug("Acesso negado: usuário não é afiliado");
            return false;
        }

        // Verificar se é um afiliado
        Optional<Affiliate> affiliateOpt = affiliateRepository.findByUsername(username);
        if (affiliateOpt.isEmpty()) {
            logger.debug("Acesso negado: afiliado não encontrado para username {}", username);
            return false;
        }

        Affiliate affiliate = affiliateOpt.get();

        // Verificar se o checkout contém produtos deste afiliado
        Optional<Checkout> checkoutOpt = checkoutRepository.findById(checkoutId);
        if (checkoutOpt.isEmpty()) {
            logger.debug("Acesso negado: checkout {} não encontrado", checkoutId);
            return false;
        }

        Checkout checkout = checkoutOpt.get();

        // Verificar se o produto do checkout pertence ao afiliado
        if (checkout.getProduct() != null) {
            boolean isProductOwner = checkout.getProduct().getAffiliate().getId().equals(affiliate.getId());
            if (isProductOwner) {
                logger.debug("Acesso permitido: produto do checkout {} pertence ao afiliado {}",
                        checkoutId, affiliate.getId());
                return true;
            }
        }

        // Verificar os produtos no pedido associado ao checkout
        boolean containsAffiliateProduct = checkout.getOrder().getCartItems().stream()
                .anyMatch(cartItem -> cartItem.getProduct().getAffiliate().getId().equals(affiliate.getId()));

        if (containsAffiliateProduct) {
            logger.debug("Acesso permitido: checkout {} contém produtos do afiliado {}",
                    checkoutId, affiliate.getId());
        } else {
            logger.debug("Acesso negado: checkout {} não contém produtos do afiliado {}",
                    checkoutId, affiliate.getId());
        }

        return containsAffiliateProduct;
    }

    // Método utilitário para verificar papel
    private boolean hasRole(UserDetails userDetails, String role) {
        boolean hasRole = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_" + role));

        logger.debug("Verificação de papel: usuário {} {} o papel {}",
                userDetails.getUsername(),
                hasRole ? "possui" : "não possui",
                role);

        return hasRole;
    }
}