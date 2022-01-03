package com.rsl.youresto.data.favorite_items.models

import com.google.gson.annotations.SerializedName

class PostFavorites(
    @SerializedName("restaurant_id")
    val restaurantId: String = "",

    @SerializedName("location_id")
    val locationId: String = "",

    @SerializedName("product_id")
    val productIds: ArrayList<String> = ArrayList()
)