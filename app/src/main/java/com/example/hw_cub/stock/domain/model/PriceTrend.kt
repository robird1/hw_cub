package com.example.hw_cub.stock.domain.model

/** Direction of a price relative to a reference price. */
enum class PriceTrend { Above, Below }

/**
 * Compares the closing price with the monthly average.
 *
 * Returns `null` when the prices are equal or either one is missing, meaning
 * neither above nor below.
 */
fun StockDay.closeVersusMonthlyAverage(): PriceTrend? {
    val close = closingPrice ?: return null
    val average = monthlyAveragePrice ?: return null
    return when (close.compareTo(average).coerceIn(-1, 1)) {
        1 -> PriceTrend.Above
        -1 -> PriceTrend.Below
        else -> null
    }
}
