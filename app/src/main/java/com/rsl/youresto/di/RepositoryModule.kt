package com.rsl.youresto.di

import com.rsl.youresto.repositories.*
import org.koin.dsl.module

val repoModule = module {
    single { LoginRepository(get(), get()) }
    single { NewTablesRepository(get()) }
    single { NewProductRepository(get(), get()) }
    single { NewCartRepository(get(), get()) }
    single { NewOrdersRepository(get(), get()) }
    single { NewFavoriteItemRepository(get(), get()) }
    single { ServerLoginRepository(get()) }
}
