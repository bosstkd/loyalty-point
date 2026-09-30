package com.carrefour.kata.domain.voucher;

import com.carrefour.kata.domain.account.LoyaltyAccount;
import com.carrefour.kata.domain.account.exception.InsufficientPointsException;
import com.carrefour.kata.domain.account.vo.SpendingType;
import com.carrefour.kata.domain.shared.DomainService;
import com.carrefour.kata.domain.shared.Points;

import java.time.LocalDate;

/**
 * Service de domaine qui coordonne l'émission de bons d'achat entre deux agrégats :
 * {@link LoyaltyAccount} (dépense de points) et {@link Voucher} (création).
 *
 * <p>Règle métier : un bon d'achat est valable exactement un an à compter de son émission.</p>
 *
 * <p>Sans état — pas de dépendances de constructeur. La persistance des deux agrégats
 * modifiés est déléguée au use case appelant dans la couche applicative.</p>
 */
@DomainService
public final class VoucherIssuanceService {

    /**
     * Émet un bon d'achat en dépensant {@code points} depuis le compte donné.
     *
     * @param account le compte de fidélité à débiter
     * @param points  le nombre de points à convertir
     * @param today   date de référence (déterministe, injectée par le use case)
     * @return le bon d'achat nouvellement émis (statut ISSUED, valable jusqu'à aujourd'hui + 1 an)
     * @throws InsufficientPointsException si le solde du compte est insuffisant
     */
    public Voucher issueVoucher(LoyaltyAccount account, Points points, LocalDate today) {
        account.spend(points, SpendingType.VOUCHER, today);
        return Voucher.issue(account.customerId(), points, today, today.plusYears(1));
    }
}
