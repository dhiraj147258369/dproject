package com.rsl.youresto.repositories

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rsl.youresto.data.main_login.MainLoginDao
import com.rsl.youresto.data.main_login.MainLoginMediatorModel
import com.rsl.youresto.data.main_login.network.MainLoginDataSource
import com.rsl.youresto.utils.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainLoginRepository constructor(
    private val loginDao: MainLoginDao,
    private val mainLoginDataSource: MainLoginDataSource?
) {

    companion object {
        @Volatile
        private var sInstance: MainLoginRepository? = null

        fun getInstance(loginDao: MainLoginDao, mainLoginDataSource: MainLoginDataSource?) =
            sInstance ?: synchronized(this) {
                sInstance ?: MainLoginRepository(loginDao, mainLoginDataSource)
            }
    }

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    fun getLoginData(mUsername: String, mPassword: String, mContext: Context): LiveData<MainLoginMediatorModel> {
        val mMainLoginMediatorData = MutableLiveData<MainLoginMediatorModel>()

        val scope = CoroutineScope(coroutineContext)

        Handler(Looper.getMainLooper()).post {
            Network.isNetworkAvailableWithInternetAccess(mContext).observeForever {
                if(it) {
                    mainLoginDataSource!!.getLoginData(mUsername, mPassword).observeForever { loginResponse ->
                        if (loginResponse.successFlag == 1) {
                            scope.launch(Dispatchers.IO) {
//                                loginDao.insertMainLoginData(loginResponse.mainLoginModel)
                            }
                        }
                        mMainLoginMediatorData.postValue(loginResponse)
                    }
                } else {
                    mMainLoginMediatorData.postValue(MainLoginMediatorModel(null,-1))
                }
            }
        }
        return mMainLoginMediatorData
    }

}