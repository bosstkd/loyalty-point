package com.carrefour.kata.infrastructure.persistence.mapper;

import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.PointsLot;
import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.account.vo.LotId;
import com.carrefour.kata.domain.shared.Points;
import com.carrefour.kata.infrastructure.persistence.entity.LoyaltyAccountEntity;
import com.carrefour.kata.infrastructure.persistence.entity.PointsLotEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LoyaltyAccountPersistenceMapper {

    @Mapping(target = "customerId", expression = "java(account.customerId().value())")
    @Mapping(target = "lots", ignore = true)
    LoyaltyAccountEntity toEntity(LoyaltyAccount account);

    default PointsLotEntity toLotEntity(PointsLot lot, String accountCustomerId) {
        return new PointsLotEntity(
                lot.id().value(), accountCustomerId,
                lot.remaining().value(), lot.earnedAt(), lot.expiresAt()
        );
    }

    default LoyaltyAccount toDomain(LoyaltyAccountEntity entity) {
        List<PointsLot> lots = entity.getLots().stream()
                .map(l -> PointsLot.restore(
                        LotId.of(l.getId()), Points.of(l.getRemaining()),
                        l.getEarnedAt(), l.getExpiresAt()))
                .toList();
        return LoyaltyAccount.restore(CustomerId.of(entity.getCustomerId()), lots);
    }
}
