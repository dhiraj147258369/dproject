package com.rsl.youresto.ui.main_screen

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.google.zxing.integration.android.IntentIntegrator
import com.rsl.youresto.R
import com.rsl.youresto.SplashActivity
import com.rsl.youresto.databinding.ActivityMainScreenBinding
import com.rsl.youresto.databinding.DialogResetTerminalBinding
import com.rsl.youresto.ui.database_download.DatabaseDownloadActivity
import com.rsl.youresto.ui.database_download.DatabaseDownloadViewModel
import com.rsl.youresto.ui.database_download.DatabaseDownloadViewModelFactory
import com.rsl.youresto.ui.main_login.MainLoginViewModel
import com.rsl.youresto.ui.main_screen.app_settings.event.ShowBluetoothDevicesEvent
import com.rsl.youresto.ui.main_screen.checkout.CheckoutFragment
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModelFactory
import com.rsl.youresto.ui.main_screen.checkout.events.DrawerEvent
import com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.QRCodeScannedEvent
import com.rsl.youresto.ui.main_screen.checkout.seats_checkout.SeatsCheckoutFragment
import com.rsl.youresto.ui.main_screen.main_product_flow.event.MainProductStoreIDEvent
import com.rsl.youresto.ui.server_login.ServerLoginActivity
import com.rsl.youresto.ui.server_login.ServerLoginViewModel
import com.rsl.youresto.utils.AppConstants.FROM_MAIN_SCREEN
import com.rsl.youresto.utils.AppConstants.GROUP_NAME
import com.rsl.youresto.utils.AppConstants.INTENT_FROM
import com.rsl.youresto.utils.AppConstants.LOCATION_SERVICE_TYPE
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_CART_ID
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_CART_NO
import com.rsl.youresto.utils.AppConstants.RESTAURANT_PASSWORD
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_NO
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppConstants.SERVICE_QUICK_SERVICE
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.custom_views.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("LogNotTimber")
class MainScreenActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainScreenBinding
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mViewModel: ServerLoginViewModel
    private lateinit var mCheckoutViewModel: CheckoutViewModel
    private lateinit var mDatabaseDownloadViewModel: DatabaseDownloadViewModel

    private val loginViewModel: MainLoginViewModel by viewModel()

    private var isTablet: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_screen)

        isTablet = resources.getBoolean(R.bool.isTablet)

        mSharedPrefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)

        val mFactory = InjectorUtils.provideServerLoginViewModelFactory(this)
        mViewModel = ViewModelProviders.of(this, mFactory).get(ServerLoginViewModel::class.java)

        val checkoutFactory: CheckoutViewModelFactory = InjectorUtils.provideCheckoutViewModelFactory(this)
        mCheckoutViewModel = ViewModelProviders.of(this, checkoutFactory).get(CheckoutViewModel::class.java)

        val databaseDownloadFactory: DatabaseDownloadViewModelFactory =
            InjectorUtils.provideDatabaseDownloadViewModelFactory(this)
        mDatabaseDownloadViewModel =
            ViewModelProviders.of(this, databaseDownloadFactory).get(DatabaseDownloadViewModel::class.java)

        initViews()
//        stopService(Intent(this, ServerLoginDetailService::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        e(javaClass.simpleName, "onActivityResult")
        if(requestCode == 909) {
            if (resultCode == Activity.RESULT_OK) {
                e(javaClass.simpleName, "requestCode == 0")
                EventBus.getDefault().post(ShowBluetoothDevicesEvent(true))
            }
        } else {
            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                if (result.contents == null) {
                    Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        EventBus.getDefault().post(QRCodeScannedEvent(false, ""))
                    }, 200)
                } else {
                    e(javaClass.simpleName, "qr: " + result.contents)
                    Handler(Looper.getMainLooper()).postDelayed({
                        EventBus.getDefault().post(QRCodeScannedEvent(true, result.contents))
                    }, 200)
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    private fun initViews() {
        mBinding.toolbar.imageViewLogOut.setOnClickListener {
            if (mDrawerLock && mDrawerEventFrom == CheckoutFragment::class.java.simpleName ||
                mDrawerLock && mDrawerEventFrom == SeatsCheckoutFragment::class.java.simpleName)
                CustomToast.makeText(this, "Cannot Logout From Here", Toast.LENGTH_SHORT).show()
            else if (!mDrawerLock)
                serverLogout()
        }

        //set drawer
        val mHeaderView = mBinding.navigationViewDrawer.getHeaderView(0)
        val mImageViewDrawerCancel = mHeaderView.findViewById<ImageView>(R.id.image_view_drawer_cancel)

        mBinding.navigationViewDrawer.setCheckedItem(R.id.navigate_tables_tab)

        val navController = (supportFragmentManager.findFragmentById(R.id.main_screen_host_fragment) as NavHostFragment?)?.navController
            ?: return

        mBinding.navigationViewDrawer.setNavigationItemSelectedListener { item ->

            val id = item.itemId
            Log.d(javaClass.simpleName, "onNavigationItemSelected: $id")
            when (id) {
                R.id.navigate_tables_tab -> {
                    mSharedPrefs.edit().putInt(LOCATION_SERVICE_TYPE, SERVICE_DINE_IN).apply()
                    mSharedPrefs.edit().putString(GROUP_NAME, "").apply()
                    navController.navigate(if (isTablet) R.id.tablesTabFragment else R.id.tablesFragment)
                }

                R.id.navigate_quick_service -> {
                    mSharedPrefs.edit().putInt(LOCATION_SERVICE_TYPE, SERVICE_QUICK_SERVICE).apply()
                    mSharedPrefs.edit().putString(GROUP_NAME, "Q").apply()
                    mSharedPrefs.edit().putString(QUICK_SERVICE_CART_ID, "").apply()
                    mSharedPrefs.edit().putString(QUICK_SERVICE_CART_NO, "").apply()
                    navController.navigate(if (isTablet) R.id.quickServiceTabFragment else R.id.quickServiceFragment)
                }

                R.id.navigate_pending_orders -> {
                    navController.navigate(if (isTablet) R.id.pendingOrderFragment2 else R.id.pendingOrderFragment)
                }

                R.id.navigate_fav_item_selection -> {
                    navController.navigate(if (isTablet) R.id.favoriteItemsFragment2 else R.id.favoriteItemsFragment)
                }

                R.id.navigate_settings -> {
                    navController.navigate(if (isTablet) R.id.settingsFragment2 else R.id.settingsFragment)
                }

//                R.id.navigate_end_shift -> endShift()

                R.id.navigate_order_history -> {

                    val mBundle = Bundle()
                    mBundle.putString(INTENT_FROM, javaClass.simpleName)

//                    Navigation.findNavController(this, R.id.main_screen_host_fragment).navigate(
//                        R.id.orderHistoryListFragment, mBundle
//                    )

                    navController.navigate(if (isTablet) R.id.orderHistoryFragment else R.id.orderHistoryListFragment, mBundle)
                }

                R.id.navigate_update_terminal -> {
                    updateTerminal()
                }

                R.id.navigate_logout -> {
                    resetTerminal()
                }
            }
            mBinding.drawerLayout.closeDrawer(GravityCompat.END)
            true
        }

        if (mSharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0) == SERVICE_QUICK_SERVICE){
            navController.navigate(if (isTablet) R.id.quickServiceTabFragment else R.id.quickServiceFragment)
            mBinding.navigationViewDrawer.menu.findItem(R.id.navigate_tables_tab).isVisible = false
        } else {
            mBinding.navigationViewDrawer.menu.findItem(R.id.navigate_quick_service).isVisible = false
            mBinding.navigationViewDrawer.menu.findItem(R.id.navigate_fav_item_selection).isVisible = false
        }

        mImageViewDrawerCancel.setOnClickListener { mBinding.drawerLayout.closeDrawer(GravityCompat.END) }
        mBinding.toolbar.imageViewMenu.setOnClickListener {
            if (mDrawerLock && mDrawerEventFrom == CheckoutFragment::class.java.simpleName ||
                mDrawerLock && mDrawerEventFrom == SeatsCheckoutFragment::class.java.simpleName)
                CustomToast.makeText(this, "Cannot Use drawer on this screen", Toast.LENGTH_SHORT).show()
            else if (!mDrawerLock)
                mBinding.drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    private fun resetTerminal() {
        val mBuilder = AlertDialog.Builder(this)
        mBuilder.setTitle("Reset Terminal?")
        mBuilder.setMessage("Do you really want to reset the terminal? This will delete your local database.")
        mBuilder.setPositiveButton("Yes, Reset") { dialog, _ ->
            confirmPassword(0)
            dialog.cancel()
        }
        mBuilder.setNegativeButton("Nope") { dialog, _ -> dialog.cancel() }
        mBuilder.show()
    }

    private fun updateTerminal() {
        val mBuilder = AlertDialog.Builder(this)
        mBuilder.setTitle("Update Terminal?")
        mBuilder.setMessage("Do you want to update the terminal?")
        mBuilder.setPositiveButton("Yes, Update") { dialog, _ ->
            confirmPassword(1)
            dialog.cancel()
        }
        mBuilder.setNegativeButton("Nope") { dialog, _ -> dialog.cancel() }
        mBuilder.show()
    }

    private lateinit var mResetTerminalBinding: DialogResetTerminalBinding

    @SuppressLint("SetTextI18n")
    private fun confirmPassword(resetOrUpdate: Int) {
        val mConfirmDialog = Dialog(this, R.style.DialogTheme)
        mResetTerminalBinding =
            DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.dialog_reset_terminal, null, false)
        mConfirmDialog.setContentView(mResetTerminalBinding.root)

        if (resetOrUpdate == 1) {
            mResetTerminalBinding.textViewResetLabel.text = "Update Terminal"
            mResetTerminalBinding.buttonReset.text = "Update"
            mResetTerminalBinding.textViewResetting.text = "Updating Terminal"
        }

        mResetTerminalBinding.buttonReset.setOnClickListener {
            val mPassword = mResetTerminalBinding.editTextPassword.text.toString().trim()
            if (mPassword == mSharedPrefs.getString(RESTAURANT_PASSWORD, "")) {

                mResetTerminalBinding.constraintLayoutReset.visibility = View.VISIBLE
                mResetTerminalBinding.buttonReset.visibility = View.GONE

                if (resetOrUpdate == 0) {
                    resetSharedPreferences()
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO){
                            loginViewModel.deleteAll()
                        }

                        val mainIntent = Intent(this@MainScreenActivity, SplashActivity::class.java)
                        startActivity(mainIntent)
                        finish()
                    }
                } else {

                    lifecycleScope.launch {
                        withContext(Dispatchers.IO){
                            loginViewModel.deleteAll()
                        }

                        val mainIntent = Intent(this@MainScreenActivity, DatabaseDownloadActivity::class.java)
                        startActivity(mainIntent)
                        finish()
                    }
                }

            } else {
                CustomToast.makeText(this, "Please enter correct password", Toast.LENGTH_SHORT).show()
            }
        }

        mConfirmDialog.show()
        //mConfirmDialog.window!!.setLayout(800, 600)
        mConfirmDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun resetSharedPreferences() {
        val editor = mSharedPrefs.edit()
        editor.clear()
        editor.apply()
    }

    fun serverLogout() {
//        submitLogoutDetails()
        val mUserLoginIntent = Intent(this@MainScreenActivity, ServerLoginActivity::class.java)
        mUserLoginIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        mUserLoginIntent.putExtra(INTENT_FROM, FROM_MAIN_SCREEN)
        startActivity(mUserLoginIntent)
        finish()
    }

    private var mDrawerLock = false
    private var mDrawerEventFrom = ""

    @Subscribe
    fun drawerMode(mEvent: DrawerEvent) {
//        mDrawerLock = if (mEvent.mLock) {
//            mBinding.drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED)
//            true
//        } else {
//            mBinding.drawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED)
//            false
//        }
//
//        mDrawerEventFrom = mEvent.mEventFrom
    }

    //Product group related
    var mGroupID: String = ""

    @Subscribe
    fun onGroupClicked(mEvent: MainProductStoreIDEvent) {
        mGroupID = mEvent.mGroupID
    }

    fun checkNavigationDrawerVisibility(): Boolean {
        return if (mBinding.drawerLayout.isDrawerOpen(GravityCompat.END)) {
            mBinding.drawerLayout.closeDrawer(GravityCompat.END)
            true
        } else {
            false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSharedPrefs.edit().apply {
            putString(SELECTED_TABLE_ID, "")
            putInt(SELECTED_TABLE_NO, 0)
            apply()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }
}
