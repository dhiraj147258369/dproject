package com.rsl.youresto.data.database_download.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

@Entity
class TaxModel(
    @PrimaryKey
    @SerializedName("taxId")
    val mTaxID: String = "",

    @SerializedName("tinNo")
    val mTIN: String = "",

    @SerializedName("taxName")
    val mTaxName: String = "",

    @SerializedName("taxPercentage")
    val mTaxPercentage: BigDecimal
)