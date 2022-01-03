package com.rsl.youresto.data.favorite_items

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rsl.youresto.data.database_download.models.FavoriteItemsModel
import com.rsl.youresto.data.database_download.models.ProductCategoryModel
import com.rsl.youresto.data.database_download.models.ProductGroupModel
import com.rsl.youresto.data.database_download.models.ProductModel
import com.rsl.youresto.ui.main_screen.favorite_items.model.FavoriteProductModel
import java.util.ArrayList

@Dao
interface FavoriteItemsDao {

    @Query("SELECT * FROM ProductCategoryModel")
    fun getCategories(): List<ProductCategoryModel>

    @Query("SELECT * FROM ProductModel WHERE mCategoryID =:categoryId")
    fun getProducts(categoryId: String): List<ProductModel>

    @Query("SELECT * FROM FavoriteItemsModel WHERE mCategoryID =:mCategoryId AND mLocationID =:mLocationId")
    fun getFavorites(mCategoryId: String, mLocationId: String): List<FavoriteItemsModel>

    @Query("""SELECT favorite.mID, favorite.mGroupID, favorite.mGroupName, favorite.mCategoryID, category.mCategoryName, favorite.mCategorySequence, 
        favorite.mLocationID, favorite.mProductArrayList FROM FavoriteItemsModel as favorite INNER JOIN ProductCategoryModel as category 
        WHERE mLocationID =:mLocationID AND category.mCategoryID = favorite.mCategoryID""")
    fun getAllFavorite(mLocationID: String): List<FavoriteItemsModel>


    @Query("SELECT * FROM ProductGroupModel")
    fun getProductGroups(): LiveData<List<ProductGroupModel>>

    @Query("SELECT * FROM ProductGroupModel WHERE mGroupID =:mGroupID")
    fun getProductGroups(mGroupID: String): LiveData<List<ProductGroupModel>>

    @Query("SELECT * FROM FavoriteItemsModel WHERE mCategoryID =:mCategoryId AND mLocationID =:mLocationId")
    fun getFavoriteItemsFromDB(mCategoryId: String, mLocationId: String): LiveData<List<FavoriteItemsModel>>

    @Insert
    fun insertFavoriteItemToDB(mFavoriteItemModel: FavoriteItemsModel): Long

    @Query("UPDATE FavoriteItemsModel SET mProductArrayList =:mProductArrayList WHERE mCategoryID=:mCategoryId AND mLocationID=:mLocationID")
    fun updateFavoriteProductList(mProductArrayList: ArrayList<FavoriteProductModel>, mCategoryId: String, mLocationID: String): Int

    @Query("SELECT * FROM FavoriteItemsModel WHERE mLocationID =:mLocationID")
    fun getAllFavoriteItemsFromDB(mLocationID: String): LiveData<List<FavoriteItemsModel>>
}