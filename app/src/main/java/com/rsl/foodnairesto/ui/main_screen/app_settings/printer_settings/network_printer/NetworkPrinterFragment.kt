package com.rsl.foodnairesto.ui.main_screen.app_settings.printer_settings.network_printer


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.rsl.foodnairesto.App

import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.data.database_download.models.KitchenModel
import com.rsl.foodnairesto.databinding.FragmentNetworkPrinterBinding
import com.rsl.foodnairesto.ui.main_screen.app_settings.AppSettingsViewModel
import com.rsl.foodnairesto.utils.AppConstants
import com.rsl.foodnairesto.utils.AppConstants.BILL_PRINTER
import com.rsl.foodnairesto.utils.AppConstants.BILL_PRINTER_OR_KITCHEN_PRINTER
import com.rsl.foodnairesto.utils.AppConstants.KITCHEN_PRINTER
import com.rsl.foodnairesto.utils.AppConstants.NETWORK_PRINTER
import com.rsl.foodnairesto.utils.AppConstants.NO_TYPE
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_BILL_PRINTER_NAME
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_BILL_PRINTER_NETWORK_IP
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_BILL_PRINTER_NETWORK_PORT
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_BILL_PRINTER_TYPE
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_KITCHEN_PRINTER_ID
import com.rsl.foodnairesto.utils.InjectorUtils
import com.rsl.foodnairesto.utils.custom_views.CustomToast
import java.util.*

/**
 * A simple [Fragment] subclass.
 *
 */
class NetworkPrinterFragment : Fragment() {

    private lateinit var mBinding: FragmentNetworkPrinterBinding
    private lateinit var mViewModel: AppSettingsViewModel
    private lateinit var mSharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_network_printer, container, false)
        val mView: View = mBinding.root

        val factory = InjectorUtils.provideAppSettingsViewModelFactory(requireActivity())
        mViewModel = ViewModelProviders.of(this, factory).get(AppSettingsViewModel::class.java)

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        checkIfBluetoothPrinterAlreadySelected()
        setNetworkPrinter()

        return mView
    }

    private fun checkIfBluetoothPrinterAlreadySelected() {
        if (mSharedPrefs.getString(BILL_PRINTER_OR_KITCHEN_PRINTER, "")!! == KITCHEN_PRINTER) {
            val mKitchenData: LiveData<KitchenModel> =
                mViewModel.getKitchenPrinter(mSharedPrefs.getString(SELECTED_KITCHEN_PRINTER_ID, "")!!)
            val mKitchenObserver: Observer<KitchenModel> = Observer {
                if (it != null) {
                    val mPrinterName: String = it.mSelectedKitchenPrinterName
                    val mPrinterType: Int = it.mPrinterType

                    if (mPrinterName != NO_TYPE && mPrinterType == 1) {
                        mBinding.textViewPrinterSelected.visibility = VISIBLE
                    } else if(mPrinterName != NO_TYPE && mPrinterType == 2) {
                        setKitchenIP(it)
                    } else {
                        mBinding.textViewPrinterSelected.visibility = GONE
                    }

                    mKitchenData.removeObservers(this)
                }
            }
            mKitchenData.observe(viewLifecycleOwner, mKitchenObserver)

        } else if (mSharedPrefs.getString(BILL_PRINTER_OR_KITCHEN_PRINTER, "")!! == BILL_PRINTER) {
            if (mSharedPrefs.getString(SELECTED_BILL_PRINTER_NAME, "")!! != NO_TYPE &&
                mSharedPrefs.getInt(SELECTED_BILL_PRINTER_TYPE, 0) == 1
            ) {
                mBinding.textViewPrinterSelected.visibility = VISIBLE
            } else if(mSharedPrefs.getString(SELECTED_BILL_PRINTER_NAME, "")!! != NO_TYPE &&
                mSharedPrefs.getInt(SELECTED_BILL_PRINTER_TYPE, 0) == 2) {
                setBillIP()
            } else {
                mBinding.textViewPrinterSelected.visibility = GONE
            }
        }
    }

    private fun setKitchenIP(kitchenModel: KitchenModel) {
        val mIPs = kitchenModel.mNetworkPrinterIP.split(".")

        mBinding.editTextIpAddress1.setText(mIPs[0])
        mBinding.editTextIpAddress2.setText(mIPs[1])
        mBinding.editTextIpAddress3.setText(mIPs[2])
        mBinding.editTextIpAddress4.setText(mIPs[3])
        mBinding.editTextPortNo.setText(kitchenModel.mNetworkPrinterPort)
    }

    private fun setBillIP() {
        val mIPs = Objects.requireNonNull<String>(mSharedPrefs.getString(SELECTED_BILL_PRINTER_NETWORK_IP, ""))
            .split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        mBinding.editTextIpAddress1.setText(mIPs[0])
        mBinding.editTextIpAddress2.setText(mIPs[1])
        mBinding.editTextIpAddress3.setText(mIPs[2])
        mBinding.editTextIpAddress4.setText(mIPs[3])
        mBinding.editTextPortNo.setText(mSharedPrefs.getString(SELECTED_BILL_PRINTER_NETWORK_PORT, ""))
    }

    private fun setNetworkPrinter(){
        if (mSharedPrefs.getString(BILL_PRINTER_OR_KITCHEN_PRINTER, "")!! == BILL_PRINTER) {
            mBinding.buttonOpenLogwood.visibility = GONE
        }

        mBinding.buttonSetNetwork.setOnClickListener {
            if(validateIP()) {
                if (mSharedPrefs.getString(BILL_PRINTER_OR_KITCHEN_PRINTER, "")!! == KITCHEN_PRINTER) {
                    updateKitchenNetworkPrinter()
                } else if(mSharedPrefs.getString(BILL_PRINTER_OR_KITCHEN_PRINTER, "")!! == BILL_PRINTER) {
                    updateBillNetworkPrinter()
                }
            }
        }

        mBinding.buttonOpenLogwood.setOnClickListener {
            Navigation.findNavController(requireActivity(),R.id.main_screen_host_fragment).navigate(R.id.logwoodSettingFragment)
        }
    }

    private var mIPAddress: String? = null
    private var mPortNo: String? = null

    private fun validateIP(): Boolean {
        val mEditTextIP1: String = mBinding.editTextIpAddress1.text.toString()
        val mEditTextIP2: String = mBinding.editTextIpAddress2.text.toString()
        val mEditTextIP3: String = mBinding.editTextIpAddress3.text.toString()
        val mEditTextIP4: String = mBinding.editTextIpAddress4.text.toString()

        val mPrinterPortNO: String = mBinding.editTextPortNo.text.toString()

        if (mEditTextIP1.isEmpty() || mEditTextIP2.isEmpty() || mEditTextIP3.isEmpty() || mEditTextIP4.isEmpty()) {
            CustomToast.makeText(requireActivity(), "Please enter valid IP", Toast.LENGTH_SHORT).show()
            return false
        } else if (mPrinterPortNO.isEmpty()) {
            CustomToast.makeText(requireActivity(), "Please enter valid Port", Toast.LENGTH_SHORT).show()
            return false
        }

        mIPAddress = "$mEditTextIP1.$mEditTextIP2.$mEditTextIP3.$mEditTextIP4"
        mPortNo = mPrinterPortNO

        return true
    }

    private fun updateKitchenNetworkPrinter() {
        val mUpdateKitchenData: LiveData<Int> = mViewModel.updateKitchenNetworkPrinter(mSharedPrefs.getString(
            SELECTED_KITCHEN_PRINTER_ID,"")!!,mIPAddress!!,mPortNo!!)

        val mUpdateKitchenObserver: Observer<Int> = Observer {
            if(it != null && it > 0) {
                CustomToast.makeText(requireActivity(), "Network Printer selected", Toast.LENGTH_SHORT).show()
                if (!App.isTablet) {
                    Navigation.findNavController(requireActivity(),R.id.main_screen_host_fragment).navigate(R.id.logwoodSettingFragment)
                }
                mUpdateKitchenData.removeObservers(this)
            }
        }
        mUpdateKitchenData.observe(viewLifecycleOwner,mUpdateKitchenObserver)
    }

    private fun updateBillNetworkPrinter() {
        val mEditor = mSharedPrefs.edit()
        mEditor.putString(SELECTED_BILL_PRINTER_NAME, "Network Printer")
        mEditor.putInt(SELECTED_BILL_PRINTER_TYPE, NETWORK_PRINTER)
        mEditor.putString(SELECTED_BILL_PRINTER_NETWORK_IP, mIPAddress)
        mEditor.putString(SELECTED_BILL_PRINTER_NETWORK_PORT, mPortNo)
        mEditor.apply()

        CustomToast.makeText(requireActivity(), "Network Printer selected", Toast.LENGTH_SHORT).show()

        Navigation.findNavController(requireActivity(),R.id.main_screen_host_fragment).navigate(R.id.settingsFragment)
    }

}
