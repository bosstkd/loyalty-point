package com.carrefour.kata.domain.account.repository;

import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.vo.CustomerId;

import java.util.List;
import java.util.Optional;

/**
 * Port défini par le domaine, implémenté dans la couche infrastructure.
 */
public interface LoyaltyAccountRepository {

    Optional<LoyaltyAccount> findByCustomerId(CustomerId customerId);

    /** Retourne les identifiants de tous les comptes de fidélité existants. Utilisé par les traitements batch. */
    List<CustomerId> findAllCustomerIds();

    void save(LoyaltyAccount account);
}
