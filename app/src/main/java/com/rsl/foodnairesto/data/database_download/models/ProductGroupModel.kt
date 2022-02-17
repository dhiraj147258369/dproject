package com.rsl.foodnairesto.data.database_download.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
class ProductGroupModel(

    @SerializedName("groupName")
    val mGroupName: String = "",

    @PrimaryKey
    @SerializedName("groupId")
    val mGroupID: String = "",

    @SerializedName("isActive")
    val mGroupActive: Boolean = true,

    @SerializedName("imageUrl")
    val mGroupImageURL: String = "",

    val mDoNotDisplayOn: Boolean = false,
    val mProductCategoryList: ArrayList<ProductCategoryModel> = ArrayList()
)