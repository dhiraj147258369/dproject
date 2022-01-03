package com.rsl.youresto.data.favorite_items.network

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rsl.youresto.data.database_download.models.FavoriteItemsModel

class FavoriteItemsNetworkDataSource(val context: Context): FavoriteItemsNetworkUtils.FavoriteItemNetworkUtilInterface {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sInstance : FavoriteItemsNetworkDataSource? = null

        fun getInstance(context: Context) : FavoriteItemsNetworkDataSource?{
            val tempInstance = sInstance
            if (tempInstance != null)
                return tempInstance

            sInstance ?: synchronized(this){
                sInstance ?: FavoriteItemsNetworkDataSource(context).also { sInstance = it }
            }

            return sInstance
        }
    }

    private var mMutableFavoriteItem: MutableLiveData<Int>? = null

    fun saveFavoriteItemsToAPI(mFavoriteItemModel: List<FavoriteItemsModel>): LiveData<Int> {
        mMutableFavoriteItem  = MutableLiveData()
        saveFavoriteItems(mFavoriteItemModel)
        return mMutableFavoriteItem!!
    }

    private fun saveFavoriteItems(mFavoriteItemModel: List<FavoriteItemsModel>) {
        FavoriteItemsNetworkUtils.saveFavoriteItems(context, mFavoriteItemModel, this)
    }

    override fun responseFromFavoriteItemSaved(mResultValue: Int) {
        mMutableFavoriteItem!!.postValue(mResultValue)
    }

}