package com.rsl.foodnairesto.utils

import android.content.Context
import com.rsl.foodnairesto.data.AppDatabase
import com.rsl.foodnairesto.data.cart.CartDataSource
import com.rsl.foodnairesto.data.favorite_items.network.FavoriteItemsNetworkDataSource
import com.rsl.foodnairesto.repositories.*
import com.rsl.foodnairesto.ui.main_screen.app_settings.AppSettingsViewModelFactory
import com.rsl.foodnairesto.ui.main_screen.cart.CartViewModelFactory
import com.rsl.foodnairesto.ui.main_screen.favorite_items.FavoriteItemsViewModelFactory
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.MainProductViewModelFactory
import com.rsl.foodnairesto.ui.main_screen.order_history.OrderHistoryViewModelFactory

object InjectorUtils {
    // This will be called from QuotesActivity

//    fun provideMainLoginViewModelFactory(context: Context): MainLoginViewModelFactory {
//        // ViewModelFactory needs a repository, which in turn needs a DAO from a database
//        // The whole dependency tree is constructed right here, in one place
//
//        val database: OrdaPOSDatabase? = OrdaPOSDatabase.getInstance(context)
//        val mainLoginDataSource: MainLoginDataSource? = MainLoginDataSource.getInstance(context)
//
//        val mainLoginRepository =
//            MainLoginRepository.getInstance(
//                database!!.mainLoginDao(),
//                mainLoginDataSource
//            )
//        return MainLoginViewModelFactory(mainLoginRepository)
//    }

    fun provideMainProductViewModelFactory(context: Context) : MainProductViewModelFactory {
        val database : AppDatabase? = AppDatabase.getInstance(context)
        val mainProductRepository: MainProductRepository = MainProductRepository.getInstance(database!!.mainProductDao())

        return MainProductViewModelFactory(mainProductRepository)
    }

    fun provideAppSettingsViewModelFactory(context: Context) : AppSettingsViewModelFactory {
        val database : AppDatabase? = AppDatabase.getInstance(context)
        val appSettingsRepository: AppSettingsRepository = AppSettingsRepository.getInstance(database!!.appSettingsDao())

        return AppSettingsViewModelFactory(appSettingsRepository)
    }

    fun provideFavoriteItemsViewModelFactory(context: Context) : FavoriteItemsViewModelFactory {
        val database : AppDatabase? = AppDatabase.getInstance(context)
        val favoriteItemsDataSource: FavoriteItemsNetworkDataSource? = FavoriteItemsNetworkDataSource.getInstance(context)
        val favoriteItemsRepository: FavoriteItemsRepository = FavoriteItemsRepository.getInstance(database!!.favoriteItemsDao(),favoriteItemsDataSource!!)

        return FavoriteItemsViewModelFactory(favoriteItemsRepository)
    }

    fun provideCartViewModelFactory(context: Context) : CartViewModelFactory{
        val database  = AppDatabase.getInstance(context)
        val cartDataSource = CartDataSource.getInstance(context)
        val cartRepository = CartRepository.getInstance(database!!.cartDao(), cartDataSource!!)

        return CartViewModelFactory(cartRepository)
    }

    fun provideOrderHistoryViewModelFactory(context: Context) : OrderHistoryViewModelFactory {
        val database  = AppDatabase.getInstance(context)
        val orderHistoryRepository = OrderHistoryRepository.getInstance(database!!.orderHistoryDao())

        return OrderHistoryViewModelFactory(orderHistoryRepository)
    }
}