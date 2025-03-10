package com.java_avanade.spring_app.repositories;

import com.java_avanade.spring_app.models.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    @Query("SELECT s FROM Stock s WHERE s.product.productCode = :productId")
    List<Stock> findByProductId(@Param("productId") Long productId);

    @Query("SELECT s FROM Stock s WHERE s.product.productCode = :productId")
    Optional<Stock> findStockByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(s.quantity) FROM Stock s WHERE s.product.productCode = :productId")
    Integer getTotalQuantityByProductId(@Param("productId") Long productId);
}