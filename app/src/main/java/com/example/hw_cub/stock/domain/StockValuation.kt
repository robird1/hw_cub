package com.example.hw_cub.stock.domain

import java.math.BigDecimal
import java.time.LocalDate

/**
 * Valuation ratios for one listed company on a single day.
 *
 * A ratio is `null` when TWSE does not publish it, e.g. no P/E ratio for a
 * loss-making company.
 */
data class StockValuation(
    val code: String,
    val name: String,
    val date: LocalDate?,
    val peRatio: BigDecimal?,
    val dividendYieldPercent: BigDecimal?,
    val pbRatio: BigDecimal?,
)
