package com.example.hw_cub.stock.domain.usecase

import com.example.hw_cub.core.DataResult
import com.example.hw_cub.stock.domain.model.StockValuation
import com.example.hw_cub.stock.domain.repository.StockRepository

/**
 * Loads the latest valuation ratios for one security.
 *
 * A successful `null` means TWSE publishes no ratios for it, e.g. an ETF.
 */
class GetStockValuationUseCase(private val repository: StockRepository) {
    suspend operator fun invoke(code: String): DataResult<StockValuation?> =
        repository.getValuation(code)
}
