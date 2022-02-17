package com.rsl.foodnairesto.network.models


import com.google.gson.annotations.SerializedName

data class NetworkCartResponse(
    var msg: String = "",
    @SerializedName("order_id")
    var orderId: Int = 0,
    var status: Boolean = false,
    @SerializedName("table_orders_id")
    var tableOrdersId: String? = "",

    @SerializedName("order_item_ids")
    var itemIds: List<String> = ArrayList()
)