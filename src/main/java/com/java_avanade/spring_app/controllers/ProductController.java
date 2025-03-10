package com.java_avanade.spring_app.controllers;

import com.java_avanade.spring_app.dtos.ProductDTO;
import com.java_avanade.spring_app.models.Product;
import com.java_avanade.spring_app.services.ProductService;
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
        @RequestMapping("/products")
        @Tag(name = "Produtos", description = "Endpoints para gerenciamento de produtos")
        @SecurityRequirement(name = "Bearer Authentication")
        public class ProductController {

            @Autowired
            private ProductService productService;

            @GetMapping
            @Operation(summary = "Listar todos os produtos", description = "Retorna uma lista de todos os produtos")
            public ResponseEntity<List<ProductDTO>> getAllProducts() {
                List<ProductDTO> products = productService.getAllProducts();
                return ResponseEntity.ok(products);
            }

            @GetMapping("/{id}")
            @Operation(summary = "Obter produto por ID", description = "Retorna os detalhes de um produto pelo ID")
            public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
                ProductDTO product = productService.getProductById(id);
                return ResponseEntity.ok(product);
            }

            @GetMapping("/affiliate/{affiliateId}")
            @Operation(summary = "Listar produtos por afiliado", description = "Retorna uma lista de produtos de um afiliado específico")
            public ResponseEntity<List<ProductDTO>> getProductsByAffiliateId(@PathVariable Long affiliateId) {
                List<ProductDTO> products = productService.getProductsByAffiliateId(affiliateId);
                return ResponseEntity.ok(products);
            }

            @GetMapping("/search")
            @Operation(summary = "Pesquisar produtos", description = "Pesquisa produtos por palavra-chave")
            public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String keyword) {
                List<ProductDTO> products = productService.searchProducts(keyword);
                return ResponseEntity.ok(products);
            }

            @GetMapping("/type/{productType}")
            @Operation(summary = "Listar produtos por tipo", description = "Retorna uma lista de produtos de um tipo específico")
            public ResponseEntity<List<ProductDTO>> getProductsByType(@PathVariable String productType) {
                List<ProductDTO> products = productService.getProductsByType(productType);
                return ResponseEntity.ok(products);
            }

            @PostMapping
            @Operation(summary = "Criar produto", description = "Cria um novo produto para um afiliado")
            @PreAuthorize("hasRole('ADMIN') or hasRole('AFFILIATE')")
            public ResponseEntity<ProductDTO> createProduct(
                    @Valid @RequestBody Product product,
                    @RequestParam Long affiliateId,
            @RequestParam(required = false) Integer initialStock) {
        ProductDTO createdProduct = productService.createProduct(product, affiliateId, initialStock);
        return ResponseEntity.ok(createdProduct);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto", description = "Atualiza os dados de um produto existente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AFFILIATE') and @securityService.isProductOwner(#id, authentication)")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody Product productDetails) {
        ProductDTO updatedProduct = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir produto", description = "Exclui um produto pelo ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AFFILIATE') and @securityService.isProductOwner(#id, authentication)")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}