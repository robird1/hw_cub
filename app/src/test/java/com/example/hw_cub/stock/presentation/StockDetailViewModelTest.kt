package com.example.hw_cub.stock.presentation

import com.example.hw_cub.core.DataError
import com.example.hw_cub.core.DataResult
import com.example.hw_cub.stock.domain.StockDay
import com.example.hw_cub.stock.domain.StockRepository
import com.example.hw_cub.stock.domain.StockValuation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class StockDetailViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeValuationRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads the valuation for its code`() = runTest(dispatcher) {
        val viewModel = StockDetailViewModel("2330", repository)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.isLoading)

        repository.complete(DataResult.Success(tsmc))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("2330"), repository.requestedCodes)
        assertEquals(tsmc, state.valuation)
        assertFalse(state.isLoading)
        assertFalse(state.isMissing)
        assertNull(state.error)
    }

    @Test
    fun `no data for the code is missing, not an error`() = runTest(dispatcher) {
        val viewModel = StockDetailViewModel("00400A", repository)
        repository.complete(DataResult.Success(null))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isMissing)
        assertNull(state.error)
        assertNull(state.valuation)
    }

    @Test
    fun `failure shows the error, and retry recovers`() = runTest(dispatcher) {
        val viewModel = StockDetailViewModel("2330", repository)
        repository.complete(DataResult.Failure(DataError.Network))
        advanceUntilIdle()

        assertEquals(DataError.Network, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isMissing)

        viewModel.load()
        repository.complete(DataResult.Success(tsmc))
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertEquals(tsmc, viewModel.uiState.value.valuation)
    }

    @Test
    fun `load while a load is running is ignored`() = runTest(dispatcher) {
        val viewModel = StockDetailViewModel("2330", repository)
        advanceUntilIdle()

        viewModel.load()
        advanceUntilIdle()

        assertEquals(1, repository.requestedCodes.size)
    }

    private val tsmc = StockValuation(
        code = "2330",
        name = "台積電",
        date = null,
        peRatio = BigDecimal("28.05"),
        dividendYieldPercent = BigDecimal("0.91"),
        pbRatio = BigDecimal("9.76"),
    )
}

/** Holds each request open until the test completes it. */
private class FakeValuationRepository : StockRepository {
    val requestedCodes = mutableListOf<String>()
    private var pending = CompletableDeferred<DataResult<StockValuation?>>()

    override suspend fun getValuation(code: String): DataResult<StockValuation?> {
        requestedCodes += code
        return pending.await().also { pending = CompletableDeferred() }
    }

    fun complete(result: DataResult<StockValuation?>) {
        pending.complete(result)
    }

    override suspend fun getDailyQuotes(): DataResult<List<StockDay>> =
        error("Not used by the detail screen")
}
