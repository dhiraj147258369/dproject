package com.rsl.youresto.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.rsl.youresto.data.database_download.models.LocationModel
import com.rsl.youresto.data.database_download.models.ServerModel
import com.rsl.youresto.utils.AppConstants.LOCATION_SERVICE_TYPE
import com.rsl.youresto.utils.AppConstants.LOGGED_IN_SERVER_ID
import com.rsl.youresto.utils.AppConstants.LOGGED_IN_SERVER_NAME
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.RESTAURANT_ID
import com.rsl.youresto.utils.AppConstants.RESTAURANT_LOGO
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_NAME
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_NO

class AppPreferences(val context: Context) {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)

    fun getRestaurantId() = sharedPrefs.getString(RESTAURANT_ID, "") ?: ""

    fun getSelectedLocation() = sharedPrefs.getString(SELECTED_LOCATION_ID, "") ?: ""
    fun getSelectedLocationName() = sharedPrefs.getString(SELECTED_LOCATION_NAME, "") ?: ""

    fun getLocationServiceType() = sharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0)

    fun getRestaurantImage() = sharedPrefs.getString(RESTAURANT_LOGO, "") ?: ""

    fun setSelectedLocation(location: LocationModel) {
        sharedPrefs.edit().apply {
            putString(SELECTED_LOCATION_NAME, location.mLocationName)
            putString(SELECTED_LOCATION_ID, location.mLocationID)
            putInt(LOCATION_SERVICE_TYPE, location.mLocationType.toInt())
            apply()
        }
    }

    fun setServerDetails(server: ServerModel){
        sharedPrefs.edit().apply {
            putString(LOGGED_IN_SERVER_ID, server.mServerID)
            putString(LOGGED_IN_SERVER_NAME, server.mServerName)
            apply()
        }
    }

    fun setTable(tableId: String, tableNO: Int){
        sharedPrefs.edit().apply {
            putString(SELECTED_TABLE_ID, tableId)
            putInt(SELECTED_TABLE_NO, tableNO)
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