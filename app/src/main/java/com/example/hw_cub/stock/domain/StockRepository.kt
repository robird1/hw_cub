package com.example.hw_cub.stock.domain

import com.example.hw_cub.core.DataResult

/** Source of daily trading data for listed securities. */
interface StockRepository {
    /** Returns every listed security's summary for the latest trading day. */
    suspend fun getDailyQuotes(): DataResult<List<StockDay>>
}
