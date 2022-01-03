package com.rsl.youresto.data.tables

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.rsl.youresto.data.database_download.models.TablesModel
import com.rsl.youresto.data.main_login.network.LOG_TAG
import com.rsl.youresto.data.tables.models.ServerTableGroupModel
import com.rsl.youresto.data.tables.models.ServerTableSeatModel
import com.rsl.youresto.utils.AppConstants.API_AMOUNT
import com.rsl.youresto.utils.AppConstants.API_GROUP_NAME
import com.rsl.youresto.utils.AppConstants.API_OCCUPIED_CHAIRS
import com.rsl.youresto.utils.AppConstants.API_OCCUPY_TABLE_ID
import com.rsl.youresto.utils.AppConstants.API_RESTAURANT_ID
import com.rsl.youresto.utils.AppConstants.API_SEATS
import com.rsl.youresto.utils.AppConstants.API_TABLE_ID
import com.rsl.youresto.utils.AppConstants.API_USER_ID
import com.rsl.youresto.utils.AppConstants.AUTH_BASIC
import com.rsl.youresto.utils.AppConstants.AUTH_CONTENT_TYPE
import com.rsl.youresto.utils.AppConstants.AUTH_CONTENT_TYPE_VALUE
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.RESTAURANT_ID
import com.rsl.youresto.utils.AppConstants.RESTAURANT_PASSWORD
import com.rsl.youresto.utils.AppConstants.RESTAURANT_USER_NAME
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_ID
import com.rsl.youresto.utils.EndPoints.CLEAR_TABLE
import com.rsl.youresto.utils.EndPoints.GET_ALL_TABLES
import com.rsl.youresto.utils.EndPoints.OCCUPY_TABLE
import com.rsl.youresto.utils.EndPoints.OCCUPY_TABLE_UPDATE
import com.rsl.youresto.utils.VolleySingleton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.net.URL
import java.util.*

@SuppressLint("LogNotTimber")
object TablesNetworkUtils {

    fun getAllTableUrl(mContext: Context): URL {
        return buildGetAllTableUrlWithCredentialsQuery(mContext)
    }

    private fun buildGetAllTableUrlWithCredentialsQuery(mContext: Context): URL {
        val sharedPreferences = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        val mRestaurantID = sharedPreferences.getString(RESTAURANT_ID, "")
        val loginQueryUri = Uri.parse(GET_ALL_TABLES).buildUpon()
            .appendQueryParameter(API_RESTAURANT_ID, mRestaurantID)
            .build()

        return URL(loginQueryUri.toString())
    }

    fun getResponseFromAPIForAllTables(mContext: Context, mGetAllTablesAPI: URL, mNetwork: TableNetworkInterface) {

        val mSharedPref = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)

        val mRequest = object : StringRequest(
            Method.GET, mGetAllTablesAPI.toString(),
            Response.Listener {
                mNetwork.onTableResponse(it)
            }, Response.ErrorListener {
                mNetwork.onTableResponse(it.toString())
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val credentials = mSharedPref.getString(RESTAURANT_USER_NAME, "")!! + ":" +
                        mSharedPref.getString(RESTAURANT_PASSWORD, "")
                val auth = AUTH_BASIC + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                headers[AUTH_CONTENT_TYPE] = AUTH_CONTENT_TYPE_VALUE
                headers["Authorization"] = auth
                return headers
            }
        }

        mRequest.retryPolicy = DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        VolleySingleton.instance?.addToRequestQueue(mRequest)

    }

    @Throws(JSONException::class)
    internal fun getRestaurantTablesData(mTableArray: JSONArray, mLocationID: String?): ArrayList<TablesModel> {

        val tableEntries = ArrayList<TablesModel>()

        for (j in 0 until mTableArray.length()) {

            val mTableObject = mTableArray.getJSONObject(j)

            val mTableDetailsArray = mTableObject.getJSONArray("table_details")

            val mGroupList = ArrayList<ServerTableGroupModel>()

            for (k in 0 until mTableDetailsArray.length()) {
                val mTableGroupObject = mTableDetailsArray.getJSONObject(k)

                val mSeatList = ArrayList<ServerTableSeatModel>()

                when {
                    mTableGroupObject.get("seats") is String && mTableGroupObject.get("seats") != "" -> {
                        val mSeats =
                            mTableGroupObject.getString("seats").split(",".toRegex()).dropLastWhile { it.isEmpty() }
                                .toTypedArray()

                        for (seat in mSeats) {
                            mSeatList.add(ServerTableSeatModel(Integer.parseInt(seat)))
                        }

                        mGroupList.add(
                            ServerTableGroupModel(
                                mTableGroupObject.getString("cart_id"),
                                mTableGroupObject.getString("cart_no"),
                                mTableGroupObject.getString("occupy_table_id"),
                                mTableGroupObject.getString("group_name"),
                                mTableObject.getInt("tableno"),
                                mTableObject.getString("table_id"),
                                mSeatList,
                                BigDecimal(mTableGroupObject.getDouble("amount")),
                                mSeatList.size > 0
                            )
                        )
                    }
                }
            }

            var mOccupiedChairs = 0

            try {
                when {
                    mTableObject.get("occupiedchairs") is Int -> mOccupiedChairs = mTableObject.getInt("occupiedchairs")
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }


            val mTableModel = TablesModel(
                mTableObject.getString("locationid"),
                mTableObject.getString("table_id"),
                mTableObject.getInt("tableno"),
                mTableObject.getInt("noofchairs"),
                mOccupiedChairs,
                mTableObject.getInt("table_type"),
                mTableObject.getString("user"),
                mTableObject.getString("user_id"),
                mGroupList
            )

            if (mLocationID == mTableObject.getString("locationid"))
                tableEntries.add(mTableModel)

        }

        return tableEntries
    }

    fun getResponseFromOccupyTable(
        mContext: Context,
        mTableModel: TablesModel?,
        mInterface: TableNetworkInterface
    ) {
        val mSharedPrefs = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        val mRestaurantID = mSharedPrefs.getString(RESTAURANT_ID, "")

        val jsonObject = JSONObject()
        try {
            jsonObject.put(API_RESTAURANT_ID, mRestaurantID)
            jsonObject.put(API_OCCUPIED_CHAIRS, mTableModel!!.mTableNoOfOccupiedChairs)
            jsonObject.put(API_USER_ID, mTableModel.mOccupiedByUserID)
            jsonObject.put(API_TABLE_ID, mTableModel.mTableID)
            jsonObject.put(API_AMOUNT, 0)
            jsonObject.put(API_GROUP_NAME, mTableModel.mGroupList!![0].mGroupName)

            val mSeats = StringBuilder()
            val mSeatList = mTableModel.mGroupList!![0].mSeatList
            for (i in 0 until mSeatList!!.size) {
                if (i == mSeatList.size - 1) {
                    mSeats.append(mSeatList[i].mSeatNO)
                } else {
                    mSeats.append(mSeatList[i].mSeatNO).append(",")
                }
            }
            jsonObject.put(API_SEATS, mSeats)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Log.e(javaClass.simpleName, "getResponseFromOccupyTable: JSON:\n$jsonObject")

        val jsonObjectRequest = object : JsonObjectRequest(Method.POST, OCCUPY_TABLE, jsonObject,
            { response -> mInterface.occupyTableAPIResponse(response.toString()) },
            { error -> mInterface.occupyTableAPIResponse(error.toString()) }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val credentials = mSharedPrefs.getString(RESTAURANT_USER_NAME, "")!! + ":" +
                        mSharedPrefs.getString(RESTAURANT_PASSWORD, "")
                val auth = AUTH_BASIC + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                headers[AUTH_CONTENT_TYPE] = AUTH_CONTENT_TYPE_VALUE
                headers["Authorization"] = auth
                return headers
            }
        }


        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        VolleySingleton.instance?.addToRequestQueue(jsonObjectRequest)
    }

    fun getResponseFromUpdateTable(
        mContext: Context,
        mTableModel: TablesModel, mGroupName: String,
        mInterface: TableNetworkInterface
    ) {
        val mSharedPrefs = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        val mRestaurantID = mSharedPrefs.getString(RESTAURANT_ID, "")


        val jsonObject = JSONObject()
        try {

            jsonObject.put(API_RESTAURANT_ID, mRestaurantID)
            jsonObject.put(API_OCCUPIED_CHAIRS, mTableModel.mTableNoOfOccupiedChairs)
            jsonObject.put(API_USER_ID, mTableModel.mOccupiedByUserID)
            jsonObject.put(API_TABLE_ID, mSharedPrefs.getString(SELECTED_TABLE_ID, ""))

            val mGroupList = mTableModel.mGroupList
            for (i in mGroupList!!.indices) {
                if (mGroupList[i].mGroupName == mGroupName) {
                    jsonObject.put(API_AMOUNT, mGroupList[i].mGroupTotal)
                    jsonObject.put(API_GROUP_NAME, mGroupList[i].mGroupName)
                    jsonObject.put(API_OCCUPY_TABLE_ID, mGroupList[i].mTableOccupiedID)

                    val mSeats = StringBuilder()
                    val mSeatList = mTableModel.mGroupList!![i].mSeatList
                    for (j in mSeatList!!.indices) {
                        if (j == mSeatList.size - 1) {
                            mSeats.append(mSeatList[j].mSeatNO)
                        } else {
                            mSeats.append(mSeatList[j].mSeatNO).append(",")
                        }
                    }
                    jsonObject.put(API_SEATS, mSeats)

                    break
                }
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        Log.e(LOG_TAG, "getResponseFromUpdateTable: JSON:\n$jsonObject")

        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST, OCCUPY_TABLE_UPDATE, jsonObject,
            { response -> mInterface.updateTableResponse(response.toString()) },
            { error -> mInterface.updateTableResponse(error.toString()) }) {
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


        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        VolleySingleton.instance?.addToRequestQueue(jsonObjectRequest)
    }

    @Throws(JSONException::class)
    internal fun getTableModel(mJSONObject: JSONObject, mTableModel: TablesModel): TablesModel {

        val mGroupList = mTableModel.mGroupList

        //get the group name from API
//        val mGroupName = mGroupList!![0].mGroupName
        val mGroupName = mJSONObject.getString("groupName")

        for (i in mGroupList!!.indices) {
            if (mGroupName == mGroupList[i].mGroupName) {
                mGroupList[i].mCartID = mJSONObject.getString("cart_id")
                mGroupList[i].mCartNO = mJSONObject.getString("cartNo")
                mGroupList[i].isOccupied = mGroupList[i].mSeatList!!.size > 0
            }
        }

        mTableModel.mGroupList = mGroupList

        return mTableModel
    }


    internal fun getResponseFromClearTable(mContext: Context, mTableID: String, mInterface: TableNetworkInterface) {

        val mSharedPrefs = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        val mRestaurantID = mSharedPrefs.getString(RESTAURANT_ID, "")

        val jsonObject = JSONObject()
        jsonObject.put(API_RESTAURANT_ID, mRestaurantID)
        jsonObject.put(API_TABLE_ID, mTableID)


        Log.e(javaClass.simpleName, "getResponseFromClearTable: $jsonObject")

        val jsonObjectRequest = object : JsonObjectRequest(Method.POST, CLEAR_TABLE, jsonObject,
            { response -> mInterface.onClearTable(false, response.toString()) },
            { error -> mInterface.onClearTable(true, error.toString()) }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val credentials = mSharedPrefs.getString(RESTAURANT_USER_NAME, "")!! + ":" +
                        mSharedPrefs.getString(RESTAURANT_PASSWORD, "")
                val auth = AUTH_BASIC + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                headers[AUTH_CONTENT_TYPE] = AUTH_CONTENT_TYPE_VALUE
                headers["Authorization"] = auth
                return headers
            }
        }


        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        VolleySingleton.instance?.addToRequestQueue(jsonObjectRequest)
    }

    interface TableNetworkInterface {
        fun onTableResponse(mResponse: String)
        fun occupyTableAPIResponse(mResponse: String)
        fun onClearTable(isException: Boolean, mResponse: String)
        fun updateTableResponse(mResponse: String)
    }
}