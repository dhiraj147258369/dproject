package com.rsl.foodnairesto

import android.app.Application
import android.content.res.Resources
import com.facebook.stetho.Stetho
import com.rsl.foodnairesto.di.*
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App: Application() {


    companion object {
        lateinit var resource: Resources
            private set

        var isTablet: Boolean = false
    }

    override fun onCreate() {
        super.onCreate()
        resource = resources
        isTablet = resources.getBoolean(R.bool.isTablet)

        startKoin {
            androidContext(this@App)
            modules(listOf(appModule, databaseModule, dataSourceModule, repoModule, viewModelModule))
        }

        setupStetho()
    }

    private fun setupStetho() {
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this)
        }
    }
}