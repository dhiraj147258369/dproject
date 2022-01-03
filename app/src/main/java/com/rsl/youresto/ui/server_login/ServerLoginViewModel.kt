package com.rsl.youresto.ui.server_login

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rsl.youresto.data.database_download.models.LocationModel
import com.rsl.youresto.data.database_download.models.ServerModel
import com.rsl.youresto.data.server_login.models.ServerLoginModel
import com.rsl.youresto.data.server_login.models.ServerShiftModel
import com.rsl.youresto.repositories.ServerLoginRepository
import java.util.*

class ServerLoginViewModel constructor(private val mRepository: ServerLoginRepository) :ViewModel(){

    fun getLocations() : LiveData<List<LocationModel>> {
        return mRepository.getLocations()
    }

    fun getServers() :LiveData<List<ServerModel>>{
        return mRepository.getServers()
    }

    fun getServerShiftDetails(
        mServerID: String,
        mCurrentDate: Date,
        mFutureDate: Date
    ): LiveData<List<ServerShiftModel>> {
        return mRepository.getServerShiftDetails(mServerID, mCurrentDate, mFutureDate)
    }

    fun getServerLoginDetails(mServerID: String): LiveData<ServerLoginModel> {
        return mRepository.getServerLoginDetails(mServerID)
    }

    fun submitLoginDetails(mServerID: String, mServerName: String, mLoginFlag: String) {
        mRepository.submitLoginDetails(mServerID, mServerName, mLoginFlag)
    }

    fun submitLogoutDetails(mServerID: String, mServerName: String, mLoginFlag: String): LiveData<Int> {
        return mRepository.submitLoginDetails(mServerID, mServerName, mLoginFlag)
    }

    fun getShiftCount(mServerID: String, mCurrentDate: Date, mFutureDate: Date): LiveData<Int> {
        return mRepository.getShiftCount(mServerID, mCurrentDate, mFutureDate)
    }

    fun startShift(mServerID: String): LiveData<Int> {
        return mRepository.startShift(mServerID)
    }

    fun endShift(mServerID: String): LiveData<Int> {
        return mRepository.endShift(mServerID)
    }
}