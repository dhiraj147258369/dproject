package com.rsl.youresto.data.main_product.models

import com.rsl.youresto.data.tables.models.ServerTableSeatModel
import java.math.BigDecimal

class CartBarModel(
    var mCartID: String? = null,
    var mCartNO: String? = null,
    var mTableOccupiedID: String? = null,
    val mGroupName: String,
    var mSeatList: ArrayList<ServerTableSeatModel>? = null,
    var mGroupTotal: BigDecimal,
    var mCartItemsCount: Int
)