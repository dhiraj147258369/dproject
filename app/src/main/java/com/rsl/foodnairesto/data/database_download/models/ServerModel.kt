package com.rsl.foodnairesto.data.database_download.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
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
    @SerializedName("usertype")
    var mUserType: String = "",

    @SerializedName("location")
    var mLocations: ArrayList<LocationUserList> = ArrayList(),

    var mServerAccessList: ArrayList<Int> = ArrayList()

    
)
data class LocationUserList(
    @SerializedName("location_id")
    var mLocation_Id:String="",
    @SerializedName("location_type")
    var mLocation_type: String = ""

)