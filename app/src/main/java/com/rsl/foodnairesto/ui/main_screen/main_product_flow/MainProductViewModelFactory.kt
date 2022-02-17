package com.rsl.foodnairesto.ui.main_screen.main_product_flow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rsl.foodnairesto.repositories.MainProductRepository

class MainProductViewModelFactory(val mRepository:MainProductRepository) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainProductViewModel(mRepository) as T
    }
}