package com.rsl.youresto.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.facebook.stetho.okhttp3.StethoInterceptor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.rsl.youresto.BuildConfig
import com.rsl.youresto.data.AppDatabase
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {

    single {
        val client = provideOkHttpClient()
        provideRetrofitInstance(client)
    }

    single { provideSharedPrefs(get()) }
    single { provideDataStore(get()) }
}

fun provideAppDataBase(application: Application): AppDatabase {
    return AppDatabase.getInstance(application)!!
}

val loggingInterceptor = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
}
fun provideOkHttpClient(): OkHttpClient {

    return OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .addNetworkInterceptor(StethoInterceptor())
        .addNetworkInterceptor(loggingInterceptor)
        .build()
}

fun provideRetrofitInstance(client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()
}

fun provideGson(): Gson = GsonBuilder().create()

fun provideSharedPrefs(application: Application): SharedPreferences =
    application.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)

fun provideDataStore(application: Application): AppPreferences = AppPreferences(application)