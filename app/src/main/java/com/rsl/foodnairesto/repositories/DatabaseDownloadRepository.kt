package com.rsl.foodnairesto.repositories

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Log.e
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.rsl.foodnairesto.data.database_download.DatabaseDownloadDao
import com.rsl.foodnairesto.data.database_download.models.ProductGroupModel
import com.rsl.foodnairesto.data.database_download.network.DatabaseDownloadDataSource
import com.rsl.foodnairesto.utils.AppExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList
import kotlin.coroutines.CoroutineContext

@SuppressLint("LogNotTimber")
class DatabaseDownloadRepository constructor(
    private val databaseDao: DatabaseDownloadDao,
    private val databaseDataSource: DatabaseDownloadDataSource?,
    private val mExecutors: AppExecutors
) {

    companion object {
        @Volatile
        private var sInstance: DatabaseDownloadRepository? = null

        fun getInstance(
            databaseDao: DatabaseDownloadDao,
            databaseDataSource: DatabaseDownloadDataSource?,
            mExecutors: AppExecutors
        ) =
            sInstance ?: synchronized(this) {
                e(DatabaseDownloadRepository::class.java.simpleName, "getInstance")
                sInstance ?: DatabaseDownloadRepository(databaseDao, databaseDataSource, mExecutors)
            }
    }

    private val mSuccessFlag = MutableLiveData<Int>()

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    private var mCount: Int = 0

    private var mMutableFavoriteSuccess: MutableLiveData<String>? = null


    fun downloadFavoriteItems(
        mRestaurantID: String,
        mGroupList: ArrayList<ProductGroupModel>,
        mLocations: ArrayList<String>
    ): LiveData<String> {
        mMutableFavoriteSuccess = MutableLiveData()

        Handler(Looper.getMainLooper()).post {
            databaseDataSource!!.getFavoriteItemsData(mRestaurantID, mGroupList, mLocations[0])
                .observeForever { favoriteItemsModels ->

                    mExecutors.diskIO().execute {
                        val mSuccess = databaseDao.insertFavoriteItems(favoriteItemsModels)

                        if (favoriteItemsModels != null && (mSuccess.isNotEmpty() || favoriteItemsModels.isEmpty())) {
                            insertFavouriteItems(1, mRestaurantID, mGroupList, mLocations)
                        }
                    }
                }
        }

        return mMutableFavoriteSuccess!!
    }

    private fun insertFavouriteItems(
        count: Int,
        mRestaurantID: String,
        mGroupModels: List<ProductGroupModel>,
        mLocations: ArrayList<String>
    ) {
        mCount = count

        e(javaClass.simpleName, "insertFavouriteItems: mCount: $mCount")

        for (i in 0 until mLocations.size) {
            if (i == mCount) {

                Handler(Looper.getMainLooper()).post {
                    databaseDataSource?.getFavoriteItemsData(
                        mRestaurantID, ArrayList(mGroupModels),
                        mLocations[mCount]
                    )!!.observeForever { favoriteItemsModels ->

                        mExecutors.diskIO().execute {
                            val mSuccess = databaseDao.insertFavoriteItems(favoriteItemsModels)

                            if (favoriteItemsModels != null && (mSuccess.isNotEmpty() || favoriteItemsModels.isEmpty())) {
                                e(javaClass.simpleName, "insertFavouriteItems: success")
                                mCount++
                                insertFavouriteItems(mCount, mRestaurantID, mGroupModels, mLocations)
                            }
                        }
                    }
                }

                break
            }
        }

        if (mLocations.size - 1 == mCount) {
            mMutableFavoriteSuccess!!.postValue("Success")
        }
    }

    private var mDataObserver: Observer<Int>? = null

    fun performGetAllData(mRestaurantID: String?): LiveData<Int> {

        deleteAllToReset().observeForever { delete ->
            if (delete > -1) {
                e("DatabaseDownload", "performGetAllData")

                val scope = CoroutineScope(coroutineContext)

                val getAllData = databaseDataSource!!.performGetAllData(mRestaurantID)

                mDataObserver = Observer {
                    Log.d(javaClass.simpleName, "it : $it")
                    mSuccessFlag.postValue(it)
                    getAllData.removeObserver(mDataObserver!!)
                }

                getAllData.observeForever(mDataObserver!!)

                databaseDataSource.getAllergenData().observeForever {
                    scope.launch(Dispatchers.IO) {
                        databaseDao.insertAllergens(it)
                    }
                }

                databaseDataSource.getGroupData().observeForever {
                    scope.launch(Dispatchers.IO) {
                        databaseDao.insertProductGroups(it)
                        //mGroupList.addAll(it)
                    }
                }

                databaseDataSource.getIngredientsData().observeForever {
                    scope.launch(Dispatchers.IO) {
                        databaseDao.insertIngredients(it)
                    }
                }

                databaseDataSource.getLocationData().observeForever {
                    scope.launch(Dispatchers.IO) {
                        databaseDao.insertLocations(it)
                    }
                }

                databaseDataSource.getTaxData().observeForever {
                    scope.launch(Dispatchers.IO) {
                        databaseDao.insertTaxes(it)
                    }
                }

                databaseDataSource.getKitchenData().observeForever {
                    scope.launch(Dispatchers.IO) {
                        e("DatabaseDownload", "getKitchenData")
                        databaseDao.insertKitchens(it)
                    }
                }

                databaseDataSource.getRestaurantTablesData().observeForever {
                    scope.launch(Dispatchers.IO) {
                        databaseDao.insertTables(it)
                    }
                }

                databaseDataSource.getServerData().observeForever {
                    scope.launch(Dispatchers.IO) {
                        databaseDao.insertServers(it)
                    }
                }

                databaseDataSource.getPaymentMethodData().observeForever {
                    scope.launch(Dispatchers.IO) {
                        databaseDao.insertPaymentMethods(it)
                    }
                }
            }

        }


        return mSuccessFlag
    }

    fun getAllGroups(): List<ProductGroupModel> {
        return databaseDao.getGroupDataFromDB()
    }

    fun getAllLocations(): List<String> {
        return databaseDao.getLocationID()
    }

    private var mDeleteDataMain: MutableLiveData<Int>? = null

    fun deleteAllWithMainLogin(): LiveData<Int> {
        mDeleteDataMain = MutableLiveData()
        val scope = CoroutineScope(coroutineContext)
        scope.launch(Dispatchers.IO) {
//            val mDeleteRestaurant= databaseDao.deleteMainLogin()
//            if (mDeleteRestaurant > -1){
//                deleteAllToReset()
//            }
        }
        return mDeleteDataMain!!
    }

    fun deleteAllToReset(): LiveData<Int> {
        val mDeleteData = MutableLiveData<Int>()
        val scope = CoroutineScope(coroutineContext)
        scope.launch(Dispatchers.IO) {
            val mDeleteProducts = databaseDao.deleteProducts()

            when {
                mDeleteProducts > -1 -> {
                    val mDeleteAllergen = databaseDao.deleteAllergen()
                    when {
                        mDeleteAllergen > -1 -> {
                            val mDeleteFavItems = databaseDao.deleteFavoriteItems()
                            when {
                                mDeleteFavItems > -1 -> {
                                    val mDeleteIngredients = databaseDao.deleteIngredients()
                                    when {
                                        mDeleteIngredients > -1 -> {
                                            val mDeleteKitchen = databaseDao.deleteKitchen()
                                            when {
                                                mDeleteKitchen > -1 -> {
                                                    val mDeleteLocation = databaseDao.deleteLocation()
                                                    when {
                                                        mDeleteLocation > -1 -> {
                                                            val mDeletePaymentMethod = databaseDao.deletePaymentMethod()
                                                            when {
                                                                mDeletePaymentMethod > -1 -> {
                                                                    val mDeleteServerLogin =
                                                                        databaseDao.deleteServerLogin()
                                                                    when {
                                                                        mDeleteServerLogin > -1 -> {
                                                                            val mDeleteServerShift =
                                                                                databaseDao.deleteServerShift()
                                                                            when {
                                                                                mDeleteServerShift > -1 -> {
                                                                                    val mDeleteServers =
                                                                                        databaseDao.deleteServers()
                                                                                    when {
                                                                                        mDeleteServers > -1 -> {
                                                                                            val mDeleteTables =
                                                                                                databaseDao.deleteTables()
                                                                                            when {
                                                                                                mDeleteTables > -1 -> {
                                                                                                    e(
                                                                                                        "DOWNLOAD",
                                                                                                        "deleteAllToReset: 1: $mDeleteProducts"
                                                                                                    )
                                                                                                    e(
                                                                                                        "DOWNLOAD",
                                                                                                        "deleteAllToReset: 2: $mDeleteAllergen"
                                                                                                    )
                                                                                                    e(
                                                                                                        "DOWNLOAD",
                                                                                                        "deleteAllToReset: 3: $mDeleteFavItems"
                                                                                                    )
                                                                                                    e(
                                                                                                        "DOWNLOAD",
                                                                                                        "deleteAllToReset: 4: $mDeleteIngredients"
                                                                                                    )
                                                                                                    e(
                                                                                                        "DOWNLOAD",
                                                                                                        "deleteAllToReset: 5: $mDeleteKitchen"
                                                                                                    )
                                                                                                    e(
                                                                                                        "DOWNLOAD",
                                                                                                        "deleteAllToReset: 6: $mDeleteLocation"
                                                                                                    )
                                                                                                    e(
                                                                                                        "DOWNLOAD",
                                                                                                        "deleteAllToReset: 8: $mDeletePaymentMethod"
                                                                                                    )
                                                                                                    e(
                                                                                                        "DOWNLOAD",
                                                                                                        "deleteAllToReset: 9: $mDeleteServerLogin"
                                                                                                    )
                                                                                                    e(
                                                                                                        "DOWNLOAD",
                                                                                                        "deleteAllToReset: 10: $mDeleteServerShift"
                                                                                                    )
                                                                                                    e(
                                                                                                        "DOWNLOAD",
                                                                                                        "deleteAllToReset: 11: $mDeleteServers"
                                                                                                    )
                                                                                                    e(
                                                                                                        "DOWNLOAD",
                                                                                                        "deleteAllToReset: 12: $mDeleteTables"
                                                                                                    )
                                                                                                    mDeleteData.postValue(
                                                                                                        databaseDao.deleteTax()
                                                                                                    )


                                                                                                    if (mDeleteDataMain != null){
                                                                                                        mDeleteDataMain!!.postValue(1)
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return mDeleteData
    }

    fun getReportsData(mRestaurantID: String, mDate: String): LiveData<Boolean> {
        val mMutableResult = MutableLiveData<Boolean>()
        databaseDataSource!!.getReportData(mRestaurantID, mDate).observeForever { reportModels ->
            when {
                reportModels != null -> if (reportModels.isNotEmpty()) {
                    when (reportModels[0].mRestaurantID) {
                        "0" -> mMutableResult.postValue(false)
                        else -> {
                            val scope = CoroutineScope(coroutineContext)
                            scope.launch(Dispatchers.IO) {
                                val mInsertResult = databaseDao.insertReportsData(reportModels)
                                when {
                                    mInsertResult.isNotEmpty() -> mMutableResult.postValue(true)
                                    else -> mMutableResult.postValue(false)
                                }
                            }
                        }
                    }
                } else {
                    mMutableResult.postValue(false)
                }
            }

        }
        return mMutableResult
    }
}