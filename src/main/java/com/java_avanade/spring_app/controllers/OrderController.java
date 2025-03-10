package com.java_avanade.spring_app.controllers;

import com.java_avanade.spring_app.dtos.OrderDTO;
import com.java_avanade.spring_app.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@Tag(name = "Pedidos", description = "Endpoints para gerenciamento de pedidos")
@SecurityRequirement(name = "Bearer Authentication")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    @Operation(summary = "Listar todos os pedidos", description = "Retorna uma lista de todos os pedidos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<OrderDTO> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter pedido por ID", description = "Retorna os detalhes de um pedido pelo ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isOrderOwner(#id, authentication) or hasRole('AFFILIATE') and @securityService.isOrderContainsAffiliateProdut(#id, authentication)")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Listar pedidos por cliente", description = "Retorna uma lista de pedidos de um cliente específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isOwnerOrAdmin(#clientId, authentication)")
    public ResponseEntity<List<OrderDTO>> getOrdersByClientId(@PathVariable Long clientId) {
        List<OrderDTO> orders = orderService.getOrdersByClientId(clientId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/affiliate/{affiliateId}")
    @Operation(summary = "Listar pedidos por afiliado", description = "Retorna uma lista de pedidos que contêm produtos de um afiliado específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AFFILIATE') and @securityService.isOwnerOrAdmin(#affiliateId, authentication)")
    public ResponseEntity<List<OrderDTO>> getOrdersByAffiliateId(@PathVariable Long affiliateId) {
        List<OrderDTO> orders = orderService.getOrdersByAffiliateId(affiliateId);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/client/{clientId}")
    @Operation(summary = "Criar pedido", description = "Cria um novo pedido para um cliente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isOwnerOrAdmin(#clientId, authentication)")
    public ResponseEntity<OrderDTO> createOrderForClient(@PathVariable Long clientId) {
        OrderDTO order = orderService.createOrderForClient(clientId);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{orderId}/products")
    @Operation(summary = "Adicionar produto ao pedido", description = "Adiciona um produto ao pedido")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isOrderOwner(#orderId, authentication)")
    public ResponseEntity<OrderDTO> addProductToOrder(
            @PathVariable Long orderId,
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam String paymentType) {
        OrderDTO updatedOrder = orderService.addProductToOrder(orderId, productId, quantity, paymentType);
        return ResponseEntity.ok(updatedOrder);
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Atualizar status do pedido", description = "Atualiza o status de um pedido")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AFFILIATE') and @securityService.isOrderContainsAffiliateProdut(#orderId, authentication)")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String newStatus) {
        OrderDTO updatedOrder = orderService.updateOrderStatus(orderId, newStatus);
        return ResponseEntity.ok(updatedOrder);
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Excluir pedido", description = "Exclui um pedido pelo ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isOrderOwner(#orderId, authentication)")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}