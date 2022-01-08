package com.rsl.youresto.data.database_download.models

import java.util.*

class ReportProductModel(
    val mGroupID: String,
    val mCategoryID: String,
    val mProductID: String,
    val mProductName: String,
    val mProductQuantity: Int,
    val mCourseType: String,
    val mProductUnitPrice: Double,
    var mProductTotalPrice: Double = 0.toDouble(),
    val mProductType: Int,
    val mSpecialInstruction: String,
    val mSpecialInstructionPrice: Double,
    val mDateTime: String,
    val mPrinterID: String,
    val mCartProductID: String,
    val mAssignedSeats: String,
    val mIngredientList: ArrayList<ReportProductIngredientModel>
)