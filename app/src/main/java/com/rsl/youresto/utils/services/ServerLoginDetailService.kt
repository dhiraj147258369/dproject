package com.rsl.youresto.utils.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import com.rsl.youresto.data.AppDatabase
import com.rsl.youresto.data.server_login.ServerLoginDataSource
import com.rsl.youresto.repositories.ServerLoginRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*

class ServerLoginDetailService : Service() {

    val NOTIFY_INTERVAL = (10 * 1000).toLong() // 10 seconds

    private var mRepository: ServerLoginRepository? = null

    // run on another Thread to avoid crash
    private val mHandler = Handler()

    private var mTimer: Timer? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        val mDatabase = AppDatabase.getInstance(this)

        val mDataSource = ServerLoginDataSource.getInstance(this)

        mRepository = ServerLoginRepository.getInstance(mDatabase!!.serverLoginDao(), mDataSource)

        // cancel if already existed
        if (mTimer != null) {
            mTimer!!.cancel()
        } else {
            // recreate new
            mTimer = Timer()
        }
        // schedule task
        mTimer!!.scheduleAtFixedRate(TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL)
    }

    internal inner class TimeDisplayTimerTask : TimerTask() {

        @ExperimentalCoroutinesApi
        override fun run() {
            // run on another thread
            mHandler.post { mRepository!!.getAndStoreAllLoginDetails() }
        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        stopSelf()

    }

    override fun onDestroy() {
        mTimer!!.cancel()
        super.onDestroy()

    }
}