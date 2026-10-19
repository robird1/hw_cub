package com.example.hw_cub.stock.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hw_cub.core.DataResult
import com.example.hw_cub.stock.domain.StockRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Loads valuation ratios for the security identified by [code]. */
class StockDetailViewModel(
    private val code: String,
    private val repository: StockRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockDetailUiState())
    val uiState: StateFlow<StockDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    /** Loads the ratios; ignored while a load is already running. */
    fun load() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            _uiState.update { state ->
                when (val result = repository.getValuation(code)) {
                    is DataResult.Success -> state.copy(
                        valuation = result.data,
                        isMissing = result.data == null,
                        isLoading = false,
                    )
                    is DataResult.Failure -> state.copy(isLoading = false, error = result.error)
                }
            }
        }
    }
}
