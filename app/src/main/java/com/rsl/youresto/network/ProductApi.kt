package com.rsl.youresto.network

import com.rsl.youresto.network.models.NetworkCartResponse
import com.rsl.youresto.network.models.PostCart
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface ProductApi {

    @POST("placeorder")
    fun submitCart(@Query("restaurant_id") restaurantId: String, @Body cart: PostCart): Call<NetworkCartResponse>

}