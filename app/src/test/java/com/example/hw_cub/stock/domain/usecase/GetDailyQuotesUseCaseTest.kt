package com.example.hw_cub.stock.domain.usecase

import com.example.hw_cub.core.DataError
import com.example.hw_cub.core.DataResult
import com.example.hw_cub.stock.domain.model.stock
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetDailyQuotesUseCaseTest {

    @Test
    fun `returns the repository's quotes`() = runTest {
        val quotes = DataResult.Success(listOf(stock("2330", "台積電")))

        assertEquals(quotes, GetDailyQuotesUseCase(StubStockRepository(quotes = quotes))())
    }

    @Test
    fun `passes failures through unchanged`() = runTest {
        val failure = DataResult.Failure(DataError.Network)

        assertEquals(failure, GetDailyQuotesUseCase(StubStockRepository(quotes = failure))())
    }
}
