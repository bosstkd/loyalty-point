package com.carrefour.kata.presentation.dto;

import java.util.List;

/**
 * DTO de réponse pour le point d'entrée de consultation du solde.
 */
public record BalanceResponse(String customerId, int balance, List<LotResponse> lots) {
}
