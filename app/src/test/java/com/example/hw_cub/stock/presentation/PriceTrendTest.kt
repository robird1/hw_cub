package com.example.hw_cub.stock.presentation

import com.example.hw_cub.stock.domain.StockDay
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class PriceTrendTest {

    @Test
    fun `close above monthly average is Above`() {
        val stock = stock(close = "15.01", average = "14.36")

        assertEquals(PriceTrend.Above, stock.closeVersusMonthlyAverage())
    }

    @Test
    fun `close below monthly average is Below`() {
        val stock = stock(close = "2420.00", average = "2516.82")

        assertEquals(PriceTrend.Below, stock.closeVersusMonthlyAverage())
    }

    @Test
    fun `equal prices ignore scale and are neutral`() {
        assertNull(stock(close = "15.0", average = "15.00").closeVersusMonthlyAverage())
    }

    @Test
    fun `missing price is neutral`() {
        assertNull(stock(close = null, average = "15.00").closeVersusMonthlyAverage())
        assertNull(stock(close = "15.00", average = null).closeVersusMonthlyAverage())
    }

    private fun stock(close: String?, average: String?) = StockDay(
        code = "2330",
        name = "台積電",
        date = null,
        tradeVolume = null,
        tradeValue = null,
        openingPrice = null,
        highestPrice = null,
        lowestPrice = null,
        closingPrice = close?.let(::BigDecimal),
        change = null,
        monthlyAveragePrice = average?.let(::BigDecimal),
        transactions = null,
    )
}
