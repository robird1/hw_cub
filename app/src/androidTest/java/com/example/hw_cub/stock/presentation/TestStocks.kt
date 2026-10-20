package com.example.hw_cub.stock.presentation

import com.example.hw_cub.stock.domain.model.StockDay
import java.time.LocalDate

/** A [StockDay] with the given [code] and [name] and no trading figures. */
internal fun stock(code: String, name: String) = StockDay(
    code = code,
    name = name,
    date = LocalDate.of(2026, 10, 16),
    tradeVolume = null,
    tradeValue = null,
    openingPrice = null,
    highestPrice = null,
    lowestPrice = null,
    closingPrice = null,
    change = null,
    monthlyAveragePrice = null,
    transactions = null,
)
