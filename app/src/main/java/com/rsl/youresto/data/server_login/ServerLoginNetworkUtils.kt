package com.rsl.youresto.data.server_login

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.rsl.youresto.data.server_login.models.ServerLoginModel
import com.rsl.youresto.data.server_login.models.ServerShiftModel
import com.rsl.youresto.utils.AppConstants.API_DATE
import com.rsl.youresto.utils.AppConstants.API_LOGIN_FLAG
import com.rsl.youresto.utils.AppConstants.API_RESTAURANT_ID
import com.rsl.youresto.utils.AppConstants.API_SHIFT_STATUS
import com.rsl.youresto.utils.AppConstants.API_TIME
import com.rsl.youresto.utils.AppConstants.API_USER_ID
import com.rsl.youresto.utils.AppConstants.AUTH_BASIC
import com.rsl.youresto.utils.AppConstants.AUTH_CONTENT_TYPE
import com.rsl.youresto.utils.AppConstants.AUTH_CONTENT_TYPE_VALUE
import com.rsl.youresto.utils.AppConstants.DATE_FORMAT_DMY_HMS
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.RESTAURANT_ID
import com.rsl.youresto.utils.AppConstants.RESTAURANT_PASSWORD
import com.rsl.youresto.utils.AppConstants.RESTAURANT_USER_NAME
import com.rsl.youresto.utils.EndPoints.SHIFT_DETAILS
import com.rsl.youresto.utils.EndPoints.USER_LOGIN
import com.rsl.youresto.utils.VolleySingleton
import org.json.JSONException
import org.json.JSONObject
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

@SuppressLint("LogNotTimber")
object ServerLoginNetworkUtils {


    fun getShiftDetailsUrl(mContext: Context, mServerID: String, mCurrentDate: Date): URL {
        return buildGetShiftDetailsURL(mContext, mServerID, mCurrentDate)
    }

    private fun buildGetShiftDetailsURL(mContext: Context, mServerID: String, mCurrentDate: Date): URL {

        val mDateFormat = SimpleDateFormat(DATE_FORMAT_DMY_HMS, Locale.ENGLISH)
        val mDateTime = mDateFormat.format(mCurrentDate)

        val sharedPreferences = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        val mRestaurantID = sharedPreferences.getString(RESTAURANT_ID, "")
        val loginQueryUri = Uri.parse(SHIFT_DETAILS).buildUpon()
            .appendQueryParameter(API_RESTAURANT_ID, mRestaurantID)
            .appendQueryParameter(API_USER_ID, mServerID)
            .appendQueryParameter(API_DATE, mDateTime)
            .build()

        return URL(loginQueryUri.toString())
    }

    fun getResponseFromAPIForShiftDetails(
        mContext: Context,
        getShiftDetailsURL: URL?,
        mInterface: ServerLoginNetworkInterface
    ) {

        val mSharedPref = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)

        val mUsername: String? = mSharedPref.getString(RESTAURANT_USER_NAME, "")
        val mPassword: String? = mSharedPref.getString(RESTAURANT_PASSWORD, "")

        val mRequest = object : StringRequest(Method.GET, getShiftDetailsURL.toString(),
            Response.Listener {
                mInterface.onShiftDetailResponse(it)
            }, Response.ErrorListener { }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                val credentials = "$mUsername:$mPassword"
                val auth = AUTH_BASIC + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                headers[AUTH_CONTENT_TYPE] = AUTH_CONTENT_TYPE_VALUE
                headers["Authorization"] = auth
                return headers
            }
        }


        VolleySingleton.instance!!.addToRequestQueue(mRequest)

    }


    fun getResponseFromServerStartShift(
        mContext: Context,
        mServerID: String,
        mShiftFlag: String,
        mInterface: ServerLoginNetworkInterface
    ) {

        val mSharedPref = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)

        val mUsername: String? = mSharedPref.getString(RESTAURANT_USER_NAME, "")
        val mPassword: String? = mSharedPref.getString(RESTAURANT_PASSWORD, "")
        val mRestaurantID: String? = mSharedPref.getString(RESTAURANT_ID, "")

        val today = Calendar.getInstance()

        val mDateFormat = SimpleDateFormat(DATE_FORMAT_DMY_HMS, Locale.ENGLISH)
        val mDateTime = mDateFormat.format(today.time)

        val jsonObject = JSONObject()

        jsonObject.put(API_RESTAURANT_ID, mRestaurantID)
        jsonObject.put(API_USER_ID, mServerID)
        jsonObject.put(API_SHIFT_STATUS, mShiftFlag)
        jsonObject.put(API_DATE, mDateTime)

        Log.e("ServerLoginNetworkUtils", "getResponseFromServerStartShift: JSON:\n$jsonObject")

        val mRequest = object : JsonObjectRequest(
            Method.POST, SHIFT_DETAILS, jsonObject,
            Response.Listener {
                mInterface.onShiftStartEnd(it.toString(), jsonObject)
            }, Response.ErrorListener {

            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                val credentials = "$mUsername:$mPassword"
                val auth = AUTH_BASIC + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                headers[AUTH_CONTENT_TYPE] = AUTH_CONTENT_TYPE_VALUE
                headers["Authorization"] = auth
                return headers
            }
        }

        VolleySingleton.instance!!.addToRequestQueue(mRequest)
    }

    @Throws(JSONException::class, ParseException::class)
    fun getShiftDetailsList(mLoginObject: JSONObject): List<ServerShiftModel> {

        val mShiftDetailsList = ArrayList<ServerShiftModel>()

        val mRestaurantID = mLoginObject.getString("restaurant_id")

        val mUserShiftArray = mLoginObject.getJSONArray("user_shifts")

        for (i in 0 until mUserShiftArray.length()) {
            val mUserShiftObject = mUserShiftArray.getJSONObject(i)

            val mShiftArray = mUserShiftObject.getJSONArray("shifts")

            for (j in 0 until mShiftArray.length()) {

                val mShiftObject = mShiftArray.getJSONObject(j)

                val mDateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
                val mDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

                val mStartDateTime = mDateTimeFormat.parse(mShiftObject.getString("start_time"))
                val date = mDateFormat.parse(mShiftObject.getString("start_time"))

                val c = Calendar.getInstance()
                c.time = date
                val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
                val mDay: String
                mDay = when (dayOfWeek) {
                    0 -> "Sunday"
                    1 -> "Monday"
                    2 -> "Tuesday"
                    3 -> "Wednesday"
                    4 -> "Thursday"
                    6 -> "Friday"
                    else -> "Saturday"
                }

                var mEndDateTime: Date? = null
                if (mShiftObject.getString("end_time").isNotEmpty()) {
                    mEndDateTime = mDateTimeFormat.parse(mShiftObject.getString("end_time"))
                }

                val mShiftModel = ServerShiftModel(
                    mRestaurantID,
                    mUserShiftObject.getString("user_id"),
                    date,
                    mDay,
                    mStartDateTime,
                    mEndDateTime, ""
                )

                mShiftDetailsList.add(mShiftModel)
            }

        }


        return mShiftDetailsList
    }

    fun getResponseFromServerLogin(
        mContext: Context,
        mUserID: String,
        mLoginFlag: String,
        mInterface: ServerLoginNetworkInterface
    ) {

        val mSharedPrefs = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        val mRestaurantID = mSharedPrefs.getString(RESTAURANT_ID, "")
        val mUserName = mSharedPrefs.getString(RESTAURANT_USER_NAME, "")
        val mPassword = mSharedPrefs.getString(RESTAURANT_PASSWORD, "")

        val today = Calendar.getInstance()

        val mDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
        val mDateTime = mDateFormat.format(today.time)

        val jsonObject = JSONObject()
        try {
            jsonObject.put(API_RESTAURANT_ID, mRestaurantID)
            jsonObject.put(API_USER_ID, mUserID)
            jsonObject.put(API_LOGIN_FLAG, mLoginFlag)
            jsonObject.put(API_TIME, mDateTime)

        } catch (e: JSONException) {
            e.printStackTrace()
        }


        Log.e(javaClass.simpleName, "getResponseFromServerLogin: JSON:\n$jsonObject")

        val mRequest = object : JsonObjectRequest(
            Method.POST, USER_LOGIN, jsonObject,
            Response.Listener {
                mInterface.onUserLoginResponse(it.toString())
            }, Response.ErrorListener { }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                val credentials = "$mUserName:$mPassword"
                val auth = AUTH_BASIC + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                headers[AUTH_CONTENT_TYPE] = AUTH_CONTENT_TYPE_VALUE
                headers["Authorization"] = auth
                return headers
            }
        }

        VolleySingleton.instance!!.addToRequestQueue(mRequest)
    }

    fun getLoginDetailsUrl(mContext: Context): URL {
        return buildGetLoginDetailsURLWithCredentialsQuery(mContext)
    }

    private fun buildGetLoginDetailsURLWithCredentialsQuery(mContext: Context): URL {

        val today = Calendar.getInstance()

        val mDateFormat = SimpleDateFormat(DATE_FORMAT_DMY_HMS, Locale.ENGLISH)
        val mDateTime = mDateFormat.format(today.time)

        val sharedPreferences = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        val mRestaurantID = sharedPreferences.getString(RESTAURANT_ID, "")
        val loginQueryUri = Uri.parse(USER_LOGIN).buildUpon()
            .appendQueryParameter(API_RESTAURANT_ID, mRestaurantID)
            .appendQueryParameter(API_DATE, mDateTime)
            .build()


        return URL(loginQueryUri.toString())

    }

    fun getResponseFromAPIForLoginDetails(
        mContext: Context,
        mGetAllLoginDetailsURl: URL?,
        mInterface: ServerLoginNetworkInterface
    ) {

        val mSharedPrefs = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        val mUserName = mSharedPrefs.getString(RESTAURANT_USER_NAME, "")
        val mPassword = mSharedPrefs.getString(RESTAURANT_PASSWORD, "")

        val mStringRequest = object : StringRequest(Method.GET, mGetAllLoginDetailsURl.toString(),
            Response.Listener {
                mInterface.onLoginDetails(it)
            }, Response.ErrorListener { }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                val credentials = "$mUserName:$mPassword"
                val auth = AUTH_BASIC + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                headers[AUTH_CONTENT_TYPE] = AUTH_CONTENT_TYPE_VALUE
                headers["Authorization"] = auth
                return headers
            }
        }

//        VolleySingleton.instance!!.addToRequestQueue(mStringRequest)

    }

    @Throws(JSONException::class, ParseException::class)
    fun getServerLoginList(mResponse: String): List<ServerLoginModel> {

        val mServerLoginList = ArrayList<ServerLoginModel>()
        val mJSONObject = JSONObject(mResponse)

        val mDataArray = mJSONObject.getJSONArray("data")

        for (i in 0 until mDataArray.length()) {
            val mLoginObject = mDataArray.getJSONObject(i)
            val mDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)

            val date = mDateFormat.parse(mLoginObject.getString("time"))

            mServerLoginList.add(
                ServerLoginModel(
                    mLoginObject.getString("user_id"),
                    mLoginObject.getString("user_name"),
                    date,
                    mLoginObject.getString("log_in_flag")
                )
            )

        }


        return mServerLoginList
    }

    interface ServerLoginNetworkInterface {
        fun onShiftDetailResponse(mResponse: String)
        fun onShiftStartEnd(mResponse: String, mPassedObject: JSONObject)
        fun onUserLoginResponse(mResponse: String)
        fun onLoginDetails(mResponse: String)
    }
}