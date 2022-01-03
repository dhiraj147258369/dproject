package com.rsl.youresto

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.rsl.youresto.ui.main_login.MainLoginActivity
import com.rsl.youresto.utils.services.CheckAppState

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val mainIntent = Intent(this, MainLoginActivity::class.java)
            mainIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(mainIntent)
            finish()
        }, 2000)

        startService(Intent(this@SplashActivity, CheckAppState::class.java))
    }
}
