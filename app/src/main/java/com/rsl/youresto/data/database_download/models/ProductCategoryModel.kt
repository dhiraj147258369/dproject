package com.rsl.youresto.data.database_download.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class ProductCategoryModel(

    @SerializedName("categoryName")
    val mCategoryName: String = "",

    @PrimaryKey
    @SerializedName("categoryId")
    val mCategoryID: String = "",

    @SerializedName("groupId")
    val mGroupID: String = "",

    @SerializedName("isActive")
    val mCategoryActive: Boolean = true,

    val mDoNotDisplayOn: Boolean,

    @SerializedName("imageUrl")
    val mCategoryImageUrl: String = "",
    val mCategorySequence: Int,
    val isCompulsory: Boolean,
    val mModifierSelection: Int,
    var mProductList: ArrayList<ProductModel>? = null)