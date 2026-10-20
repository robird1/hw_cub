package com.example.hw_cub.stock.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hw_cub.R
import com.example.hw_cub.core.DataError
import com.example.hw_cub.stock.domain.StockDay
import com.example.hw_cub.ui.theme.HwCubTheme
import com.example.hw_cub.ui.theme.LocalPriceColors
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Stock list destination: wires [StockListViewModel] to [StockListScreen]. */
@Composable
fun StockListRoute(
    onStockClick: (StockDay) -> Unit,
    viewModel: StockListViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StockListScreen(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onReload = viewModel::load,
        onRefreshErrorShown = viewModel::onRefreshErrorShown,
        onSortOrderChange = viewModel::onSortOrderChange,
        onStockClick = onStockClick,
    )
}

/**
 * Stateless stock list: search field plus loading, error, empty or list content.
 *
 * The menu button opens a bottom sheet for choosing [StockListUiState.sortOrder].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(
    state: StockListUiState,
    onQueryChange: (String) -> Unit,
    onReload: () -> Unit,
    onRefreshErrorShown: () -> Unit,
    onSortOrderChange: (StockSortOrder) -> Unit,
    onStockClick: (StockDay) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showSortSheet by rememberSaveable { mutableStateOf(false) }
    RefreshErrorSnackbar(state.refreshError, snackbarHostState, onReload, onRefreshErrorShown)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { StockListTitle(state.tradeDate) },
                actions = { MenuButton(onClick = { showSortSheet = true }) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            SearchField(state.query, onQueryChange)
            StockListContent(state, onReload, onStockClick)
        }
    }

    if (showSortSheet) {
        SortOrderSheet(
            selected = state.sortOrder,
            onSelect = onSortOrderChange,
            onDismiss = { showSortSheet = false },
        )
    }
}

/** Bottom sheet listing the sort orders; picking one applies it and closes the sheet. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortOrderSheet(
    selected: StockSortOrder?,
    onSelect: (StockSortOrder) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(Modifier.selectableGroup().padding(bottom = 16.dp)) {
            StockSortOrder.entries.forEach { order ->
                SortOrderItem(order, isSelected = order == selected) {
                    onSelect(order)
                    scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                }
            }
        }
    }
}

@Composable
private fun SortOrderItem(order: StockSortOrder, isSelected: Boolean, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(order.labelRes)) },
        trailingContent = if (isSelected) {
            { Icon(painterResource(R.drawable.ic_check), contentDescription = null) }
        } else {
            null
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.selectable(
            selected = isSelected,
            onClick = onClick,
            role = Role.RadioButton,
        ),
    )
}

private val StockSortOrder.labelRes: Int
    @StringRes get() = when (this) {
        StockSortOrder.CodeDescending -> R.string.sort_code_descending
        StockSortOrder.CodeAscending -> R.string.sort_code_ascending
    }

@Composable
private fun StockListContent(
    state: StockListUiState,
    onReload: () -> Unit,
    onStockClick: (StockDay) -> Unit,
) {
    when {
        !state.hasLoaded && state.loadError != null ->
            LoadErrorState(state.loadError, onRetry = onReload)
        !state.hasLoaded -> LoadingState()
        else -> RefreshableStockList(state, onReload, onStockClick)
    }
}

/**
 * Stock cards in as many columns as fit [StockCardMinWidth]: one on a phone in
 * portrait, two in landscape or on a tablet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RefreshableStockList(
    state: StockListUiState,
    onRefresh: () -> Unit,
    onStockClick: (StockDay) -> Unit,
) {
    val gridState = rememberLazyGridState()
    ScrollToTopOnSortChange(state.sortOrder, gridState)
    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        if (state.stocks.isEmpty()) {
            EmptyState(state.query)
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(StockCardMinWidth),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.stocks, key = StockDay::code) { stock ->
                    StockCard(stock, onClick = { onStockClick(stock) })
                }
            }
        }
    }
}

/**
 * Jumps to the first item after the sort order changes, so the list does not
 * stay anchored to whichever stock was visible. The last applied order is
 * saved so returning from the detail screen keeps the scroll position.
 */
@Composable
private fun ScrollToTopOnSortChange(sortOrder: StockSortOrder?, gridState: LazyGridState) {
    var appliedOrder by rememberSaveable { mutableStateOf(sortOrder) }
    LaunchedEffect(sortOrder) {
        if (sortOrder != appliedOrder) {
            appliedOrder = sortOrder
            gridState.scrollToItem(0)
        }
    }
}

@Composable
private fun RefreshErrorSnackbar(
    error: DataError?,
    snackbarHostState: SnackbarHostState,
    onRetry: () -> Unit,
    onShown: () -> Unit,
) {
    if (error == null) return
    val message = error.message()
    val retryLabel = stringResource(R.string.retry)
    LaunchedEffect(error) {
        val result = snackbarHostState.showSnackbar(message, actionLabel = retryLabel)
        onShown()
        if (result == SnackbarResult.ActionPerformed) onRetry()
    }
}

@Composable
private fun StockListTitle(tradeDate: LocalDate?) {
    Column {
        Text(stringResource(R.string.stock_list_title))
        if (tradeDate != null) {
            Text(
                text = stringResource(R.string.trade_date, tradeDate.format(DateFormatter)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MenuButton(onClick: () -> Unit) {
    OutlinedIconButton(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(end = 8.dp),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_menu),
            contentDescription = stringResource(R.string.menu),
        )
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        label = { Text(stringResource(R.string.search_label)) },
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
private fun StockCard(stock: StockDay, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StockHeader(stock.code, stock.name)
            PriceGrid(stock)
            TradeSummary(stock)
        }
    }
}

@Composable
private fun StockHeader(code: String, name: String) {
    Column {
        Text(
            text = code,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = name,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun PriceGrid(stock: StockDay) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row {
            PriceCell(stringResource(R.string.opening_price), Modifier.weight(1f)) {
                PriceText(stock.openingPrice)
            }
            PriceCell(stringResource(R.string.closing_price), Modifier.weight(1f)) {
                ClosingPriceText(stock)
            }
        }
        Row {
            PriceCell(stringResource(R.string.highest_price), Modifier.weight(1f)) {
                PriceText(stock.highestPrice)
            }
            PriceCell(stringResource(R.string.lowest_price), Modifier.weight(1f)) {
                PriceText(stock.lowestPrice)
            }
        }
        Row {
            PriceCell(stringResource(R.string.price_change), Modifier.weight(1f)) {
                stock.change?.let { PriceChange(it) } ?: PriceText(null)
            }
            PriceCell(stringResource(R.string.monthly_average_price), Modifier.weight(1f)) {
                PriceText(stock.monthlyAveragePrice)
            }
        }
    }
}

/** A label on the left with its value centered in the remaining space. */
@Composable
private fun PriceCell(label: String, modifier: Modifier, value: @Composable () -> Unit) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f),
        )
        Box(Modifier.weight(1.4f), contentAlignment = Alignment.Center) { value() }
    }
}

@Composable
private fun PriceText(
    price: BigDecimal?,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
) {
    Text(
        text = price?.toPlainString() ?: stringResource(R.string.not_available),
        style = MaterialTheme.typography.titleMedium,
        color = color,
        modifier = modifier,
    )
}

/** Closing price, red above the monthly average and green below it. */
@Composable
private fun ClosingPriceText(stock: StockDay) {
    val colors = LocalPriceColors.current
    val price = stock.closingPrice?.toPlainString().orEmpty()
    val (color, description) = when (stock.closeVersusMonthlyAverage()) {
        PriceTrend.Above ->
            colors.up to stringResource(R.string.above_monthly_average, price)
        PriceTrend.Below ->
            colors.down to stringResource(R.string.below_monthly_average, price)
        null -> Color.Unspecified to null
    }
    val modifier = description?.let {
        Modifier.semantics { contentDescription = it }
    } ?: Modifier
    PriceText(price = stock.closingPrice, color = color, modifier = modifier)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TradeSummary(stock: StockDay) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SummaryItem(stringResource(R.string.transactions), stock.transactions)
        SummaryItem(stringResource(R.string.trade_volume), stock.tradeVolume)
        SummaryItem(stringResource(R.string.trade_value), stock.tradeValue)
    }
}

@Composable
private fun SummaryItem(label: String, value: Long?) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value?.let(IntegerFormat::format) ?: stringResource(R.string.not_available),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun PriceChange(change: BigDecimal) {
    val amount = change.abs().setScale(2, RoundingMode.HALF_UP).toPlainString()
    val colors = LocalPriceColors.current
    val (text, color, description) = when (change.signum()) {
        1 -> Triple("▲ $amount", colors.up, stringResource(R.string.change_up, amount))
        -1 -> Triple("▼ $amount", colors.down, stringResource(R.string.change_down, amount))
        else -> Triple(amount, MaterialTheme.colorScheme.onSurfaceVariant, amount)
    }
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.semantics { contentDescription = description },
    )
}

@Composable
private fun EmptyState(query: String) {
    val message = if (query.isBlank()) {
        stringResource(R.string.empty_no_data)
    } else {
        stringResource(R.string.empty_no_matches, query.trim())
    }
    CenteredMessage(message)
}

private val StockCardMinWidth = 360.dp

private val IntegerFormat: NumberFormat = NumberFormat.getIntegerInstance()

@Preview(showBackground = true)
@Composable
private fun StockListScreenPreview() {
    val sample = StockDay(
        code = "2330",
        name = "台積電",
        date = LocalDate.of(2026, 10, 16),
        tradeVolume = 27_595_457,
        tradeValue = 415_025_661,
        openingPrice = BigDecimal("1080.00"),
        highestPrice = BigDecimal("1090.00"),
        lowestPrice = BigDecimal("1075.00"),
        closingPrice = BigDecimal("1085.00"),
        change = BigDecimal("5.0000"),
        monthlyAveragePrice = BigDecimal("1102.45"),
        transactions = 7_336,
    )
    HwCubTheme {
        StockListScreen(
            state = StockListUiState(
                stocks = listOf(
                    sample,
                    sample.copy(code = "2317", name = "鴻海", change = BigDecimal("-2.5000")),
                    sample.copy(
                        code = "020011",
                        name = "統一微波高息20N",
                        openingPrice = null,
                        highestPrice = null,
                        lowestPrice = null,
                        closingPrice = null,
                        tradeVolume = null,
                        tradeValue = null,
                        transactions = null,
                    ),
                ),
                tradeDate = sample.date,
                hasLoaded = true,
            ),
            onQueryChange = {},
            onReload = {},
            onRefreshErrorShown = {},
            onSortOrderChange = {},
            onStockClick = {},
        )
    }
}
