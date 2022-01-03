package com.rsl.youresto.di

import com.rsl.youresto.data.cart.CartApi
import com.rsl.youresto.data.cart.CartRemoteSource
import com.rsl.youresto.data.favorite_items.network.FavoriteApi
import com.rsl.youresto.data.favorite_items.network.FavoriteDataSource
import com.rsl.youresto.data.order_history.OrderHistoryRemoteSource
import com.rsl.youresto.network.*
import org.koin.dsl.module
import retrofit2.Retrofit

val dataSourceModule = module {
    single { LoginRemoteSource(get(), get()) }
    single { get<Retrofit>().create(LoginApi::class.java) }

    single { ProductRemoteSource(get(), get()) }
    single { get<Retrofit>().create(ProductApi::class.java) }

    single { CartRemoteSource(get(), get()) }
    single { get<Retrofit>().create(CartApi::class.java) }

    single { OrderHistoryRemoteSource(get(), get()) }
    single { get<Retrofit>().create(OrdersApi::class.java) }

    single { FavoriteDataSource(get(), get()) }
    single { get<Retrofit>().create(FavoriteApi::class.java) }
}
