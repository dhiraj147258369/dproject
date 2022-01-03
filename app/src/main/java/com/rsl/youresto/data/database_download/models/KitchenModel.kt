package com.rsl.youresto.data.database_download.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
class KitchenModel(

    @PrimaryKey
    @SerializedName("kitchenId")
    val mKitchenID: String = "",

    @SerializedName("kitchenName")
    val mKitchenName: String = "",
    val mSelectedKitchenPrinterName: String = "",
    val mSelectedKitchenPrinterSize: Int = 80,
    val mLogWoodServerIP: String = "",
    val mLogWoodServerPort: String = "",
    val mNetworkPrinterIP: String = "",
    val mNetworkPrinterPort: String = "",
    var mPrinterType: Int = 0
)