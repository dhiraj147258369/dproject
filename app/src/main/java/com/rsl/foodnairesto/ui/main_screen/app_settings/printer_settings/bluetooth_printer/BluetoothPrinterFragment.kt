package com.rsl.foodnairesto.ui.main_screen.app_settings.printer_settings.bluetooth_printer


import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.getDefaultAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.databinding.FragmentBluetoothPrinterBinding
import com.rsl.foodnairesto.ui.main_screen.app_settings.AppSettingsViewModel
import com.rsl.foodnairesto.ui.main_screen.app_settings.event.ShowBluetoothDevicesEvent
import com.rsl.foodnairesto.ui.main_screen.app_settings.printer_settings.event.OpenSettingEvent
import com.rsl.foodnairesto.utils.AppConstants
import com.rsl.foodnairesto.utils.AppConstants.BILL_PRINTER
import com.rsl.foodnairesto.utils.AppConstants.BLUETOOTH_PRINTER
import com.rsl.foodnairesto.utils.AppConstants.NO_TYPE
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_BILL_PRINTER_NAME
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_BILL_PRINTER_NETWORK_IP
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_BILL_PRINTER_NETWORK_PORT
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_BILL_PRINTER_TYPE
import com.rsl.foodnairesto.utils.AppPreferences
import com.rsl.foodnairesto.utils.InjectorUtils
import com.rsl.foodnairesto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import java.util.*

@SuppressLint("LogNotTimber")
class BluetoothPrinterFragment : Fragment() {

    private lateinit var mBinding: FragmentBluetoothPrinterBinding
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mViewModel: AppSettingsViewModel
    private val prefs: AppPreferences by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_bluetooth_printer, container, false)
        val mView: View = mBinding.root

        val factory = InjectorUtils.provideAppSettingsViewModelFactory(requireActivity())
        mViewModel = ViewModelProviders.of(this, factory).get(AppSettingsViewModel::class.java)

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        getBluetoothDevices()

        return mView
    }

    private var mBluetoothDevices = ArrayList<BluetoothDevice>()
    private var mBAdapter: BluetoothPrinterAdapter? = null
    private var mSelectedBluetoothPrinter: BluetoothDevice? = null


    private fun getBluetoothDevices() {
        e(javaClass.simpleName,"getBluetoothDevices()")

        mBluetoothDevices = ArrayList()

        var printerDeviceCheck = false
        val mBluetoothAdapter = getDefaultAdapter()

        if (mBluetoothAdapter == null) CustomToast.makeText(requireActivity(), "No bluetooth device available", Toast.LENGTH_SHORT).show()

        when {
            mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled -> {
                val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                requireActivity().startActivityForResult(enableBluetooth, 909)
            }
        }

        val pairedDevices: Set<BluetoothDevice>
        when {
            mBluetoothAdapter != null -> {
                pairedDevices = mBluetoothAdapter.bondedDevices
                when {
                    pairedDevices.isNotEmpty() -> pairedDevices.forEach { device ->
                        mBluetoothDevices.add(device)
                        printerDeviceCheck = true
                    }
                }
            }
        }


        when {
            printerDeviceCheck -> {
                mBAdapter = BluetoothPrinterAdapter(mBluetoothDevices, requireActivity())
                mBinding.recyclerViewBluetoothPrinter.layoutManager = LinearLayoutManager(requireActivity())
                mBinding.recyclerViewBluetoothPrinter.adapter = mBAdapter
                e(javaClass.simpleName, "printer size: " + mBluetoothDevices.size)
//                checkIfOtherPrinterIsSelected()
            }
            else -> CustomToast.makeText(requireActivity(), "Printer not found", Toast.LENGTH_SHORT).show()
        }

        mBinding.buttonSetBluetoothPrinter.setOnClickListener {
            mSelectedBluetoothPrinter = mBAdapter?.getSelectedBluetooth()
            when {
                mSelectedBluetoothPrinter != null ->
                    if(prefs.getSelectedPrinterForSetting() == BILL_PRINTER)
                        updateBillPrinter()
                    else
                        updateKitchenPrinter()
                else -> CustomToast.makeText(
                    requireActivity(),
                    "Please Select Bluetooth Printer", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateBillPrinter() {
        val mEditor = mSharedPrefs.edit()
        mEditor.putString(SELECTED_BILL_PRINTER_NAME, mSelectedBluetoothPrinter?.name)
        mEditor.putInt(SELECTED_BILL_PRINTER_TYPE, BLUETOOTH_PRINTER)
        //mEditor.putInt(SELECTED_BILL_PRINT_PAPER_SIZE, mPrinterPaper)
        mEditor.putString(SELECTED_BILL_PRINTER_NETWORK_IP, "")
        mEditor.putString(SELECTED_BILL_PRINTER_NETWORK_PORT, "")
        mEditor.apply()

        CustomToast.makeText(
            requireActivity(), mSelectedBluetoothPrinter?.name + " selected",
            Toast.LENGTH_SHORT
        ).show()

        EventBus.getDefault().post(OpenSettingEvent(1))
        //Navigation.findNavController(requireActivity(),R.id.main_screen_host_fragment).navigate(R.id.settingsFragment)
    }

    private fun updateKitchenPrinter() {
        prefs.setKitchenPrinter(mSelectedBluetoothPrinter?.name ?: NO_TYPE, BLUETOOTH_PRINTER, "", "")
        CustomToast.makeText(requireActivity(), mSelectedBluetoothPrinter?.name + " selected", Toast.LENGTH_SHORT).show()
        EventBus.getDefault().post(OpenSettingEvent(1))
    }

    @Subscribe
    fun onAllowBluetoothDevicePermission(mEvent: ShowBluetoothDevicesEvent) {
        if(mEvent.mResult) {
            getBluetoothDevices()
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
