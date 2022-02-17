package com.rsl.foodnairesto.data.database_download.models

import com.google.gson.annotations.SerializedName
import java.util.*

class NewPaymentMethodModel (
    var idp:String="",

    var paymentType: String="",

    var paymentAmount: Double=0.0
)