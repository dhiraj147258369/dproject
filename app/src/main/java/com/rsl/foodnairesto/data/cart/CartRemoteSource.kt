package com.rsl.foodnairesto.data.cart

import android.util.Log
import com.rsl.foodnairesto.data.checkout.model.NetworkCheckoutResponse
import com.rsl.foodnairesto.data.checkout.model.PostCheckout
import com.rsl.foodnairesto.network.*
import com.rsl.foodnairesto.network.models.NetworkCartResponse
import com.rsl.foodnairesto.network.models.PostCart
import com.rsl.foodnairesto.network.models.ReceiveCart
import com.rsl.foodnairesto.utils.AppPreferences

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

    fun syncCarts(): Resource<List<ReceiveCart>> {
        Log.e("syncCarts",prefs.getRestaurantId()+","+prefs.getSelectedLocation())
        return when (val res = ApiResponse.create(api.syncCarts(prefs.getRestaurantId(), prefs.getSelectedLocation()))) {
            is ApiSuccessResponse -> Resource.success(res.data)
            is ApiSuccessEmptyResponse -> Resource.success(null)
            is ApiErrorResponse -> Resource.error(res.errorMessage, null)
            is ApiSuccessEmptyResponseWithHeaders -> Resource.success(null, res.headers)
        }
    }

}