package com.rsl.foodnairesto.ui.main_screen

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.SplashActivity
import com.rsl.foodnairesto.databinding.ActivityMainScreenBinding
import com.rsl.foodnairesto.databinding.DialogResetTerminalBinding
import com.rsl.foodnairesto.ui.database_download.DatabaseDownloadActivity
import com.rsl.foodnairesto.ui.main_login.MainLoginViewModel
import com.rsl.foodnairesto.ui.main_screen.app_settings.event.ShowBluetoothDevicesEvent
import com.rsl.foodnairesto.ui.main_screen.cart.NewCartViewModel
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.event.MainProductStoreIDEvent
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.tables.NewTablesViewModel
import com.rsl.foodnairesto.ui.server_login.ServerLoginActivity
import com.rsl.foodnairesto.utils.AppConstants.FROM_MAIN_SCREEN
import com.rsl.foodnairesto.utils.AppConstants.INTENT_FROM
import com.rsl.foodnairesto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.foodnairesto.utils.AppConstants.SERVICE_QUICK_SERVICE
import com.rsl.foodnairesto.utils.AppPreferences
import com.rsl.foodnairesto.utils.custom_views.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("LogNotTimber")
class MainScreenActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainScreenBinding
    private val prefs: AppPreferences by inject()
    private val cartViewModel: NewCartViewModel by viewModel()
    private val tablesViewModel: NewTablesViewModel by viewModel()
    private val loginViewModel: MainLoginViewModel by viewModel()
    var doubleBackToExitPressedOnce = false

    private var isTablet: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_screen)

        isTablet = resources.getBoolean(R.bool.isTablet)

        initViews()
        syncCarts()

        if (prefs.getLocationServiceType() == SERVICE_QUICK_SERVICE){
            setQuickServiceTableId()
        }

//        stopService(Intent(this, ServerLoginDetailService::class.java))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        e(javaClass.simpleName, "onActivityResult")
        if(requestCode == 909) {
            if (resultCode == Activity.RESULT_OK) {
                e(javaClass.simpleName, "requestCode == 0")
                EventBus.getDefault().post(ShowBluetoothDevicesEvent(true))
            }
        } else {
//            val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
//            if (result != null) {
//                if (result.contents == null) {
//                    Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show()
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        EventBus.getDefault().post(QRCodeScannedEvent(false, ""))
//                    }, 200)
//                } else {
//                    e(javaClass.simpleName, "qr: " + result.contents)
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        EventBus.getDefault().post(QRCodeScannedEvent(true, result.contents))
//                    }, 200)
//                }
//            } else {
//                super.onActivityResult(requestCode, resultCode, data)
//            }
        }
    }

    private fun initViews() {
        mBinding.toolbar.imageViewLogOut.setOnClickListener {
//            if (mDrawerLock && mDrawerEventFrom == Checkou::class.java.simpleName)
//                CustomToast.makeText(this, "Cannot Logout From Here", Toast.LENGTH_SHORT).show()
//            else if (!mDrawerLock)
//                serverLogout()

            serverLogout()
        }

        //set drawer
        val mHeaderView = mBinding.navigationViewDrawer.getHeaderView(0)
        val mImageViewDrawerCancel = mHeaderView.findViewById<ImageView>(R.id.image_view_drawer_cancel)

        if(prefs.getLocationServiceType()== SERVICE_QUICK_SERVICE){
            mBinding.navigationViewDrawer.setCheckedItem(R.id.navigate_quick_service)
        }else {
            mBinding.navigationViewDrawer.setCheckedItem(R.id.navigate_tables_tab)
        }

         val navController = (supportFragmentManager.findFragmentById(R.id.main_screen_host_fragment) as NavHostFragment?)?.navController
            ?: return

        mBinding.navigationViewDrawer.setNavigationItemSelectedListener { item ->

            val id = item.itemId
            Log.d(javaClass.simpleName, "onNavigationItemSelected: $id")
            when (id) {
                R.id.navigate_tables_tab -> {
                    prefs.setSelectedLocationType(SERVICE_DINE_IN)
                    navController.navigate(if (isTablet) R.id.tablesTabFragment else R.id.tablesFragment)
                }

                R.id.navigate_quick_service -> {
                    prefs.setSelectedLocationType(SERVICE_QUICK_SERVICE)
                    prefs.clearOrderData()
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

        if (prefs.getLocationServiceType() == SERVICE_QUICK_SERVICE){
            navController.navigate(if (isTablet) R.id.quickServiceTabFragment else R.id.quickServiceFragment)
            mBinding.navigationViewDrawer.menu.findItem(R.id.navigate_tables_tab).isVisible = false
        } else {
            mBinding.navigationViewDrawer.menu.findItem(R.id.navigate_quick_service).isVisible = false
            mBinding.navigationViewDrawer.menu.findItem(R.id.navigate_fav_item_selection).isVisible = false
        }

        mImageViewDrawerCancel.setOnClickListener { mBinding.drawerLayout.closeDrawer(GravityCompat.END) }
        mBinding.toolbar.imageViewMenu.setOnClickListener {
//            if (mDrawerLock && mDrawerEventFrom == CheckoutFragment::class.java.simpleName)
//                CustomToast.makeText(this, "Cannot Use drawer on this screen", Toast.LENGTH_SHORT).show()
//            else if (!mDrawerLock)
//                mBinding.drawerLayout.openDrawer(GravityCompat.END)

            mBinding.drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    private fun syncCarts() {
        cartViewModel.syncCarts()
    }

    private fun setQuickServiceTableId() {
        lifecycleScope.launch {
            val tables = withContext(Dispatchers.IO){
                tablesViewModel.getTableWithLocation()
            }

            if (tables.isNotEmpty()){

                prefs.setTable(tables[0].mTableID, tables[0].mTableNo)
            }
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
            if (mPassword == prefs.getRestaurantPassword()) {

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
       prefs.clearSharedPreferences()
    }

    fun serverLogout() {
        val mUserLoginIntent = Intent(this@MainScreenActivity, ServerLoginActivity::class.java)
        mUserLoginIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        mUserLoginIntent.putExtra(INTENT_FROM, FROM_MAIN_SCREEN)
        startActivity(mUserLoginIntent)
        finish()
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
        prefs.clearTableData()
        super.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

        override fun onBackPressed() {







       if((mBinding.navigationViewDrawer.menu.findItem(R.id.navigate_quick_service).isChecked) || (mBinding.navigationViewDrawer.menu.findItem(R.id.navigate_tables_tab).isChecked)){
           if (doubleBackToExitPressedOnce) {
               super.onBackPressed()
               return
           }

           doubleBackToExitPressedOnce = true
           Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

           Handler(Looper.getMainLooper()).postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
       }else{
           super.onBackPressed()
           return
       }
    }
    fun checktablesfrag(){
        mBinding.navigationViewDrawer.setCheckedItem(R.id.navigate_tables_tab)

    }
    fun checkquickservicefrag(){
        mBinding.navigationViewDrawer.setCheckedItem(R.id.navigate_quick_service)

    }

}