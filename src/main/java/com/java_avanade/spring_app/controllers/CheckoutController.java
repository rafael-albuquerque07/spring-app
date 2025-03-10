package com.java_avanade.spring_app.controllers;

import com.java_avanade.spring_app.dtos.CheckoutDTO;
import com.java_avanade.spring_app.services.CheckoutService;
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
@RequestMapping("/checkouts")
@Tag(name = "Checkouts", description = "Endpoints para gerenciamento de checkouts")
@SecurityRequirement(name = "Bearer Authentication")
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    @GetMapping
    @Operation(summary = "Listar todos os checkouts", description = "Retorna uma lista de todos os checkouts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CheckoutDTO>> getAllCheckouts() {
        List<CheckoutDTO> checkouts = checkoutService.getAllCheckouts();
        return ResponseEntity.ok(checkouts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter checkout por ID", description = "Retorna os detalhes de um checkout pelo ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isCheckoutOwner(#id, authentication) or hasRole('AFFILIATE') and @securityService.isCheckoutContainsAffiliateProduct(#id, authentication)")
    public ResponseEntity<CheckoutDTO> getCheckoutById(@PathVariable Long id) {
        CheckoutDTO checkout = checkoutService.getCheckoutById(id);
        return ResponseEntity.ok(checkout);
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Obter checkout por ID de pedido", description = "Retorna os detalhes de um checkout pelo ID do pedido")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isOrderOwner(#orderId, authentication) or hasRole('AFFILIATE') and @securityService.isOrderContainsAffiliateProdut(#orderId, authentication)")
    public ResponseEntity<CheckoutDTO> getCheckoutByOrderId(@PathVariable Long orderId) {
        CheckoutDTO checkout = checkoutService.getCheckoutByOrderId(orderId);
        return ResponseEntity.ok(checkout);
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Listar checkouts por cliente", description = "Retorna uma lista de checkouts de um cliente específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isOwnerOrAdmin(#clientId, authentication)")
    public ResponseEntity<List<CheckoutDTO>> getCheckoutsByClientId(@PathVariable Long clientId) {
        List<CheckoutDTO> checkouts = checkoutService.getCheckoutsByClientId(clientId);
        return ResponseEntity.ok(checkouts);
    }

    @GetMapping("/affiliate/{affiliateId}")
    @Operation(summary = "Listar checkouts por afiliado", description = "Retorna uma lista de checkouts que contêm produtos de um afiliado específico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AFFILIATE') and @securityService.isOwnerOrAdmin(#affiliateId, authentication)")
    public ResponseEntity<List<CheckoutDTO>> getCheckoutsByAffiliateId(@PathVariable Long affiliateId) {
        List<CheckoutDTO> checkouts = checkoutService.getCheckoutsByAffiliateId(affiliateId);
        return ResponseEntity.ok(checkouts);
    }

    @PostMapping("/process")
    @Operation(summary = "Processar checkout", description = "Processa o checkout de um pedido")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isOrderOwner(#checkoutRequest.orderId, authentication)")
    public ResponseEntity<CheckoutDTO> processCheckout(@Valid @RequestBody CheckoutDTO.CheckoutRequest checkoutRequest) {
        CheckoutDTO checkout = checkoutService.processCheckout(checkoutRequest);
        return ResponseEntity.ok(checkout);
    }

    @PutMapping("/{checkoutId}/payment")
    @Operation(summary = "Atualizar status de pagamento", description = "Atualiza o status de pagamento de um checkout")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AFFILIATE') and @securityService.isCheckoutContainsAffiliateProduct(#checkoutId, authentication)")
    public ResponseEntity<CheckoutDTO> updatePaymentStatus(
            @PathVariable Long checkoutId,
            @RequestParam String paymentStatus) {
        CheckoutDTO updatedCheckout = checkoutService.updatePaymentStatus(checkoutId, paymentStatus);
        return ResponseEntity.ok(updatedCheckout);
    }

    @PutMapping("/{checkoutId}/address")
    @Operation(summary = "Atualizar endereço de entrega", description = "Atualiza o endereço de entrega de um checkout")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isCheckoutOwner(#checkoutId, authentication)")
    public ResponseEntity<CheckoutDTO> updateShippingAddress(
            @PathVariable Long checkoutId,
            @RequestParam String newShippingAddress) {
        CheckoutDTO updatedCheckout = checkoutService.updateShippingAddress(checkoutId, newShippingAddress);
        return ResponseEntity.ok(updatedCheckout);
    }
}