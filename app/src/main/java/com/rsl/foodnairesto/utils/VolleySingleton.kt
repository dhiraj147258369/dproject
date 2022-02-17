package com.rsl.foodnairesto.utils

import android.annotation.SuppressLint
import com.android.volley.Request
import com.android.volley.RequestQueue
import java.io.File

@SuppressLint("LogNotTimber")
class VolleySingleton {

    private var isAidl: Boolean = false

    fun isAidl(): Boolean {
        return isAidl
    }

    fun setAidl(aidl: Boolean) {
        isAidl = aidl
    }

//    override fun onCreate() {
//        super.onCreate()
//        instance = this
//
//        isAidl = true
//        AidlUtil.getInstance().connectPrinterService(this)
//    }

    private val requestQueue: RequestQueue? = null

    fun <T> addToRequestQueue(request: Request<T>) {
        request.tag = TAG
        requestQueue?.add(request)
    }

    companion object {
        private val TAG = VolleySingleton::class.java.simpleName
        @get:Synchronized
        var instance: VolleySingleton? = null
            private set
    }

    fun clearApplicationData() {
//        val cache = cacheDir
//        val appDir = File(cache.parent)
//        if (appDir.exists()) {
//            val children = appDir.list()
//            for (s in children) {
//                if (s != "lib" && s != "app_pics") {
//                    deleteDir(File(appDir, s))
//                    Log.i("TAG", "File /data/data/APP_PACKAGE/$s DELETED")
//                }
//            }
//        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            for (i in children.indices) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
            }
        }

        return dir!!.delete()
    }
}