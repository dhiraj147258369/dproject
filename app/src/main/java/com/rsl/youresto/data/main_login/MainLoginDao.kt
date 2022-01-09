package com.rsl.youresto.data.main_login

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rsl.youresto.data.database_download.models.*
import com.rsl.youresto.data.main_login.network.Login

@Dao
interface MainLoginDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertLoginData(login: Login)

    @Query("SELECT * FROM Login")
    fun getRestaurantData(): Login


    @Insert
    fun insertProductGroups(groupModels: List<ProductGroupModel>)

    @Insert
    fun insertProductCategories(groupModels: List<ProductCategoryModel>)

    @Insert
    fun insertProducts(groupModels: List<ProductModel>)

    @Insert
    fun insertIngredients(ingredientsModels: List<IngredientsModel>)

    @Insert
    fun insertLocations(locationModels: List<LocationModel>)

    @Insert
    fun insertTaxes(locationModels: List<TaxModel>)

    @Insert
    fun insertKitchens(kitchenModels: List<KitchenModel>)

    @Insert
    fun insertTables(tablesModels: List<TablesModel>)

    @Insert
    fun insertServers(serverModels: List<ServerModel>)

    @Insert
    fun insertPaymentMethods(paymentMethodModels: List<PaymentMethodModel>)

    @Insert
    fun insertFavoriteItems(mFavoriteItem: List<FavoriteItemsModel>): LongArray



    @Query("DELETE FROM ProductModel")
    fun deleteProducts(): Int

    @Query("DELETE FROM ProductCategoryModel")
    fun deleteCategories(): Int

    @Query("DELETE FROM ProductGroupModel")
    fun deleteGroups(): Int

    @Query("DELETE FROM AllergenModel")
    fun deleteAllergen(): Int

    @Query("DELETE FROM FavoriteItemsModel")
    fun deleteFavoriteItems(): Int

    @Query("DELETE FROM IngredientsModel")
    fun deleteIngredients(): Int

    @Query("DELETE FROM KitchenModel")
    fun deleteKitchen(): Int

    @Query("DELETE FROM LocationModel")
    fun deleteLocation(): Int

//    @Query("DELETE FROM MainLoginModel")
//    fun deleteMainLogin(): Int

    @Query("DELETE FROM PaymentMethodModel")
    fun deletePaymentMethod(): Int

    @Query("DELETE FROM ServerLoginModel")
    fun deleteServerLogin(): Int

    @Query("DELETE FROM ServerShiftModel")
    fun deleteServerShift(): Int

    @Query("DELETE FROM ServerModel")
    fun deleteServers(): Int

    @Query("DELETE FROM TablesModel")
    fun deleteTables(): Int

    @Query("DELETE FROM TaxModel")
    fun deleteTax(): Int
}