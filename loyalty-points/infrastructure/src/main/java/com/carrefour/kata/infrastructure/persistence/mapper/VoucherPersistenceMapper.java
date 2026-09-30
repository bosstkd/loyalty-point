package com.carrefour.kata.infrastructure.persistence.mapper;

import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.shared.Points;
import com.carrefour.kata.domain.voucher.Voucher;
import com.carrefour.kata.domain.voucher.vo.VoucherId;
import com.carrefour.kata.domain.voucher.vo.VoucherStatus;
import com.carrefour.kata.infrastructure.persistence.entity.VoucherEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VoucherPersistenceMapper {

    @Mapping(target = "id", expression = "java(voucher.id().value())")
    @Mapping(target = "customerId", expression = "java(voucher.customerId().value())")
    @Mapping(target = "value", expression = "java(voucher.value().value())")
    @Mapping(target = "status", expression = "java(voucher.status().name())")
    @Mapping(target = "issuedAt", expression = "java(voucher.issuedAt())")
    @Mapping(target = "expiresAt", expression = "java(voucher.expiresAt())")
    VoucherEntity toEntity(Voucher voucher);

    default Voucher toDomain(VoucherEntity entity) {
        return Voucher.restore(
                VoucherId.of(entity.getId()),
                CustomerId.of(entity.getCustomerId()),
                Points.of(entity.getValue()),
                entity.getIssuedAt(),
                entity.getExpiresAt(),
                VoucherStatus.valueOf(entity.getStatus())
        );
    }
}
