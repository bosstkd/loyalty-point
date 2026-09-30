package com.carrefour.kata.infrastructure.persistence.jpa;

import com.carrefour.kata.infrastructure.persistence.entity.LoyaltyAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LoyaltyAccountJpaRepository extends JpaRepository<LoyaltyAccountEntity, String> {

    /** Projection : charge uniquement la colonne clé primaire — évite de charger tous les lots pour l'itération batch. */
    @Query("SELECT a.customerId FROM LoyaltyAccountEntity a")
    List<String> findAllCustomerIds();
}
