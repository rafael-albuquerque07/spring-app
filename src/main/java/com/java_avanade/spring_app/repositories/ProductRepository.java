package com.java_avanade.spring_app.repositories;

import com.java_avanade.spring_app.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByAffiliateId(Long affiliateId);

    @Query("SELECT p FROM Product p WHERE " +
            "p.name LIKE %:keyword% OR " +
            "p.description LIKE %:keyword% OR " +
            "p.productType LIKE %:keyword%")
    List<Product> searchByKeyword(String keyword);

    List<Product> findByProductType(String productType);
}