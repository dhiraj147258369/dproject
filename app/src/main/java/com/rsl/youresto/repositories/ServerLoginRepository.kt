package com.rsl.youresto.repositories

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.rsl.youresto.data.database_download.models.LocationModel
import com.rsl.youresto.data.database_download.models.ServerModel
import com.rsl.youresto.data.server_login.ServerLoginDao
import com.rsl.youresto.data.server_login.ServerLoginDataSource
import com.rsl.youresto.data.server_login.models.ServerLoginModel
import com.rsl.youresto.data.server_login.models.ServerShiftModel
import com.rsl.youresto.utils.AppConstants.API_SHIFT_END
import com.rsl.youresto.utils.AppConstants.API_SHIFT_START
import kotlinx.coroutines.*
import java.util.*
import kotlin.coroutines.CoroutineContext
@SuppressLint("LogNotTimber")
class ServerLoginRepository constructor(
    private val serverLoginDao: ServerLoginDao,
    private val serverLoginDataSource: ServerLoginDataSource?
) {

    companion object {
        @Volatile
        private var sInstance: ServerLoginRepository? = null

        fun getInstance(serverLoginDao: ServerLoginDao, serverLoginDataSource: ServerLoginDataSource?) =
            sInstance ?: synchronized(this) {
                sInstance ?: ServerLoginRepository(serverLoginDao, serverLoginDataSource)
            }
    }

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    fun getLocations(): LiveData<List<LocationModel>> {
        return serverLoginDao.getLocations()
    }

    fun getServers(): LiveData<List<ServerModel>> {
        return serverLoginDao.getServers()
    }

    private var serverShiftObserver: Observer<List<ServerShiftModel>>? = null
    private var serverStartEndShiftObserver: Observer<List<ServerShiftModel>>? = null

    fun getServerShiftDetails(
        mServerID: String,
        mCurrentDate: Date,
        mFutureDate: Date
    ): LiveData<List<ServerShiftModel>> {

        val scope = CoroutineScope(coroutineContext)

        val mServerShiftData: MutableLiveData<List<ServerShiftModel>> = MutableLiveData()

        val serverShiftData: LiveData<List<ServerShiftModel>> =
            serverLoginDao.getLocalShiftDetails(mServerID, mCurrentDate, mFutureDate)

        serverShiftObserver = Observer {
            when {
                it.isEmpty() -> {
                    serverLoginDataSource!!.getShiftStatusFromServer(mServerID, mCurrentDate).observeForever { serverList ->
                        when {
                            serverList.isNotEmpty() -> {

                                scope.launch(Dispatchers.IO) {
                                    serverLoginDao.insertShiftDetails(serverList)
                                }

                                var mOldShift = false

                                for (i in 0 until serverList.size) {
                                    val mDate = serverList[i].mStartTimeStamp

                                    mOldShift = mDate!!.time < mCurrentDate.time
                                }

                                when {
                                    mOldShift -> {
                                        //start the new shift
                                        val mServerStartEndData = serverLoginDataSource.startEndShift(mServerID, API_SHIFT_START)
                                        serverStartEndShiftObserver = Observer { shiftDetailModel ->
                                            Log.e(javaClass.simpleName, "getServerShiftDetails: Starting shift mOldShift")
                                            when {
                                                shiftDetailModel.isNotEmpty() -> {
                                                    scope.launch(Dispatchers.IO) {
                                                        serverLoginDao.insertShiftDetails(shiftDetailModel)
                                                        mServerShiftData.postValue(shiftDetailModel)
                                                    }
                                                    mServerStartEndData.removeObserver(serverStartEndShiftObserver!!)
                                                }
                                            }
                                        }
                                        mServerStartEndData.observeForever(serverStartEndShiftObserver!!)
                                    }
                                    else -> mServerShiftData.postValue(serverList)
                                }

                            }
                            else -> {
                                //start the new shift
                                val mServerStartEndData = serverLoginDataSource.startEndShift(mServerID, API_SHIFT_START)
                                serverStartEndShiftObserver = Observer { shiftDetailModel ->
                                    Log.e(javaClass.simpleName, "getServerShiftDetails: Starting shift mOldShift")
                                    when {
                                        shiftDetailModel.isNotEmpty() -> {
                                            scope.launch(Dispatchers.IO) {
                                                serverLoginDao.insertShiftDetails(shiftDetailModel)
                                                mServerShiftData.postValue(shiftDetailModel)
                                            }
                                            mServerStartEndData.removeObserver(serverStartEndShiftObserver!!)
                                        }
                                    }
                                }
                                mServerStartEndData.observeForever(serverStartEndShiftObserver!!)
                            }
                        }
                    }
                    serverShiftData.removeObserver(serverShiftObserver!!)
                }
                else -> mServerShiftData.postValue(it)
            }

        }

        serverShiftData.observeForever(serverShiftObserver!!)

        return mServerShiftData
    }

    fun getServerLoginDetails(mServerID: String): LiveData<ServerLoginModel> {
        val mServerLoginData = MutableLiveData<ServerLoginModel>()
        val scope = CoroutineScope(coroutineContext)
        scope.launch(Dispatchers.IO) {
            mServerLoginData.postValue(serverLoginDao.getServerLogin(mServerID))
        }
        return mServerLoginData
    }

    fun submitLoginDetails(mServerID: String, mServerName: String, mLoginFlag: String): LiveData<Int> {
        serverLoginDataSource!!.submitLoginDetails(mServerID, mLoginFlag)
        val mServerData = MutableLiveData<Int>()
        val scope = CoroutineScope(coroutineContext)
        scope.launch(Dispatchers.IO) {
            val mServerLoginModel = ServerLoginModel(mServerID, mServerName, Date(), mLoginFlag)

            val mInsert = serverLoginDao.insertSingleLoginDetails(mServerLoginModel)
            mServerData.postValue(mInsert.toInt())
        }
        return mServerData
    }

    @ExperimentalCoroutinesApi
    fun getAndStoreAllLoginDetails() {

        val scope = CoroutineScope(coroutineContext)

        serverLoginDataSource!!.getAndStoreAllLoginDetails().observeForever { serverLoginModels ->

            scope.launch(Dispatchers.IO) {

                val mLoginModel: Date = serverLoginDao.getLastRecord()

                when {
                    mLoginModel != null -> {
                        val mFinalServerLoginModels = ArrayList<ServerLoginModel>()
                        for (i in 0 until serverLoginModels!!.size) {
                            when {
                                serverLoginModels[i].mDateTime.time > mLoginModel.time -> mFinalServerLoginModels.add(serverLoginModels[i])
                            }
                        }
                        when {
                            mFinalServerLoginModels.size > 0 -> serverLoginDao.insertLoginDetails(mFinalServerLoginModels)
                        }
                    }
                    else -> serverLoginDao.insertLoginDetails(serverLoginModels)
                }

            }

        }

    }

    fun getShiftCount(mServerID: String, mCurrentDate: Date, mFutureDate: Date): LiveData<Int> {
        val mShiftCountData = MutableLiveData<Int>()
        serverLoginDao.getLocalShiftDetails(mServerID, mCurrentDate, mFutureDate)
            .observeForever { shiftModelList ->
                if (shiftModelList != null) {
                    mShiftCountData.postValue(shiftModelList.size)
                }
            }
        return mShiftCountData
    }

    fun startShift(mServerID: String): LiveData<Int> {
        val mStartShiftData = MutableLiveData<Int>()
        serverLoginDataSource!!.startEndShift(mServerID, API_SHIFT_START)
            .observeForever {

                val scope = CoroutineScope(coroutineContext)
                scope.launch(Dispatchers.IO) {
                    serverLoginDao.insertShiftDetails(it)
                    mStartShiftData.postValue(1)
                }

            }
        return mStartShiftData
    }

    fun endShift(mServerID: String): LiveData<Int> {
        val mEndShiftData = MutableLiveData<Int>()

        serverLoginDataSource!!.startEndShift(mServerID, API_SHIFT_END)
            .observeForever { shiftDetailModel ->
                if (shiftDetailModel != null) {

                    val scope = CoroutineScope(coroutineContext)
                    scope.launch(Dispatchers.IO) {
                        val mUpdate =
                            serverLoginDao.updateShiftDetails(shiftDetailModel[0].mEndTimeStamp, mServerID)
                        if (mUpdate > 0)
                            mEndShiftData.postValue(1)
                    }

                }
            }

        return mEndShiftData
    }
}