package com.rsl.youresto.data.cart.models

import com.rsl.youresto.data.database_download.models.IngredientsModel
import java.math.BigDecimal

class CartSubProductModel(
    val mProductID: String,
    val mCategoryID: String,
    val mGroupID: String,
    val mIngredientsList: ArrayList<IngredientsModel>,
    val mProductName: String,
    val mDineInPrice: BigDecimal,
    val mQuickServicePrice: BigDecimal,
    val mDeliveryPrice: BigDecimal,
    val mProductType: Int,
    var mProductSequence: Int = 0,
    var mCategorySequence: Int = 0
)