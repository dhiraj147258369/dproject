package com.rsl.youresto.data.database_download.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log.e
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rsl.youresto.data.database_download.models.*
import com.rsl.youresto.ui.main_screen.favorite_items.model.FavoriteProductModel
import org.json.JSONObject

@SuppressLint("LogNotTimber")
class DatabaseDownloadDataSource(val context: Context) : DownloadDatabaseNetworkUtils.DownloadDatabaseNetworkInterface {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sInstance : DatabaseDownloadDataSource? = null

        fun getInstance(context: Context) : DatabaseDownloadDataSource?{
            val tempInstance = sInstance
            if (tempInstance != null)
                return tempInstance

            sInstance ?: synchronized(this){
                sInstance ?: DatabaseDownloadDataSource(context).also { sInstance = it }
            }

            return sInstance
        }
    }

    private var mSuccessFlag  = MutableLiveData<Int>()

    fun performGetAllData(mRestaurantID : String?) : LiveData<Int> {
        fetchData(mRestaurantID)
        return mSuccessFlag
    }

    private fun fetchData(mRestaurantID: String?) {
        val getAllDataRequestUrl = DownloadDatabaseNetworkUtils.getDataURL(mRestaurantID)

        DownloadDatabaseNetworkUtils.getResponseFromDatabaseDownload(context, getAllDataRequestUrl.toString(), this)
    }

    private val mDownloadedAllergenModel =  MutableLiveData<List<AllergenModel>>()
    private val mDownloadedGroupModel = MutableLiveData<List<ProductGroupModel>>()
    private val mDownloadedTypeModel =  MutableLiveData<List<IngredientsModel>>()
    private val mDownloadedLocationModel = MutableLiveData<List<LocationModel>>()
    private val mDownloadedTableModel = MutableLiveData<List<TablesModel>>()
    private val mDownloadedTaxModel = MutableLiveData<List<TaxModel>>()
    private val mDownloadedKitchenModel= MutableLiveData<List<KitchenModel>>()
    private val mDownloadedPaymentModel = MutableLiveData<List<PaymentMethodModel>>()
    private val mDownloadedServerData =  MutableLiveData<List<ServerModel>>()
    private var mMutableFavoriteData: MutableLiveData<List<FavoriteItemsModel>>? = null

    fun getAllergenData(): LiveData<List<AllergenModel>> {
        return mDownloadedAllergenModel
    }

    fun getGroupData(): LiveData<List<ProductGroupModel>> {
        return mDownloadedGroupModel
    }

    fun getIngredientsData(): LiveData<List<IngredientsModel>> {
        return mDownloadedTypeModel
    }

    fun getLocationData(): LiveData<List<LocationModel>> {
        return mDownloadedLocationModel
    }

    fun getKitchenData(): LiveData<List<KitchenModel>> {
        return mDownloadedKitchenModel
    }

    fun getRestaurantTablesData(): LiveData<List<TablesModel>> {
        return mDownloadedTableModel
    }

    fun getTaxData(): LiveData<List<TaxModel>> {
        return mDownloadedTaxModel
    }

    fun getServerData(): LiveData<List<ServerModel>> {
        return mDownloadedServerData
    }

    fun getPaymentMethodData(): LiveData<List<PaymentMethodModel>> {
        return mDownloadedPaymentModel
    }

    private var mGroupList: ArrayList<ProductGroupModel> = ArrayList()

    fun getFavoriteItemsData(mRestaurantID: String?, mGroupList: ArrayList<ProductGroupModel>?, mLocation: String?)
            : LiveData<List<FavoriteItemsModel>> {
        mSuccessFlag  = MutableLiveData()
        this.mGroupList = ArrayList()
        this.mGroupList.addAll(mGroupList!!)
        mMutableFavoriteData = MutableLiveData()
        favoriteItems(mRestaurantID,mLocation)
        return mMutableFavoriteData!!
    }

    private fun favoriteItems(mRestaurantID: String?, mLocation: String?) {
        val favoriteItemUrl = DownloadDatabaseNetworkUtils.getFavoriteItemURL(mRestaurantID,mLocation)
        DownloadDatabaseNetworkUtils.getResponseFromFavoriteItemsAPI(context, favoriteItemUrl.toString(), mLocation, this)
    }

    override fun favoriteItemsResponse(mProductList: ArrayList<DownloadedFavoriteModel>, mLocationID: String) {
        val mFavoriteList = ArrayList<FavoriteItemsModel>()
        e(javaClass.simpleName, "favoriteItemsResponse: Favorite Items: " + mProductList.size)

        for (i in mGroupList.indices) {
            val mCategoryList = mGroupList[i].mProductCategoryList
            for (j in mCategoryList.indices) {
                val mFavoriteProductList = ArrayList<FavoriteProductModel>()
                val productList = mCategoryList[j].mProductList
                for (k in mProductList.indices) {
                    for (l in productList!!.indices) {
                        if (mProductList[k].mProductID == productList[l].mProductID) {
                            e(javaClass.simpleName, "favoriteItemsResponse: ProductID match: " + mProductList[k].mProductID)
                            val mFavoriteProductModel = FavoriteProductModel(
                                productList[l].mProductID,
                                productList[l].mProductName,
                                productList[l].mProductType,
                                productList[l].mGroupID ?: "",
                                mGroupList[i].mGroupName,
                                mCategoryList[j].mCategoryID,
                                mCategoryList[j].mCategoryName,
                                productList[l].mProductImageUrl,
                                mProductList[k].mProductSequence,
                                mCategoryList[j].mCategorySequence,
                                productList[l].mDineInPrice.toDouble(),
                                productList[l].mQuickServicePrice.toDouble(),
                                true
                            )
                            mFavoriteProductList.add(mFavoriteProductModel)
                            break
                        }
                    }
                }
                when {
                    mFavoriteProductList.size > 0 -> {
                        e(javaClass.simpleName, "favoriteItemsResponse: mFavoriteProductList size: " + mFavoriteProductList.size)
                        val mFavoriteModel = FavoriteItemsModel(
                            mGroupList[i].mGroupID,
                            mGroupList[i].mGroupName,
                            mCategoryList[j].mCategoryID,
                            mCategoryList[j].mCategoryName,
                            mCategoryList[j].mCategorySequence,
                            mLocationID,
                            mFavoriteProductList
                        )
                        mFavoriteList.add(mFavoriteModel)
                    }
                }
            }
        }
        e(javaClass.simpleName, "favoriteItemsResponse: Favorite List Count: " + mFavoriteList.size)
        mMutableFavoriteData!!.postValue(mFavoriteList)
    }

    override fun onGetALLResponse(mFlag: Int, response: String) {

        e("DatabaseDownload", "onGetALLResponse: $response")

        if (mFlag == 1) {
            e("DatabaseDownload", "onGetALLResponse: mFlag == 1")

            val mJSONObject = JSONObject(response)

            if (mJSONObject.getString("status").equals("ok", true)){

                val mJSONArray = mJSONObject.getJSONArray("data")

                val mRestaurantDataObject = mJSONArray.getJSONObject(0)

                val mAllergenList =
                    DownloadDatabaseNetworkUtils.getAllergenData(mRestaurantDataObject.getJSONArray("allergen"))

                mDownloadedAllergenModel.postValue(mAllergenList)

                val mServerModelList =
                    DownloadDatabaseNetworkUtils.getServerData(mRestaurantDataObject.getJSONArray("user_terminals"))

                mDownloadedServerData.postValue(mServerModelList)

                val mIngredientsList =
                    DownloadDatabaseNetworkUtils.getIngredientsData(mRestaurantDataObject.getJSONArray("ingredients"))

                mDownloadedTypeModel.postValue(mIngredientsList)

                val mGroupList = DownloadDatabaseNetworkUtils.getGroupData(mRestaurantDataObject.getJSONArray("groups"), mIngredientsList)

                mDownloadedGroupModel.postValue(mGroupList)

                val mLocationList =
                    DownloadDatabaseNetworkUtils.getLocationData(mRestaurantDataObject.getJSONArray("locations"))

                mDownloadedLocationModel.postValue(mLocationList)

                val mTableList = DownloadDatabaseNetworkUtils.getRestaurantTablesData(
                    mRestaurantDataObject.getJSONArray("locations"),
                    context
                )

                mDownloadedTableModel.postValue(mTableList)

                val mTaxList =
                    DownloadDatabaseNetworkUtils.getTaxData(mRestaurantDataObject.getJSONArray("tax"))

                mDownloadedTaxModel.postValue(mTaxList)

                val mPaymentMethodList =
                    DownloadDatabaseNetworkUtils.getPaymentData(mRestaurantDataObject.getJSONArray("payment_method"))

                mDownloadedPaymentModel.postValue(mPaymentMethodList)

                val mKitchenList =
                    DownloadDatabaseNetworkUtils.getKitchenData(mRestaurantDataObject.getJSONArray("kitchen"))

                mDownloadedKitchenModel.postValue(mKitchenList)

                mDownloadedKitchenModel.observeForever {
                    e(javaClass.simpleName, "mDownloadedKitchenModel: $it")
                    mSuccessFlag.postValue(1)
                }

            }
        }else {
            when {
                response.contains("TimeoutError") -> mSuccessFlag.postValue(-2)
                response.contains("NoConnectionError") -> mSuccessFlag.postValue(-3)
                else -> mSuccessFlag.postValue(-1)
            }
        }
    }

    private var mReportData: MutableLiveData<List<ReportModel>>? = null

    fun getReportData(mRestaurantID: String, mDate: String): LiveData<List<ReportModel>> {
        mReportData = MutableLiveData()
        fetchReportData(mRestaurantID, mDate)
        return mReportData!!
    }

    private fun fetchReportData(mRestaurantID: String, mDate: String) {
        val reportsDataRequestUrl = DownloadDatabaseNetworkUtils.getReportsDataURL(mRestaurantID, mDate)
        DownloadDatabaseNetworkUtils.getResponseFromReportsDataAPI(context, reportsDataRequestUrl.toString(), this)
    }

    override fun onReportsResponse(mReportList: ArrayList<ReportModel>) {
        mReportData!!.postValue(mReportList)
    }
}