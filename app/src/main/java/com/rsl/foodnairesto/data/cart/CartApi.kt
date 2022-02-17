package com.rsl.foodnairesto.data.cart

import com.rsl.foodnairesto.data.checkout.model.NetworkCheckoutResponse
import com.rsl.foodnairesto.data.checkout.model.PostCheckout
import com.rsl.foodnairesto.network.models.NetworkCartResponse
import com.rsl.foodnairesto.network.models.PostCart
import com.rsl.foodnairesto.network.models.ReceiveCart
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