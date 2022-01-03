package com.rsl.youresto.utils

import android.content.Context
import com.rsl.youresto.data.AppDatabase
import com.rsl.youresto.data.cart.CartDataSource
import com.rsl.youresto.data.checkout.CheckoutDataSource
import com.rsl.youresto.data.database_download.network.DatabaseDownloadDataSource
import com.rsl.youresto.data.favorite_items.network.FavoriteItemsNetworkDataSource
import com.rsl.youresto.data.server_login.ServerLoginDataSource
import com.rsl.youresto.data.tables.TableDataSource
import com.rsl.youresto.repositories.*
import com.rsl.youresto.ui.database_download.DatabaseDownloadViewModelFactory
import com.rsl.youresto.ui.main_screen.app_settings.AppSettingsViewModelFactory
import com.rsl.youresto.ui.main_screen.cart.CartViewModelFactory
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModelFactory
import com.rsl.youresto.ui.main_screen.favorite_items.FavoriteItemsViewModelFactory
import com.rsl.youresto.ui.main_screen.main_product_flow.MainProductViewModelFactory
import com.rsl.youresto.ui.main_screen.order_history.OrderHistoryViewModelFactory
import com.rsl.youresto.ui.main_screen.tables_and_tabs.tables.TablesViewModelFactory
import com.rsl.youresto.ui.server_login.ServerLoginViewModelFactory

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

    fun provideDatabaseDownloadViewModelFactory(context: Context): DatabaseDownloadViewModelFactory {

        val database: AppDatabase? = AppDatabase.getInstance(context)
        val databaseDownloadDataSource: DatabaseDownloadDataSource? = DatabaseDownloadDataSource.getInstance(context)
        val appExecutors = AppExecutors.getInstance()

        val databaseDownloadRepository =
            DatabaseDownloadRepository.getInstance(database!!.databaseDownloadDao(), databaseDownloadDataSource, appExecutors)

        return DatabaseDownloadViewModelFactory(databaseDownloadRepository)
    }

    fun provideServerLoginViewModelFactory(context: Context) : ServerLoginViewModelFactory{
        val database: AppDatabase? = AppDatabase.getInstance(context)
        val  serverLoginDataSource: ServerLoginDataSource? = ServerLoginDataSource.getInstance(context)

        val serverLoginRepository = ServerLoginRepository.getInstance(database!!.serverLoginDao(), serverLoginDataSource)

        return ServerLoginViewModelFactory(serverLoginRepository)
    }

    fun provideTablesViewModelFactory(context: Context) : TablesViewModelFactory {
        val database: AppDatabase? = AppDatabase.getInstance(context)
        val tablesDataSource: TableDataSource? = TableDataSource.getInstance(context)
        val tablesRepository: TablesRepository = TablesRepository.getInstance(database!!.tablesDao(), tablesDataSource)

        return TablesViewModelFactory(tablesRepository)
    }

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

    fun provideCheckoutViewModelFactory(context: Context) : CheckoutViewModelFactory {
        val database  = AppDatabase.getInstance(context)
        val checkoutDataSource: CheckoutDataSource = CheckoutDataSource.getInstance(context)!!
        val checkoutRepository = CheckoutRepository.getInstance(database!!.checkoutDao(), checkoutDataSource)

        return CheckoutViewModelFactory(checkoutRepository)
    }

    fun provideOrderHistoryViewModelFactory(context: Context) : OrderHistoryViewModelFactory {
        val database  = AppDatabase.getInstance(context)
        val orderHistoryRepository = OrderHistoryRepository.getInstance(database!!.orderHistoryDao())

        return OrderHistoryViewModelFactory(orderHistoryRepository)
    }
}