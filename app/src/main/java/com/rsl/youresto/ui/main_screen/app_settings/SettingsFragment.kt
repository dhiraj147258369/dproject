package com.rsl.youresto.ui.main_screen.app_settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.App
import com.rsl.youresto.R
import com.rsl.youresto.databinding.FragmentSettingsBinding
import com.rsl.youresto.ui.main_screen.MainScreenActivity
import com.rsl.youresto.ui.main_screen.app_settings.printer_settings.event.SelectPrinterEvent
import com.rsl.youresto.ui.main_screen.app_settings.printer_settings.kitchen_printer.KitchenPrinterEditEvent
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@SuppressLint("LogNotTimber")
class SettingsFragment : Fragment() {

    private lateinit var mBinding: FragmentSettingsBinding
    private lateinit var mSharedPrefs: SharedPreferences
    private var mSelectedLocationType: Int? = null
    private var mTabNo = 0

    private var isTablet: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isTablet = resources.getBoolean(R.bool.isTablet)

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)
        mSelectedLocationType = mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE, 0)

//        mTabNo = SettingsFragmentArgs.fromBundle(requireArguments()).openTab

        if (!isTablet) {
            val mFragmentAdapter = SettingsPagerAdapter(childFragmentManager)
            mBinding.viewpagerSettings?.adapter = mFragmentAdapter
            mBinding.tabsSettings?.setupWithViewPager(mBinding.viewpagerSettings)
            mBinding.tabsSettings?.getTabAt(mTabNo)!!.select()
        }
    }

    @Subscribe
    fun openSelectPrinterFragment(mEvent: SelectPrinterEvent) {
        if(mEvent.mResult) {
            if (!App.isTablet){
                val action = SettingsFragmentDirections.actionSettingsFragmentToSelectPrinterFragment()
                Navigation.findNavController(requireActivity(), R.id.main_screen_host_fragment).navigate(action)
            } else {
                val dialog = PrinterSettingsDialog()
                dialog.isCancelable = true
                dialog.show(childFragmentManager, "PrinterSettingsDialog")
            }
        }
    }

    private var doubleBackToExitPressedOnce = false

    private fun pressAgainMethod() {
        val mDrawerVisibility = (activity as MainScreenActivity).checkNavigationDrawerVisibility()

        if (mDrawerVisibility)
            return

        if (doubleBackToExitPressedOnce) {
            (activity as MainScreenActivity).serverLogout()
            return
        }

        this.doubleBackToExitPressedOnce = true
        if (!requireActivity().isFinishing)
            CustomToast.makeText(requireActivity(), "Press again to exit", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    //TODO: HANDLE BACK PRESS
//    override fun handleOnBackPressed(): Boolean {
//        pressAgainMethod()
//        return true
//    }


    @Subscribe
    fun kitchenPrinterEdit(mEvent: KitchenPrinterEditEvent) {
        if (mEvent.mResult) {
            e(javaClass.simpleName, "kitchenPrinterEdit")

             val action = SettingsFragmentDirections.actionSettingsFragmentToSelectPrinterFragment()
            findNavController().navigate(action)
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
