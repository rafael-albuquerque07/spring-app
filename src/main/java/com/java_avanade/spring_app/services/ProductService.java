package com.java_avanade.spring_app.services;

import com.java_avanade.spring_app.dtos.ProductDTO;
import com.java_avanade.spring_app.exceptions.ResourceNotFoundException;
import com.java_avanade.spring_app.models.Affiliate;
import com.java_avanade.spring_app.models.Product;
import com.java_avanade.spring_app.models.Stock;
import com.java_avanade.spring_app.repositories.AffiliateRepository;
import com.java_avanade.spring_app.repositories.ProductRepository;
import com.java_avanade.spring_app.repositories.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AffiliateRepository affiliateRepository;

    @Autowired
    private StockRepository stockRepository;

    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));
        return convertToDTO(product);
    }

    public Product getProductEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));
    }

    public List<ProductDTO> getProductsByAffiliateId(Long affiliateId) {
        return productRepository.findByAffiliateId(affiliateId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> searchProducts(String keyword) {
        return productRepository.searchByKeyword(keyword).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsByType(String productType) {
        return productRepository.findByProductType(productType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductDTO createProduct(Product product, Long affiliateId, Integer initialStock) {
        Affiliate affiliate = affiliateRepository.findById(affiliateId)
                .orElseThrow(() -> new ResourceNotFoundException("Afiliado", "id", affiliateId));

        product.setAffiliate(affiliate);
        Product savedProduct = productRepository.save(product);

        // Criar estoque inicial se fornecido
        if (initialStock != null && initialStock > 0) {
            Stock stock = new Stock();
            stock.setProduct(savedProduct);
            stock.setQuantity(initialStock);
            stockRepository.save(stock);
        }

        return convertToDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, Product productDetails) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));

        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setProductType(productDetails.getProductType());
        product.setProductChoice(productDetails.getProductChoice());
        product.setImageUrl(productDetails.getImageUrl());

        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", id));

        // Remover relacionamentos com estoque
        stockRepository.findByProductId(id).forEach(stock -> {
            stockRepository.delete(stock);
        });

        productRepository.delete(product);
    }

    public ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductCode(product.getProductCode());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setProductType(product.getProductType());
        dto.setProductChoice(product.getProductChoice());
        dto.setImageUrl(product.getImageUrl());

        // Dados do afiliado
        if (product.getAffiliate() != null) {
            dto.setAffiliateId(product.getAffiliate().getId());
            dto.setAffiliateName(product.getAffiliate().getName());
        }

        // Estoque disponível
        Integer availableStock = stockRepository.getTotalQuantityByProductId(product.getProductCode());
        dto.setAvailableStock(availableStock != null ? availableStock : 0);

        return dto;
    }
}