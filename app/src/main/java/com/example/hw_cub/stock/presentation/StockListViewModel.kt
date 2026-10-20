package com.example.hw_cub.stock.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hw_cub.core.DataResult
import com.example.hw_cub.stock.domain.model.StockDay
import com.example.hw_cub.stock.domain.model.StockSortOrder
import com.example.hw_cub.stock.domain.usecase.FilterAndSortStocksUseCase
import com.example.hw_cub.stock.domain.usecase.GetDailyQuotesUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Loads the daily quote list, filters it by the search query and sorts it.
 *
 * The query and sort order are kept in [savedStateHandle] so they survive
 * process death; the quotes themselves are reloaded.
 */
class StockListViewModel(
    private val getDailyQuotes: GetDailyQuotesUseCase,
    private val filterAndSortStocks: FilterAndSortStocksUseCase,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        StockListUiState(
            query = savedStateHandle[KEY_QUERY] ?: "",
            sortOrder = savedStateHandle[KEY_SORT_ORDER],
        ),
    )
    val uiState: StateFlow<StockListUiState> = _uiState.asStateFlow()

    private var allStocks: List<StockDay> = emptyList()
    private var loadJob: Job? = null

    init {
        load()
    }

    /** Reloads the list; ignored while a load is already running. */
    fun load() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = true, loadError = null, refreshError = null)
            }
            when (val result = getDailyQuotes()) {
                is DataResult.Success -> onLoaded(result.data)
                is DataResult.Failure -> _uiState.update {
                    if (it.hasLoaded) {
                        it.copy(isLoading = false, refreshError = result.error)
                    } else {
                        it.copy(isLoading = false, loadError = result.error)
                    }
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        savedStateHandle[KEY_QUERY] = query
        _uiState.update {
            it.copy(query = query, stocks = visibleStocks(query, it.sortOrder))
        }
    }

    fun onSortOrderChange(order: StockSortOrder) {
        savedStateHandle[KEY_SORT_ORDER] = order
        _uiState.update {
            it.copy(sortOrder = order, stocks = visibleStocks(it.query, order))
        }
    }

    fun onRefreshErrorShown() {
        _uiState.update { it.copy(refreshError = null) }
    }

    private fun onLoaded(stocks: List<StockDay>) {
        allStocks = stocks
        _uiState.update {
            it.copy(
                stocks = visibleStocks(it.query, it.sortOrder),
                tradeDate = stocks.firstNotNullOfOrNull(StockDay::date),
                isLoading = false,
                hasLoaded = true,
            )
        }
    }

    private fun visibleStocks(query: String, order: StockSortOrder?): List<StockDay> =
        filterAndSortStocks(allStocks, query, order)

    private companion object {
        const val KEY_QUERY = "query"
        const val KEY_SORT_ORDER = "sortOrder"
    }
}
