package com.rsl.youresto.ui.main_screen.favorite_items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rsl.youresto.repositories.FavoriteItemsRepository

@Suppress("UNCHECKED_CAST")
class FavoriteItemsViewModelFactory constructor(private val mRepository: FavoriteItemsRepository): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return FavoriteItemsViewModel(mRepository) as T
    }
}