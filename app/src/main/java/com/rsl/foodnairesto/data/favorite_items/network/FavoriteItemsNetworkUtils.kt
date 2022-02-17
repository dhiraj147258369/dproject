package com.rsl.foodnairesto.data.favorite_items.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Base64
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.rsl.foodnairesto.data.database_download.models.FavoriteItemsModel
import com.rsl.foodnairesto.utils.AppConstants.API_LOCATION_ID
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_ID
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_LIST
import com.rsl.foodnairesto.utils.AppConstants.API_RESTAURANT_ID
import com.rsl.foodnairesto.utils.AppConstants.API_SEQUENCE_NO
import com.rsl.foodnairesto.utils.AppConstants.MY_PREFERENCES
import com.rsl.foodnairesto.utils.AppConstants.RESTAURANT_ID
import com.rsl.foodnairesto.utils.AppConstants.RESTAURANT_PASSWORD
import com.rsl.foodnairesto.utils.AppConstants.RESTAURANT_USER_NAME
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.foodnairesto.utils.EndPoints.FAVORITE_ITEMS
import com.rsl.foodnairesto.utils.VolleySingleton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
@SuppressLint("LogNotTimber")
object FavoriteItemsNetworkUtils {


    internal fun saveFavoriteItems(
        mContext: Context,
        mFavoriteList: List<FavoriteItemsModel>,
        mInterface: FavoriteItemNetworkUtilInterface
    ) {
        val mSharedPrefs = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        val mRestaurantID = mSharedPrefs.getString(RESTAURANT_ID, "")
        val mLocationID = mSharedPrefs.getString(SELECTED_LOCATION_ID, "")

        val jsonObject = JSONObject()
        try {
            jsonObject.put(API_RESTAURANT_ID, mRestaurantID)
            jsonObject.put(API_LOCATION_ID, mLocationID)

            val mProductIdArray = JSONArray()
            for (i in mFavoriteList.indices) {
                val mProductList = mFavoriteList[i].mProductArrayList
                for (j in mProductList.indices) {
                    val mProductId = mProductList[j].mProductID
                    val mProductIdObj = JSONObject()
                    mProductIdObj.put(API_PRODUCT_ID, mProductId)
                    mProductIdObj.put(API_SEQUENCE_NO, mProductList[j].mProductSequence)
                    mProductIdArray.put(mProductIdObj)
                }
            }
            jsonObject.put(API_PRODUCT_LIST, mProductIdArray)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Log.e(javaClass.simpleName, "saveFavoriteItems: jsonObject Param: $jsonObject")


        val mRequest = object : JsonObjectRequest(FAVORITE_ITEMS, jsonObject, { response ->
            if (response != null) {
                Log.e(javaClass.simpleName, "saveFavoriteItems: response: $response")
                try {
                    if (response.getString("status") == "OK")
                        mInterface.responseFromFavoriteItemSaved(1)
                    else if (response.getString("status") == "Failed")
                        mInterface.responseFromFavoriteItemSaved(0)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            } else
                mInterface.responseFromFavoriteItemSaved(0)

        }, { mInterface.responseFromFavoriteItemSaved(2) }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val credentials = mSharedPrefs.getString(RESTAURANT_USER_NAME, "")!! + ":" +
                        mSharedPrefs.getString(RESTAURANT_PASSWORD, "")
                val auth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = auth
                return headers
            }
        }

        mRequest.retryPolicy = DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        VolleySingleton.instance!!.addToRequestQueue(mRequest)

    }

    interface FavoriteItemNetworkUtilInterface {
        fun responseFromFavoriteItemSaved(mResultValue: Int)
    }
}