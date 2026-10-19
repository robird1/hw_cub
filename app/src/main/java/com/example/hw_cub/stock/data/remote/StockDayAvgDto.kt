package com.example.hw_cub.stock.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * One row of TWSE `/exchangeReport/STOCK_DAY_AVG_ALL`: closing price and
 * monthly average price. Only the fields this app reads are declared.
 */
@Serializable
data class StockDayAvgDto(
    @SerialName("Code") val code: String? = null,
    @SerialName("MonthlyAveragePrice") val monthlyAveragePrice: String? = null,
)
