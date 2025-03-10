package com.java_avanade.spring_app.repositories;

import com.java_avanade.spring_app.models.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    @Query("SELECT c FROM Cart c WHERE c.order.orderId = :orderId")
    List<Cart> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT c FROM Cart c WHERE c.order.client.id = :clientId")
    List<Cart> findCartItemsByClientId(@Param("clientId") Long clientId);

    @Query("DELETE FROM Cart c WHERE c.order.orderId = :orderId")
    void deleteByOrderId(@Param("orderId") Long orderId);
}