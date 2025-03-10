package com.java_avanade.spring_app.controllers;

import com.java_avanade.spring_app.dtos.ClientDTO;
import com.java_avanade.spring_app.models.Client;
import com.java_avanade.spring_app.services.ClientService;
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
@RequestMapping("/clients")
@Tag(name = "Clientes", description = "Endpoints para gerenciamento de clientes")
@SecurityRequirement(name = "Bearer Authentication")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping
    @Operation(summary = "Listar todos os clientes", description = "Retorna uma lista de todos os clientes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ClientDTO>> getAllClients() {
        List<ClientDTO> clients = clientService.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter cliente por ID", description = "Retorna os detalhes de um cliente pelo ID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isOwnerOrAdmin(#id, authentication)")
    public ResponseEntity<ClientDTO> getClientById(@PathVariable Long id) {
        ClientDTO client = clientService.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obter cliente por ID de usuário", description = "Retorna os detalhes de um cliente pelo ID do usuário")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isUserIdMatch(#userId, authentication)")
    public ResponseEntity<ClientDTO> getClientByUserId(@PathVariable Long userId) {
        ClientDTO client = clientService.getClientByUserId(userId);
        return ResponseEntity.ok(client);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza os dados de um cliente existente")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CLIENT') and @securityService.isOwnerOrAdmin(#id, authentication)")
    public ResponseEntity<ClientDTO> updateClient(@PathVariable Long id, @Valid @RequestBody Client clientDetails) {
        ClientDTO updatedClient = clientService.updateClient(id, clientDetails);
        return ResponseEntity.ok(updatedClient);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cliente", description = "Exclui um cliente pelo ID")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}