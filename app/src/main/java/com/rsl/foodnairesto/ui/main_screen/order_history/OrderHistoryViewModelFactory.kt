package com.rsl.foodnairesto.ui.main_screen.order_history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rsl.foodnairesto.repositories.OrderHistoryRepository

@Suppress("UNCHECKED_CAST")
class OrderHistoryViewModelFactory constructor(private val mRepository: OrderHistoryRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return OrderHistoryViewModel(mRepository) as T
    }
}