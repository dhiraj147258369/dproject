package com.rsl.foodnairesto.data.main_login

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MainLoginModel(
    val mRestaurantID: String,
    val mRestaurantName: String,
    val mShortName: String,
    val mIPAddress: String,
    val mWebAddress: String,
    val mMailAddress: String,
    val mAddress1: String,
    val mAddress2: String,
    val mAddress3: String,
    val mCity: String,
    val mPinCode: String,
    val mMobileNo: String,
    val mReceiptMessage1: String,
    val mReceiptMessage2: String,
    val mUsername: String,
    val mPassword: String,
    val mRestaurantLogo: String,
    val mMerchantTID: String,
    val mMerchantTK: String
){
    @field:PrimaryKey(autoGenerate = true)
    var mID: Int? = null
}