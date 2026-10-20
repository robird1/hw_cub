package com.example.hw_cub.stock.domain.model

/** A [StockDay] with the given [code] and [name] and every other field empty. */
fun stock(code: String, name: String) = StockDay(
    code = code,
    name = name,
    date = null,
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
