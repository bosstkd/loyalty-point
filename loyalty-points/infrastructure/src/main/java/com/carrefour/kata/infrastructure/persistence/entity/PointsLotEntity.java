package com.carrefour.kata.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "points_lot")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PointsLotEntity {

    @Id
    private String id;

    @Column(name = "account_customer_id")
    private String accountCustomerId;

    private int remaining;

    private LocalDate earnedAt;

    private LocalDate expiresAt;
}
