package com.example.hw_cub.stock.domain.model

import java.math.BigDecimal
import java.time.LocalDate

/**
 * One listed security's trading summary for a single day.
 *
 * Numeric fields are `null` when the security did not trade that day or the
 * source value was malformed; they are never defaulted to zero.
 */
data class StockDay(
    val code: String,
    val name: String,
    val date: LocalDate?,
    val tradeVolume: Long?,
    val tradeValue: Long?,
    val openingPrice: BigDecimal?,
    val highestPrice: BigDecimal?,
    val lowestPrice: BigDecimal?,
    val closingPrice: BigDecimal?,
    val change: BigDecimal?,
    val monthlyAveragePrice: BigDecimal?,
    val transactions: Long?,
)
