package com.example.hw_cub.stock.domain.usecase

import com.example.hw_cub.core.DataResult
import com.example.hw_cub.stock.domain.model.StockDay
import com.example.hw_cub.stock.domain.repository.StockRepository

/** Loads every listed security's summary for the latest trading day. */
class GetDailyQuotesUseCase(private val repository: StockRepository) {
    suspend operator fun invoke(): DataResult<List<StockDay>> = repository.getDailyQuotes()
}
