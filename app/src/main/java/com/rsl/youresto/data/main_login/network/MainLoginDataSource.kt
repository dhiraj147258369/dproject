package com.rsl.youresto.data.main_login.network

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log.e
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageRequest
import com.rsl.youresto.data.main_login.MainLoginMediatorModel
import com.rsl.youresto.data.main_login.MainLoginModel
import com.rsl.youresto.utils.ImageStorage
import com.rsl.youresto.utils.VolleySingleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL
import java.util.*
import kotlin.coroutines.CoroutineContext

val LOG_TAG: String = MainLoginDataSource::class.java.simpleName

@SuppressLint("LogNotTimber")
class MainLoginDataSource constructor(private val mContext: Context) :
    MainLoginNetworkUtils.MainLoginNetworkInterface {


    companion object {

        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sInstance: MainLoginDataSource? = null

        fun getInstance(mContext: Context): MainLoginDataSource? {

            val tempInstance = sInstance
            if (tempInstance != null)
                return tempInstance

            sInstance ?: synchronized(this) {
                sInstance
                    ?: MainLoginDataSource(
                        mContext
                    ).also { sInstance = it }
            }

            return sInstance
        }

    }

    private var mMediatorData: MutableLiveData<MainLoginMediatorModel>? = null

    fun getLoginData(mUsername: String, mPassword: String): LiveData<MainLoginMediatorModel> {
        mMediatorData = MutableLiveData()
        fetchLoginData(mUsername, mPassword)

        return mMediatorData!!
    }

    private fun fetchLoginData(mUsername: String, mPassword: String) {
        MainLoginNetworkUtils.getResponseFromAPI(mUsername, mPassword, this)
    }


    override fun onLoginResponse(flag: Int, response: String, mUsername: String, mPassword: String) {
        e(LOG_TAG, "Response: $response")

        if (flag == 1) {
            val mJSONObject = JSONObject(response)

            if (mJSONObject.getString("status").equals("ok", true)) {
                val mJSONArray = mJSONObject.getJSONArray("data")

                getMainLoginModel(mJSONArray.getJSONObject(0), mUsername, mPassword)
            }
        }
    }


    override fun onErrorResponse(flag: Int, response: VolleyError, mUsername: String, mPassword: String) {
        e(LOG_TAG, "Error Response: $response")
        e(LOG_TAG, "Error Response: ${response.toString().contains("NoConnectionError")}")

        if (response.toString().contains("TimeoutError")) {
            mMediatorData!!.postValue(MainLoginMediatorModel(null, -2))
        } else if (response.toString().contains("NoConnectionError")) {
            mMediatorData!!.postValue(MainLoginMediatorModel(null, -3))
        } else {
            if (flag == 0) {
                if (response.networkResponse.statusCode == 401)
                    mMediatorData!!.postValue(MainLoginMediatorModel(null, 0))
                else
                    mMediatorData!!.postValue(MainLoginMediatorModel(null, -1))

            }
        }


    }

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    private fun getMainLoginModel(mDataObject: JSONObject, mUsername: String, mPassword: String) {

        val mURL = mDataObject.getString("merchant_image").replace("localhost", "54.90.226.3")
        val mImageURL = URL(mURL)

        val scope = CoroutineScope(coroutineContext)

        scope.launch(Dispatchers.IO) {

            val mImageRequest = object : ImageRequest(mImageURL.toString(), Response.Listener {

                val mPath = ImageStorage.saveToInternalStorage(it, "restaurant_logo", mContext)

                e(LOG_TAG, "Path: $mPath")

                val mainLoginModel = MainLoginModel(
                    mDataObject.getString("restaurant_id"),
                    mDataObject.getString("restaurant_name"),
                    mDataObject.getString("shortname"),
                    "",
                    mDataObject.getString("webadd"),
                    mDataObject.getString("mailid"),
                    mDataObject.getString("add1"),
                    mDataObject.getString("add2"),
                    "",
                    mDataObject.getString("city"),
                    mDataObject.getString("pin"),
                    mDataObject.getString("mess1"),
                    mDataObject.getString("mess2"),
                    mDataObject.getString("mobno"),
                    mUsername,
                    mPassword,
                    mPath,
                    mDataObject.getString("merchant_TID"),
                    mDataObject.getString("merchant_TK")
                )

                mMediatorData!!.postValue(MainLoginMediatorModel(mainLoginModel, 1))

            },
                0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565,
                Response.ErrorListener { mMediatorData!!.postValue(MainLoginMediatorModel(null, -3)) }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    val credentials = "$mUsername:$mPassword"
                    val auth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                    headers["Content-Type"] = "application/json"
                    headers["Authorization"] = auth
                    return headers
                }
            }

            VolleySingleton.instance?.addToRequestQueue(mImageRequest)

        }


    }

}