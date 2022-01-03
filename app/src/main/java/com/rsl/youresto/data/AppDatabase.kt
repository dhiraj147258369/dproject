package com.rsl.youresto.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rsl.youresto.data.app_settings.AppSettingsDao
import com.rsl.youresto.data.app_settings.model.SelectablePinPad
import com.rsl.youresto.data.cart.CartDao
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.checkout.CheckoutDao
import com.rsl.youresto.data.checkout.model.CheckoutModel
import com.rsl.youresto.data.database_download.DatabaseDownloadDao
import com.rsl.youresto.data.database_download.models.*
import com.rsl.youresto.data.favorite_items.FavoriteItemsDao
import com.rsl.youresto.data.main_login.MainLoginDao
import com.rsl.youresto.data.main_login.network.Login
import com.rsl.youresto.data.main_product.MainProductDao
import com.rsl.youresto.data.order_history.OrderHistoryDao
import com.rsl.youresto.data.server_login.ServerLoginDao
import com.rsl.youresto.data.server_login.models.ServerLoginModel
import com.rsl.youresto.data.server_login.models.ServerShiftModel
import com.rsl.youresto.data.tables.TablesDao
import com.rsl.youresto.data.tables.models.LocalTableGroupModel
import com.rsl.youresto.utils.Converters

@Database(entities = [Login::class, AllergenModel::class, ProductGroupModel::class, ProductCategoryModel::class, ProductModel::class, IngredientsModel::class,
    LocationModel::class, TablesModel::class, KitchenModel::class, TaxModel::class, ServerModel::class, PaymentMethodModel::class,
    ServerShiftModel::class, ServerLoginModel::class, LocalTableGroupModel::class, FavoriteItemsModel::class,
    CartProductModel::class, CheckoutModel::class, ReportModel::class, SelectablePinPad::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun mainLoginDao(): MainLoginDao

    abstract fun databaseDownloadDao(): DatabaseDownloadDao

    abstract fun serverLoginDao(): ServerLoginDao

    abstract fun tablesDao(): TablesDao

    abstract fun mainProductDao(): MainProductDao

    abstract fun appSettingsDao(): AppSettingsDao

    abstract fun favoriteItemsDao(): FavoriteItemsDao

    abstract fun cartDao(): CartDao

    abstract fun checkoutDao(): CheckoutDao

    abstract fun orderHistoryDao(): OrderHistoryDao

    companion object {
        @Volatile
        private var sInstance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase? {

            val tempInstance = sInstance
            if (tempInstance != null)
                return tempInstance

            if (sInstance == null) {
                sInstance ?: synchronized(this) {
                    sInstance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java, "youresto.db"
                    ).build()
                }
            }

            return sInstance
        }
    }

}