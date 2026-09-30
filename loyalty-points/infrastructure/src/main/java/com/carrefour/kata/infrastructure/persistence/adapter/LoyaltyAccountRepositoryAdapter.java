package com.carrefour.kata.infrastructure.persistence.adapter;

import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.repository.LoyaltyAccountRepository;
import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.infrastructure.persistence.entity.LoyaltyAccountEntity;
import com.carrefour.kata.infrastructure.persistence.entity.PointsLotEntity;
import com.carrefour.kata.infrastructure.persistence.jpa.LoyaltyAccountJpaRepository;
import com.carrefour.kata.infrastructure.persistence.mapper.LoyaltyAccountPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LoyaltyAccountRepositoryAdapter implements LoyaltyAccountRepository {

    private final LoyaltyAccountJpaRepository jpa;
    private final LoyaltyAccountPersistenceMapper mapper;

    @Override
    public Optional<LoyaltyAccount> findByCustomerId(CustomerId customerId) {
        return jpa.findById(customerId.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<CustomerId> findAllCustomerIds() {
        return jpa.findAllCustomerIds().stream()
                .map(CustomerId::of)
                .toList();
    }

    @Override
    public void save(LoyaltyAccount account) {
        LoyaltyAccountEntity entity = mapper.toEntity(account);
        List<PointsLotEntity> lots = account.lots().stream()
                .map(lot -> mapper.toLotEntity(lot, account.customerId().value()))
                .toList();
        entity.setLots(lots);
        jpa.save(entity);
    }
}
