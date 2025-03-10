package com.java_avanade.spring_app.services;

import com.java_avanade.spring_app.dtos.CheckoutDTO;
import com.java_avanade.spring_app.exceptions.ResourceNotFoundException;
import com.java_avanade.spring_app.models.Cart;
import com.java_avanade.spring_app.models.Checkout;
import com.java_avanade.spring_app.models.Order;
import com.java_avanade.spring_app.models.Product;
import com.java_avanade.spring_app.repositories.CheckoutRepository;
import com.java_avanade.spring_app.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CheckoutService {

    @Autowired
    private CheckoutRepository checkoutRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    public List<CheckoutDTO> getAllCheckouts() {
        return checkoutRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CheckoutDTO getCheckoutById(Long id) {
        Checkout checkout = checkoutRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout", "id", id));
        return convertToDTO(checkout);
    }

    public CheckoutDTO getCheckoutByOrderId(Long orderId) {
        Checkout checkout = checkoutRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout", "orderId", orderId));
        return convertToDTO(checkout);
    }

    public List<CheckoutDTO> getCheckoutsByClientId(Long clientId) {
        return checkoutRepository.findCheckoutsByClientId(clientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CheckoutDTO> getCheckoutsByAffiliateId(Long affiliateId) {
        return checkoutRepository.findCheckoutsByAffiliateId(affiliateId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public CheckoutDTO processCheckout(CheckoutDTO.CheckoutRequest checkoutRequest) {
        Order order = orderRepository.findById(checkoutRequest.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", checkoutRequest.getOrderId()));

        // Verificar se o pedido já possui checkout
        if (order.getCheckout() != null) {
            throw new IllegalStateException("Este pedido já possui um checkout finalizado");
        }

        // Verificar se o pedido está em status válido para checkout
        if (!"OPEN".equals(order.getStatus()) && !"PROCESSING".equals(order.getStatus())) {
            throw new IllegalStateException("O pedido deve estar aberto ou em processamento para finalizar o checkout");
        }

        // Criar checkout
        Checkout checkout = new Checkout();
        checkout.setOrder(order);

        // Se o pedido tiver apenas um produto, associá-lo ao checkout
        if (order.getCartItems().size() == 1) {
            Cart cartItem = order.getCartItems().iterator().next();
            checkout.setProduct(cartItem.getProduct());
            checkout.setQuantity(cartItem.getQuantity());
        } else {
            // Caso contrário, deixar o produto como null (será associado ao primeiro produto)
            if (!order.getCartItems().isEmpty()) {
                Product firstProduct = order.getCartItems().iterator().next().getProduct();
                checkout.setProduct(firstProduct);

                // Somar quantidade total de todos os itens
                Integer totalQuantity = order.getCartItems().stream()
                        .mapToInt(Cart::getQuantity)
                        .sum();
                checkout.setQuantity(totalQuantity);
            }
        }

        checkout.setTotalPrice(order.getTotalAmount());
        checkout.setPaymentStatus("PENDING");
        checkout.setShippingAddress(checkoutRequest.getShippingAddress());

        Checkout savedCheckout = checkoutRepository.save(checkout);

        // Atualizar status do pedido
        orderService.updateOrderStatus(order.getOrderId(), "AWAITING_PAYMENT");

        return convertToDTO(savedCheckout);
    }

    @Transactional
    public CheckoutDTO updatePaymentStatus(Long checkoutId, String paymentStatus) {
        Checkout checkout = checkoutRepository.findById(checkoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout", "id", checkoutId));

        checkout.setPaymentStatus(paymentStatus);
        Checkout updatedCheckout = checkoutRepository.save(checkout);

        // Atualizar status do pedido de acordo com o status do pagamento
        if ("PAID".equals(paymentStatus)) {
            orderService.updateOrderStatus(checkout.getOrder().getOrderId(), "PAID");
        } else if ("CANCELED".equals(paymentStatus)) {
            orderService.updateOrderStatus(checkout.getOrder().getOrderId(), "CANCELED");
        }

        return convertToDTO(updatedCheckout);
    }

    @Transactional
    public CheckoutDTO updateShippingAddress(Long checkoutId, String newShippingAddress) {
        Checkout checkout = checkoutRepository.findById(checkoutId)
                .orElseThrow(() -> new ResourceNotFoundException("Checkout", "id", checkoutId));

        checkout.setShippingAddress(newShippingAddress);
        Checkout updatedCheckout = checkoutRepository.save(checkout);

        return convertToDTO(updatedCheckout);
    }

    public CheckoutDTO convertToDTO(Checkout checkout) {
        CheckoutDTO dto = new CheckoutDTO();
        dto.setId(checkout.getId());
        dto.setOrderId(checkout.getOrder().getOrderId());
        dto.setCheckoutDate(checkout.getCheckoutDate());
        dto.setTotalPrice(checkout.getTotalPrice());
        dto.setPaymentStatus(checkout.getPaymentStatus());
        dto.setShippingAddress(checkout.getShippingAddress());

        // Nome do cliente
        if (checkout.getOrder().getClient() != null) {
            dto.setClientName(checkout.getOrder().getClient().getName());
        }

        return dto;
    }
}