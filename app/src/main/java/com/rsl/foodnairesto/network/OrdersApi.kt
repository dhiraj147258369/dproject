package com.rsl.foodnairesto.network

import com.rsl.foodnairesto.network.models.NetworkReportModel
import retrofit2.Call
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface OrdersApi {

    @POST("cartReport")
    @FormUrlEncoded
    fun getCartReports(@FieldMap params: HashMap<String, String>): Call<NetworkReportModel>
}