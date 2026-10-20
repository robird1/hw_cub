package com.example.hw_cub.stock.presentation

import com.example.hw_cub.core.DataError
import com.example.hw_cub.stock.domain.StockDay
import java.time.LocalDate

/**
 * Everything the stock list screen renders.
 *
 * [loadError] is a failed load with nothing to show (rendered inline with
 * Retry); [refreshError] is a failed refresh while data stays visible
 * (rendered as a snackbar). They are never set together. A `null`
 * [sortOrder] keeps the order TWSE returns.
 */
data class StockListUiState(
    val stocks: List<StockDay> = emptyList(),
    val query: String = "",
    val sortOrder: StockSortOrder? = null,
    val tradeDate: LocalDate? = null,
    val isLoading: Boolean = false,
    val hasLoaded: Boolean = false,
    val loadError: DataError? = null,
    val refreshError: DataError? = null,
)
