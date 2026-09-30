package com.carrefour.kata.domain.voucher.exception;

import com.carrefour.kata.domain.shared.DomainException;
import com.carrefour.kata.domain.voucher.vo.VoucherId;
import com.carrefour.kata.domain.voucher.vo.VoucherStatus;

/**
 * Levée lorsqu'une transition de cycle de vie n'est pas autorisée pour le
 * statut actuel du bon d'achat (ex. : utiliser un bon USED ou EXPIRED).
 */
public class VoucherNotUsableException extends DomainException {

    public VoucherNotUsableException(VoucherId id, VoucherStatus status) {
        super("Voucher %s cannot be used: status is %s".formatted(id.value(), status));
    }
}
