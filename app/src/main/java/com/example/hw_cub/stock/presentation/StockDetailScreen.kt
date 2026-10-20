package com.example.hw_cub.stock.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hw_cub.R
import com.example.hw_cub.stock.domain.model.StockValuation
import com.example.hw_cub.ui.theme.HwCubTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Stock detail destination for [code].
 *
 * [name] comes from the list so the header shows immediately, before the
 * ratios load.
 */
@Composable
fun StockDetailRoute(
    code: String,
    name: String,
    onBack: () -> Unit,
    viewModel: StockDetailViewModel = koinViewModel { parametersOf(code) },
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StockDetailScreen(
        code = code,
        name = name,
        state = state,
        onBack = onBack,
        onRetry = viewModel::load,
    )
}

/** Stateless detail screen: P/E ratio, dividend yield and P/B ratio. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockDetailScreen(
    code: String,
    name: String,
    state: StockDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { DetailTitle(code, name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            DetailContent(state, onRetry)
        }
    }
}

@Composable
private fun DetailContent(state: StockDetailUiState, onRetry: () -> Unit) {
    when {
        state.isLoading -> LoadingState()
        state.error != null -> LoadErrorState(state.error, onRetry)
        state.isMissing -> CenteredMessage(stringResource(R.string.valuation_missing))
        state.valuation != null -> ValuationContent(state.valuation)
    }
}

@Composable
private fun DetailTitle(code: String, name: String) {
    Column {
        Text(
            text = name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ValuationContent(valuation: StockValuation) {
    val dateFormatter = rememberDateFormatter()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RatioCard(stringResource(R.string.pe_ratio), valuation.peRatio)
        RatioCard(stringResource(R.string.dividend_yield), valuation.dividendYieldPercent)
        RatioCard(stringResource(R.string.pb_ratio), valuation.pbRatio)
        valuation.date?.let {
            Text(
                text = stringResource(R.string.data_date, it.format(dateFormatter)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

@Composable
private fun RatioCard(label: String, value: BigDecimal?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value?.toPlainString() ?: stringResource(R.string.not_available),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StockDetailScreenPreview() {
    HwCubTheme {
        StockDetailScreen(
            code = "2330",
            name = "台積電",
            state = StockDetailUiState(
                valuation = StockValuation(
                    code = "2330",
                    name = "台積電",
                    date = LocalDate.of(2026, 10, 16),
                    peRatio = BigDecimal("28.05"),
                    dividendYieldPercent = BigDecimal("0.91"),
                    pbRatio = BigDecimal("9.76"),
                ),
            ),
            onBack = {},
            onRetry = {},
        )
    }
}
