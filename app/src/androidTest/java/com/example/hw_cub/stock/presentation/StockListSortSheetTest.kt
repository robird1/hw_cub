package com.example.hw_cub.stock.presentation

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.hw_cub.R
import com.example.hw_cub.stock.domain.model.StockDay
import com.example.hw_cub.stock.domain.model.StockSortOrder
import com.example.hw_cub.stock.domain.usecase.FilterAndSortStocksUseCase
import com.example.hw_cub.ui.theme.HwCubTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class StockListSortSheetTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val descending get() = string(R.string.sort_code_descending)
    private val ascending get() = string(R.string.sort_code_ascending)

    @Test
    fun menuButtonOpensSheetWithNothingSelectedByDefault() {
        setScreen(sortOrder = null)

        openSortSheet()

        composeRule.onNodeWithText(descending).assertIsNotSelected()
        composeRule.onNodeWithText(ascending).assertIsNotSelected()
    }

    @Test
    fun sheetMarksTheCurrentSortOrderAsSelected() {
        setScreen(sortOrder = StockSortOrder.CodeAscending)

        openSortSheet()

        composeRule.onNodeWithText(ascending).assertIsSelected()
        composeRule.onNodeWithText(descending).assertIsNotSelected()
    }

    @Test
    fun pickingAnOrderReportsItAndClosesTheSheet() {
        val picked = mutableListOf<StockSortOrder>()
        setScreen(sortOrder = null, onSortOrderChange = { picked += it })

        openSortSheet()
        composeRule.onNodeWithText(descending).performClick()

        composeRule.waitUntil { !composeRule.isShown(descending) }
        assertEquals(listOf(StockSortOrder.CodeDescending), picked)
    }

    @Test
    fun pickingAnOrderResortsTheList() {
        composeRule.setContent {
            var sortOrder by remember { mutableStateOf<StockSortOrder?>(null) }
            HwCubTheme {
                StockListScreen(
                    state = loadedState(sortOrder),
                    onQueryChange = {},
                    onReload = {},
                    onRefreshErrorShown = {},
                    onSortOrderChange = { sortOrder = it },
                    onStockClick = {},
                )
            }
        }
        assertTrue(isBefore("2317", "2330"))

        openSortSheet()
        composeRule.onNodeWithText(descending).performClick()
        composeRule.waitForIdle()
        assertTrue(isBefore("2330", "2317"))

        openSortSheet()
        composeRule.onNodeWithText(ascending).performClick()
        composeRule.waitForIdle()
        assertTrue(isBefore("2317", "2330"))
    }

    private fun setScreen(
        sortOrder: StockSortOrder?,
        onSortOrderChange: (StockSortOrder) -> Unit = {},
    ) {
        composeRule.setContent {
            HwCubTheme {
                StockListScreen(
                    state = loadedState(sortOrder),
                    onQueryChange = {},
                    onReload = {},
                    onRefreshErrorShown = {},
                    onSortOrderChange = onSortOrderChange,
                    onStockClick = {},
                )
            }
        }
    }

    private fun openSortSheet() {
        composeRule.onNodeWithContentDescription(string(R.string.menu)).performClick()
        composeRule.waitUntil { composeRule.isShown(descending) }
    }

    /** Reading order: higher row first, then left column first on wide screens. */
    private fun isBefore(first: String, second: String): Boolean {
        val a = position(first)
        val b = position(second)
        return a.y < b.y || (a.y == b.y && a.x < b.x)
    }

    private fun position(code: String) =
        composeRule.onNodeWithText(code).fetchSemanticsNode().positionInRoot

    private fun string(@StringRes id: Int): String = composeRule.activity.getString(id)

    /** Response order is 2317, 2330; a sort order rearranges it like the ViewModel does. */
    private fun loadedState(sortOrder: StockSortOrder?) = StockListUiState(
        stocks = FilterAndSortStocksUseCase()(
            listOf(stock("2317", "鴻海"), stock("2330", "台積電")),
            query = "",
            order = sortOrder,
        ),
        sortOrder = sortOrder,
        tradeDate = LocalDate.of(2026, 10, 16),
        hasLoaded = true,
    )

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

private fun AndroidComposeTestRule<*, *>.isShown(text: String): Boolean =
    onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
