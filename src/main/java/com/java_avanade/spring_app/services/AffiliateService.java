package com.java_avanade.spring_app.services;

import com.java_avanade.spring_app.dtos.AffiliateDTO;
import com.java_avanade.spring_app.exceptions.ResourceNotFoundException;
import com.java_avanade.spring_app.models.Affiliate;
import com.java_avanade.spring_app.models.Product;
import com.java_avanade.spring_app.repositories.AffiliateRepository;
import com.java_avanade.spring_app.repositories.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AffiliateService {

    @Autowired
    private AffiliateRepository affiliateRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<AffiliateDTO> getAllAffiliates() {
        return affiliateRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AffiliateDTO getAffiliateById(Long id) {
        Affiliate affiliate = affiliateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Afiliado", "id", id));
        return convertToDTO(affiliate);
    }

    // Método atualizado para usar adminId em vez de userId
    public AffiliateDTO getAffiliateByUserId(Long userId) {
        // Na nova estrutura, usamos adminId em vez de userId
        List<Affiliate> affiliates = affiliateRepository.findByAdminId(userId);
        if (affiliates.isEmpty()) {
            throw new ResourceNotFoundException("Afiliado", "adminId", userId);
        }
        return convertToDTO(affiliates.get(0));
    }

    public AffiliateDTO updateAffiliate(Long id, Affiliate affiliateDetails) {
        Affiliate affiliate = affiliateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Afiliado", "id", id));

        affiliate.setName(affiliateDetails.getName());
        if (affiliateDetails.getEmail() != null && !affiliateDetails.getEmail().isEmpty()) {
            affiliate.setEmail(affiliateDetails.getEmail());
        }

        Affiliate updatedAffiliate = affiliateRepository.save(affiliate);
        return convertToDTO(updatedAffiliate);
    }

    public void deleteAffiliate(Long id) {
        Affiliate affiliate = affiliateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Afiliado", "id", id));

        affiliateRepository.delete(affiliate);
    }

    public List<Product> getAffiliateProducts(Long affiliateId) {
        Affiliate affiliate = affiliateRepository.findById(affiliateId)
                .orElseThrow(() -> new ResourceNotFoundException("Afiliado", "id", affiliateId));

        return productRepository.findByAffiliateId(affiliate.getId());
    }

    public AffiliateDTO convertToDTO(Affiliate affiliate) {
        AffiliateDTO dto = new AffiliateDTO();
        dto.setId(affiliate.getId());
        dto.setName(affiliate.getName());
        dto.setEmail(affiliate.getEmail());

        // Obter IDs dos produtos
        List<Long> productIds = affiliate.getProducts().stream()
                .map(Product::getProductCode)
                .collect(Collectors.toList());
        dto.setProductIds(productIds);

        return dto;
    }
}