package com.rsl.youresto.data.cart.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rsl.youresto.data.database_download.models.IngredientCategoryModel
import com.rsl.youresto.data.database_download.models.IngredientsModel
import com.rsl.youresto.data.database_download.models.SubProductCategoryModel
import com.rsl.youresto.data.tables.models.ServerTableSeatModel
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

@Entity
class CartProductModel(
    val mLocationID: String,
    val mTableID: String,
    val mTableNO: Int,
    val mGroupName: String,
    var mTotalGuestsCount: Int,
    var mAssignedSeats: ArrayList<ServerTableSeatModel>? = null,
    var mCartID: String,
    var mCartNO: String,
    var mCartProductID: String,
    val mOrderType: Int,
    val mServerID: String,
    val mServerName: String,
    var mProductID: String? = null,
    val mProductCategoryID: String,
    val mProductCategoryName: String,
    val mProductGroupID: String,
    val mProductGroupName: String,
    val mCourseType: String,
    val mProductType: Int,
    val mProductName: String,
    var mProductUnitPrice: BigDecimal,
    var mProductQuantity: BigDecimal,
    var mProductTotalPrice: BigDecimal,
    var mSpecialInstruction: String,
    var mSpecialInstructionPrice: BigDecimal,
    var mEditModifierList: ArrayList<IngredientCategoryModel>? = null,
    var mShowModifierList: ArrayList<IngredientsModel>? = null,
    var mSubProductCategoryList: ArrayList<SubProductCategoryModel>? = null,
    var mSubProductsList: ArrayList<CartSubProductModel>,
    var mDateInMillis: Date,
    var mDate: String,
    var mKitchenPrintFlag: Int,
    val mPrinterID: String,
    var mSequenceNO: Int,
    var isSelectedForRepeatOrder: Boolean,
    var taxName: String = "",
    var taxPercentage: Double = 0.0,
    var tableOrderId: String = "",
) {
    @PrimaryKey(autoGenerate = true)
    var mID: Int = 0
}