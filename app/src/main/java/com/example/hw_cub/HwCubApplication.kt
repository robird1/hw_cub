package com.example.hw_cub

import android.app.Application
import com.example.hw_cub.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/** Starts Koin before any screen is created. */
class HwCubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@HwCubApplication)
            modules(appModules)
        }
    }
}
