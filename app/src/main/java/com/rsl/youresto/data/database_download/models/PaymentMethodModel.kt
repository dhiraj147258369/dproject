package com.rsl.youresto.data.database_download.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
class PaymentMethodModel(
    @PrimaryKey
    @SerializedName("id")
    val mPaymentMethodID: String = "1",

    @SerializedName("name")
    val mPaymentMethodName: String = "1",

    @SerializedName("type")
    val mPaymentMethodType: Int,

    val mPaymentImageResource: Int
) {
    override fun toString(): String {
        return mPaymentMethodName
    }
}