package com.rsl.youresto.data.server_login

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rsl.youresto.data.server_login.models.ServerLoginModel
import com.rsl.youresto.data.server_login.models.ServerShiftModel
import com.rsl.youresto.utils.AppConstants.API_DATE
import com.rsl.youresto.utils.AppConstants.API_RESTAURANT_ID
import com.rsl.youresto.utils.AppConstants.API_USER_ID
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
@SuppressLint("LogNotTimber")
class ServerLoginDataSource constructor(val context: Context) : ServerLoginNetworkUtils.ServerLoginNetworkInterface {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sInstance: ServerLoginDataSource? = null

        fun getInstance(mContext: Context): ServerLoginDataSource? {
            val tempInstance = sInstance
            if (tempInstance != null)
                return tempInstance

            sInstance ?: synchronized(this) {
                sInstance ?: ServerLoginDataSource(mContext).also { sInstance = it }
            }

            return sInstance
        }
    }

    private val mServerShiftData: MutableLiveData<List<ServerShiftModel>> = MutableLiveData()

    fun getShiftStatusFromServer(mServerID: String, mCurrentDate: Date): LiveData<List<ServerShiftModel>> {
        val mGetShiftDetailsURL = ServerLoginNetworkUtils.getShiftDetailsUrl(context, mServerID, mCurrentDate)

        ServerLoginNetworkUtils.getResponseFromAPIForShiftDetails(context, mGetShiftDetailsURL, this)

        return mServerShiftData
    }

    private val mServerStartShiftData: MutableLiveData<List<ServerShiftModel>> = MutableLiveData()

    fun startEndShift(mServerID: String, mShiftFlag: String): LiveData<List<ServerShiftModel>> {

        ServerLoginNetworkUtils.getResponseFromServerStartShift(context, mServerID, mShiftFlag, this)

        return mServerStartShiftData
    }

    override fun onShiftDetailResponse(mResponse: String) {

        val mJSONObject = JSONObject(mResponse)

        val mDataJSONArray = mJSONObject.getJSONArray("data")

        val mLoginObject = mDataJSONArray.getJSONObject(0)

        if (mLoginObject.has("user_shifts")) {

            val mShiftList =
                ArrayList(ServerLoginNetworkUtils.getShiftDetailsList(mDataJSONArray.getJSONObject(0)))
            mServerShiftData.postValue(mShiftList)
        }else{
            mServerShiftData.postValue(ArrayList())
        }
    }


    override fun onShiftStartEnd(mResponse: String, mPassedObject: JSONObject) {

        Log.e("ServerLoginDataSource", "onShiftStartEnd: $mResponse")

        try {
            val mJSONObject = JSONObject(mResponse)
            val mJSONArray = mJSONObject.getJSONArray("data")
            val mObject = mJSONArray.getJSONObject(0)
            if (mObject.getString("msg").contains("Shift Started") || mObject.getString("msg").contains("Shift Ended")) {

                val mDateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
                val mDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

                val mDateTime = mDateTimeFormat.parse(mPassedObject.getString(API_DATE))
                val date = mDateFormat.parse(mPassedObject.getString(API_DATE))
                val c = Calendar.getInstance()
                c.time = date
                val dayOfWeek = c.get(Calendar.DAY_OF_WEEK) - 1
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

                var mStartTime: Date? = null
                var mEndTime: Date? = null
                if (mObject.getString("msg").contains("Shift Started"))
                    mStartTime = mDateTime
                else
                    mEndTime = mDateTime

                val mShiftModel = ServerShiftModel(
                    mPassedObject.getString(API_RESTAURANT_ID),
                    mPassedObject.getString(API_USER_ID),
                    date,
                    mDay,
                    mStartTime,
                    mEndTime,
                    ""
                )

                val mShiftList = java.util.ArrayList<ServerShiftModel>()
                mShiftList.add(mShiftModel)

                Log.e("", "onShiftStartEnd: " + mShiftModel.mStartTimeStamp)

                mServerStartShiftData.postValue(mShiftList)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val mSubmitLoginData = MutableLiveData<Int>()

    fun submitLoginDetails(mServerID: String, mLoginFlag: String): LiveData<Int> {
        submitLogin(mServerID, mLoginFlag)
        return mSubmitLoginData
    }

    private fun submitLogin(mServerID: String, mLoginFlag: String) {
        ServerLoginNetworkUtils.getResponseFromServerLogin(context, mServerID, mLoginFlag, this)
    }

    override fun onUserLoginResponse(mResponse: String) {
        Log.e(javaClass.simpleName, "userLoginResponse: $mResponse")

            val mJSONObject = JSONObject(mResponse)

            if (mJSONObject.getString("status").equals("ok", ignoreCase = true)) {
                mSubmitLoginData.postValue(1)
            }else{
                mSubmitLoginData.postValue(0)
            }
    }

    private val mServerLoginData = MutableLiveData<List<ServerLoginModel>>()

    fun getAndStoreAllLoginDetails(): LiveData<List<ServerLoginModel>> {

        val mGetAllLoginDetailsURl = ServerLoginNetworkUtils.getLoginDetailsUrl(context)

        ServerLoginNetworkUtils.getResponseFromAPIForLoginDetails(context, mGetAllLoginDetailsURl, this)

        return mServerLoginData
    }

    override fun onLoginDetails(mResponse: String) {
        mServerLoginData.postValue(ServerLoginNetworkUtils.getServerLoginList(mResponse))
    }
}