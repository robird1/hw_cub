package com.example.hw_cub.stock.domain.usecase

import com.example.hw_cub.core.DataError
import com.example.hw_cub.core.DataResult
import com.example.hw_cub.stock.domain.model.StockValuation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class GetStockValuationUseCaseTest {

    @Test
    fun `requests the valuation for the given code`() = runTest {
        val valuation = DataResult.Success(tsmc)
        val repository = StubStockRepository(valuation = valuation)

        val result = GetStockValuationUseCase(repository)("2330")

        assertEquals(valuation, result)
        assertEquals(listOf("2330"), repository.requestedCodes)
    }

    @Test
    fun `no published ratios is a successful null`() = runTest {
        val result = GetStockValuationUseCase(StubStockRepository())("00400A")

        assertEquals(DataResult.Success(null), result)
    }

    @Test
    fun `passes failures through unchanged`() = runTest {
        val failure = DataResult.Failure(DataError.Server(503))

        val result = GetStockValuationUseCase(StubStockRepository(valuation = failure))("2330")

        assertEquals(failure, result)
    }

    private val tsmc = StockValuation(
        code = "2330",
        name = "台積電",
        date = null,
        peRatio = BigDecimal("25.10"),
        dividendYieldPercent = BigDecimal("1.20"),
        pbRatio = BigDecimal("7.30"),
    )
}
