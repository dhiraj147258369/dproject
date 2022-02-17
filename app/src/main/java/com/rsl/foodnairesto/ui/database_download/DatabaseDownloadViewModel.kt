package com.rsl.foodnairesto.ui.database_download

import android.annotation.SuppressLint
import android.util.Log.e
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rsl.foodnairesto.data.database_download.models.ProductGroupModel
import com.rsl.foodnairesto.repositories.DatabaseDownloadRepository


class DatabaseDownloadViewModel constructor(private val mRepository: DatabaseDownloadRepository) :ViewModel(){

    fun performGetAllData(mRestaurantID : String?) : LiveData<Int> {
        return mRepository.performGetAllData(mRestaurantID)
    }

    fun deleteAllWithMainLogin() : LiveData<Int> {
        return mRepository.deleteAllWithMainLogin()
    }

    fun downloadFavoriteItems(mRestaurantID: String, mGroupList: ArrayList<ProductGroupModel>, mLocations: ArrayList<String>) : LiveData<String> {
        return mRepository.downloadFavoriteItems(mRestaurantID, mGroupList, mLocations)
    }

    fun getAllGroups(): List<ProductGroupModel> {
        return mRepository.getAllGroups()
    }

    fun getAllLocations(): List<String> {
        return mRepository.getAllLocations()
    }

    fun deleteAllToReset(): LiveData<Int> {
        return mRepository.deleteAllToReset()
    }

    fun getReportsData(mRestaurantID: String, mDate: String): LiveData<Boolean> {
        return mRepository.getReportsData(mRestaurantID, mDate)
    }

    @SuppressLint("LogNotTimber")
    override fun onCleared() {
        e(javaClass.simpleName, "onCleared")
        super.onCleared()
    }
}