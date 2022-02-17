package com.rsl.foodnairesto.data.order_history

import com.rsl.foodnairesto.network.*
import com.rsl.foodnairesto.network.models.NetworkReportModel
import com.rsl.foodnairesto.utils.AppPreferences

class OrderHistoryRemoteSource(val api: OrdersApi, val prefs: AppPreferences) {

    fun getCartReports(): Resource<NetworkReportModel> {
        val params = HashMap<String, String>()
        params["restaurant_id"] = prefs.getRestaurantId()
        return when (val res = ApiResponse.create(api.getCartReports(params))) {
            is ApiSuccessResponse -> Resource.success(res.data)
            is ApiSuccessEmptyResponse -> Resource.success(null)
            is ApiErrorResponse -> Resource.error(res.errorMessage, null)
            is ApiSuccessEmptyResponseWithHeaders -> Resource.success(null, res.headers)
        }
    }
}