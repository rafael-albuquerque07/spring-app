package com.java_avanade.spring_app.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long orderId;
    private Long clientId;
    private String clientName;
    private List<CartDTO> cartItems;
    private LocalDateTime orderDate;
    private String status;
    private BigDecimal totalAmount;
    private Boolean hasCheckout;
}