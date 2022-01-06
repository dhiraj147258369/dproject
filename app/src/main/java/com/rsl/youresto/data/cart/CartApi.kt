package com.rsl.youresto.data.cart

import com.rsl.youresto.data.checkout.model.NetworkCheckoutResponse
import com.rsl.youresto.data.checkout.model.PostCheckout
import com.rsl.youresto.network.models.NetworkCartResponse
import com.rsl.youresto.network.models.PostCart
import com.rsl.youresto.network.models.ReceiveCart
import retrofit2.Call
import retrofit2.http.*

interface CartApi {

    @POST("placeorder")
    fun submitCart(@Query("restaurant_id") restaurantId: String, @Body cart: PostCart): Call<NetworkCartResponse>

    @POST("create_invoice")
    fun checkoutOrder(@Body checkout: PostCheckout): Call<NetworkCheckoutResponse>

    @FormUrlEncoded
    @POST("allorders_by_location")
    fun syncCarts(
        @Field("restaurant_id") names: String,
        @Field("location_id") locationId: String): Call<List<ReceiveCart>>

}