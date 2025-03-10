package com.java_avanade.spring_app.repositories;

import com.java_avanade.spring_app.models.Checkout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CheckoutRepository extends JpaRepository<Checkout, Long> {
    @Query("SELECT c FROM Checkout c WHERE c.order.orderId = :orderId")
    Optional<Checkout> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT c FROM Checkout c WHERE c.order.client.id = :clientId")
    List<Checkout> findCheckoutsByClientId(@Param("clientId") Long clientId);

    @Query("SELECT c FROM Checkout c WHERE c.product.affiliate.id = :affiliateId")
    List<Checkout> findCheckoutsByAffiliateId(@Param("affiliateId") Long affiliateId);
}