package com.rsl.foodnairesto.data.cart

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rsl.foodnairesto.data.cart.models.CartProductModel
import com.rsl.foodnairesto.data.database_download.models.ProductModel
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("LogNotTimber")
class CartDataSource(val context: Context) : CartNetworkUtils.CartNetworkInterface {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sInstance: CartDataSource? = null

        fun getInstance(context: Context): CartDataSource? {
            val tempInstance = sInstance
            if (tempInstance != null) return sInstance

            sInstance ?: synchronized(this) {
                sInstance ?: CartDataSource(context).also { sInstance = it }
            }

            return sInstance
        }
    }

    private var mSubmitCartData: MutableLiveData<ArrayList<CartProductModel>>? = null

    fun submitCartItemToTheServer(
        mProductList: ArrayList<CartProductModel>
    ): LiveData<ArrayList<CartProductModel>> {
        mSubmitCartData = MutableLiveData()
        CartNetworkUtils.submitCart(context, mProductList, this)
        return mSubmitCartData!!
    }

    fun updateCartItemToTheServer(
        mProductCartModel: ArrayList<CartProductModel>
    ) {
        CartNetworkUtils.submitCart(context, mProductCartModel, this)
    }

    override fun onSubmitCart(mResponse: String) {
        Log.e(javaClass.simpleName, "onSubmitCart: $mResponse")
        if (mSubmitCartData != null) {
            try {
                if (JSONObject(mResponse).getString("status")
                        .lowercase(Locale.getDefault()) == "ok".lowercase(Locale.getDefault()))
                    mSubmitCartData!!.postValue(CartNetworkUtils.parseSubmitCartResponse(JSONObject(mResponse)))
            } catch (e: Exception) {
                e.printStackTrace()
                mSubmitCartData!!.postValue(ArrayList())
            }
            mSubmitCartData = null
        }

    }

    private var mProductList: ArrayList<ProductModel>? = null
    private var mLocationID: String = ""
    private var mSyncCartData: MutableLiveData<List<CartProductModel>>? = null

    fun syncCart(mProductList: ArrayList<ProductModel>, mLocationID: String): LiveData<List<CartProductModel>> {
        this.mProductList = mProductList
        this.mLocationID = mLocationID
        mSyncCartData = MutableLiveData()

        val mSyncCartURL = CartNetworkUtils.getSyncCartUrl(context)

        CartNetworkUtils.syncCart(context, mSyncCartURL!!, this)

        return mSyncCartData!!
    }

    private var mDeleteProductData: MutableLiveData<Int>? = null

    override fun onSyncCart(mResponse: String) {
        Log.e(javaClass.simpleName, "onSyncCart: response: $mResponse")

        try {
            val mJSONObject = JSONObject(mResponse)

            if (mJSONObject.getString("status").equals("OK", ignoreCase = true)) {
                mSyncCartData!!.postValue(
                    CartNetworkUtils.getCartData(
                        mProductList!!,
                        mLocationID,
                        mJSONObject
                    )
                )
            } else {
                mSyncCartData!!.postValue(ArrayList())
            }
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Exception: $e")
            mSyncCartData!!.postValue(ArrayList())
        }
    }

    fun deleteCart(mCart: CartProductModel): LiveData<Int> {
        mDeleteProductData = MutableLiveData()
        CartNetworkUtils.deleteCartProduct(context, mCart, this)
        return mDeleteProductData!!
    }

    override fun onDeleteCartProduct(mResponse: String) {
        Log.e(javaClass.simpleName, "onDeleteCartProduct: response: $mResponse")

        try {
            val mJSONObject = JSONObject(mResponse)
            if (mJSONObject.getString("status").equals("OK", true)) {
                mDeleteProductData!!.postValue(1)
            } else {
                mDeleteProductData!!.postValue(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            mDeleteProductData!!.postValue(0)
        }


    }
}