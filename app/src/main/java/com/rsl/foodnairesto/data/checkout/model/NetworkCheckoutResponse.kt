package com.rsl.foodnairesto.data.checkout.model


import com.google.gson.annotations.SerializedName

data class NetworkCheckoutResponse(
    @SerializedName("invoice_id")
    var invoiceId: Int = 0,
    var msg: String = "",
    var status: Boolean = false
)