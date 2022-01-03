package com.rsl.youresto.network

import com.rsl.youresto.App
import com.rsl.youresto.R
import org.json.JSONObject
import retrofit2.Call
import retrofit2.HttpException
import java.io.IOException

sealed class ApiResponse<T> {
    companion object {
        fun <T> create(apiCall: Call<T>): ApiResponse<T> {
            val resources = App.resource
            try {
                val response = apiCall.execute()
                return if (response.isSuccessful) {
                    val body = response.body()

                    // Empty body
                    if (body == null || response.code() == 204 || response.code() == 302) {
                        val headers = response.headers()
                        if (headers.size > 0) {
                            val headersMap: HashMap<String, Any> = hashMapOf()
                            headers.forEach {
                                headersMap[it.first] = it.second
                            }
                            return ApiSuccessEmptyResponseWithHeaders(headersMap)
                        }
                        return ApiSuccessEmptyResponse()
                    } else {
                        ApiSuccessResponse(body)
                    }
                } else {

                    val msg = response.errorBody()?.string()
                    val errorMessage = if (msg.isNullOrEmpty()) {
                        response.message()
                    } else {
                        val errorObject = JSONObject(msg)
                        if (errorObject.has("message")) errorObject.getString("message") else null
                    }

//                    val responseBody = object : TypeToken<ErrorData>() {}.type
//                    val errorData: ErrorData? =  Gson().fromJson(msg, responseBody)
                    ApiErrorResponse(errorMessage ?: resources.getString(R.string.general_error), null)

                }
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                return when (throwable) {
                    is IOException -> ApiErrorResponse(resources.getString(R.string.no_internet_connected), null)
                    is HttpException -> {
                        val errorMessage = throwable.response()?.errorBody()?.string()
                        ApiErrorResponse(
                            errorMessage ?: resources.getString(R.string.general_error), null
                        )
                    }
                    else -> {
                        ApiErrorResponse(resources.getString(R.string.general_error), null)
                    }
                }
            }
        }
    }
}

class ApiSuccessResponse<T>(val data: T) : ApiResponse<T>()
class ApiSuccessEmptyResponse<T> : ApiResponse<T>()
class ApiSuccessEmptyResponseWithHeaders<T>(val headers: Map<String, Any>) : ApiResponse<T>()
class ApiErrorResponse<T>(val errorMessage: String, val data: T?) : ApiResponse<T>()