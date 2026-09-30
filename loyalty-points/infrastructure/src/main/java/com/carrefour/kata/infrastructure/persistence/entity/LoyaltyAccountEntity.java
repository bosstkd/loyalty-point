package com.carrefour.kata.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "loyalty_account")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LoyaltyAccountEntity {

    @Id
    private String customerId;

    @OneToMany(mappedBy = "accountCustomerId", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PointsLotEntity> lots = new ArrayList<>();
}
