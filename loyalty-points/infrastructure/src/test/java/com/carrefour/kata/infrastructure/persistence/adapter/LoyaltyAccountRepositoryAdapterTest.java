package com.carrefour.kata.infrastructure.persistence.adapter;

import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.PointsLot;
import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.shared.Points;
import com.carrefour.kata.infrastructure.persistence.jpa.LoyaltyAccountJpaRepository;
import com.carrefour.kata.infrastructure.persistence.mapper.LoyaltyAccountPersistenceMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(LoyaltyAccountRepositoryAdapterTest.AdapterConfig.class)
class LoyaltyAccountRepositoryAdapterTest {

    @Configuration
    @AutoConfigurationPackage(basePackages = "com.carrefour.kata.infrastructure")
    static class AdapterConfig {
        @Bean
        LoyaltyAccountPersistenceMapper loyaltyAccountPersistenceMapper() {
            return Mappers.getMapper(LoyaltyAccountPersistenceMapper.class);
        }

        @Bean
        LoyaltyAccountRepositoryAdapter loyaltyAccountRepositoryAdapter(
                LoyaltyAccountJpaRepository jpa,
                LoyaltyAccountPersistenceMapper mapper) {
            return new LoyaltyAccountRepositoryAdapter(jpa, mapper);
        }
    }

    @Autowired
    private LoyaltyAccountRepositoryAdapter adapter;

    @Test
    void save_and_findByCustomerId_should_persist_account_with_lots() {
        // Given
        CustomerId customerId = CustomerId.of("customer-42");
        LoyaltyAccount account = LoyaltyAccount.openFor(customerId);

        LocalDate earnedAt1 = LocalDate.of(2026, 1, 1);
        LocalDate expiresAt1 = LocalDate.of(2027, 1, 1);
        LocalDate earnedAt2 = LocalDate.of(2026, 3, 15);
        LocalDate expiresAt2 = LocalDate.of(2027, 3, 15);

        account.earn(Points.of(100), earnedAt1, expiresAt1);
        account.earn(Points.of(200), earnedAt2, expiresAt2);

        // When
        adapter.save(account);
        Optional<LoyaltyAccount> found = adapter.findByCustomerId(customerId);

        // Then
        assertThat(found).isPresent();
        LoyaltyAccount restored = found.get();
        assertThat(restored.customerId()).isEqualTo(customerId);
        assertThat(restored.balance(LocalDate.of(2026, 6, 1))).isEqualTo(Points.of(300));

        // FIFO order preserved: earliest earnedAt first
        List<PointsLot> lots = restored.lots();
        assertThat(lots).hasSize(2);
        assertThat(lots.get(0).earnedAt()).isEqualTo(earnedAt1);
        assertThat(lots.get(0).remaining()).isEqualTo(Points.of(100));
        assertThat(lots.get(1).earnedAt()).isEqualTo(earnedAt2);
        assertThat(lots.get(1).remaining()).isEqualTo(Points.of(200));
    }

    @Test
    void findByCustomerId_should_return_empty_when_account_does_not_exist() {
        Optional<LoyaltyAccount> found = adapter.findByCustomerId(CustomerId.of("unknown"));
        assertThat(found).isEmpty();
    }
}
