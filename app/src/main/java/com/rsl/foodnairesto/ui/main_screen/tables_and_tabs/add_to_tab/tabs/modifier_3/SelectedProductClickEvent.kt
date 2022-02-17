package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_3

import com.rsl.foodnairesto.data.database_download.models.ProductModel

class SelectedProductClickEvent(
    val mProductList: ArrayList<ProductModel>,
    val mSelectedProductID: String
)