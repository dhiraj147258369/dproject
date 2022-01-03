package com.rsl.youresto.data.database_download.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class AllergenModel(val mAllergenID: String,
                    val mAllergenName: String,
                    val mAllergenDescription: String) {

    @PrimaryKey(autoGenerate = true)
    var mID: Int = 0
}