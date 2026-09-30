package com.carrefour.kata.infrastructure.persistence.jpa;

import com.carrefour.kata.infrastructure.persistence.entity.VoucherEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VoucherJpaRepository extends JpaRepository<VoucherEntity, String> {

    List<VoucherEntity> findByCustomerId(String customerId);
}
