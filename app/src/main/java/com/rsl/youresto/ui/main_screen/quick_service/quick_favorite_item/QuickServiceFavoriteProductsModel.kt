package com.rsl.youresto.ui.main_screen.quick_service.quick_favorite_item

import com.rsl.youresto.data.database_download.models.ProductModel
import java.util.ArrayList

class QuickServiceFavoriteProductsModel(
    val mID: Int,
    val mCategoryID: String,
    val mCategoryName: String,
    val mCategorySequence: Int,
    val mProductList: ArrayList<ProductModel>
)