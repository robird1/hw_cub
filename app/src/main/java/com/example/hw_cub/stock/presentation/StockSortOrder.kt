package com.example.hw_cub.stock.presentation

import com.example.hw_cub.stock.domain.StockDay

/** Order the stock list can be sorted in from the menu. */
enum class StockSortOrder { CodeDescending, CodeAscending }

/** Sorts by [order], or keeps the TWSE response order when [order] is `null`. */
fun List<StockDay>.sortedIn(order: StockSortOrder?): List<StockDay> = when (order) {
    StockSortOrder.CodeDescending -> sortedByDescending(StockDay::code)
    StockSortOrder.CodeAscending -> sortedBy(StockDay::code)
    null -> this
}
