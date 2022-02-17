package com.rsl.foodnairesto.di

import com.rsl.foodnairesto.data.cart.CartApi
import com.rsl.foodnairesto.data.cart.CartRemoteSource
import com.rsl.foodnairesto.data.favorite_items.network.FavoriteApi
import com.rsl.foodnairesto.data.favorite_items.network.FavoriteDataSource
import com.rsl.foodnairesto.data.order_history.OrderHistoryRemoteSource
import com.rsl.foodnairesto.network.*
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
