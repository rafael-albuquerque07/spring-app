package com.java_avanade.spring_app.repositories;

import com.java_avanade.spring_app.models.Affiliate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AffiliateRepository extends JpaRepository<Affiliate, Long> {
    Optional<Affiliate> findByEmail(String email);
    Optional<Affiliate> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByIdAndAdminId(Long id, Long adminId);
    List<Affiliate> findByAdminId(Long adminId);
}