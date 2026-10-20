package com.example.hw_cub.stock.domain.usecase

import com.example.hw_cub.stock.domain.model.StockDay
import com.example.hw_cub.stock.domain.model.StockSortOrder

/**
 * Picks the stocks to show: those matching the search query, in the given
 * sort order.
 *
 * Filtering runs first so sorting only touches the matches.
 */
class FilterAndSortStocksUseCase {

    /**
     * Keeps the stocks whose code starts with [query] or whose name contains
     * it, ignoring case and surrounding spaces, then sorts them by [order].
     *
     * A blank [query] keeps every stock; a `null` [order] keeps the TWSE
     * response order.
     */
    operator fun invoke(
        stocks: List<StockDay>,
        query: String,
        order: StockSortOrder?,
    ): List<StockDay> = stocks.matching(query).sortedIn(order)

    private fun List<StockDay>.matching(query: String): List<StockDay> {
        val term = query.trim()
        if (term.isEmpty()) return this
        return filter {
            it.code.startsWith(term, ignoreCase = true) ||
                it.name.contains(term, ignoreCase = true)
        }
    }

    private fun List<StockDay>.sortedIn(order: StockSortOrder?): List<StockDay> =
        when (order) {
            StockSortOrder.CodeDescending -> sortedByDescending(StockDay::code)
            StockSortOrder.CodeAscending -> sortedBy(StockDay::code)
            null -> this
        }
}
