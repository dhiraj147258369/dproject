package com.rsl.youresto.data.database_download.models

import java.math.BigDecimal
import java.util.ArrayList

class GenericProducts(
    val mID: Int,
    val mGenericProductID: String,
    val mProductID: String,
    val mCategoryID: String,
    val mGroupID: String,
    val mGenericProductName: String,
    val mDineInPrice: BigDecimal,
    val mQuickServicePrice: BigDecimal,
    val mDeliveryPrice: BigDecimal,
    val mIngredientLimit: Int,
    var mIngredientCategoryList: ArrayList<IngredientCategoryModel>,
    var isSelected: Boolean = false)