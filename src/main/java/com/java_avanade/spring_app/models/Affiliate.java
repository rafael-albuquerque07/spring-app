package com.java_avanade.spring_app.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "affiliates")
public class Affiliate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    // Campos para autenticação própria
    @Column(unique = true)
    private String username;

    private String password;

    // Referência explícita ao admin
    @Column(name = "admin_id", nullable = false)
    private Long adminId = 1L;  // Default para o admin principal

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "affiliate_roles", joinColumns = @JoinColumn(name = "affiliate_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    @OneToMany(mappedBy = "affiliate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();
}