package com.example.hw_cub.stock.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One row of TWSE `/exchangeReport/STOCK_DAY_ALL`.
 *
 * TWSE sends every value as a string, uses `""` for "no trade", and dates in
 * the ROC calendar (`1151016` is 2026-10-16). Fields are nullable so one
 * missing key degrades a row instead of failing the whole response.
 */
@Serializable
data class StockDayDto(
    @SerialName("Date") val date: String? = null,
    @SerialName("Code") val code: String? = null,
    @SerialName("Name") val name: String? = null,
    @SerialName("TradeVolume") val tradeVolume: String? = null,
    @SerialName("TradeValue") val tradeValue: String? = null,
    @SerialName("OpeningPrice") val openingPrice: String? = null,
    @SerialName("HighestPrice") val highestPrice: String? = null,
    @SerialName("LowestPrice") val lowestPrice: String? = null,
    @SerialName("ClosingPrice") val closingPrice: String? = null,
    @SerialName("Change") val change: String? = null,
    @SerialName("Transaction") val transaction: String? = null,
)
