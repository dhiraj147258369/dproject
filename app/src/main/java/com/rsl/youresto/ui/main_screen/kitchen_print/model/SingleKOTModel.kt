package com.rsl.youresto.ui.main_screen.kitchen_print.model

import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.database_download.models.KitchenModel

class SingleKOTModel(
    var mSerialNO: Int = 0,
    var mKitchenModel: KitchenModel = KitchenModel(),
    var mProductList: ArrayList<CartProductModel> = ArrayList()
)