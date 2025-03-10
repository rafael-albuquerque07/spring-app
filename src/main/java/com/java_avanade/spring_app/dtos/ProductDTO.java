package com.java_avanade.spring_app.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long productCode;
    private String name;
    private String description;
    private BigDecimal price;
    private String productType;
    private String productChoice;
    private Long affiliateId;
    private String affiliateName;
    private Integer availableStock;
    private String imageUrl;
}