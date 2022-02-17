package com.rsl.foodnairesto.data.database_download.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rsl.foodnairesto.ui.main_screen.favorite_items.model.FavoriteProductModel


@Entity
class FavoriteItemsModel(
            val mGroupID: String,
            val mGroupName: String,
            val mCategoryID: String,
            val mCategoryName: String,
            val mCategorySequence: Int,
            val mLocationID: String,
            val mProductArrayList: ArrayList<FavoriteProductModel>) {

    @PrimaryKey(autoGenerate = true)
    var mID: Int = 0
}