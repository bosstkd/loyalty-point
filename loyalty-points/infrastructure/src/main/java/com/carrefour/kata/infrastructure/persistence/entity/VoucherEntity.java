package com.carrefour.kata.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "voucher")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class VoucherEntity {

    @Id
    private String id;

    private String customerId;

    @Column(name = "points_value")
    private int value;

    private LocalDate issuedAt;

    private LocalDate expiresAt;

    private String status;
}
