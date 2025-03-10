package com.java_avanade.spring_app.repositories;

import com.java_avanade.spring_app.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByClientId(Long clientId);

    @Query("SELECT o FROM Order o WHERE o.client.id = :clientId AND o.status = :status")
    List<Order> findOrderListByClientIdAndStatus(Long clientId, String status);

    @Query("SELECT o FROM Order o JOIN o.cartItems c WHERE c.product.affiliate.id = :affiliateId")
    List<Order> findOrdersByAffiliateId(Long affiliateId);

    Optional<Order> findByClientIdAndStatus(Long clientId, String status);
}