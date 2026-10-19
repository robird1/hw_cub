package com.example.hw_cub.stock.data

import com.example.hw_cub.stock.data.remote.StockDayDto
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class StockDayMapperTest {

    private val json = Json { ignoreUnknownKeys = true }

    // Rows copied from a real STOCK_DAY_ALL response (2026-10-16).
    private val response = """
        [
          {"Date":"1151016","Code":"00400A","Name":"主動國泰動能高息",
           "TradeVolume":"27595457","TradeValue":"415025661","OpeningPrice":"15.05",
           "HighestPrice":"15.16","LowestPrice":"14.90","ClosingPrice":"15.01",
           "Change":"0.0700","Transaction":"7336"},
          {"Date":"1151016","Code":"020011","Name":"統一微波高息20N",
           "TradeVolume":"","TradeValue":"","OpeningPrice":"","HighestPrice":"",
           "LowestPrice":"","ClosingPrice":"","Change":"0.0000","Transaction":""}
        ]
    """.trimIndent()

    @Test
    fun `decodes the real response shape and maps a traded row`() {
        val rows = json.decodeFromString<List<StockDayDto>>(response)

        val stock = requireNotNull(rows[0].toDomain())

        assertEquals("00400A", stock.code)
        assertEquals("主動國泰動能高息", stock.name)
        assertEquals(LocalDate.of(2026, 10, 16), stock.date)
        assertEquals(27_595_457L, stock.tradeVolume)
        assertEquals(BigDecimal("15.01"), stock.closingPrice)
        assertEquals(BigDecimal("0.0700"), stock.change)
        assertEquals(7_336L, stock.transactions)
    }

    @Test
    fun `untraded row keeps its identity with null numbers`() {
        val rows = json.decodeFromString<List<StockDayDto>>(response)

        val stock = requireNotNull(rows[1].toDomain())

        assertEquals("020011", stock.code)
        assertNull(stock.closingPrice)
        assertNull(stock.tradeVolume)
        assertEquals(BigDecimal("0.0000"), stock.change)
    }

    @Test
    fun `row without a code is dropped`() {
        assertNull(StockDayDto(code = " ", name = "No identity").toDomain())
    }

    @Test
    fun `malformed fields degrade to null instead of dropping the row`() {
        val stock = requireNotNull(
            StockDayDto(code = "2330", date = "1151399", closingPrice = "--").toDomain(),
        )

        assertNull(stock.date)
        assertNull(stock.closingPrice)
    }

    @Test
    fun `negative change and thousands separators parse`() {
        val stock = requireNotNull(
            StockDayDto(code = "2330", change = "-5.0000", tradeVolume = "1,234,567").toDomain(),
        )

        assertEquals(BigDecimal("-5.0000"), stock.change)
        assertEquals(1_234_567L, stock.tradeVolume)
    }

    @Test
    fun `parses ROC dates`() {
        assertEquals(LocalDate.of(2026, 10, 16), parseRocDate("1151016"))
        assertEquals(LocalDate.of(2010, 1, 5), parseRocDate("0990105"))
        assertNull(parseRocDate(""))
        assertNull(parseRocDate("115-10-16"))
    }
}
