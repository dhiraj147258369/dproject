package com.rsl.youresto.ui.main_screen.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rsl.youresto.repositories.CartRepository

@Suppress("UNCHECKED_CAST")
class CartViewModelFactory(private val mRepository: CartRepository) : ViewModelProvider.NewInstanceFactory(){

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CartViewModel(mRepository) as T
    }
}