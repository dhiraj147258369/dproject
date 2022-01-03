package com.rsl.youresto.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rsl.youresto.data.database_download.models.FavoriteItemsModel
import com.rsl.youresto.data.database_download.models.ProductGroupModel
import com.rsl.youresto.data.favorite_items.FavoriteItemsDao
import com.rsl.youresto.data.favorite_items.network.FavoriteItemsNetworkDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class FavoriteItemsRepository constructor(
    private val favoriteItemsDao: FavoriteItemsDao,
    private val mDataSource: FavoriteItemsNetworkDataSource
) {

    companion object {
        @Volatile
        private var sInstance: FavoriteItemsRepository? = null

        fun getInstance(favoriteItemsDao: FavoriteItemsDao, mDataSource: FavoriteItemsNetworkDataSource) =
            sInstance ?: synchronized(this) {
                sInstance ?: FavoriteItemsRepository(favoriteItemsDao, mDataSource)
            }
    }

    private var parentJob = Job()
    private val coRoutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    fun getGroupProducts(): LiveData<List<ProductGroupModel>> {
        return favoriteItemsDao.getProductGroups()
    }

    fun getGroupProducts(mGroupID: String): LiveData<List<ProductGroupModel>> {
        return favoriteItemsDao.getProductGroups(mGroupID)
    }

    fun getFavoriteItemsFromDB(mCategoryId: String, mLocationId: String): LiveData<List<FavoriteItemsModel>> {
        return favoriteItemsDao.getFavoriteItemsFromDB(mCategoryId, mLocationId)
    }

    fun getAllFavoriteItemsFromDB(mLocationID: String): LiveData<List<FavoriteItemsModel>> {
        return favoriteItemsDao.getAllFavoriteItemsFromDB(mLocationID)
    }

    fun updateFavoriteItemToDB(mFavoriteItemModel: FavoriteItemsModel, mCategoryId: String, mLocationID: String): LiveData<Int> {
        val mMutableUpdateFavoriteItem = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mMutableUpdateFavoriteItem.postValue(
                favoriteItemsDao.updateFavoriteProductList(
                    mFavoriteItemModel.mProductArrayList, mCategoryId, mLocationID
                )
            )
        }
        return mMutableUpdateFavoriteItem
    }

    fun insertFavoriteItemToDB(mFavoriteItemModel: FavoriteItemsModel): LiveData<Long> {
        val mMutableInsertFavoriteItem = MutableLiveData<Long>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mMutableInsertFavoriteItem.postValue(favoriteItemsDao.insertFavoriteItemToDB(mFavoriteItemModel))
        }
        return mMutableInsertFavoriteItem
    }

    fun saveFavoriteItemsToAPI(mFavoriteItemModel: List<FavoriteItemsModel>): LiveData<Int> {
        val mMutableSaveFavoriteItems = MutableLiveData<Int>()
        mDataSource.saveFavoriteItemsToAPI(mFavoriteItemModel).observeForever(mMutableSaveFavoriteItems::postValue)
        return mMutableSaveFavoriteItems
    }
}