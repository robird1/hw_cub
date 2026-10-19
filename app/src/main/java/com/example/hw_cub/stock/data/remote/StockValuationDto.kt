package com.example.hw_cub.stock.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One row of TWSE `/exchangeReport/BWIBBU_ALL`: P/E ratio, dividend yield
 * and P/B ratio. Only listed companies appear (no ETFs), and `PEratio` is
 * `""` when the ratio is not meaningful, e.g. for loss-making companies.
 */
@Serializable
data class StockValuationDto(
    @SerialName("Date") val date: String? = null,
    @SerialName("Code") val code: String? = null,
    @SerialName("Name") val name: String? = null,
    @SerialName("PEratio") val peRatio: String? = null,
    @SerialName("DividendYield") val dividendYield: String? = null,
    @SerialName("PBratio") val pbRatio: String? = null,
)
