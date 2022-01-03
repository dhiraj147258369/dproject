package com.rsl.youresto.repositories

import com.rsl.youresto.data.favorite_items.FavoriteItemsDao
import com.rsl.youresto.data.favorite_items.models.PostFavorites
import com.rsl.youresto.data.favorite_items.network.FavoriteDataSource
import com.rsl.youresto.data.favorite_items.network.NetworkSaveFavoriteResponse
import com.rsl.youresto.network.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewFavoriteItemRepository(val dao: FavoriteItemsDao, val remoteSource: FavoriteDataSource) {

    suspend fun getCategories() = withContext(Dispatchers.IO) {dao.getCategories()}

    suspend fun getProducts(categoryId: String) = withContext(Dispatchers.IO) {dao.getProducts(categoryId)}

    suspend fun getFavorites(categoryId: String, locationId: String)
        = withContext(Dispatchers.IO) {dao.getFavorites(categoryId, locationId)}

    suspend fun getAllFavorite(locationId: String)
        = withContext(Dispatchers.IO) {dao.getAllFavorite(locationId)}



    suspend fun saveFavorites(): Resource<NetworkSaveFavoriteResponse> {
        val locationId = remoteSource.prefs.getSelectedLocation()

        val favorites = withContext(Dispatchers.IO) {dao.getAllFavorite(locationId)}

        val favoritesIds = ArrayList<String>()

        favorites.map {
            it.mProductArrayList.map { product -> favoritesIds.add(product.mProductID) }
        }

        val postFavorites = PostFavorites(
            remoteSource.prefs.getRestaurantId(),
            locationId,
            favoritesIds
        )

        return withContext(Dispatchers.IO) {
            remoteSource.saveFavorites(postFavorites)
        }
    }
}