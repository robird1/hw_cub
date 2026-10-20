package com.example.hw_cub.stock.domain.usecase

import com.example.hw_cub.stock.domain.model.StockDay
import com.example.hw_cub.stock.domain.model.StockSortOrder
import com.example.hw_cub.stock.domain.model.StockSortOrder.CodeAscending
import com.example.hw_cub.stock.domain.model.StockSortOrder.CodeDescending
import com.example.hw_cub.stock.domain.model.stock
import org.junit.Assert.assertEquals
import org.junit.Test

class FilterAndSortStocksUseCaseTest {

    private val filterAndSort = FilterAndSortStocksUseCase()
    private val tsmc = stock("2330", "台積電")
    private val foxconn = stock("2317", "鴻海")
    private val etf = stock("0050", "元大台灣50")
    private val leveragedEtf = stock("00631L", "元大台灣50正2")
    private val responseOrder = listOf(foxconn, etf, tsmc, leveragedEtf)

    private fun search(query: String) = filterAndSort(responseOrder, query, order = null)

    private fun sort(order: StockSortOrder?) = filterAndSort(responseOrder, query = "", order)

    @Test
    fun `blank query keeps every stock`() {
        assertEquals(responseOrder, search(""))
        assertEquals(responseOrder, search("   "))
    }

    @Test
    fun `code matches by prefix only`() {
        assertEquals(listOf(foxconn, tsmc), search("23"))
        assertEquals(emptyList<StockDay>(), search("30"))
    }

    @Test
    fun `name matches anywhere in the name`() {
        assertEquals(listOf(etf, leveragedEtf), search("台灣"))
    }

    @Test
    fun `match ignores case and surrounding spaces`() {
        assertEquals(listOf(leveragedEtf), search(" 00631l "))
    }

    @Test
    fun `no order keeps the response order`() {
        assertEquals(responseOrder, sort(null))
    }

    @Test
    fun `descending sorts by code from highest`() {
        assertEquals(listOf(tsmc, foxconn, leveragedEtf, etf), sort(CodeDescending))
    }

    @Test
    fun `ascending sorts codes as text`() {
        assertEquals(listOf(etf, leveragedEtf, foxconn, tsmc), sort(CodeAscending))
    }

    @Test
    fun `filters by the query, then sorts the matches`() {
        assertEquals(listOf(tsmc, foxconn), filterAndSort(responseOrder, "23", CodeDescending))
    }
}
