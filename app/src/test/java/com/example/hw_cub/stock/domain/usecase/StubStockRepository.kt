package com.example.hw_cub.stock.domain.usecase

import com.example.hw_cub.core.DataResult
import com.example.hw_cub.stock.domain.model.StockDay
import com.example.hw_cub.stock.domain.model.StockValuation
import com.example.hw_cub.stock.domain.repository.StockRepository

/** Returns fixed results and records which valuation codes were requested. */
class StubStockRepository(
    private val quotes: DataResult<List<StockDay>> = DataResult.Success(emptyList()),
    private val valuation: DataResult<StockValuation?> = DataResult.Success(null),
) : StockRepository {
    val requestedCodes = mutableListOf<String>()

    override suspend fun getDailyQuotes(): DataResult<List<StockDay>> = quotes

    override suspend fun getValuation(code: String): DataResult<StockValuation?> {
        requestedCodes += code
        return valuation
    }
}
