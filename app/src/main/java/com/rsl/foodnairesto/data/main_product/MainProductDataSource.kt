package com.rsl.foodnairesto.data.main_product

import android.annotation.SuppressLint
import android.content.Context

class MainProductDataSource constructor(val context: Context) {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sInstance: MainProductDataSource? = null

        fun getInstance(context:Context) : MainProductDataSource? {

            val tempInstance = sInstance
            if (tempInstance != null)
                return tempInstance

            sInstance ?: synchronized(this){
                sInstance ?: MainProductDataSource(context)
            }.also { sInstance = it }

            return sInstance
        }
    }
}