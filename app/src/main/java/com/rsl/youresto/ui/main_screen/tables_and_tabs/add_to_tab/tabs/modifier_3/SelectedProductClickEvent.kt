package com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_3

import com.rsl.youresto.data.database_download.models.ProductModel

class SelectedProductClickEvent(
    val mProductList: ArrayList<ProductModel>,
    val mSelectedProductID: String
)