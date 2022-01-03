package com.rsl.youresto.data.database_download.models

class IngredientCategoryModel(
    val mCategoryID: String,
    var mCategoryName: String,
    var mIngredientsList: ArrayList<IngredientsModel>? = null,
    var isCompulsory: Boolean,
    var mModifierSelection: Int,
    var mCategorySequence: Int
)