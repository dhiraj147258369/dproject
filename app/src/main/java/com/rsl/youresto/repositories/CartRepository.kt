package com.rsl.youresto.repositories

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.util.Log.e
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rsl.youresto.data.cart.CartDao
import com.rsl.youresto.data.cart.CartDataSource
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.database_download.models.ProductModel
import com.rsl.youresto.data.main_login.network.LOG_TAG
import com.rsl.youresto.data.main_login.network.Login
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.coroutines.CoroutineContext

@SuppressLint("LogNotTimber")
class CartRepository(
    private val cartDao: CartDao,
    private val cartDataSource: CartDataSource
) {
    companion object {
        @Volatile
        private var sInstance: CartRepository? = null

        fun getInstance(cartDao: CartDao, cartDataSource: CartDataSource) =
            sInstance ?: synchronized(this) {
                sInstance ?: CartRepository(cartDao, cartDataSource)
            }
    }

    private var parentJob = Job()
    private val coRoutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    fun insertCartProduct(mProductCartModel: CartProductModel): LiveData<Long> {
        val mInsertCartData: MutableLiveData<Long> = MutableLiveData()

        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            val i = cartDao.insertCartProduct(mProductCartModel)
            if (i > -1) {
                val mId = cartDao.getCartRowID().toLong()
                e(javaClass.simpleName, "mID: $mId")
                mInsertCartData.postValue(mId)
            }
        }
        return mInsertCartData
    }

    fun submitCartItemToTheServer(mProductCartModel: CartProductModel): LiveData<Int> {

        val mSubmitCartTOServerData = MutableLiveData<Int>()
        val mProductList = ArrayList<CartProductModel>()
        mProductList.add(mProductCartModel)
        cartDataSource.submitCartItemToTheServer(mProductList).observeForever {
            when {
                it != null && it.size > 0 -> {
                    val scope = CoroutineScope(coRoutineContext)
                    scope.launch(Dispatchers.IO) {
                        loop@ for (i in 0 until it.size) {

                            when {
                                it[i].mProductID.equals(mProductCartModel.mProductID) && it[i].mSequenceNO == mProductCartModel.mSequenceNO -> {
                                    e(javaClass.simpleName, "submitCartItemToTheServer: ee $i")
                                    e(javaClass.simpleName, "submitCartItemToTheServer: mCartProductID: " + it[i].mCartProductID)
                                    e(javaClass.simpleName, "submitCartItemToTheServer: mProductCartModel.mID: " + mProductCartModel.mID)
                                    val mUpdate = cartDao.updateCartProductID(it[i].mCartProductID, mProductCartModel.mID)

                                    e(LOG_TAG, "submitCartItemToTheServer: mUpdate$mUpdate")
                                    if (mUpdate > -1) {
                                        e(LOG_TAG, "submitCartItemToTheServer: tyu $i")
                                        mSubmitCartTOServerData.postValue(1)
                                        break@loop
                                    }
                                }
                            }
                        }
                    }
                }
                else -> mSubmitCartTOServerData.postValue(-1)
            }
        }
        return mSubmitCartTOServerData
    }

    fun submitRepeatOrderItemsToServer(mProductList: ArrayList<CartProductModel>): LiveData<Int> {

        val mSubmitCartTOServerData = MutableLiveData<Int>()
        cartDataSource.submitCartItemToTheServer(mProductList).observeForever {
            when {
                it != null && it.size > 0 -> {
                    val scope = CoroutineScope(coRoutineContext)
                    scope.launch(Dispatchers.IO) {
                        loop@ for (i in 0 until it.size) {
                            for(j in mProductList.indices) {
                                if (it[i].mProductID.equals(mProductList[j].mProductID) && it[i].mSequenceNO == mProductList[j].mSequenceNO) {
                                    e(javaClass.simpleName, "submitCartItemToTheServer: ee $i")
                                    e(javaClass.simpleName, "submitCartItemToTheServer: mCartProductID: " + it[i].mCartProductID)
                                    e(javaClass.simpleName, "submitCartItemToTheServer: mProductCartModel.mID: " + mProductList[j].mID)
                                    val mUpdate = cartDao.updateCartProductID(it[i].mCartProductID, mProductList[j].mID)

                                    e(LOG_TAG, "submitCartItemToTheServer: mUpdate$mUpdate")
                                    if (mUpdate > -1) {
                                        e(LOG_TAG, "submitCartItemToTheServer: tyu $i")
                                        break
                                        /*mSubmitCartTOServerData.postValue(1)
                                        break@loop*/
                                    }
                                }
                            }
                            if(i == it.size - 1) {
                                mSubmitCartTOServerData.postValue(1)
                            }
                        }
                    }
                }
                else -> mSubmitCartTOServerData.postValue(-1)
            }
        }
        return mSubmitCartTOServerData
    }

    fun getCartData(mTableNO: Int, mGroupName: String): LiveData<List<CartProductModel>> {
        return cartDao.getCartData(mTableNO, mGroupName)
    }

    fun getCartWithoutLiveData(mTableNO: String, mGroupName: String): LiveData<List<CartProductModel>> {
        val mCartData = MutableLiveData<List<CartProductModel>>()

        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mCartData.postValue( cartDao.getCartWithoutLiveData(mTableNO))
        }

        return mCartData
    }

    fun getCartDataByTable(mTableNO: Int): LiveData<List<CartProductModel>> {
        return cartDao.getCartDataByTable(mTableNO)
    }

    fun getCartQuickServiceData(mCartID: String): LiveData<List<CartProductModel>> {
        val mCartData = MutableLiveData<List<CartProductModel>>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mCartData.postValue(cartDao.getCartQuickServiceData(mCartID))
        }
        return mCartData
    }

    fun getTableCartData(mTableNO: Int): LiveData<List<CartProductModel>> {
        return cartDao.getTableCartData(mTableNO)
    }

    fun getTableCartDataForQuickService(mTableNO: Int, mCartID: String): LiveData<List<CartProductModel>> {
        return cartDao.getTableCartDataForQuickService(mTableNO, mCartID)
    }

    fun syncCart(mLocationID: String) : LiveData<Int>{

        val syncCartData = MutableLiveData<Int>()

        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            val mGroupList = cartDao.getGroups()
            when {
                mGroupList.isNotEmpty() -> {
                    val mLocalProductList = ArrayList<ProductModel>()
                    for (i in mGroupList.indices)
                        for (j in 0 until mGroupList[i].mProductCategoryList.size)
                            mLocalProductList.addAll(mGroupList[i].mProductCategoryList[j].mProductList!!)

                    Handler(Looper.getMainLooper()).post {
                        cartDataSource.syncCart(mLocalProductList, mLocationID).observeForever {

                            when {
                                it != null -> {
                                    e(javaClass.simpleName, "syncCart: ${it.size}")
                                    when {
                                        it.isNotEmpty() -> {

                                            val scope2 = CoroutineScope(coRoutineContext)
                                            scope2.launch(Dispatchers.IO) {
                                                val mDelete = cartDao.deleteCartByLocation(mLocationID)
                                                when {
                                                    mDelete > -1 -> {
                                                        val mInsert = cartDao.insertBulkCartProduct(it)

                                                        when {
                                                            mInsert.size > -1 -> syncCartData.postValue(1)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        else -> syncCartData.postValue(1)
                                    }
                                }
                            }


                        }
                    }

                }
            }
        }
        return syncCartData
    }

    fun updateProductQuantity(
        mRowID: Int,
        mProductQuantity: BigDecimal,
        mProductTotalPrice: BigDecimal
    ): LiveData<Int> {
        val mUpdateQtyData: MutableLiveData<Int> = MutableLiveData()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mUpdateQtyData.postValue(cartDao.updateProductQuantity(mRowID, mProductQuantity, mProductTotalPrice))
        }
        return mUpdateQtyData
    }

    fun getCartProduct(mRowID: Int): LiveData<CartProductModel> {
        val mProductData = MutableLiveData<CartProductModel>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mProductData.postValue(cartDao.getCartProduct(mRowID))
        }
        return mProductData
    }

    fun updateProductToServer(mCart: CartProductModel): LiveData<Int> {
        val mUpdateProductTOServerData = MutableLiveData<Int>()
        val mProductList = ArrayList<CartProductModel>()
        mProductList.add(mCart)
        cartDataSource.submitCartItemToTheServer(mProductList).observeForever {
            when {
                it != null && it.size > 0 -> {
                    val scope1 = CoroutineScope(coRoutineContext)
                    scope1.launch(Dispatchers.IO) {
                        loop@ for (i in 0 until it.size) {

                            when {
                                it[i].mProductID.equals(mCart.mProductID) && it[i].mSequenceNO == mCart.mSequenceNO -> {
                                    e(javaClass.simpleName, "submitCartItemToTheServer: update:  $i")
                                    val mUpdate = cartDao.updateCartProductID(it[i].mCartProductID, mCart.mID)

                                    e(LOG_TAG, "submitCartItemToTheServer: mUpdate: $mUpdate")
                                    if (mUpdate > -1) {
                                        e(LOG_TAG, "submitCartItemToTheServer: tyu $i")
                                        mUpdateProductTOServerData.postValue(1)
                                        break@loop
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return mUpdateProductTOServerData
    }

    fun deleteCartOnServer(mCart: CartProductModel): LiveData<Int> {
        val mDeleteData = MutableLiveData<Int>()
        cartDataSource.deleteCart(mCart)

        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mDeleteData.postValue(cartDao.deleteCartProduct(mCart.mID))
        }

        return mDeleteData
    }

    fun getPendingOrderCartData(mLocationID: String, mServerID: String): LiveData<List<CartProductModel>> {
        val mCartData = MutableLiveData<List<CartProductModel>>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            if (mLocationID != "" && mServerID != "") {
                mCartData.postValue(cartDao.getPendingOrderDataForLocationAndServer(mLocationID, mServerID))
            } else if (mLocationID != "" && mServerID == "") {
                mCartData.postValue(cartDao.getPendingOrderDataForLocation(mLocationID))
            } else if (mLocationID == "" && mServerID != "") {
                mCartData.postValue(cartDao.getPendingOrderDataForServer(mServerID))
            } else {
                mCartData.postValue(cartDao.getAllPendingOrderData())
            }
        }
        return mCartData
    }

    fun getRestaurantDetails(): LiveData<Login> {
        val mRestaurantData = MutableLiveData<Login>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mRestaurantData.postValue(cartDao.getRestaurantData())
        }
        return mRestaurantData
    }

    fun getCartProductByID(mCartProductID: String): LiveData<CartProductModel> {
        val mCartData = MutableLiveData<CartProductModel>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mCartData.postValue(cartDao.getCartProductByID(mCartProductID))
        }
        return mCartData

    }

    fun getAllCartDataIrrespectiveOfLocations(): LiveData<List<CartProductModel>> {
        return cartDao.getAllCartDataIrrespectiveOfLocations()
    }

    fun updateCartProduct(mCartProduct: CartProductModel): LiveData<Int> {

        val mUpdateData = MutableLiveData<Int>()
        val mProductList = ArrayList<CartProductModel>()
        mProductList.add(mCartProduct)
        cartDataSource.submitCartItemToTheServer(mProductList).observeForever {
            when {
                it != null && it.size > 0 -> {
                    val scope1 = CoroutineScope(coRoutineContext)
                    scope1.launch(Dispatchers.IO) {
                        loop@ for (i in 0 until it.size) {

                            when {
                                it[i].mProductID.equals(mCartProduct.mProductID) && it[i].mSequenceNO == mCartProduct.mSequenceNO -> {
                                    e(javaClass.simpleName, "submitCartItemToTheServer: update:  $i")

                                    mCartProduct.mCartProductID = it[i].mCartProductID

                                    val mUpdate = cartDao.updateCartProduct(mCartProduct)

                                    e(LOG_TAG, "submitCartItemToTheServer: mUpdate: $mUpdate")
                                    if (mUpdate > -1) {
                                        e(LOG_TAG, "submitCartItemToTheServer: tyu $i")
                                        mUpdateData.postValue(1)
                                        break@loop
                                    }
                                }
                            }
                        }
                    }
                }
                else ->{
                    mUpdateData.postValue(-1)
                }
            }
        }

        return mUpdateData
    }

    fun changeKOTFlag(mPrinterID: String, mCartID: String): LiveData<Int> {
        val mKOTData = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {

            val mUpdate = cartDao.changeKOTFlag(mPrinterID, mCartID)

            e(CartRepository::class.java.simpleName, "changeKOTFlag: $mUpdate" )

            if (mUpdate > -1){
                mKOTData.postValue(mUpdate)

                val mCartProductList = ArrayList(cartDao.getCartQuickServiceData(mCartID))

                cartDataSource.updateCartItemToTheServer(mCartProductList)
            }

        }
        return mKOTData
    }

    fun insertBulkCartProduct(mProductList: List<CartProductModel>): LiveData<LongArray> {
        val mInsertCartData: MutableLiveData<LongArray> = MutableLiveData()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            val mInsert = cartDao.insertBulkCartProduct(mProductList)
            if(mInsert.size > -1) {
                mInsertCartData.postValue(mInsert)
            }
        }

        return mInsertCartData
    }
}