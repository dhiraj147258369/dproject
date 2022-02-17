package com.rsl.foodnairesto.di

import com.rsl.foodnairesto.repositories.*
import org.koin.dsl.module

val repoModule = module {
    single { LoginRepository(get(), get()) }
    single { NewTablesRepository(get(), get()) }
    single { NewProductRepository(get(), get()) }
    single { NewCartRepository(get(), get()) }
    single { NewOrdersRepository(get(), get()) }
    single { NewFavoriteItemRepository(get(), get()) }
    single { ServerLoginRepository(get()) }
}
