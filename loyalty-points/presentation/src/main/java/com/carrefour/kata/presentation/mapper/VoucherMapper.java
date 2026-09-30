package com.carrefour.kata.presentation.mapper;

import com.carrefour.kata.application.usecase.issuevoucher.IssueVoucherResponse;
import com.carrefour.kata.presentation.dto.VoucherResponse;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct : réponses applicatives -> DTOs de présentation pour les opérations sur les bons d'achat.
 */
@Mapper(componentModel = "spring")
public interface VoucherMapper {

    VoucherResponse toVoucherResponse(IssueVoucherResponse response);
}
