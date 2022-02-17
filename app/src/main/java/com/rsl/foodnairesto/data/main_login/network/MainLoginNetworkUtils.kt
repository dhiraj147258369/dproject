package com.rsl.foodnairesto.data.main_login.network

import android.util.Base64
import com.android.volley.*

import com.android.volley.toolbox.StringRequest
import com.rsl.foodnairesto.utils.EndPoints
import com.rsl.foodnairesto.utils.VolleySingleton
import java.nio.charset.Charset

import java.util.HashMap

object MainLoginNetworkUtils {

    fun getResponseFromAPI(
        mUsername: String,
        mPassword: String,
        mInterface: MainLoginNetworkInterface
    ) {

        val stringRequest = object : StringRequest(
            Method.GET, EndPoints.APP_LOGIN,
            Response.Listener { response ->
                mInterface.onLoginResponse(1, response, mUsername, mPassword)
            },
            Response.ErrorListener {
                mInterface.onErrorResponse(0, it, mUsername, mPassword)
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val credentials = "$mUsername:$mPassword"
                val auth = "Basic " +
                        Base64.encodeToString(credentials.toByteArray(Charset.forName("UTF-8")),
                            Base64.NO_WRAP)
                headers["Content-Type"] = "application/json"
                headers["Authorization"] = auth
                return headers
            }
        }

        stringRequest.retryPolicy = DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        VolleySingleton.instance?.addToRequestQueue(stringRequest)
    }

    interface MainLoginNetworkInterface {
        fun onLoginResponse(
            flag : Int, response: String, mUsername: String, mPassword: String
        )
        fun onErrorResponse(
            flag : Int, response: VolleyError, mUsername: String, mPassword: String
        )
    }
}