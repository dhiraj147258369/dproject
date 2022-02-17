package com.rsl.foodnairesto.data.checkout.model

import java.math.BigDecimal

class PaymentTransaction(
    var mTransactionID: String,
    var mReferenceNO: String,
    val mPaymentMethodID: String,
    val mPaymentMethodName: String,
    val mPaymentMethodType: Int,
    var mTransactionAmount: BigDecimal,
    //cash
    var mCash: BigDecimal,
    var mChange: BigDecimal,
    //card
    val mCardNo: String,
    val mCardType: String,
    val mCardProvider: String,
    val mCardEntryType: String,
    //wallet
    var mWalletName: String,
    var mWalletQRCode: String
)