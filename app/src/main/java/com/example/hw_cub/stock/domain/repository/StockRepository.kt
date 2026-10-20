package com.example.hw_cub.stock.domain.repository

import com.example.hw_cub.core.DataResult
import com.example.hw_cub.stock.domain.model.StockDay
import com.example.hw_cub.stock.domain.model.StockValuation

/** Source of daily trading data for listed securities. */
interface StockRepository {
    /** Returns every listed security's summary for the latest trading day. */
    suspend fun getDailyQuotes(): DataResult<List<StockDay>>

    /**
     * Returns the latest valuation ratios for [code], or a successful `null`
     * when TWSE publishes none for it (ETFs and other non-company securities).
     */
    suspend fun getValuation(code: String): DataResult<StockValuation?>
}
