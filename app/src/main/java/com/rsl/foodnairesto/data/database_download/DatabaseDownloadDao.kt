package com.rsl.foodnairesto.data.database_download

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rsl.foodnairesto.data.database_download.models.*

@Dao
interface DatabaseDownloadDao {

    @Insert
    fun insertAllergens(allergenModels: List<AllergenModel>)

    @Insert
    fun insertProductGroups(groupModels: List<ProductGroupModel>)

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

    @Query("SELECT * FROM LocationModel")
    fun getLocationsFromDB(): List<LocationModel>

    @Query("SELECT mLocationID FROM LocationModel")
    fun getLocationID(): List<String>

    @Query("SELECT * FROM ProductGroupModel")
    fun getGroupDataFromDB(): List<ProductGroupModel>

    @Query("DELETE FROM ProductGroupModel")
    fun deleteProducts(): Int

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

    @Insert
    fun insertReportsData(mReportData: List<ReportModel>): LongArray
}