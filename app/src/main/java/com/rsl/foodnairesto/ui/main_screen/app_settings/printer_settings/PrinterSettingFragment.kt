package com.rsl.foodnairesto.ui.main_screen.app_settings.printer_settings

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.databinding.FragmentPrinterSettingBinding
import com.rsl.foodnairesto.ui.main_screen.app_settings.printer_settings.event.SelectPrinterEvent
import com.rsl.foodnairesto.utils.AppConstants
import com.rsl.foodnairesto.utils.AppConstants.BILL_PRINTER
import com.rsl.foodnairesto.utils.AppConstants.BILL_PRINTER_ENABLED
import com.rsl.foodnairesto.utils.AppConstants.BILL_PRINTER_OR_KITCHEN_PRINTER
import com.rsl.foodnairesto.utils.AppConstants.KITCHEN_PRINTER
import com.rsl.foodnairesto.utils.AppConstants.NO_TYPE
import com.rsl.foodnairesto.utils.AppConstants.PAPER_SIZE_50
import com.rsl.foodnairesto.utils.AppConstants.PAPER_SIZE_80
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_BILL_PRINTER_NAME
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_BILL_PRINTER_NETWORK_IP
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_BILL_PRINTER_NETWORK_PORT
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_BILL_PRINT_PAPER_SIZE
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_KITCHEN_PRINTER_ID
import com.rsl.foodnairesto.utils.AppPreferences
import com.rsl.foodnairesto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATED_IDENTITY_EQUALS")
@SuppressLint("LogNotTimber")
class PrinterSettingFragment : Fragment() {

    private lateinit var mBinding: FragmentPrinterSettingBinding
    private lateinit var mSharedPrefs: SharedPreferences
    private var mSelectedLocationType: Int? = null
    private val prefs: AppPreferences by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_printer_setting, container, false)
        val mView: View = mBinding.root

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)
        mSelectedLocationType = mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE, 0)

        setPaperSizeSpinner()
        billPrinter()
        kitchenPrinter()
        return mView
    }

    private var mPaperSelectedListener: AdapterView.OnItemSelectedListener? = null
    private var mKitchenPaperSelectedListener: AdapterView.OnItemSelectedListener? = null

    private fun setPaperSizeSpinner() {

        val mPaperSize = ArrayList<String>()
        mPaperSize.add("50mm")
        mPaperSize.add("80mm")

        val mTypeAdapter =
            ArrayAdapter(requireActivity(), R.layout.spinner_item_printer_paper_size, mPaperSize)
        mTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.spinnerBillPaperSize.adapter = mTypeAdapter
        mBinding.spinnerKitchenPaperSize.adapter = mTypeAdapter

        if (prefs.getSelectedBillPrinterPaperSize() == PAPER_SIZE_50) mBinding.spinnerBillPaperSize.setSelection(0)
        else mBinding.spinnerBillPaperSize.setSelection(1)

        mPaperSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, mPosition: Int, id: Long) {
                when (mPosition) {
                    0 -> prefs.setBillPrinterPaperSize(PAPER_SIZE_50)
                    1 -> prefs.setBillPrinterPaperSize(PAPER_SIZE_80)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }

        mBinding.spinnerBillPaperSize.onItemSelectedListener = mPaperSelectedListener

        //kitchen printer related
        if (prefs.getSelectedKitchenPrinterPaperSize() == PAPER_SIZE_50) mBinding.spinnerKitchenPaperSize.setSelection(0)
        else mBinding.spinnerKitchenPaperSize.setSelection(1)

        mKitchenPaperSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, mPosition: Int, id: Long) {
                when (mPosition) {
                    0 -> prefs.setKitchenPrinterPaperSize(PAPER_SIZE_50)
                    1 -> prefs.setKitchenPrinterPaperSize(PAPER_SIZE_80)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }

        mBinding.spinnerKitchenPaperSize.onItemSelectedListener = mKitchenPaperSelectedListener

        askBluetoothPermission()
    }

    private fun askBluetoothPermission() {
        val code = 5 // app defined constant used for onRequestPermissionsResult

        val permissionsToRequest = arrayOf(
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        var allPermissionsGranted = true

        for (permission in permissionsToRequest) {
            allPermissionsGranted =
                allPermissionsGranted && ContextCompat.checkSelfPermission(
                    Objects.requireNonNull(requireActivity()),
                    permission
                ) === PackageManager.PERMISSION_GRANTED
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(requireActivity(), permissionsToRequest, code)
        }
    }

    private fun billPrinter() {
        if (mSharedPrefs.getString(SELECTED_BILL_PRINTER_NAME, "") != NO_TYPE &&
            mSharedPrefs.getString(SELECTED_BILL_PRINTER_NAME, "") != "") {
            mBinding.checkboxBillPrinter.setOnCheckedChangeListener(null)
            mBinding.checkboxBillPrinter.isChecked = true
            val mBill = "Bill Printer: " + mSharedPrefs.getString(SELECTED_BILL_PRINTER_NAME, "")!!
            mBinding.checkboxBillPrinter.text = mBill

            if (mSharedPrefs.getInt(SELECTED_BILL_PRINT_PAPER_SIZE, 0) == PAPER_SIZE_50) {
                Log.e(
                    javaClass.simpleName,
                    "billPrinter: paper: " + mSharedPrefs.getInt(SELECTED_BILL_PRINT_PAPER_SIZE, 0)
                )
                mBinding.spinnerBillPaperSize.onItemSelectedListener = null
                mBinding.spinnerBillPaperSize.setSelection(0)
            } else {
                mBinding.spinnerBillPaperSize.onItemSelectedListener = null
                mBinding.spinnerBillPaperSize.setSelection(1)
            }

            if (Objects.requireNonNull<String>(
                    mSharedPrefs.getString(
                        SELECTED_BILL_PRINTER_NETWORK_IP,
                        ""
                    )
                ).isNotEmpty()
            ) {
                mBinding.textViewBillPrinterNetworkIp.visibility = View.VISIBLE
                val mNetworkIP = "Network IP: " + mSharedPrefs.getString(SELECTED_BILL_PRINTER_NETWORK_IP, "") +
                        ":" + mSharedPrefs.getString(SELECTED_BILL_PRINTER_NETWORK_PORT, "")
                mBinding.textViewBillPrinterNetworkIp.text = mNetworkIP
            }

        }

        mBinding.spinnerBillPaperSize.onItemSelectedListener = mPaperSelectedListener

        mBinding.checkboxBillPrinter.setOnCheckedChangeListener(mBillListener)

        mBinding.buttonBluetoothReset.setOnClickListener {
            if (mBinding.checkboxBillPrinter.isChecked)
                resetBillPrinter()
        }
    }

    private fun kitchenPrinter() {
        if (prefs.getSelectedKitchenPrinterName() != NO_TYPE && prefs.getSelectedKitchenPrinterName().isNotBlank()) {
            mBinding.kitchenPrinterCheckbox.setOnCheckedChangeListener(null)
            mBinding.kitchenPrinterCheckbox.isChecked = true
            val mBill = "Kitchen Printer: ${prefs.getSelectedKitchenPrinterName()}"
            mBinding.kitchenPrinterCheckbox.text = mBill

            if (prefs.getSelectedKitchenPrinterPaperSize() == PAPER_SIZE_50) {
                mBinding.spinnerKitchenPaperSize.onItemSelectedListener = null
                mBinding.spinnerKitchenPaperSize.setSelection(0)
            } else {
                mBinding.spinnerKitchenPaperSize.onItemSelectedListener = null
                mBinding.spinnerKitchenPaperSize.setSelection(1)
            }

            if (prefs.getSelectedKitchenPrinterIP().isNotBlank()) {
                mBinding.kitchenPrinterNetworkIP.visibility = View.VISIBLE
                val mNetworkIP = "Network IP: ${prefs.getSelectedKitchenPrinterIP()}:${prefs.getSelectedKitchenPrinterPort()}"
                mBinding.kitchenPrinterNetworkIP.text = mNetworkIP
            }

        }

        mBinding.spinnerKitchenPaperSize.onItemSelectedListener = mKitchenPaperSelectedListener

        mBinding.kitchenPrinterCheckbox.setOnCheckedChangeListener(kitchenPrinterListener)

        mBinding.buttonBluetoothResetKitchen.setOnClickListener {
            if (mBinding.kitchenPrinterCheckbox.isChecked)
                resetBillPrinter()
        }
    }

    private fun resetBillPrinter() {
        prefs.resetBillPrinter()
        val mName = "Bill Printer"
        mBinding.checkboxBillPrinter.text = mName
        mBinding.checkboxBillPrinter.isChecked = false
        CustomToast.makeText(requireActivity(), "Bill printer disabled", Toast.LENGTH_SHORT).show()
        mBinding.textViewBillPrinterNetworkIp.visibility = View.GONE
    }

    private fun resetKitchenPrinter() {
        prefs.resetKitchenPrinter()
        val mName = "Kitchen Printer"
        mBinding.kitchenPrinterCheckbox.text = mName
        mBinding.kitchenPrinterCheckbox.isChecked = false
        CustomToast.makeText(requireActivity(), "Kitchen printer disabled", Toast.LENGTH_SHORT).show()
        mBinding.kitchenPrinterNetworkIP.visibility = View.GONE
    }

    private val mBillListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
            val mEditor = mSharedPrefs.edit()
            mEditor.putString(BILL_PRINTER_OR_KITCHEN_PRINTER, BILL_PRINTER)
            mEditor.putString(SELECTED_KITCHEN_PRINTER_ID, "0")
            mEditor.putBoolean(BILL_PRINTER_ENABLED, true)
            mEditor.apply()
            EventBus.getDefault().post(SelectPrinterEvent(true))
        } else {
            resetBillPrinter()
        }
    }

    private val kitchenPrinterListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
            val mEditor = mSharedPrefs.edit()
            mEditor.putString(BILL_PRINTER_OR_KITCHEN_PRINTER, KITCHEN_PRINTER)
            mEditor.apply()
            EventBus.getDefault().post(SelectPrinterEvent(true))
        } else {
            resetKitchenPrinter()
        }
    }

    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
    }

}
