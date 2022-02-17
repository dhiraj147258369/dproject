package com.rsl.foodnairesto.data.tables.models

class LocalTableSeatModel(
    val mSeatNO: Int,
    var mGroup: String? = null,
    val mTableNO: Int,
    var mTableID: String? = null,
    var isSelected: Boolean = false,
    var isPaid: Boolean = false,
    var isOccupied: Boolean = false
)