package com.rsl.youresto.ui.database_download

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.rsl.youresto.R
import com.rsl.youresto.databinding.ActivityDatabaseDownloadBinding
import com.rsl.youresto.ui.main_login.MainLoginViewModel
import com.rsl.youresto.ui.server_login.ServerLoginActivity
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppPreferences
import com.rsl.youresto.utils.custom_views.CustomToast
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("LogNotTimber")
class DatabaseDownloadActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityDatabaseDownloadBinding

    private val loginViewModel: MainLoginViewModel by viewModel()
    private val prefs: AppPreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_database_download)

        mBinding.buttonRetry.setOnClickListener {
            resetApp()
            mBinding.buttonRetry.isEnabled = false
        }

        getData()
    }

    private fun getData() {
        loginViewModel.downloadData()

        loginViewModel.data.observe(this) { event ->
            event?.getContentIfNotHandled()?.let {
                if (it.status){
                    val mLoggedInIntent = Intent(this, ServerLoginActivity::class.java)
                    mLoggedInIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    mLoggedInIntent.putExtra(
                        AppConstants.INTENT_FROM,
                        AppConstants.FROM_RESTAURANT_LOGIN
                    )
                    startActivity(mLoggedInIntent)
                    finish()
                } else {
                    CustomToast.createToast(this, "Something went wrong. Please try again!", Toast.LENGTH_SHORT).show()
                    showRetry()
                }
            }
        }
    }

    private fun showRetry() {
        mBinding.textViewNoInternetConnection.visibility = VISIBLE
        mBinding.buttonRetry.visibility = VISIBLE
        mBinding.textViewDownloadLabel.visibility = GONE
        mBinding.progressBarDatabaseDownload.visibility = GONE
    }

    private fun resetApp(){

        prefs.clearSharedPreferences()

        getData()
        mBinding.textViewNoInternetConnection.visibility = GONE
        mBinding.buttonRetry.visibility = GONE
        mBinding.textViewDownloadLabel.visibility = VISIBLE
        mBinding.progressBarDatabaseDownload.visibility = VISIBLE

    }

}
