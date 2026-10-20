package com.example.hw_cub.stock.presentation

import androidx.lifecycle.SavedStateHandle
import com.example.hw_cub.core.DataError
import com.example.hw_cub.core.DataResult
import com.example.hw_cub.stock.domain.model.StockDay
import com.example.hw_cub.stock.domain.model.StockSortOrder
import com.example.hw_cub.stock.domain.model.StockValuation
import com.example.hw_cub.stock.domain.repository.StockRepository
import com.example.hw_cub.stock.domain.usecase.FilterAndSortStocksUseCase
import com.example.hw_cub.stock.domain.usecase.GetDailyQuotesUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class StockListViewModelTest {

    private val dispatcher = StandardTestDispatcher()
    private val repository = FakeStockRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun newViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        StockListViewModel(
            GetDailyQuotesUseCase(repository),
            FilterAndSortStocksUseCase(),
            savedStateHandle,
        )

    private fun TestScope.loadedViewModel(stocks: List<StockDay>): StockListViewModel {
        val viewModel = newViewModel()
        repository.complete(DataResult.Success(stocks))
        advanceUntilIdle()
        return viewModel
    }

    @Test
    fun `cold load shows loading, then the stocks and trade date`() = runTest(dispatcher) {
        val viewModel = newViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.hasLoaded)

        repository.complete(DataResult.Success(listOf(tsmc, foxconn)))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.hasLoaded)
        assertEquals(listOf(tsmc, foxconn), state.stocks)
        assertEquals(LocalDate.of(2026, 10, 16), state.tradeDate)
    }

    @Test
    fun `cold load failure shows load error, and retry recovers`() = runTest(dispatcher) {
        val viewModel = newViewModel()
        repository.complete(DataResult.Failure(DataError.Network))
        advanceUntilIdle()

        assertEquals(DataError.Network, viewModel.uiState.value.loadError)
        assertNull(viewModel.uiState.value.refreshError)
        assertFalse(viewModel.uiState.value.hasLoaded)

        viewModel.load()
        advanceUntilIdle()
        assertNull(viewModel.uiState.value.loadError)
        assertTrue(viewModel.uiState.value.isLoading)

        repository.complete(DataResult.Success(listOf(tsmc)))
        advanceUntilIdle()
        assertEquals(listOf(tsmc), viewModel.uiState.value.stocks)
    }

    @Test
    fun `refresh failure keeps the stocks and reports a refresh error`() = runTest(dispatcher) {
        val viewModel = loadedViewModel(listOf(tsmc))

        viewModel.load()
        repository.complete(DataResult.Failure(DataError.Server(503)))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(tsmc), state.stocks)
        assertEquals(DataError.Server(503), state.refreshError)
        assertNull(state.loadError)

        viewModel.onRefreshErrorShown()
        assertNull(viewModel.uiState.value.refreshError)
    }

    @Test
    fun `successful empty response is loaded with no stocks`() = runTest(dispatcher) {
        val viewModel = loadedViewModel(emptyList())

        val state = viewModel.uiState.value
        assertTrue(state.hasLoaded)
        assertTrue(state.stocks.isEmpty())
        assertNull(state.loadError)
        assertNull(state.tradeDate)
    }

    @Test
    fun `query filters by code prefix or name, and survives a reload`() = runTest(dispatcher) {
        val viewModel = loadedViewModel(listOf(tsmc, foxconn))

        viewModel.onQueryChange("23")
        assertEquals(listOf(tsmc, foxconn), viewModel.uiState.value.stocks)

        viewModel.onQueryChange("鴻")
        assertEquals(listOf(foxconn), viewModel.uiState.value.stocks)

        viewModel.load()
        repository.complete(DataResult.Success(listOf(tsmc, foxconn)))
        advanceUntilIdle()
        assertEquals(listOf(foxconn), viewModel.uiState.value.stocks)

        viewModel.onQueryChange("")
        assertEquals(listOf(tsmc, foxconn), viewModel.uiState.value.stocks)
    }

    @Test
    fun `sort order sorts by code and defaults to response order`() = runTest(dispatcher) {
        val viewModel = loadedViewModel(listOf(foxconn, etf, tsmc))
        assertNull(viewModel.uiState.value.sortOrder)
        assertEquals(listOf(foxconn, etf, tsmc), viewModel.uiState.value.stocks)

        viewModel.onSortOrderChange(StockSortOrder.CodeDescending)
        assertEquals(StockSortOrder.CodeDescending, viewModel.uiState.value.sortOrder)
        assertEquals(listOf(tsmc, foxconn, etf), viewModel.uiState.value.stocks)

        viewModel.onSortOrderChange(StockSortOrder.CodeAscending)
        assertEquals(listOf(etf, foxconn, tsmc), viewModel.uiState.value.stocks)
    }

    @Test
    fun `sort order applies to filtered results and survives a reload`() = runTest(dispatcher) {
        val viewModel = loadedViewModel(listOf(foxconn, etf, tsmc))
        viewModel.onSortOrderChange(StockSortOrder.CodeDescending)

        viewModel.onQueryChange("23")
        assertEquals(listOf(tsmc, foxconn), viewModel.uiState.value.stocks)

        viewModel.load()
        repository.complete(DataResult.Success(listOf(foxconn, tsmc)))
        advanceUntilIdle()
        assertEquals(listOf(tsmc, foxconn), viewModel.uiState.value.stocks)
    }

    @Test
    fun `query and sort order survive process death`() = runTest(dispatcher) {
        val savedStateHandle = SavedStateHandle()
        val before = newViewModel(savedStateHandle)
        repository.complete(DataResult.Success(listOf(foxconn, etf, tsmc)))
        advanceUntilIdle()
        before.onQueryChange("23")
        before.onSortOrderChange(StockSortOrder.CodeDescending)

        // A fresh handle with only the saved values, as Android rebuilds it.
        val savedValues = savedStateHandle.keys().associateWith { savedStateHandle.get<Any>(it) }
        val restored = newViewModel(SavedStateHandle(savedValues))
        repository.complete(DataResult.Success(listOf(foxconn, etf, tsmc)))
        advanceUntilIdle()

        val state = restored.uiState.value
        assertEquals("23", state.query)
        assertEquals(StockSortOrder.CodeDescending, state.sortOrder)
        assertEquals(listOf(tsmc, foxconn), state.stocks)
    }

    @Test
    fun `load while a load is running is ignored`() = runTest(dispatcher) {
        val viewModel = newViewModel()
        advanceUntilIdle()

        viewModel.load()
        viewModel.load()
        advanceUntilIdle()

        assertEquals(1, repository.calls)
    }

    private val tsmc = stock("2330", "台積電")
    private val foxconn = stock("2317", "鴻海")
    private val etf = stock("0050", "元大台灣50")

    private fun stock(code: String, name: String) = StockDay(
        code = code,
        name = name,
        date = LocalDate.of(2026, 10, 16),
        tradeVolume = null,
        tradeValue = null,
        openingPrice = null,
        highestPrice = null,
        lowestPrice = null,
        closingPrice = null,
        change = null,
        monthlyAveragePrice = null,
        transactions = null,
    )
}

/** Holds each request open until the test completes it, so loading frames are observable. */
private class FakeStockRepository : StockRepository {
    var calls = 0
        private set
    private var pending = CompletableDeferred<DataResult<List<StockDay>>>()

    override suspend fun getDailyQuotes(): DataResult<List<StockDay>> {
        calls++
        return pending.await().also { pending = CompletableDeferred() }
    }

    fun complete(result: DataResult<List<StockDay>>) {
        pending.complete(result)
    }

    override suspend fun getValuation(code: String): DataResult<StockValuation?> =
        error("Not used by the list screen")
}
