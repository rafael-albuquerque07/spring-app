package com.java_avanade.spring_app.controllers;

import com.java_avanade.spring_app.dtos.StockDTO;
import com.java_avanade.spring_app.services.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stocks")
@Tag(name = "Estoques", description = "Endpoints para gerenciamento de estoques")
@SecurityRequirement(name = "Bearer Authentication")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping
    @Operation(summary = "Listar todos os estoques", description = "Retorna uma lista de todos os estoques")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AFFILIATE')")
    public ResponseEntity<List<StockDTO>> getAllStocks() {
        List<StockDTO> stocks = stockService.getAllStocks();
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter estoque por ID", description = "Retorna os detalhes de um estoque pelo ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AFFILIATE')")
    public ResponseEntity<StockDTO> getStockById(@PathVariable Long id) {
        StockDTO stock = stockService.getStockById(id);
        return ResponseEntity.ok(stock);
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Listar estoques por produto", description = "Retorna uma lista de estoques de um produto específico")
    public ResponseEntity<List<StockDTO>> getStocksByProductId(@PathVariable Long productId) {
        List<StockDTO> stocks = stockService.getStocksByProductId(productId);
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/product/{productId}/quantity")
    @Operation(summary = "Obter quantidade total em estoque", description = "Retorna a quantidade total em estoque de um produto")
    public ResponseEntity<Integer> getTotalStockQuantity(@PathVariable Long productId) {
        Integer quantity = stockService.getTotalStockQuantityByProductId(productId);
        return ResponseEntity.ok(quantity);
    }

    @PostMapping("/product/{productId}/add")
    @Operation(summary = "Adicionar estoque", description = "Adiciona uma quantidade ao estoque de um produto")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AFFILIATE') and @securityService.isProductOwner(#productId, authentication)")
    public ResponseEntity<StockDTO> addStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        StockDTO stock = stockService.addStock(productId, quantity);
        return ResponseEntity.ok(stock);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar estoque", description = "Atualiza a quantidade em estoque")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AFFILIATE') and @securityService.isStockOwner(#id, authentication)")
    public ResponseEntity<StockDTO> updateStock(
            @PathVariable Long id,
            @RequestParam Integer newQuantity) {
        StockDTO stock = stockService.updateStock(id, newQuantity);
        return ResponseEntity.ok(stock);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover estoque", description = "Remove um estoque pelo ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AFFILIATE') and @securityService.isStockOwner(#id, authentication)")
    public ResponseEntity<Void> removeStock(@PathVariable Long id) {
        stockService.removeStock(id);
        return ResponseEntity.noContent().build();
    }
}