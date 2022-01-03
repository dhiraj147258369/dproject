package com.rsl.youresto.network

import com.rsl.youresto.data.main_login.network.NetworkLogin
import com.rsl.youresto.network.models.PostLogin
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LoginApi {

    @POST("applogin")
    fun authenticateUserWithEmail(@Body login: PostLogin): Call<NetworkLogin>

    @GET("getAllData")
    fun getData(@Query("restaurantId") restaurantId: String): Call<NetworkRestaurantData>

    @GET("getFavoriteProduct")
    fun getFavorites(@Query("restaurant_id") restaurantId: String, @Query("location_id") locationId: String): Call<NetworkResponseFavorites>
}