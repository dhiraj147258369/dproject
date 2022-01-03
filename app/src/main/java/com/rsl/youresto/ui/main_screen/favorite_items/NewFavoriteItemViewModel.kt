package com.rsl.youresto.ui.main_screen.favorite_items

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rsl.youresto.data.favorite_items.network.NetworkSaveFavoriteResponse
import com.rsl.youresto.repositories.NewFavoriteItemRepository
import com.rsl.youresto.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewFavoriteItemViewModel(private val repo: NewFavoriteItemRepository): ViewModel() {

    suspend fun getCategories() = repo.getCategories()

    suspend fun getProducts(categoryId: String) = repo.getProducts(categoryId)

    suspend fun getFavorites(categoryId: String, locationId: String)
            = repo.getFavorites(categoryId, locationId)

    suspend fun getAllFavorite(locationId: String)
            = repo.getAllFavorite(locationId)

    private val _favoriteData = MutableLiveData<Event<NetworkSaveFavoriteResponse>>()
    val favoriteData: LiveData<Event<NetworkSaveFavoriteResponse>> get() = _favoriteData

    fun saveFavorites() {
        viewModelScope.launch {
            val resource = withContext(Dispatchers.IO) {
                repo.saveFavorites()
            }
            _favoriteData.value = Event(resource.data!!)
        }
    }
}