package com.example.hw_cub.stock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hw_cub.core.DataResult
import com.example.hw_cub.stock.domain.StockDay
import com.example.hw_cub.stock.domain.StockRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Loads the daily quote list and filters it by the search query. */
class StockListViewModel(
    private val repository: StockRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockListUiState())
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
            when (val result = repository.getDailyQuotes()) {
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
        _uiState.update {
            it.copy(query = query, stocks = allStocks.matching(query))
        }
    }

    fun onRefreshErrorShown() {
        _uiState.update { it.copy(refreshError = null) }
    }

    private fun onLoaded(stocks: List<StockDay>) {
        allStocks = stocks
        _uiState.update {
            it.copy(
                stocks = stocks.matching(it.query),
                tradeDate = stocks.firstNotNullOfOrNull(StockDay::date),
                isLoading = false,
                hasLoaded = true,
            )
        }
    }
}

private fun List<StockDay>.matching(query: String): List<StockDay> {
    val term = query.trim()
    if (term.isEmpty()) return this
    return filter {
        it.code.startsWith(term, ignoreCase = true) ||
            it.name.contains(term, ignoreCase = true)
    }
}
