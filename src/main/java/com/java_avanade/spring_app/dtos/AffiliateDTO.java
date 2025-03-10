package com.java_avanade.spring_app.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AffiliateDTO {
    private Long id;
    private String name;
    private String email;
    private List<Long> productIds;
}