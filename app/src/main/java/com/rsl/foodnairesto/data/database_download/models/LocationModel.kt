package com.rsl.foodnairesto.data.database_download.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.util.ArrayList

@Entity
class LocationModel(

    @PrimaryKey
    @SerializedName("locationId")
    val mLocationID: String = "",

    @SerializedName("locationName")
    val mLocationName: String = "",

    @SerializedName("locationtype")
    val mLocationType: String = "1"
) {
    @Ignore
    var tables: ArrayList<TablesModel> = ArrayList()
}