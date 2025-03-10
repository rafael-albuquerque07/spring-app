package com.java_avanade.spring_app.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutDTO {
    private Long id;
    private Long orderId;
    private String clientName;
    private LocalDateTime checkoutDate;
    private BigDecimal totalPrice;
    private String paymentStatus;
    private String shippingAddress;

    // Para requisições de checkout
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckoutRequest {
        private Long orderId;
        private String shippingAddress;
        private String paymentMethod;
    }
}