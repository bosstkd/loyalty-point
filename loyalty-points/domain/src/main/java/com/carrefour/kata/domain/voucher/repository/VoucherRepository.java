package com.carrefour.kata.domain.voucher.repository;

import com.carrefour.kata.domain.account.vo.CustomerId;
import com.carrefour.kata.domain.voucher.Voucher;
import com.carrefour.kata.domain.voucher.vo.VoucherId;

import java.util.List;
import java.util.Optional;

/**
 * Port défini par le domaine, implémenté dans la couche infrastructure.
 */
public interface VoucherRepository {

    Optional<Voucher> findById(VoucherId id);

    List<Voucher> findByCustomerId(CustomerId customerId);

    void save(Voucher voucher);
}
