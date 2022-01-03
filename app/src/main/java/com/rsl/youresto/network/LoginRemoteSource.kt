package com.rsl.youresto.network

import com.google.gson.annotations.SerializedName
import com.rsl.youresto.data.database_download.models.*
import com.rsl.youresto.data.main_login.network.NetworkLogin
import com.rsl.youresto.network.models.PostLogin
import com.rsl.youresto.utils.AppPreferences

class LoginRemoteSource(private val loginApi: LoginApi, val prefs: AppPreferences) {

    fun authenticateUserWithEmail(login: PostLogin): Resource<NetworkLogin> {
        return when (val res = ApiResponse.create(loginApi.authenticateUserWithEmail(login))) {
            is ApiSuccessResponse -> Resource.success(res.data)
            is ApiSuccessEmptyResponse -> Resource.success(null)
            is ApiErrorResponse -> Resource.error(res.errorMessage, null)
            is ApiSuccessEmptyResponseWithHeaders -> Resource.success(null, res.headers)
        }
    }

    fun getData(): Resource<NetworkRestaurantData> {
        val apiCall = loginApi.getData(prefs.getRestaurantId())
        return when (val res = ApiResponse.create(apiCall)) {
            is ApiSuccessResponse -> Resource.success(res.data)
            is ApiSuccessEmptyResponse -> Resource.success(null)
            is ApiErrorResponse -> Resource.error(res.errorMessage, null)
            is ApiSuccessEmptyResponseWithHeaders -> Resource.success(null, res.headers)
        }
    }

    fun getFavorites(locationId: String): Resource<NetworkResponseFavorites> {
        val apiCall = loginApi.getFavorites(prefs.getRestaurantId(), locationId)
        return when (val res = ApiResponse.create(apiCall)) {
            is ApiSuccessResponse -> Resource.success(res.data)
            is ApiSuccessEmptyResponse -> Resource.success(null)
            is ApiErrorResponse -> Resource.error(res.errorMessage, null)
            is ApiSuccessEmptyResponseWithHeaders -> Resource.success(null, res.headers)
        }
    }

}

data class NetworkRestaurantData(
    var status: Boolean = false,
    var data: NetworkAllData = NetworkAllData()
)

data class NetworkAllData(
    var users: ArrayList<ServerModel> = ArrayList(),
    var ingredients: ArrayList<IngredientsModel> = ArrayList(),
    var locations: ArrayList<LocationModel> = ArrayList(),
    var tax: ArrayList<TaxModel> = ArrayList(),
    var paymentMethods: ArrayList<PaymentMethodModel> = ArrayList(),
    var kitchens: ArrayList<KitchenModel> = ArrayList(),
    var productGorups: ArrayList<ProductGroupModel> = ArrayList(),
    var productCategories: ArrayList<ProductCategoryModel> = ArrayList(),
    var products: ArrayList<ProductModel> = ArrayList(),
)

data class NetworkResponseFavorites(
    val status: Boolean = false,
    val data: List<NetworkFavorite> = ArrayList()
)

data class NetworkFavorite(
    val id: String = "",

    @SerializedName("product_id")
    val productId: String
)