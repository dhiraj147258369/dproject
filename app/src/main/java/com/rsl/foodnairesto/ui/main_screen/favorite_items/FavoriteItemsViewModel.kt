package com.rsl.foodnairesto.ui.main_screen.favorite_items


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rsl.foodnairesto.data.database_download.models.FavoriteItemsModel
import com.rsl.foodnairesto.data.database_download.models.ProductGroupModel
import com.rsl.foodnairesto.repositories.FavoriteItemsRepository

class FavoriteItemsViewModel constructor(private val mRepository: FavoriteItemsRepository): ViewModel() {

    fun getGroupProducts(): LiveData<List<ProductGroupModel>> {
        return mRepository.getGroupProducts()
    }

    fun getGroupProducts(mGroupID: String): LiveData<List<ProductGroupModel>> {
        return mRepository.getGroupProducts(mGroupID)
    }

    fun getFavoriteItemsFromDB(mCategoryId: String, mLocationId: String): LiveData<List<FavoriteItemsModel>> {
        return mRepository.getFavoriteItemsFromDB(mCategoryId, mLocationId)
    }

    fun getAllFavoriteItemsFromDB(mLocationID: String): LiveData<List<FavoriteItemsModel>> {
        return mRepository.getAllFavoriteItemsFromDB(mLocationID)
    }

    fun updateFavoriteItemToDB(mFavoriteItemModel: FavoriteItemsModel, mCategoryId: String, mLocationID: String): LiveData<Int> {
        return mRepository.updateFavoriteItemToDB(mFavoriteItemModel, mCategoryId, mLocationID)
    }

    fun insertFavoriteItemToDB(mFavoriteItemModel: FavoriteItemsModel): LiveData<Long> {
        return mRepository.insertFavoriteItemToDB(mFavoriteItemModel)
    }

    fun saveFavoriteItemsToAPI(mFavoriteItemModel: List<FavoriteItemsModel>): LiveData<Int> {
        return mRepository.saveFavoriteItemsToAPI(mFavoriteItemModel)
    }
}