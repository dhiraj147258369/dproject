package com.rsl.youresto.data.favorite_items.network

import com.rsl.youresto.data.favorite_items.models.PostFavorites
import com.rsl.youresto.network.*
import com.rsl.youresto.utils.AppPreferences

class FavoriteDataSource(val api: FavoriteApi, val prefs: AppPreferences) {

    fun saveFavorites(favorites: PostFavorites): Resource<NetworkSaveFavoriteResponse> {
        return when (val res = ApiResponse.create(api.saveFavorites(favorites))) {
            is ApiSuccessResponse -> Resource.success(res.data)
            is ApiSuccessEmptyResponse -> Resource.success(null)
            is ApiErrorResponse -> Resource.error(res.errorMessage, null)
            is ApiSuccessEmptyResponseWithHeaders -> Resource.success(null, res.headers)
        }
    }
}