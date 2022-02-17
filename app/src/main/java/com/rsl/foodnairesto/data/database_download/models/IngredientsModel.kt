package com.rsl.foodnairesto.data.database_download.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

@Entity
class IngredientsModel(

    @PrimaryKey
    @SerializedName("ingredientId")
    val mIngredientID: String = "",

    @SerializedName("ingredientName")
    val mIngredientName: String = "",

    @SerializedName("ingredientPrice")
    val mIngredientPrice: BigDecimal,

    var mIngredientQuantity: BigDecimal,

    @SerializedName("productId")
    val mIngredientProductID: String = "",

    @SerializedName("productCategoryId")
    val mIngredientCategoryID: String = "",

    var isSelected: Boolean = false,
    var isCompulsory: Boolean = false,
    var mSelectionType: Int = 0,
    var mCartIngredientID: String? = null,

    var ingredientQuantity: String = "",
) {
    override fun toString(): String {
        return "IngredientsModel(mIngredientID='$mIngredientID', mIngredientName='$mIngredientName', mIngredientPrice=$mIngredientPrice, mIngredientQuantity=$mIngredientQuantity, mIngredientProductID='$mIngredientProductID', mIngredientCategoryID='$mIngredientCategoryID', isSelected=$isSelected, isCompulsory=$isCompulsory, mSelectionType=$mSelectionType, mCartIngredientID=$mCartIngredientID, ingredientQuantity='$ingredientQuantity')"
    }
}