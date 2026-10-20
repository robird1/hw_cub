package com.example.hw_cub.stock.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class PriceTrendTest {

    @Test
    fun `close above monthly average is Above`() {
        val stock = priced(close = "15.01", average = "14.36")

        assertEquals(PriceTrend.Above, stock.closeVersusMonthlyAverage())
    }

    @Test
    fun `close below monthly average is Below`() {
        val stock = priced(close = "2420.00", average = "2516.82")

        assertEquals(PriceTrend.Below, stock.closeVersusMonthlyAverage())
    }

    @Test
    fun `equal prices ignore scale and are neutral`() {
        assertNull(priced(close = "15.0", average = "15.00").closeVersusMonthlyAverage())
    }

    @Test
    fun `missing price is neutral`() {
        assertNull(priced(close = null, average = "15.00").closeVersusMonthlyAverage())
        assertNull(priced(close = "15.00", average = null).closeVersusMonthlyAverage())
    }

    private fun priced(close: String?, average: String?) = stock("2330", "台積電").copy(
        closingPrice = close?.let(::BigDecimal),
        monthlyAveragePrice = average?.let(::BigDecimal),
    )
}
