package com.rsl.youresto.repositories

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rsl.youresto.data.database_download.models.AllergenModel
import com.rsl.youresto.data.database_download.models.ProductGroupModel
import com.rsl.youresto.data.main_product.MainProductDao
import com.rsl.youresto.utils.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainProductRepository(private val mainProductDao: MainProductDao) {

    companion object {
        @Volatile
        private var sInstance: MainProductRepository? = null

        fun getInstance(mainProductDao: MainProductDao) =
            sInstance ?: synchronized(this){
                sInstance ?: MainProductRepository(mainProductDao)
            }

    }

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    fun getProductGroups() : LiveData<List<ProductGroupModel>> {
        return mainProductDao.getProductGroups(0)
    }

    fun getProductGroupCategory(mGroupID : String?) : LiveData<ProductGroupModel>{
        return mainProductDao.getProductGroupCategory(mGroupID)
    }

    fun getAllergens(mAllergenIDs: List<String>): LiveData<List<AllergenModel>> {
        return mainProductDao.getAllergens(mAllergenIDs)
    }

    fun checkInternet(mContext: Context): LiveData<Boolean> {
        val mInternetData = MutableLiveData<Boolean>()
        val scope = CoroutineScope(coroutineContext)
        scope.launch(Dispatchers.IO) {
            Handler(Looper.getMainLooper()).post {
                Network.isNetworkAvailableWithInternetAccess(mContext).observeForever {
                    mInternetData.postValue(it)
                }
            }
        }
        return mInternetData
    }

}