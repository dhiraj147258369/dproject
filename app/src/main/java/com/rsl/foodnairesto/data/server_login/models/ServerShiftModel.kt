package com.rsl.foodnairesto.data.server_login.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
class ServerShiftModel(
    val mRestaurantID: String,
    val mServerID: String,
    val mShiftDate: Date,
    val mShiftDay: String,
    val mStartTimeStamp: Date?,
    val mEndTimeStamp: Date?,
    val mShiftHours: String) {

    @PrimaryKey(autoGenerate = true)
    var mID: Int = 0
}