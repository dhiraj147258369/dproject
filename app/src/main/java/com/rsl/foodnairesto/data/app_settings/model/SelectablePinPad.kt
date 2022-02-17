package com.rsl.foodnairesto.data.app_settings.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class SelectablePinPad(
    var mPinPadName: String,
    var mConnectionType: String,
    val mLocationID: String
) {
    @PrimaryKey(autoGenerate = true)
    var mID: Int = 0
}