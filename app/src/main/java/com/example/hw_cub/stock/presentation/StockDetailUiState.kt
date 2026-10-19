package com.example.hw_cub.stock.presentation

import com.example.hw_cub.core.DataError
import com.example.hw_cub.stock.domain.StockValuation

/**
 * Everything the stock detail screen renders.
 *
 * [isMissing] means TWSE publishes no ratios for this security (a stable
 * outcome, no Retry); [error] means the request failed (retryable). They are
 * never set together.
 */
data class StockDetailUiState(
    val valuation: StockValuation? = null,
    val isLoading: Boolean = false,
    val isMissing: Boolean = false,
    val error: DataError? = null,
)
