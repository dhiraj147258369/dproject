package com.rsl.youresto.data.database_download.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*
import kotlin.collections.ArrayList

@Entity
class ReportModel(
 var mRestaurantID: String = "",
 var mPaymentSelectionType: String = "",
 var mTableNO: Int = 0,
 var mCartID: String = "",
 var mCartNO: String = "",
 var mCartTotal: Double = 0.0,
 var mTableID: String = "",
 var mDiscountType: String = "",
 var mDiscountPercent: Double = 0.0,
 var mDiscountAmount: Double = 0.0,
 var mTaxPercent: Double = 0.0,
 var mTaxAmount: Double = 0.0,
 var mAmountPaid: Double = 0.0,
 var mOrderTotal: Double = 0.0,
 var mDateTime: String = "",
 var mDateTimeInTimeStamp: Date = Date(),
 var mOrderType: Int = 0,
 var mServerName: String = "",
 var mPaymentList: ArrayList<ReportPaymentModel> = ArrayList(),
 var mProductList: ArrayList<ReportProductModel> = ArrayList(),
 var deliverCharges: Double = 0.0
) {
 @PrimaryKey
 var id: String = ""
 //    @PrimaryKey(autoGenerate = true)
 var mID: Int = 0
}