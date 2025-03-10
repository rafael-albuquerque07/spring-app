package com.java_avanade.spring_app.controllers;

import com.java_avanade.spring_app.dtos.CartDTO;
import com.java_avanade.spring_app.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/carts")
@Tag(name = "Carrinhos", description = "Endpoints para gerenciamento de itens no carrinho")
@SecurityRequirement(name = "Bearer Authentication")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    @Operation(summary = "Listar todos os itens do carrinho", description = "Retorna uma lista de todos os itens de carrinho")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CartDTO>> getAllCartItems() {
        List<CartDTO> cartItems = cartService.getAllCartItems();
        return ResponseEntity.ok(cartItems);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter item do carrinho por ID", description = "Retorna os detalhes de um item do carrinho pelo ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isCartItemOwner(#id, authentication)")
    public ResponseEntity<CartDTO> getCartItemById(@PathVariable Long id) {
        CartDTO cartItem = cartService.getCartItemById(id);
        return ResponseEntity.ok(cartItem);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Listar itens do carrinho por pedido", description = "Retorna uma lista de itens do carrinho de um pedido específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isOrderOwner(#orderId, authentication) or hasRole('AFFILIATE') and @securityService.isOrderContainsAffiliateProdut(#orderId, authentication)")
    public ResponseEntity<List<CartDTO>> getCartItemsByOrderId(@PathVariable Long orderId) {
        List<CartDTO> cartItems = cartService.getCartItemsByOrderId(orderId);
        return ResponseEntity.ok(cartItems);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Listar itens do carrinho por cliente", description = "Retorna uma lista de itens do carrinho de um cliente específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isOwnerOrAdmin(#clientId, authentication)")
    public ResponseEntity<List<CartDTO>> getCartItemsByClientId(@PathVariable Long clientId) {
        List<CartDTO> cartItems = cartService.getCartItemsByClientId(clientId);
        return ResponseEntity.ok(cartItems);
    }

    @PostMapping("/order/{orderId}/add")
    @Operation(summary = "Adicionar item ao carrinho", description = "Adiciona um produto ao carrinho de um pedido")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isOrderOwner(#orderId, authentication)")
    public ResponseEntity<CartDTO> addCartItem(
            @PathVariable Long orderId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam String paymentType) {
        CartDTO cartItem = cartService.addCartItem(orderId, productId, quantity, paymentType);
        return ResponseEntity.ok(cartItem);
    }

    @PutMapping("/{cartItemId}")
    @Operation(summary = "Atualizar item do carrinho", description = "Atualiza a quantidade ou o tipo de pagamento de um item do carrinho")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isCartItemOwner(#cartItemId, authentication)")
    public ResponseEntity<CartDTO> updateCartItem(
            @PathVariable Long cartItemId,
            @RequestParam Integer newQuantity,
            @RequestParam(required = false) String paymentType) {
        CartDTO updatedCartItem = cartService.updateCartItem(cartItemId, newQuantity, paymentType);
        return ResponseEntity.ok(updatedCartItem);
    }

    @DeleteMapping("/{cartItemId}")
    @Operation(summary = "Remover item do carrinho", description = "Remove um item do carrinho pelo ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isCartItemOwner(#cartItemId, authentication)")
    public ResponseEntity<Void> removeCartItem(@PathVariable Long cartItemId) {
        cartService.removeCartItem(cartItemId);
        return ResponseEntity.noContent().build();
    }
}