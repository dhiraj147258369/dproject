package com.rsl.youresto.network.models


import com.google.gson.annotations.SerializedName

data class NetworkCartResponse(
    var msg: String = "",
    @SerializedName("order_id")
    var orderId: Int = 0,
    var status: Boolean = false,
    @SerializedName("table_orders_id")
    var tableOrdersId: Any = Any(),

    @SerializedName("order_item_ids")
    var itemIds: List<String> = ArrayList()
)