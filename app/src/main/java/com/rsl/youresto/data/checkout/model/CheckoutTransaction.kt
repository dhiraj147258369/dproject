package com.rsl.youresto.data.checkout.model

import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class CheckoutTransaction(
    val mSeatList: ArrayList<Int>,
    var isSelected: Boolean,
    var isFullPaid: Boolean,
    val mSeatCartTotal: BigDecimal,
    var mTaxAmount: BigDecimal,
    var mTipAmount: BigDecimal,
    var mTipPercent: BigDecimal,
    var mServiceChargeAmount: BigDecimal,
    var mServiceChargePercent: BigDecimal,
    var mDiscountAmount: BigDecimal,
    var mDiscountPercent: BigDecimal,
    var mVoucherCode: String,
    var mVoucherAmount: BigDecimal,
    var mVoucherPercent: BigDecimal,
    var mOrderTotal: BigDecimal,
    var mAmountPaid: BigDecimal,
    var mAmountRemaining: BigDecimal,
    val mTimeStamp: Date,
    var isSentToServer: Boolean,
    var mPaymentTransaction: ArrayList<PaymentTransaction>
    )