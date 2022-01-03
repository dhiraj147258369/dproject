package com.rsl.youresto.data.favorite_items.network

import com.rsl.youresto.data.favorite_items.models.PostFavorites
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface FavoriteApi {

    @POST("saveFavoriteProduct")
    fun saveFavorites(@Body favorites: PostFavorites): Call<NetworkSaveFavoriteResponse>
}

data class NetworkSaveFavoriteResponse(
    val status: String = "",
    val data: String = ""
)