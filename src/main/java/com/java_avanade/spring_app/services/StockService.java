package com.java_avanade.spring_app.services;

import com.java_avanade.spring_app.dtos.StockDTO;
import com.java_avanade.spring_app.exceptions.InsufficientStockException;
import com.java_avanade.spring_app.exceptions.ResourceNotFoundException;
import com.java_avanade.spring_app.models.Product;
import com.java_avanade.spring_app.models.Stock;
import com.java_avanade.spring_app.repositories.ProductRepository;
import com.java_avanade.spring_app.repositories.StockRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<StockDTO> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public StockDTO getStockById(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque", "id", id));
        return convertToDTO(stock);
    }

    public List<StockDTO> getStocksByProductId(Long productId) {
        return stockRepository.findByProductId(productId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Integer getTotalStockQuantityByProductId(Long productId) {
        Integer totalQuantity = stockRepository.getTotalQuantityByProductId(productId);
        return totalQuantity != null ? totalQuantity : 0;
    }

    @Transactional
    public StockDTO addStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto", "id", productId));

        Optional<Stock> existingStock = stockRepository.findStockByProductId(productId);

        if (existingStock.isPresent()) {
            Stock stock = existingStock.get();
            stock.setQuantity(stock.getQuantity() + quantity);
            Stock updatedStock = stockRepository.save(stock);
            return convertToDTO(updatedStock);
        } else {
            Stock newStock = new Stock();
            newStock.setProduct(product);
            newStock.setQuantity(quantity);
            Stock savedStock = stockRepository.save(newStock);
            return convertToDTO(savedStock);
        }
    }

    @Transactional
    public StockDTO updateStock(Long stockId, Integer newQuantity) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque", "id", stockId));

        stock.setQuantity(newQuantity);
        Stock updatedStock = stockRepository.save(stock);
        return convertToDTO(updatedStock);
    }

    @Transactional
    public void removeStock(Long stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Estoque", "id", stockId));

        stockRepository.delete(stock);
    }

    @Transactional
    public void checkAndReduceStock(Long productId, Integer requestedQuantity) {
        Integer availableQuantity = getTotalStockQuantityByProductId(productId);

        if (availableQuantity < requestedQuantity) {
            throw new InsufficientStockException(productId, requestedQuantity, availableQuantity);
        }

        Optional<Stock> existingStock = stockRepository.findStockByProductId(productId);
        if (existingStock.isPresent()) {
            Stock stock = existingStock.get();
            stock.setQuantity(stock.getQuantity() - requestedQuantity);
            stockRepository.save(stock);
        } else {
            throw new ResourceNotFoundException("Estoque não encontrado para o produto ID: " + productId);
        }
    }

    public StockDTO convertToDTO(Stock stock) {
        StockDTO dto = new StockDTO();
        dto.setId(stock.getId());
        dto.setProductId(stock.getProduct().getProductCode());
        dto.setProductName(stock.getProduct().getName());
        dto.setQuantity(stock.getQuantity());
        return dto;
    }
}