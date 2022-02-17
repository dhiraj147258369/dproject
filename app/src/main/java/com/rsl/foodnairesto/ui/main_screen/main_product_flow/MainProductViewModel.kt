package com.rsl.foodnairesto.ui.main_screen.main_product_flow

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rsl.foodnairesto.data.database_download.models.AllergenModel
import com.rsl.foodnairesto.data.database_download.models.ProductGroupModel
import com.rsl.foodnairesto.repositories.MainProductRepository

class MainProductViewModel constructor(val mRepository: MainProductRepository) : ViewModel(){

    fun getProductGroups() :LiveData<List<ProductGroupModel>>{
        return mRepository.getProductGroups()
    }

    fun getProductGroupCategory(mGroupID : String?) : LiveData<ProductGroupModel>{
        return mRepository.getProductGroupCategory(mGroupID)
    }

    fun getAllergens(mAllergenIDs: List<String>): LiveData<List<AllergenModel>> {
        return mRepository.getAllergens(mAllergenIDs)
    }

    fun checkInternet(mContext: Context): LiveData<Boolean> {
        return mRepository.checkInternet(mContext)
    }
}