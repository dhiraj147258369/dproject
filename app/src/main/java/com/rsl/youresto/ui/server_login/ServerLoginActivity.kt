package com.rsl.youresto.ui.server_login

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.rsl.youresto.R
import com.rsl.youresto.utils.custom_views.CustomToast

class ServerLoginActivity : AppCompatActivity() {

    private var isTablet: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_server_login)

        isTablet = resources.getBoolean(R.bool.isTablet)
    }

    private var doubleBackToExitPressedOnce = false

    override fun onBackPressed() {
        getCurrentFragment()?.let {
            if (isTablet){
                showBackToast()
            } else {
                if (it is LocationFragment) showBackToast()
                else if (it is ServerLoginFragment) it.backPress()
            }
        }
    }

    private fun showBackToast(){
        if (doubleBackToExitPressedOnce) finish()
        this.doubleBackToExitPressedOnce = true
        CustomToast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler(Looper.getMainLooper()).postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    private fun getCurrentFragment(): Fragment? {
        return (supportFragmentManager.findFragmentById(R.id.server_login_host_fragment) as NavHostFragment).childFragmentManager.primaryNavigationFragment
    }
}
