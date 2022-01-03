package com.rsl.youresto.utils

import android.util.Base64
import com.bumptech.glide.load.model.LazyHeaderFactory

class BasicAuthorization(private val username: String?, private val password: String?) : LazyHeaderFactory {

    override fun buildHeader(): String {
        return "Basic " + Base64.encodeToString("$username:$password".toByteArray(), Base64.NO_WRAP)
    }
}