package com.java_avanade.spring_app.services;

import com.java_avanade.spring_app.dtos.CartDTO;
import com.java_avanade.spring_app.dtos.OrderDTO;
import com.java_avanade.spring_app.exceptions.ResourceNotFoundException;
import com.java_avanade.spring_app.models.Cart;
import com.java_avanade.spring_app.models.Client;
import com.java_avanade.spring_app.models.Order;
import com.java_avanade.spring_app.models.Product;
import com.java_avanade.spring_app.repositories.CartRepository;
import com.java_avanade.spring_app.repositories.ClientRepository;
import com.java_avanade.spring_app.repositories.OrderRepository;
import com.java_avanade.spring_app.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private StockService stockService;

    @Autowired
    private CartService cartService;

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
        return convertToDTO(order);
    }

    public Order getOrderEntityById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", id));
    }

    public List<OrderDTO> getOrdersByClientId(Long clientId) {
        return orderRepository.findByClientId(clientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByClientIdAndStatus(Long clientId, String status) {
        return orderRepository.findOrderListByClientIdAndStatus(clientId, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByAffiliateId(Long affiliateId) {
        return orderRepository.findOrdersByAffiliateId(affiliateId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDTO createOrderForClient(Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clientId));

        // Verificar se já existe um pedido em aberto para o cliente
        Optional<Order> existingOpenOrder = orderRepository.findByClientIdAndStatus(clientId, "OPEN");
        if (existingOpenOrder.isPresent()) {
            return convertToDTO(existingOpenOrder.get());
        }

        // Criar novo pedido
        Order order = new Order();
        order.setClient(client);
        order.setStatus("OPEN");
        order.setTotalAmount(BigDecimal.ZERO);

        Order savedOrder = orderRepository.save(order);
        return convertToDTO(savedOrder);
    }

    @Transactional
    public OrderDTO addProductToOrder(Long orderId, Long productId, Integer quantity, String paymentType) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", orderId));

        // Verificar se o pedido está aberto
        if (!"OPEN".equals(order.getStatus())) {
            throw new IllegalStateException("Não é possível adicionar produtos a um pedido que não está aberto");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", productId));

        // Verificar e reduzir o estoque
        stockService.checkAndReduceStock(productId, quantity);

        // Adicionar produto ao carrinho
        Cart cartItem = new Cart();
        cartItem.setOrder(order);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setPaymentType(paymentType);
        cartRepository.save(cartItem);

        // Atualizar valor total do pedido
        updateOrderTotal(order);

        return convertToDTO(order);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", orderId));

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        return convertToDTO(updatedOrder);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", orderId));

        // Remover todos os itens do carrinho associados a este pedido
        cartRepository.deleteByOrderId(orderId);

        // Remover o pedido
        orderRepository.delete(order);
    }

    public OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setOrderDate(order.getOrderDate());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());

        // Dados do cliente
        if (order.getClient() != null) {
            dto.setClientId(order.getClient().getId());
            dto.setClientName(order.getClient().getName());
        }

        // Itens do carrinho
        List<CartDTO> cartItems = order.getCartItems().stream()
                .map(cartService::convertToDTO)
                .collect(Collectors.toList());
        dto.setCartItems(cartItems);

        // Verificar se existe checkout
        dto.setHasCheckout(order.getCheckout() != null);

        return dto;
    }

    private void updateOrderTotal(Order order) {
        BigDecimal total = BigDecimal.ZERO;

        for (Cart cartItem : order.getCartItems()) {
            BigDecimal itemTotal = cartItem.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(itemTotal);
        }

        order.setTotalAmount(total);
        orderRepository.save(order);
    }
}