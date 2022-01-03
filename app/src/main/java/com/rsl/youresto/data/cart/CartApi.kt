package com.rsl.youresto.data.cart

import com.rsl.youresto.data.checkout.model.NetworkCheckoutResponse
import com.rsl.youresto.data.checkout.model.PostCheckout
import com.rsl.youresto.network.models.NetworkCartResponse
import com.rsl.youresto.network.models.PostCart
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface CartApi {

    @POST("placeorder")
    fun submitCart(@Query("restaurant_id") restaurantId: String, @Body cart: PostCart): Call<NetworkCartResponse>

    @POST("create_invoice")
    fun checkoutOrder(@Body checkout: PostCheckout): Call<NetworkCheckoutResponse>

}