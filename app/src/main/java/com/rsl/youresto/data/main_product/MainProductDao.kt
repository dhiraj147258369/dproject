package com.rsl.youresto.data.main_product

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import androidx.room.Update
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.database_download.models.*

@Dao
interface MainProductDao {

    @Query("SELECT * FROM ProductGroupModel WHERE mDoNotDisplayOn =:mDisplayFlag")
    fun getProductGroups(mDisplayFlag:Int) : LiveData<List<ProductGroupModel>>

    @Query("SELECT * FROM ProductCategoryModel WHERE mGroupID =:mGroupID")
    fun getProductCategories(mGroupID : String?) : LiveData<List<ProductCategoryModel>>

    @Query("SELECT * FROM ProductModel WHERE mCategoryID =:categoryId")
    fun getProducts(categoryId : String?) : LiveData<List<ProductModel>>

    @Query("SELECT * FROM ProductModel WHERE mCategoryID =:categoryId AND mProductName LIKE :searchText")
    fun getSearchedProducts(categoryId : String, searchText: String) : LiveData<List<ProductModel>>

    @Query("SELECT * FROM ProductModel")
    fun getAllProducts() : LiveData<List<ProductModel>>

    @Query("SELECT * FROM ProductModel WHERE mProductName LIKE :searchText")
    fun getSearchedAllProducts(searchText: String) : LiveData<List<ProductModel>>

    @Query("SELECT * FROM ProductModel WHERE mProductID = :productId")
    fun getProduct(productId: String): LiveData<ProductModel>

    @Query("SELECT * FROM IngredientsModel WHERE mIngredientID IN (:ingredientIds)")
    fun getProductIngredients(ingredientIds: List<String>): List<IngredientsModel>


    @Insert(onConflict = REPLACE)
    fun saveCartProduct(product: CartProductModel): Long

    @Update
    fun updateCartProduct(product: CartProductModel)


    @Query("SELECT * FROM CartProductModel WHERE mTableID =:mTableNO")
    fun getCartData(mTableNO: String): List<CartProductModel>

    @Query("SELECT * FROM CartProductModel WHERE mCartID =:mCartId")
    fun getCartDataById(mCartId: String): List<CartProductModel>

    @Query("SELECT * FROM CartProductModel WHERE mCartProductID=:mCartProductID")
    fun getCartProductByID(mCartProductID: String):CartProductModel

    @Query("SELECT COUNT(*) FROM CartProductModel")
    fun getCartCount(): Int


    @Query("SELECT * FROM TablesModel WHERE mLocationID =:locationId")
    fun getTables(locationId: String): List<TablesModel>



    @Query("SELECT * FROM ProductGroupModel WHERE mGroupID =:mGroupID")
    fun getProductGroupCategory(mGroupID : String?) : LiveData<ProductGroupModel>

    @Query("SELECT * FROM AllergenModel WHERE mAllergenID IN (:mAllergenIDs)")
    fun getAllergens(mAllergenIDs: List<String>): LiveData<List<AllergenModel>>

}