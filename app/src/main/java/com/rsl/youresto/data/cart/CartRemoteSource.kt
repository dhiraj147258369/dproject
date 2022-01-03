package com.rsl.youresto.data.cart

import com.rsl.youresto.data.checkout.model.NetworkCheckoutResponse
import com.rsl.youresto.data.checkout.model.PostCheckout
import com.rsl.youresto.network.*
import com.rsl.youresto.network.models.NetworkCartResponse
import com.rsl.youresto.network.models.PostCart
import com.rsl.youresto.utils.AppPreferences

class CartRemoteSource(val api: CartApi, val prefs: AppPreferences) {

    fun submitCart(cart: PostCart): Resource<NetworkCartResponse> {
        return when (val res = ApiResponse.create(api.submitCart(prefs.getRestaurantId(), cart))) {
            is ApiSuccessResponse -> Resource.success(res.data)
            is ApiSuccessEmptyResponse -> Resource.success(null)
            is ApiErrorResponse -> Resource.error(res.errorMessage, null)
            is ApiSuccessEmptyResponseWithHeaders -> Resource.success(null, res.headers)
        }
    }

    fun checkoutOrder(checkout: PostCheckout): Resource<NetworkCheckoutResponse> {
        return when (val res = ApiResponse.create(api.checkoutOrder(checkout))) {
            is ApiSuccessResponse -> Resource.success(res.data)
            is ApiSuccessEmptyResponse -> Resource.success(null)
            is ApiErrorResponse -> Resource.error(res.errorMessage, null)
            is ApiSuccessEmptyResponseWithHeaders -> Resource.success(null, res.headers)
        }
    }

}