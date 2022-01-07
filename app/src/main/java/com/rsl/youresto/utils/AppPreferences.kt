package com.rsl.youresto.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.rsl.youresto.data.database_download.models.LocationModel
import com.rsl.youresto.data.database_download.models.ServerModel
import com.rsl.youresto.utils.AppConstants.ENABLE_KITCHEN_PRINT
import com.rsl.youresto.utils.AppConstants.LOCATION_SERVICE_TYPE
import com.rsl.youresto.utils.AppConstants.LOGGED_IN_SERVER_ID
import com.rsl.youresto.utils.AppConstants.LOGGED_IN_SERVER_NAME
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_CART_ID
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_CART_NO
import com.rsl.youresto.utils.AppConstants.RESTAURANT_ID
import com.rsl.youresto.utils.AppConstants.RESTAURANT_LOGO
import com.rsl.youresto.utils.AppConstants.RESTAURANT_PASSWORD
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_NAME
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_NO

class AppPreferences(val context: Context) {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)

    //------------------- RESTAURANT RELATED ----------------------------//

    fun getRestaurantId() = sharedPrefs.getString(RESTAURANT_ID, "") ?: ""
    fun getRestaurantPassword() = sharedPrefs.getString(RESTAURANT_PASSWORD, "") ?: ""

    fun getRestaurantImage() = sharedPrefs.getString(RESTAURANT_LOGO, "") ?: ""

    //------------------- LOCATION RELATED ----------------------------//

    fun getSelectedLocation() = sharedPrefs.getString(SELECTED_LOCATION_ID, "") ?: ""
    fun getSelectedLocationName() = sharedPrefs.getString(SELECTED_LOCATION_NAME, "") ?: ""
    fun getLocationServiceType() = sharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0)

    fun setSelectedLocation(location: LocationModel) {
        sharedPrefs.edit().apply {
            putString(SELECTED_LOCATION_NAME, location.mLocationName)
            putString(SELECTED_LOCATION_ID, location.mLocationID)
            putInt(LOCATION_SERVICE_TYPE, location.mLocationType.toInt())
            apply()
        }
    }

    fun setSelectedLocationType(locationType: Int) {
        sharedPrefs.edit().apply {
            putInt(LOCATION_SERVICE_TYPE, locationType)
            apply()
        }
    }

    //------------------- SERVER RELATED ----------------------------//

    fun setServerDetails(server: ServerModel){
        sharedPrefs.edit().apply {
            putString(LOGGED_IN_SERVER_ID, server.mServerID)
            putString(LOGGED_IN_SERVER_NAME, server.mServerName)
            apply()
        }
    }

    //------------------- TABLE RELATED ----------------------------//

    fun setTable(tableId: String, tableNO: Int){
        sharedPrefs.edit().apply {
            putString(SELECTED_TABLE_ID, tableId)
            putInt(SELECTED_TABLE_NO, tableNO)
            apply()
        }
    }
    fun getSelectedTableId() = sharedPrefs.getString(SELECTED_TABLE_ID, "") ?: ""
    fun getSelectedTableNo() = sharedPrefs.getInt(SELECTED_TABLE_NO, 0)


    //------------------- QUICK SERVICE RELATED ----------------------------//
    fun selectedQuickServiceCartId() = sharedPrefs.getString(QUICK_SERVICE_CART_ID, "") ?: ""
    fun setQuickServiceCartId(cartId: String){
        sharedPrefs.edit().apply {
            putString(QUICK_SERVICE_CART_ID, cartId)
            apply()
        }
    }






    fun getKitchenPrintEnableFlag() = sharedPrefs.getBoolean(ENABLE_KITCHEN_PRINT, false)




    fun clearTableData() {
        sharedPrefs.edit().apply {
            putString(SELECTED_TABLE_ID, "")
            putInt(SELECTED_TABLE_NO, 0)
            apply()
        }
    }

    fun clearOrderData() {
        sharedPrefs.edit().apply {
            putString(QUICK_SERVICE_CART_ID, "")
            putString(QUICK_SERVICE_CART_NO, "")
            apply()
        }
    }

    fun clearSharedPreferences(){
        sharedPrefs.edit().apply {
            clear()
            apply()
        }
    }
}