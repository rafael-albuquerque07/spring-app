package com.java_avanade.spring_app.controllers;

import com.java_avanade.spring_app.dtos.AffiliateDTO;
import com.java_avanade.spring_app.models.Affiliate;
import com.java_avanade.spring_app.models.Product;
import com.java_avanade.spring_app.services.AffiliateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/affiliates")
@Tag(name = "Afiliados", description = "Endpoints para gerenciamento de afiliados")
@SecurityRequirement(name = "Bearer Authentication")
public class AffiliateController {

    @Autowired
    private AffiliateService affiliateService;

    @GetMapping
    @Operation(summary = "Listar todos os afiliados", description = "Retorna uma lista de todos os afiliados")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AffiliateDTO>> getAllAffiliates() {
        List<AffiliateDTO> affiliates = affiliateService.getAllAffiliates();
        return ResponseEntity.ok(affiliates);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter afiliado por ID", description = "Retorna os detalhes de um afiliado pelo ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AFFILIATE') and @securityService.isOwnerOrAdmin(#id, authentication)")
    public ResponseEntity<AffiliateDTO> getAffiliateById(@PathVariable Long id) {
        AffiliateDTO affiliate = affiliateService.getAffiliateById(id);
        return ResponseEntity.ok(affiliate);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obter afiliado por ID de usuário", description = "Retorna os detalhes de um afiliado pelo ID do usuário")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AFFILIATE') and @securityService.isOwnerOrAdmin(#userId, authentication)")
    public ResponseEntity<AffiliateDTO> getAffiliateByUserId(@PathVariable Long userId) {
        AffiliateDTO affiliate = affiliateService.getAffiliateByUserId(userId);
        return ResponseEntity.ok(affiliate);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar afiliado", description = "Atualiza os dados de um afiliado existente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AFFILIATE') and @securityService.isOwnerOrAdmin(#id, authentication)")
    public ResponseEntity<AffiliateDTO> updateAffiliate(@PathVariable Long id, @Valid @RequestBody Affiliate affiliateDetails) {
        AffiliateDTO updatedAffiliate = affiliateService.updateAffiliate(id, affiliateDetails);
        return ResponseEntity.ok(updatedAffiliate);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir afiliado", description = "Exclui um afiliado pelo ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAffiliate(@PathVariable Long id) {
        affiliateService.deleteAffiliate(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/products")
    @Operation(summary = "Listar produtos do afiliado", description = "Retorna uma lista de produtos de um afiliado específico")
    public ResponseEntity<List<Product>> getAffiliateProducts(@PathVariable Long id) {
        List<Product> products = affiliateService.getAffiliateProducts(id);
        return ResponseEntity.ok(products);
    }
}