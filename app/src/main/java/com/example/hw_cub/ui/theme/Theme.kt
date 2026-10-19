package com.example.hw_cub.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Colors for price movement.
 *
 * Follows the Taiwan market convention: red means up, green means down.
 */
@Immutable
data class PriceColors(val up: Color, val down: Color)

private val LightPriceColors = PriceColors(up = Color(0xFFC62828), down = Color(0xFF2E7D32))
private val DarkPriceColors = PriceColors(up = Color(0xFFEF9A9A), down = Color(0xFF81C784))

/** Price movement colors for the current theme. */
val LocalPriceColors = staticCompositionLocalOf { LightPriceColors }

private val LightColors = lightColorScheme(primary = Color(0xFF0D47A1))
private val DarkColors = darkColorScheme(primary = Color(0xFF90CAF9))

/** App theme: Material 3 with dynamic color on Android 12+. */
@Composable
fun HwCubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    CompositionLocalProvider(
        LocalPriceColors provides if (darkTheme) DarkPriceColors else LightPriceColors,
    ) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
