package com.example.hw_cub.di

import com.example.hw_cub.BuildConfig
import com.example.hw_cub.stock.data.DefaultStockRepository
import com.example.hw_cub.stock.data.remote.TwseService
import com.example.hw_cub.stock.domain.repository.StockRepository
import com.example.hw_cub.stock.domain.usecase.FilterAndSortStocksUseCase
import com.example.hw_cub.stock.domain.usecase.GetDailyQuotesUseCase
import com.example.hw_cub.stock.domain.usecase.GetStockValuationUseCase
import com.example.hw_cub.stock.presentation.StockDetailViewModel
import com.example.hw_cub.stock.presentation.StockListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/** HTTP client, JSON and Retrofit services. */
val networkModule = module {
    single { Json { ignoreUnknownKeys = true } }
    single {
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BASIC
                    } else {
                        HttpLoggingInterceptor.Level.NONE
                    }
                },
            )
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.TWSE_BASE_URL)
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }
    single { get<Retrofit>().create(TwseService::class.java) }
}

/** Stock feature: repository, use cases and the list and detail ViewModels. */
val stockModule = module {
    single<StockRepository> { DefaultStockRepository(get(), Dispatchers.Default) }
    factoryOf(::GetDailyQuotesUseCase)
    factoryOf(::GetStockValuationUseCase)
    factoryOf(::FilterAndSortStocksUseCase)
    viewModelOf(::StockListViewModel)
    viewModel { (code: String) -> StockDetailViewModel(code, get()) }
}

/** Every module the app starts Koin with. */
val appModules = listOf(networkModule, stockModule)
