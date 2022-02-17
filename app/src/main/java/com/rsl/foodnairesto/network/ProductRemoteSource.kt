package com.rsl.foodnairesto.network

import com.rsl.foodnairesto.network.models.NetworkCartResponse
import com.rsl.foodnairesto.network.models.PostCart
import com.rsl.foodnairesto.utils.AppPreferences

class ProductRemoteSource(val productApi: ProductApi, val prefs: AppPreferences) {

    fun submitCart(cart: PostCart): Resource<NetworkCartResponse> {
        return when (val res = ApiResponse.create(productApi.submitCart(prefs.getRestaurantId(), cart))) {
            is ApiSuccessResponse -> Resource.success(res.data)
            is ApiSuccessEmptyResponse -> Resource.success(null)
            is ApiErrorResponse -> Resource.error(res.errorMessage, null)
            is ApiSuccessEmptyResponseWithHeaders -> Resource.success(null, res.headers)
        }
    }
}