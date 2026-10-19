package com.example.hw_cub.di

import com.example.hw_cub.BuildConfig
import com.example.hw_cub.stock.data.DefaultStockRepository
import com.example.hw_cub.stock.data.remote.TwseService
import com.example.hw_cub.stock.domain.StockRepository
import com.example.hw_cub.stock.presentation.StockListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

/** Stock list feature: repository and ViewModel. */
val stockModule = module {
    single<StockRepository> { DefaultStockRepository(get(), Dispatchers.Default) }
    viewModelOf(::StockListViewModel)
}

/** Every module the app starts Koin with. */
val appModules = listOf(networkModule, stockModule)
