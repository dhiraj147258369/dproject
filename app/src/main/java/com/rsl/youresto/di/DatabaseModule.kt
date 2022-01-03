package com.rsl.youresto.di

import org.koin.dsl.module

val databaseModule = module {

    single { provideAppDataBase(get()) }

    single { provideAppDataBase(get()).mainLoginDao() }
    single { provideAppDataBase(get()).tablesDao() }
    single { provideAppDataBase(get()).mainProductDao() }
    single { provideAppDataBase(get()).cartDao() }
    single { provideAppDataBase(get()).orderHistoryDao() }
    single { provideAppDataBase(get()).favoriteItemsDao() }
}