package com.rsl.youresto.data.checkout.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rsl.youresto.data.database_download.models.TaxModel
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

@Entity
class CheckoutModel(
    val mTableNO: Int,
    val mTableID: String,
    val mGroupName: String,
    val mCartID: String,
    val mCartNO: String,
    var mCartPaymentID: String,
    var mCartTotal: BigDecimal,
    var mTaxAmount: BigDecimal,
    val mTaxPercent: BigDecimal,
    val mTaxList: ArrayList<TaxModel>,
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
    val mOrderType: Int,
    val mPaymentSelectionType: Int,
    var mFullyPaid: Int,
    var mAmountPaid: BigDecimal,
    var mAmountRemaining: BigDecimal,
    val mTimeStamp: Date,
    var mCheckoutTransaction: ArrayList<CheckoutTransaction>
) {

    @PrimaryKey(autoGenerate = true)
    var mID: Int = 0
}