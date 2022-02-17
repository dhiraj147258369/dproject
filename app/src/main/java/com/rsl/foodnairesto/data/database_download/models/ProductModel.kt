package com.rsl.foodnairesto.data.database_download.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

@Entity
data class ProductModel(
    var mID: Int = 0,

    @PrimaryKey
    @SerializedName("productID")
    var mProductID: String = "",

    @SerializedName("isActive")
    var mProductActive: Boolean = true,

    @SerializedName("categoryId")
    var mCategoryID: String? = "",

    var mCategoryName: String = "",

    @SerializedName("groupId")
    var mGroupID: String? = "",

    var mGroupName: String = "",

    @SerializedName("ingredients")
    var mIngredientsList: ArrayList<String> = ArrayList(),
    var mAllergenList: ArrayList<String> = ArrayList(),

    @SerializedName("productName")
    var mProductName: String = "",

    @SerializedName("description")
    var mProductDescription: String? = "",

    @SerializedName("price")
    var mDineInPrice: BigDecimal = BigDecimal(0),

    @SerializedName("tablewise_price")
    var mTablewisePrice:ArrayList<TableWisePrice> = ArrayList(),

    var mQuickServicePrice: BigDecimal = BigDecimal(0),
    var mDeliveryPrice: BigDecimal = BigDecimal(0),

    @SerializedName("imageUrl")
    var mProductImageUrl: String = "",

    @SerializedName("type")
    var mProductType: Int = 0,
    var mProductSequence: Int = 0,
    var mCategorySequence: Int = 0,

    @SerializedName("kitchenId")
    var mPrinterID: String = "",

    @Ignore
    var mGenericProductList: ArrayList<GenericProducts> = ArrayList(),

    @Ignore
    var mSubProductCategoryList: ArrayList<SubProductCategoryModel> = ArrayList(),

    var isSelected: Boolean = false,

    @SerializedName("tax_name")
    var taxName: String = "",

    @SerializedName("tax_per")
    var taxPercentage: String = ""

)

data class TableWisePrice(
    @SerializedName("table_category_id")
    var mTableid:String="",
    @SerializedName("price")
    var mPricew:BigDecimal = BigDecimal(0)

)