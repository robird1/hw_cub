package com.example.hw_cub.stock.presentation

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hw_cub.stock.domain.model.StockSortOrder
import com.example.hw_cub.stock.domain.usecase.FilterAndSortStocksUseCase
import com.example.hw_cub.ui.theme.HwCubTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class StockListScrollTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    /** Codes 1000 to 1059, all starting with "10", so "10" matches every stock. */
    private val allStocks = (1000..1059).map { stock(it.toString(), "名稱$it") }

    @Test
    fun typingInTheSearchJumpsBackToTheFirstResult() {
        setScreen()
        scrollFarDown()

        composeRule.onNode(hasSetTextAction()).performTextInput("10")

        composeRule.onNodeWithText("1000").assertIsDisplayed()
    }

    @Test
    fun clearingTheSearchJumpsBackToTheFirstResult() {
        setScreen()
        composeRule.onNode(hasSetTextAction()).performTextInput("10")
        scrollFarDown()

        composeRule.onNode(hasSetTextAction()).performTextClearance()

        composeRule.onNodeWithText("1000").assertIsDisplayed()
    }

    @Test
    fun changingTheSortOrderJumpsBackToTheFirstResult() {
        var sortOrder by mutableStateOf<StockSortOrder?>(StockSortOrder.CodeAscending)
        setScreen(sortOrderOverride = { sortOrder })
        scrollFarDown()

        sortOrder = StockSortOrder.CodeDescending

        composeRule.onNodeWithText("1059").assertIsDisplayed()
    }

    private fun scrollFarDown() {
        composeRule.onNode(hasScrollToIndexAction()).performScrollToIndex(50)
        composeRule.onNodeWithText("1000").assertDoesNotExist()
    }

    /** Screen with a working search box, filtered and sorted like the ViewModel does. */
    private fun setScreen(sortOrderOverride: (() -> StockSortOrder?)? = null) {
        composeRule.setContent {
            var query by remember { mutableStateOf("") }
            val sortOrder = sortOrderOverride?.invoke()
            HwCubTheme {
                StockListScreen(
                    state = StockListUiState(
                        stocks = FilterAndSortStocksUseCase()(allStocks, query, sortOrder),
                        query = query,
                        sortOrder = sortOrder,
                        tradeDate = LocalDate.of(2026, 10, 16),
                        hasLoaded = true,
                    ),
                    onQueryChange = { query = it },
                    onReload = {},
                    onRefreshErrorShown = {},
                    onSortOrderChange = {},
                    onStockClick = {},
                )
            }
        }
    }
}
