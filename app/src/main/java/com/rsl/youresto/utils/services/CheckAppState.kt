package com.rsl.youresto.utils.services

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.rsl.youresto.data.AppDatabase
import com.rsl.youresto.data.server_login.ServerLoginDataSource
import com.rsl.youresto.repositories.ServerLoginRepository
import com.rsl.youresto.utils.AppConstants.API_LOG_OUT
import com.rsl.youresto.utils.AppConstants.LOGGED_IN_SERVER_ID
import com.rsl.youresto.utils.AppConstants.LOGGED_IN_SERVER_NAME
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
@SuppressLint("LogNotTimber")
class CheckAppState : Service(){

    private var mRepository: ServerLoginRepository? = null
    private var mServerID: String? = null
    private var mServerName: String? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onCreate() {
        super.onCreate()

        val mSharedPrefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)

        mServerID = mSharedPrefs.getString(LOGGED_IN_SERVER_ID, "")
        mServerName = mSharedPrefs.getString(LOGGED_IN_SERVER_NAME, "")

        val mCheckIfAppIsInForeground = appInForeground(this)

        Log.e(javaClass.simpleName, "onCreate: $mCheckIfAppIsInForeground")

        if (!mCheckIfAppIsInForeground) {
            val mDatabase = AppDatabase.getInstance(this)

            val mDataSource = ServerLoginDataSource.getInstance(this)

            mRepository =
                ServerLoginRepository.getInstance(mDatabase!!.serverLoginDao(), mDataSource)

            autoLogoutServer()
        }
    }


    private fun autoLogoutServer() {

        mRepository!!.getServerLoginDetails(mServerID!!).observeForever { serverLoginModel ->
            if (serverLoginModel != null) {
                Log.e(javaClass.simpleName, "checkLogin: " + serverLoginModel.mLogInFlag)
                if (serverLoginModel.mLogInFlag != API_LOG_OUT) {
                    mRepository!!.submitLoginDetails(mServerID!!, mServerName!!, API_LOG_OUT)
                }
            }
        }


    }

    private fun appInForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningAppProcesses: List<ActivityManager.RunningAppProcessInfo>?
        runningAppProcesses = activityManager.runningAppProcesses
        if (runningAppProcesses == null) {
            return false
        }

        for (runningAppProcess in runningAppProcesses) {
            if (runningAppProcess.processName == context.packageName && runningAppProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true
            }
        }
        return false
    }
}