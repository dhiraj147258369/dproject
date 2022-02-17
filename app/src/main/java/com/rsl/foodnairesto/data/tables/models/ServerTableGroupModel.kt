package com.rsl.foodnairesto.data.tables.models

import java.math.BigDecimal

class ServerTableGroupModel(
    var mCartID: String? = null,
    var mCartNO: String? = null,
    var mTableOccupiedID: String? = null,
    val mGroupName: String,
    val mTableNO: Int,
    val mTableID: String?,
    var mSeatList: ArrayList<ServerTableSeatModel>? = null,
    var mGroupTotal: BigDecimal,
    var isOccupied: Boolean = false
)