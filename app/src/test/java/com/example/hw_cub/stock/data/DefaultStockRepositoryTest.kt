package com.example.hw_cub.stock.data

import com.example.hw_cub.core.DataError
import com.example.hw_cub.core.DataResult
import com.example.hw_cub.stock.data.remote.StockDayAvgDto
import com.example.hw_cub.stock.data.remote.StockDayDto
import com.example.hw_cub.stock.data.remote.StockValuationDto
import com.example.hw_cub.stock.data.remote.TwseService
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.math.BigDecimal
import java.time.LocalDate

class DefaultStockRepositoryTest {

    private val service = FakeTwseService()
    private val repository = DefaultStockRepository(service, UnconfinedTestDispatcher())

    @Test
    fun `joins monthly average by code`() = runTest {
        service.daily = listOf(StockDayDto(code = "2330"), StockDayDto(code = "2317"))
        service.averages = listOf(
            StockDayAvgDto(code = "2330", monthlyAveragePrice = "2516.82"),
            StockDayAvgDto(code = "030001", monthlyAveragePrice = "1.20"),
        )

        val stocks = (repository.getDailyQuotes() as DataResult.Success).data

        assertEquals(listOf("2330", "2317"), stocks.map { it.code })
        assertEquals(BigDecimal("2516.82"), stocks[0].monthlyAveragePrice)
        assertNull(stocks[1].monthlyAveragePrice)
    }

    @Test
    fun `valuation is found by code and maps ratios`() = runTest {
        service.valuations = listOf(
            StockValuationDto(code = "1101", peRatio = "", dividendYield = "2.93", pbRatio = "0.88"),
            StockValuationDto(
                date = "1151016",
                code = "2330",
                name = "台積電",
                peRatio = "28.05",
                dividendYield = "0.91",
                pbRatio = "9.76",
            ),
        )

        val valuation = (repository.getValuation("2330") as DataResult.Success).data

        assertEquals("台積電", valuation?.name)
        assertEquals(LocalDate.of(2026, 10, 16), valuation?.date)
        assertEquals(BigDecimal("28.05"), valuation?.peRatio)
        assertEquals(BigDecimal("0.91"), valuation?.dividendYieldPercent)
        assertEquals(BigDecimal("9.76"), valuation?.pbRatio)
    }

    @Test
    fun `empty PE ratio stays null`() = runTest {
        service.valuations = listOf(
            StockValuationDto(code = "1101", peRatio = "", dividendYield = "2.93", pbRatio = "0.88"),
        )

        val valuation = (repository.getValuation("1101") as DataResult.Success).data

        assertNull(valuation?.peRatio)
        assertEquals(BigDecimal("0.88"), valuation?.pbRatio)
    }

    @Test
    fun `unknown code is a successful null`() = runTest {
        service.valuations = listOf(StockValuationDto(code = "2330"))

        assertEquals(DataResult.Success(null), repository.getValuation("00400A"))
    }

    @Test
    fun `valuation network failure maps to Network`() = runTest {
        service.valuationsError = IOException("timeout")

        assertEquals(DataResult.Failure(DataError.Network), repository.getValuation("2330"))
    }

    @Test
    fun `network failure in either request maps to Network`() = runTest {
        service.averagesError = IOException("timeout")

        assertEquals(DataResult.Failure(DataError.Network), repository.getDailyQuotes())
    }

    @Test
    fun `http failure maps to Server with its code`() = runTest {
        service.dailyError = HttpException(Response.error<Any>(503, "".toResponseBody()))

        assertEquals(DataResult.Failure(DataError.Server(503)), repository.getDailyQuotes())
    }
}

private class FakeTwseService : TwseService {
    var daily: List<StockDayDto> = emptyList()
    var averages: List<StockDayAvgDto> = emptyList()
    var dailyError: Exception? = null
    var averagesError: Exception? = null
    var valuations: List<StockValuationDto> = emptyList()
    var valuationsError: Exception? = null

    override suspend fun getStockDayAll(): List<StockDayDto> {
        dailyError?.let { throw it }
        return daily
    }

    override suspend fun getStockDayAvgAll(): List<StockDayAvgDto> {
        averagesError?.let { throw it }
        return averages
    }

    override suspend fun getBwibbuAll(): List<StockValuationDto> {
        valuationsError?.let { throw it }
        return valuations
    }
}
