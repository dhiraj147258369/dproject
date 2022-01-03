package com.rsl.youresto.data.database_download.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.rsl.youresto.data.tables.models.ServerTableGroupModel
import java.util.*

@Entity
class TablesModel(

    @SerializedName("locationId")
    var mLocationID: String = "",

    @PrimaryKey
    @SerializedName("tableId")
    var mTableID: String = "",

    @SerializedName("table_no")
    var mTableNo: Int = 0,

    @SerializedName("noOfChairs")
    var mTableTotalNoOfChairs: Int = 0,

    var mTableNoOfOccupiedChairs: Int = 0,

    @SerializedName("table_type")
    var mTableType: Int = 0,

    var mOccupiedByUser: String = "",
    var mOccupiedByUserID: String = "",
    var mGroupList: ArrayList<ServerTableGroupModel>? = null
)