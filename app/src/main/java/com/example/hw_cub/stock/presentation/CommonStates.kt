package com.example.hw_cub.stock.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.hw_cub.R
import com.example.hw_cub.core.DataError
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * Medium date in the current language, e.g. "Oct 16, 2026" or "2026年10月16日".
 *
 * Built per composition locale rather than once per process, so switching the
 * system language updates dates without restarting the app.
 */
@Composable
internal fun rememberDateFormatter(): DateTimeFormatter {
    val locale = currentLocale()
    return remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    }
}

/** Whole numbers with the current language's digit grouping, e.g. "12,280". */
@Composable
internal fun rememberIntegerFormat(): NumberFormat {
    val locale = currentLocale()
    return remember(locale) { NumberFormat.getIntegerInstance(locale) }
}

@Composable
private fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

/** Full-screen spinner for a first load with nothing to show yet. */
@Composable
internal fun LoadingState() {
    val description = stringResource(R.string.loading)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.semantics { contentDescription = description })
    }
}

/** Failed first load: the error message with a Retry button. */
@Composable
internal fun LoadErrorState(error: DataError, onRetry: () -> Unit) {
    CenteredMessage(error.message()) {
        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

/** A centered message with an optional action below it. */
@Composable
internal fun CenteredMessage(message: String, action: @Composable () -> Unit = {}) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        action()
    }
}

/** User-facing text for a [DataError]. */
@Composable
internal fun DataError.message(): String = when (this) {
    DataError.Network -> stringResource(R.string.error_network)
    is DataError.Server -> stringResource(R.string.error_server, code)
    DataError.InvalidResponse -> stringResource(R.string.error_invalid_response)
}
