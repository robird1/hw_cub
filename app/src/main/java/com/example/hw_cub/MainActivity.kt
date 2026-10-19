package com.example.hw_cub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.hw_cub.stock.presentation.StockListRoute
import com.example.hw_cub.ui.theme.HwCubTheme

/** Single activity hosting the Compose UI. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HwCubTheme {
                StockListRoute()
            }
        }
    }
}
