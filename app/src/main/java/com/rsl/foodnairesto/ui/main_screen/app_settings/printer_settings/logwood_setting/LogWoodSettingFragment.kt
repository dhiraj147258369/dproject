package com.rsl.foodnairesto.ui.main_screen.app_settings.printer_settings.logwood_setting


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.data.database_download.models.KitchenModel
import com.rsl.foodnairesto.databinding.FragmentLogwoodSettingBinding
import com.rsl.foodnairesto.ui.main_screen.app_settings.AppSettingsViewModel
import com.rsl.foodnairesto.utils.AppConstants
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_KITCHEN_PRINTER_ID
import com.rsl.foodnairesto.utils.InjectorUtils
import com.rsl.foodnairesto.utils.custom_views.CustomToast

@SuppressLint("LogNotTimber")
class LogWoodSettingFragment : Fragment() {

    private lateinit var mBinding: FragmentLogwoodSettingBinding
    private lateinit var mViewModel: AppSettingsViewModel
    private lateinit var mSharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_logwood_setting, container, false)
        val mView: View = mBinding.root

        val factory = InjectorUtils.provideAppSettingsViewModelFactory(requireActivity())
        mViewModel = ViewModelProviders.of(this, factory).get(AppSettingsViewModel::class.java)

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        skip()
        setLogWoodIP()
        checkExistingLogwoodId()

        return mView
    }

    private fun skip() {
        mBinding.buttonSkipServerIpAddress.setOnClickListener {
            val action = LogWoodSettingFragmentDirections.actionLogwoodSettingFragmentToSettingsFragment(1)
            findNavController().navigate(action)
        }
    }

    private fun checkExistingLogwoodId() {
        if(mSharedPrefs.getString(SELECTED_KITCHEN_PRINTER_ID,"")!! != "" ) {
            val mKitchenData: LiveData<KitchenModel> = mViewModel.getKitchenPrinter(mSharedPrefs.getString(SELECTED_KITCHEN_PRINTER_ID, "")!!)
            val mKitchenObserver: Observer<KitchenModel> = Observer {
                if (it != null) {
                    if(it.mLogWoodServerIP != "") {
                        val mLogwoodIP = it.mLogWoodServerIP

                        val mIPs = mLogwoodIP.split(".")

                        mBinding.editTextIpAddress1.setText(mIPs[0])
                        mBinding.editTextIpAddress2.setText(mIPs[1])
                        mBinding.editTextIpAddress3.setText(mIPs[2])
                        mBinding.editTextIpAddress4.setText(mIPs[3])
                        mBinding.editTextPortNo.setText(it.mLogWoodServerPort)
                    }
                    mKitchenData.removeObservers(this)
                }
            }
            mKitchenData.observe(viewLifecycleOwner, mKitchenObserver)
        }
    }

    private fun setLogWoodIP() {
        mBinding.buttonDoneServerIpAddress.setOnClickListener {
            if(validateIP()) {
                updateLogWoodIP()
            }
        }
    }

    private var mIPAddress: String? = null
    private var mPortNO: String? = null

    private fun validateIP(): Boolean {
        val mEditTextIP1: String = mBinding.editTextIpAddress1.text.toString()
        val mEditTextIP2: String = mBinding.editTextIpAddress2.text.toString()
        val mEditTextIP3: String = mBinding.editTextIpAddress3.text.toString()
        val mEditTextIP4: String = mBinding.editTextIpAddress4.text.toString()

        val mEditTextPortNO: String = mBinding.editTextPortNo.text.toString()

        if (mEditTextIP1.isEmpty() || mEditTextIP2.isEmpty() || mEditTextIP3.isEmpty() || mEditTextIP4.isEmpty() || mEditTextPortNO.isEmpty()) {
            CustomToast.makeText(requireActivity(), "Please enter valid IP", Toast.LENGTH_SHORT).show()
            return false
        }

        mIPAddress = "$mEditTextIP1.$mEditTextIP2.$mEditTextIP3.$mEditTextIP4"
        mPortNO = mEditTextPortNO
        return true
    }


    private fun updateLogWoodIP() {
        e(javaClass.simpleName,"IP: $mIPAddress PORT: $mPortNO")
        e(javaClass.simpleName,"IP: $mIPAddress")
        val mUpdateLogWoodData: LiveData<Int> = mViewModel.updateLogWoodIP(mIPAddress!!, mPortNO!!, mSharedPrefs.getString(SELECTED_KITCHEN_PRINTER_ID,"")!!)
        val mUpdateLogWoodObserver: Observer<Int> = Observer {
            if(it != null && it > 0) {
                CustomToast.makeText(requireActivity(), "Logwood selected", Toast.LENGTH_SHORT).show()
                val action = LogWoodSettingFragmentDirections.actionLogwoodSettingFragmentToSettingsFragment(1)
                findNavController().navigate(action)
                mUpdateLogWoodData.removeObservers(this)
            }
        }
        mUpdateLogWoodData.observe(viewLifecycleOwner,mUpdateLogWoodObserver)
    }

    //TODO: HANDLE BACK PRESS
//    override fun handleOnBackPressed(): Boolean {
//        val mDrawerVisibility = (activity as MainScreenActivity).checkNavigationDrawerVisibility()
//
//        if (!mDrawerVisibility){
//            val action = LogWoodSettingFragmentDirections.actionLogwoodSettingFragmentToSelectPrinterFragment()
//            findNavController().navigate(action)
//        }
//
//        return true
//    }
//
//    override fun onResume() {
//        requireActivity().onBackPressedDispatcher.addCallback(this)
//        super.onResume()
//    }

}
