package com.java_avanade.spring_app.services;

import com.java_avanade.spring_app.dtos.CartDTO;
import com.java_avanade.spring_app.exceptions.ResourceNotFoundException;
import com.java_avanade.spring_app.models.Cart;
import com.java_avanade.spring_app.models.Order;
import com.java_avanade.spring_app.models.Product;
import com.java_avanade.spring_app.repositories.CartRepository;
import com.java_avanade.spring_app.repositories.OrderRepository;
import com.java_avanade.spring_app.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockService stockService;

    public List<CartDTO> getAllCartItems() {
        return cartRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CartDTO getCartItemById(Long id) {
        Cart cartItem = cartRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item do carrinho", "id", id));
        return convertToDTO(cartItem);
    }

    public List<CartDTO> getCartItemsByOrderId(Long orderId) {
        return cartRepository.findByOrderId(orderId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CartDTO> getCartItemsByClientId(Long clientId) {
        return cartRepository.findCartItemsByClientId(clientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CartDTO addCartItem(Long orderId, Long productId, Integer quantity, String paymentType) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", orderId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", productId));

        // Verificar estoque
        stockService.checkAndReduceStock(productId, quantity);

        Cart cartItem = new Cart();
        cartItem.setOrder(order);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setPaymentType(paymentType);

        Cart savedCartItem = cartRepository.save(cartItem);

        // Atualizar o valor total do pedido
        updateOrderTotal(order);

        return convertToDTO(savedCartItem);
    }

    @Transactional
    public CartDTO updateCartItem(Long cartItemId, Integer newQuantity, String paymentType) {
        Cart cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item do carrinho", "id", cartItemId));

        // Se a quantidade vai aumentar, verificar estoque
        if (newQuantity > cartItem.getQuantity()) {
            int additionalQuantity = newQuantity - cartItem.getQuantity();
            stockService.checkAndReduceStock(cartItem.getProduct().getProductCode(), additionalQuantity);
        }
        // Se a quantidade vai diminuir, retornar ao estoque
        else if (newQuantity < cartItem.getQuantity()) {
            int returnedQuantity = cartItem.getQuantity() - newQuantity;
            stockService.addStock(cartItem.getProduct().getProductCode(), returnedQuantity);
        }

        cartItem.setQuantity(newQuantity);
        if (paymentType != null && !paymentType.isEmpty()) {
            cartItem.setPaymentType(paymentType);
        }

        Cart updatedCartItem = cartRepository.save(cartItem);

        // Atualizar o valor total do pedido
        updateOrderTotal(cartItem.getOrder());

        return convertToDTO(updatedCartItem);
    }

    @Transactional
    public void removeCartItem(Long cartItemId) {
        Cart cartItem = cartRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item do carrinho", "id", cartItemId));

        // Retornar a quantidade ao estoque
        stockService.addStock(cartItem.getProduct().getProductCode(), cartItem.getQuantity());

        Order order = cartItem.getOrder();
        cartRepository.delete(cartItem);

        // Atualizar o valor total do pedido
        updateOrderTotal(order);
    }

    public CartDTO convertToDTO(Cart cartItem) {
        CartDTO dto = new CartDTO();
        dto.setId(cartItem.getId());
        dto.setOrderId(cartItem.getOrder().getOrderId());
        dto.setProductId(cartItem.getProduct().getProductCode());
        dto.setProductName(cartItem.getProduct().getName());
        dto.setQuantity(cartItem.getQuantity());
        dto.setUnitPrice(cartItem.getProduct().getPrice());
        dto.setTotalPrice(cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        dto.setPaymentType(cartItem.getPaymentType());
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