package com.carrefour.kata.presentation.mapper;

import com.carrefour.kata.application.usecase.earnpoints.EarnPointsResponse;
import com.carrefour.kata.application.usecase.getbalance.GetBalanceResponse;
import com.carrefour.kata.application.usecase.spendpoints.SpendPointsResponse;
import com.carrefour.kata.presentation.dto.BalanceResponse;
import com.carrefour.kata.presentation.dto.LotResponse;
import org.mapstruct.Mapper;

/**
 * Mapper MapStruct : réponses applicatives -> DTOs de présentation pour les opérations sur les comptes.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    BalanceResponse toBalanceResponse(GetBalanceResponse response);

    LotResponse toLotResponse(GetBalanceResponse.LotSummary lotSummary);

    com.carrefour.kata.presentation.dto.EarnPointsResponse toEarnPointsResponse(EarnPointsResponse response);

    com.carrefour.kata.presentation.dto.SpendPointsResponse toSpendPointsResponse(SpendPointsResponse response);
}
