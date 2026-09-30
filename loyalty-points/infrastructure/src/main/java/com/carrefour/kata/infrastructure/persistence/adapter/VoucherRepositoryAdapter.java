package com.carrefour.kata.infrastructure.persistence.adapter;

import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.voucher.Voucher;
import com.carrefour.kata.domain.voucher.repository.VoucherRepository;
import com.carrefour.kata.domain.voucher.vo.VoucherId;
import com.carrefour.kata.infrastructure.persistence.jpa.VoucherJpaRepository;
import com.carrefour.kata.infrastructure.persistence.mapper.VoucherPersistenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VoucherRepositoryAdapter implements VoucherRepository {

    private final VoucherJpaRepository jpa;
    private final VoucherPersistenceMapper mapper;

    @Override
    public Optional<Voucher> findById(VoucherId id) {
        return jpa.findById(id.value())
                .map(mapper::toDomain);
    }

    @Override
    public List<Voucher> findByCustomerId(CustomerId customerId) {
        return jpa.findByCustomerId(customerId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void save(Voucher voucher) {
        jpa.save(mapper.toEntity(voucher));
    }
}
