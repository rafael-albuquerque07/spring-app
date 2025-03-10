package com.java_avanade.spring_app.services;

import com.java_avanade.spring_app.dtos.ClientDTO;
import com.java_avanade.spring_app.exceptions.ResourceNotFoundException;
import com.java_avanade.spring_app.models.Client;
import com.java_avanade.spring_app.models.Order;
import com.java_avanade.spring_app.repositories.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public List<ClientDTO> getAllClients() {
        return clientRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ClientDTO getClientById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        return convertToDTO(client);
    }

    // Método atualizado para usar adminId em vez de userId
    public ClientDTO getClientByAdminId(Long adminId) {
        List<Client> clients = clientRepository.findByAdminId(adminId);
        if (clients.isEmpty()) {
            throw new ResourceNotFoundException("Cliente", "adminId", adminId);
        }
        return convertToDTO(clients.get(0));
    }

    // Método atualizado para buscar por username
    public ClientDTO getClientByUsername(String username) {
        Client client = clientRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "username", username));
        return convertToDTO(client);
    }

    // Método mantido para compatibilidade, mas reimplementado para usar adminId
    public ClientDTO getClientByUserId(Long userId) {
        // Assumindo que userId agora se refere ao adminId
        return getClientByAdminId(userId);
    }

    public Client getClientEntityById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
    }

    public ClientDTO updateClient(Long id, Client clientDetails) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        client.setName(clientDetails.getName());
        if (clientDetails.getEmail() != null && !clientDetails.getEmail().isEmpty()) {
            client.setEmail(clientDetails.getEmail());
        }

        // Atualizar campos de autenticação se fornecidos
        if (clientDetails.getUsername() != null && !clientDetails.getUsername().isEmpty()) {
            client.setUsername(clientDetails.getUsername());
        }

        if (clientDetails.getPassword() != null && !clientDetails.getPassword().isEmpty()) {
            client.setPassword(clientDetails.getPassword());
        }

        Client updatedClient = clientRepository.save(client);
        return convertToDTO(updatedClient);
    }

    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        clientRepository.delete(client);
    }

    public ClientDTO convertToDTO(Client client) {
        ClientDTO dto = new ClientDTO();
        dto.setId(client.getId());
        dto.setName(client.getName());
        dto.setEmail(client.getEmail());

        // Obter IDs dos pedidos
        List<Long> orderIds = client.getOrders().stream()
                .map(Order::getOrderId)
                .collect(Collectors.toList());
        dto.setOrderIds(orderIds);

        return dto;
    }
}