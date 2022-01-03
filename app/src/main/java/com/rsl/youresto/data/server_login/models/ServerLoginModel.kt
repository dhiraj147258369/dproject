package com.rsl.youresto.data.server_login.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
class ServerLoginModel(
    val mServerID: String,
    val mServerName: String,
    val mDateTime: Date,
    val mLogInFlag: String) {

    @PrimaryKey(autoGenerate = true)
    var mID: Int = 0
}