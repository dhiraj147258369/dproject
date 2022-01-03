package com.rsl.youresto.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.rsl.youresto.utils.AppConstants.LOCATION_SERVICE_TYPE
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.RESTAURANT_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_NO

class AppPreferences(val context: Context) {

    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)

    fun getRestaurantId() = sharedPrefs.getString(RESTAURANT_ID, "") ?: ""

    fun getSelectedLocation() = sharedPrefs.getString(SELECTED_LOCATION_ID, "") ?: ""

    fun getLocationServiceType() = sharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0)

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