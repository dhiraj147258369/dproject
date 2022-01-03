package com.rsl.youresto.ui.main_screen.app_settings.printer_settings

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
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.rsl.youresto.R
import com.rsl.youresto.data.database_download.models.KitchenModel
import com.rsl.youresto.databinding.FragmentPrinterSettingBinding
import com.rsl.youresto.ui.main_screen.app_settings.AppSettingsViewModel
import com.rsl.youresto.ui.main_screen.app_settings.printer_settings.event.SelectPrinterEvent
import com.rsl.youresto.ui.main_screen.app_settings.printer_settings.kitchen_printer.KitchenPrinterAdapter
import com.rsl.youresto.ui.main_screen.app_settings.printer_settings.kitchen_printer.KitchenPrinterEvent
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.BILL_PRINTER
import com.rsl.youresto.utils.AppConstants.BILL_PRINTER_ENABLED
import com.rsl.youresto.utils.AppConstants.BILL_PRINTER_OR_KITCHEN_PRINTER
import com.rsl.youresto.utils.AppConstants.NO_TYPE
import com.rsl.youresto.utils.AppConstants.PAPER_SIZE_50
import com.rsl.youresto.utils.AppConstants.PAPER_SIZE_80
import com.rsl.youresto.utils.AppConstants.SELECTED_BILL_PRINTER_NAME
import com.rsl.youresto.utils.AppConstants.SELECTED_BILL_PRINTER_NETWORK_IP
import com.rsl.youresto.utils.AppConstants.SELECTED_BILL_PRINTER_NETWORK_PORT
import com.rsl.youresto.utils.AppConstants.SELECTED_BILL_PRINTER_TYPE
import com.rsl.youresto.utils.AppConstants.SELECTED_BILL_PRINT_PAPER_SIZE
import com.rsl.youresto.utils.AppConstants.SELECTED_KITCHEN_PRINTER_ID
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.collections.ArrayList

@Suppress("DEPRECATED_IDENTITY_EQUALS")
@SuppressLint("LogNotTimber")
class PrinterSettingFragment : Fragment() {

    private lateinit var mBinding: FragmentPrinterSettingBinding
    private lateinit var mViewModel: AppSettingsViewModel
    private lateinit var mSharedPrefs: SharedPreferences
    private var mKitchenPrinterList: ArrayList<KitchenModel>? = null
    private var mKitchenPrinterAdapter: KitchenPrinterAdapter? = null
    private var mSelectedLocationType: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_printer_setting, container, false)
        val mView: View = mBinding.root

        val factory = InjectorUtils.provideAppSettingsViewModelFactory(requireActivity())
        mViewModel = ViewModelProviders.of(this, factory).get(AppSettingsViewModel::class.java)

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)
        mSelectedLocationType = mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE, 0)

        setPaperSizeSpinner()
        getPrinters()
        billPrinter()

        return mView
    }

    private fun getPrinters() {
        val mLayoutManager = LinearLayoutManager(requireActivity())
        mBinding.recyclerViewKitchenPrinters.layoutManager = mLayoutManager

        mKitchenPrinterList = ArrayList()

        mViewModel.getAllKitchenPrinters().observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                mKitchenPrinterList!!.clear()
                mKitchenPrinterList!!.addAll(it)
                mKitchenPrinterAdapter = KitchenPrinterAdapter(requireActivity(), mKitchenPrinterList!!)
                mBinding.recyclerViewKitchenPrinters.adapter = mKitchenPrinterAdapter
            }
        })
    }

    private var mPaperSelectedListener: AdapterView.OnItemSelectedListener? = null

    private fun setPaperSizeSpinner() {

        val mPaperSize = ArrayList<String>()
        mPaperSize.add("50mm")
        mPaperSize.add("80mm")

        val mTypeAdapter =
            ArrayAdapter(requireActivity(), R.layout.spinner_item_printer_paper_size, mPaperSize)
        mTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.spinnerBillPaperSize.adapter = mTypeAdapter

        val mEditor = mSharedPrefs.edit()

        if (mSharedPrefs.getInt(
                SELECTED_BILL_PRINT_PAPER_SIZE,
                0
            ) == PAPER_SIZE_50
        ) mBinding.spinnerBillPaperSize.setSelection(0)
        else mBinding.spinnerBillPaperSize.setSelection(1)

        mPaperSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, mPosition: Int, id: Long) {
                when (mPosition) {
                    0 -> {
                        Log.e(javaClass.simpleName, "onItemSelected: case 0")
                        mEditor.putInt(SELECTED_BILL_PRINT_PAPER_SIZE, PAPER_SIZE_50)
                        mEditor.apply()
                    }
                    1 -> {
                        Log.e(javaClass.simpleName, "onItemSelected: case 1")
                        mEditor.putInt(SELECTED_BILL_PRINT_PAPER_SIZE, PAPER_SIZE_80)
                        mEditor.apply()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) = Unit
        }

        mBinding.spinnerBillPaperSize.onItemSelectedListener = mPaperSelectedListener

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

    private fun resetBillPrinter() {
        val mEditor = mSharedPrefs.edit()
        mEditor.putString(SELECTED_BILL_PRINTER_NAME, NO_TYPE)
        mEditor.putString(SELECTED_BILL_PRINTER_NETWORK_IP, "")
        mEditor.putString(SELECTED_BILL_PRINTER_NETWORK_PORT, "")
        mEditor.putInt(SELECTED_BILL_PRINTER_TYPE, 0)
        mEditor.putBoolean(BILL_PRINTER_ENABLED, false)
        mEditor.apply()
        val mName = "Bill Printer"
        mBinding.checkboxBillPrinter.text = mName
        mBinding.checkboxBillPrinter.isChecked = false
        CustomToast.makeText(requireActivity(), "Bill printer disabled", Toast.LENGTH_SHORT).show()
        mBinding.textViewBillPrinterNetworkIp.visibility = View.GONE
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

    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
    }

    @Subscribe
    fun onKitchenPrinterPaperSelectedOrUncheckPrinter(mEvent: KitchenPrinterEvent) {
        if (mEvent.mResult) {
            mViewModel.updateKitchenPrinterPaper(mEvent.mKitchen.mKitchenID, mEvent.mKitchen.mSelectedKitchenPrinterName, mEvent.mPaperSize,
                mEvent.mKitchen.mPrinterType).observe(viewLifecycleOwner, {})

        } else {
            // -----------------  onUnchecked kitchen printer  --------------------
            val mUpdatePrinter: LiveData<Int> =
                mViewModel.clearKitchenPrinterData(mEvent.mKitchen.mKitchenID)

            val mUpdatePrinterObserver: Observer<Int> = Observer {
                if (it != null && it < 1) {
                    mKitchenPrinterAdapter!!.notifyDataSetChanged()
                    mUpdatePrinter.removeObservers(this)
                }
            }
            mUpdatePrinter.observe(viewLifecycleOwner, mUpdatePrinterObserver)
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
