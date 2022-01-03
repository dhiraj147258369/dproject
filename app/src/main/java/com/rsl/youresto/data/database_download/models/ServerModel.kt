package com.rsl.youresto.data.database_download.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.ArrayList

@Entity
class ServerModel(

    @PrimaryKey
    @SerializedName("id")
    var mServerID: String = "",

    @SerializedName("name")
    var mServerName: String = "",

    @SerializedName("pin")
    var mServerPassword: String = "",

    @SerializedName("password")
    var mServerActualPassword: String = "",

    var mServerAccessList: ArrayList<Int> = ArrayList()
)