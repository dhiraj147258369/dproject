package com.rsl.youresto.data.tables.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class LocalTableGroupModel(
    var mGroupName: String? = null,
    var isSelected: Boolean = false,
    val mTableNO: Int,
    val mTableID: String?,
    val mLocationID: String?,
    var mSeatList: ArrayList<LocalTableSeatModel>? = null
) {
    @PrimaryKey(autoGenerate = true)
    var mID: Int = 0
}