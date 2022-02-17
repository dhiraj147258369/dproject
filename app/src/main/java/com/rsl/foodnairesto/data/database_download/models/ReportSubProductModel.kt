package com.rsl.foodnairesto.data.database_download.models

import java.util.ArrayList

class ReportSubProductModel(
     val mSubProductID: String,
     val mSubProductName: String,
     val mSubProductUnitPrice: Double,
     val mSubProductQuantity: Int,
     val mCartSubProductID: String,
     val mIngredientList: ArrayList<ReportProductIngredientModel>
)