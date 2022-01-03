package com.rsl.youresto.ui.main_screen.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rsl.youresto.repositories.CheckoutRepository

@Suppress("UNCHECKED_CAST")
class CheckoutViewModelFactory(val mRepository: CheckoutRepository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CheckoutViewModel(mRepository) as T
    }
}