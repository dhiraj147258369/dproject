package com.rsl.youresto.ui.main_screen.favorite_items.model

class FavoriteProductModel(val mProductID: String,
                           val mProductName: String,
                           val productType: Int = 1,
                           val mGroupID: String,
                           val mCategoryID: String,
                           val mGroupName: String,
                           val mCategoryName: String,
                           val mProductImage: String,
                           val mProductSequence: Int,
                           var mCategorySequence: Int = 0,
                           var dineInPrice: Double = 0.0,
                           var quickServicePrice: Double = 0.0,
                           var isSelected: Boolean = false)