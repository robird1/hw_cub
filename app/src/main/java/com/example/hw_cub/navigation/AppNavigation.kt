package com.example.hw_cub.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.hw_cub.stock.presentation.StockDetailRoute
import com.example.hw_cub.stock.presentation.StockListRoute
import kotlinx.serialization.Serializable

/** The stock list, the app's start destination. */
@Serializable
data object StockListKey : NavKey

/** Valuation details for one security; [name] is shown while ratios load. */
@Serializable
data class StockDetailKey(val code: String, val name: String) : NavKey

/**
 * App navigation graph.
 *
 * The back stack survives configuration changes and process death, and each
 * entry gets its own ViewModel store so a detail ViewModel is cleared when
 * its screen is popped.
 */
@Composable
fun AppNavigation() {
    val backStack = rememberNavBackStack(StockListKey)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<StockListKey> {
                StockListRoute(
                    onStockClick = { stock ->
                        backStack.add(StockDetailKey(stock.code, stock.name))
                    },
                )
            }
            entry<StockDetailKey> { key ->
                StockDetailRoute(
                    code = key.code,
                    name = key.name,
                    onBack = { backStack.removeLastOrNull() },
                )
            }
        },
    )
}
