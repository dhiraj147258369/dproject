package com.rsl.youresto.ui.main_screen.app_settings.general_settings


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.creditcall.chipdnamobile.*
import com.rsl.youresto.R
import com.rsl.youresto.data.app_settings.model.SelectablePinPad
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.databinding.DialogCardPaymentBinding
import com.rsl.youresto.databinding.FragmentGeneralSettingBinding
import com.rsl.youresto.ui.main_screen.app_settings.AppSettingsViewModel
import com.rsl.youresto.ui.main_screen.cart.CartViewModel
import com.rsl.youresto.ui.main_screen.cart.CartViewModelFactory
import com.rsl.youresto.ui.main_screen.checkout.payment_options.card.PinPadModel
import com.rsl.youresto.ui.main_screen.checkout.payment_options.card.PinPadRecyclerAdapter
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.AUTO_LOGOUT_ENABLED
import com.rsl.youresto.utils.AppConstants.ENABLE_KITCHEN_PRINT
import com.rsl.youresto.utils.AppConstants.ENABLE_LOGWOOD
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.PAYMENT_TERMINAL_CONNECTION_TYPE
import com.rsl.youresto.utils.AppConstants.PAYMENT_TERMINAL_ENABLED
import com.rsl.youresto.utils.AppConstants.PAYMENT_TERMINAL_NAME
import com.rsl.youresto.utils.AppConstants.SEAT_SELECTION_ENABLED
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.youresto.utils.AppExecutors
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("LogNotTimber")
class GeneralSettingFragment : Fragment() {

    private lateinit var mBinding: FragmentGeneralSettingBinding
    private var mSharedPref: SharedPreferences? = null
    private lateinit var mCartViewModel: CartViewModel
    private lateinit var mAppSettingViewModel: AppSettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_general_setting, container, false)
        val mView = mBinding.root

        mSharedPref = requireActivity().getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)

        val cartFactory: CartViewModelFactory =
            InjectorUtils.provideCartViewModelFactory(requireActivity())
        mCartViewModel = ViewModelProviders.of(this, cartFactory).get(CartViewModel::class.java)

        val factory = InjectorUtils.provideAppSettingsViewModelFactory(requireActivity())
        mAppSettingViewModel =
            ViewModelProviders.of(this, factory).get(AppSettingsViewModel::class.java)

        checkCart()
        autoLogOut()
        paymentTerminal()
        editPaymentTerminal()
        seatSelection()
        enableKitchenPrint()
        enableLogwood()

        return mView
    }

    private fun autoLogOut() {
        if (mSharedPref!!.getBoolean(AUTO_LOGOUT_ENABLED, false)) {
            mBinding.checkboxAutoLogout.setOnCheckedChangeListener(null)
            mBinding.checkboxAutoLogout.isChecked = true
        } else {
            mBinding.checkboxAutoLogout.setOnCheckedChangeListener(null)
            mBinding.checkboxAutoLogout.isChecked = false
        }
        mBinding.checkboxAutoLogout.setOnCheckedChangeListener(mAutoLogoutListener)
    }

    private val mAutoLogoutListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        val mEditor = mSharedPref!!.edit()
        if (isChecked) {
            mEditor.putBoolean(AUTO_LOGOUT_ENABLED, true)
            CustomToast.makeText(requireActivity(), "Auto Logout Enabled", Toast.LENGTH_SHORT).show()
        } else {
            mEditor.putBoolean(AUTO_LOGOUT_ENABLED, false)
            CustomToast.makeText(requireActivity(), "Auto Logout Disabled", Toast.LENGTH_SHORT).show()
        }

        mEditor.apply()
    }

    private val mCartList = ArrayList<CartProductModel>()

    private fun checkCart() {
        mCartViewModel.getAllCartDataIrrespectiveOfLocations().observe(viewLifecycleOwner, {
            if (it != null) mCartList.addAll(it)
        })
    }

    private fun seatSelection() {
        if (mSharedPref!!.getBoolean(SEAT_SELECTION_ENABLED, false)) {
            mBinding.checkboxSeatSelection.setOnCheckedChangeListener(null)
            mBinding.checkboxSeatSelection.isChecked = true
        } else {
            mBinding.checkboxSeatSelection.setOnCheckedChangeListener(null)
            mBinding.checkboxSeatSelection.isChecked = false
        }
        mBinding.checkboxSeatSelection.setOnCheckedChangeListener(mSeatSelectionListener)
    }

    private val mSeatSelectionListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        val mEditor = mSharedPref!!.edit()
        if (isChecked) {
            if (mCartList.size == 0) {
                mEditor.putBoolean(SEAT_SELECTION_ENABLED, true)
                mSharedPref!!.edit().putString(AppConstants.GROUP_NAME, "").apply()
                CustomToast.makeText(requireActivity(), "Seat Selection Enabled", Toast.LENGTH_SHORT)
                    .show()
            } else {
                mBinding.checkboxSeatSelection.isChecked = false
                CustomToast.makeText(
                    requireActivity(),
                    "All table should be empty of all locations to enable this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            if (mCartList.size == 0) {
                mEditor.putBoolean(SEAT_SELECTION_ENABLED, false)
                mSharedPref!!.edit().putString(AppConstants.GROUP_NAME, "Z").apply()
                CustomToast.makeText(requireActivity(), "Seat Selection Disabled", Toast.LENGTH_SHORT)
                    .show()
            } else {
                mBinding.checkboxSeatSelection.isChecked = true
                CustomToast.makeText(
                    requireActivity(),
                    "All table should be empty of all locations to disable this feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        mEditor.apply()
    }

    private fun enableKitchenPrint() {
        if (mSharedPref!!.getBoolean(ENABLE_KITCHEN_PRINT, false)) {
            mBinding.checkboxEnableKitchenPrint.setOnCheckedChangeListener(null)
            mBinding.checkboxEnableKitchenPrint.isChecked = true
        } else {
            mBinding.checkboxEnableKitchenPrint.setOnCheckedChangeListener(null)
            mBinding.checkboxEnableKitchenPrint.isChecked = false
        }
        mBinding.checkboxEnableKitchenPrint.setOnCheckedChangeListener(mEnableKitchenPrintListener)
    }

    private val mEnableKitchenPrintListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            val mEditor = mSharedPref!!.edit()
            if (isChecked) {
                mEditor.putBoolean(ENABLE_KITCHEN_PRINT, true)
                CustomToast.makeText(requireActivity(), "Kitchen Print Enabled", Toast.LENGTH_SHORT).show()
            } else {
                mEditor.putBoolean(ENABLE_KITCHEN_PRINT, false)
                CustomToast.makeText(requireActivity(), "Kitchen Print Disabled", Toast.LENGTH_SHORT)
                    .show()
            }

            mEditor.apply()
        }

    private fun enableLogwood() {
        if (mSharedPref!!.getBoolean(ENABLE_LOGWOOD, false)) {
            mBinding.checkboxEnableLogwood.setOnCheckedChangeListener(null)
            mBinding.checkboxEnableLogwood.isChecked = true
        } else {
            mBinding.checkboxEnableLogwood.setOnCheckedChangeListener(null)
            mBinding.checkboxEnableLogwood.isChecked = false
        }
        mBinding.checkboxEnableLogwood.setOnCheckedChangeListener(mEnableLogwoodListener)
    }

    private val mEnableLogwoodListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        val mEditor = mSharedPref!!.edit()
        if (isChecked) {
            mEditor.putBoolean(ENABLE_LOGWOOD, true)
            CustomToast.makeText(requireActivity(), "Logwood Enabled", Toast.LENGTH_SHORT).show()
        } else {
            mEditor.putBoolean(ENABLE_LOGWOOD, false)
            CustomToast.makeText(requireActivity(), "Logwood Disabled", Toast.LENGTH_SHORT).show()
        }

        mEditor.apply()
    }

    private fun editPaymentTerminal() = mBinding.buttonPaymentEdit.setOnClickListener {
        e(javaClass.simpleName, "button on click")
        deviceSelection()
    }

    private fun paymentTerminal() {
        if (mSharedPref!!.getBoolean(PAYMENT_TERMINAL_ENABLED, false)) {
            mBinding.checkboxPaymentTerminal.setOnCheckedChangeListener(null)
            mBinding.checkboxPaymentTerminal.isChecked = true
            val mPaymentTerminal =
                "Payment Terminal: " + mSharedPref!!.getString(PAYMENT_TERMINAL_NAME, "")!!
            mBinding.checkboxPaymentTerminal.text = mPaymentTerminal
        } else {
            mBinding.checkboxPaymentTerminal.setOnCheckedChangeListener(null)
            mBinding.checkboxPaymentTerminal.isChecked = false
            val mPaymentTerminal = "Payment Terminal"
            mBinding.checkboxPaymentTerminal.text = mPaymentTerminal
            mBinding.buttonPaymentEdit.isEnabled = false
        }
        mBinding.checkboxPaymentTerminal.setOnCheckedChangeListener(mPaymentTerminalListener)
    }

    private val mPaymentTerminalListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if (isChecked) {
            deviceSelection()
        } else {
            val mEditor = mSharedPref!!.edit()
            mEditor.putBoolean(PAYMENT_TERMINAL_ENABLED, false)
            mEditor.apply()
            mBinding.buttonPaymentEdit.isEnabled = false
            val mPaymentTerminal = "Payment Terminal"
            mBinding.checkboxPaymentTerminal.text = mPaymentTerminal
            if (!requireActivity().isFinishing)
                CustomToast.makeText(
                    requireActivity(),
                    "Payment Terminal Disabled",
                    Toast.LENGTH_SHORT
                ).show()

            mAppSettingViewModel.getPaymentDevice(
                mSharedPref!!.getString(
                    SELECTED_LOCATION_ID,
                    ""
                )!!
            ).observe(viewLifecycleOwner, { selectablePinPad ->
                if (selectablePinPad != null) {
                    selectablePinPad.mPinPadName = ""
                    selectablePinPad.mConnectionType = ""
                    mAppSettingViewModel.updatePaymentDevice(selectablePinPad, false)
                        .observe(viewLifecycleOwner, {
                            if (!requireActivity().isFinishing) CustomToast.makeText(
                                requireActivity(),
                                "Payment Terminal Disabled",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                }
            })
        }
    }

    private var mPinPadListDialog: Dialog? = null
    private var mPinPadAdapter: PinPadRecyclerAdapter? = null
    private val mPinPadList = ArrayList<PinPadModel>()

    private fun deviceSelection() {
        e(javaClass.simpleName, "deviceSelection")

        val parameters = Parameters()
        parameters.add(ParameterKeys.SearchConnectionTypeBluetooth, ParameterValues.TRUE)
        parameters.add(ParameterKeys.SearchConnectionTypeUsb, ParameterValues.TRUE)

        ChipDnaMobile.getInstance().clearAllAvailablePinPadsListeners()
        ChipDnaMobile.getInstance().addAvailablePinPadsListener(AvailablePinPadsListener())
        ChipDnaMobile.getInstance().getAvailablePinPads(parameters)

        mPinPadListDialog = Dialog(requireActivity())

        val mPinPadDialogBinding =
            DataBindingUtil.inflate<DialogCardPaymentBinding>(
                LayoutInflater.from(context),
                R.layout.dialog_card_payment,
                null,
                false
            )

        mPinPadListDialog!!.setContentView(mPinPadDialogBinding.root)

        mPinPadAdapter = PinPadRecyclerAdapter(mPinPadList)
        mPinPadDialogBinding.recyclerViewPinPads.adapter = mPinPadAdapter

        mPinPadListDialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        mPinPadListDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        mPinPadListDialog!!.show()
        mPinPadListDialog!!.setCancelable(false)

        if(mPinPadList.size == 0) {
            mPinPadDialogBinding.textViewNoDeviceFound.visibility = VISIBLE
            mPinPadDialogBinding.recyclerViewPinPads.visibility = GONE
            mPinPadDialogBinding.buttonCardCancel.text = "OK"
        }

        mPinPadDialogBinding.buttonCardCancel.setOnClickListener {
            mPinPadListDialog!!.dismiss()
            mBinding.checkboxPaymentTerminal.isChecked = false
        }
    }

    @Subscribe
    fun onPinPadClicked(mPinPad: PinPadModel) {
        val mEditor = mSharedPref!!.edit()
        mEditor.putBoolean(PAYMENT_TERMINAL_ENABLED, true)
        mEditor.putString(PAYMENT_TERMINAL_NAME, mPinPad.pinPadName)
        mEditor.putString(PAYMENT_TERMINAL_CONNECTION_TYPE, mPinPad.connectionType)
        mEditor.apply()

        val mPaymentTerminal = "Payment Terminal: " + mPinPad.pinPadName
        mBinding.checkboxPaymentTerminal.text = mPaymentTerminal

        mAppSettingViewModel.getPaymentDevice(mSharedPref!!.getString(SELECTED_LOCATION_ID, "")!!)
            .observe(viewLifecycleOwner, { selectablePinPad ->
                val mPaymentDevice: SelectablePinPad?
                val mInsertUpdate: Boolean

                if (selectablePinPad != null) {
                    selectablePinPad.mPinPadName = mPinPad.pinPadName
                    selectablePinPad.mConnectionType = mPinPad.connectionType
                    mPaymentDevice = selectablePinPad
                    mInsertUpdate = false
                } else {
                    mPaymentDevice = SelectablePinPad(
                        mPinPad.pinPadName,
                        mPinPad.connectionType,
                        mSharedPref!!.getString(SELECTED_LOCATION_ID, "")!!
                    )
                    mInsertUpdate = true
                }

                mAppSettingViewModel.updatePaymentDevice(mPaymentDevice, mInsertUpdate)
                    .observe(viewLifecycleOwner, {
                        if (it > -1)
                            CustomToast.makeText(requireActivity(), mPinPad.pinPadName + " Selected.", Toast.LENGTH_SHORT).show()
                    })
            })

        mPinPadListDialog!!.dismiss()
        mBinding.buttonPaymentEdit.isEnabled = true
    }

    private inner class AvailablePinPadsListener : IAvailablePinPadsListener {

        @SuppressLint("StaticFieldLeak")
        override fun onAvailablePinPads(parameters: Parameters) {

            val availablePinPadsXml = parameters.getValue(ParameterKeys.AvailablePinPads)

            object : AsyncTask<String, Void, List<PinPadModel>>() {
                override fun doInBackground(vararg params: String): List<PinPadModel> {
                    val availablePinPadsList = ArrayList<PinPadModel>()
                    try {
                        val availablePinPadsHashMap =
                            ChipDnaMobileSerializer.deserializeAvailablePinPads(params[0])

                        for (connectionType in availablePinPadsHashMap.keys) {
                            for (mPinPad in Objects.requireNonNull<ArrayList<String>>(
                                availablePinPadsHashMap[connectionType]
                            )) {
                                availablePinPadsList.add(PinPadModel(mPinPad, connectionType))
                            }
                        }
                    } catch (e: XmlPullParserException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    return availablePinPadsList
                }

                override fun onPostExecute(availablePinPadsList: List<PinPadModel>?) {
                    mPinPadList.clear()

                    if (availablePinPadsList != null) {
                        mPinPadList.addAll(availablePinPadsList)
                    }

                    if (mPinPadAdapter != null)
                        mPinPadAdapter!!.notifyDataSetChanged()
                }
            }.execute(availablePinPadsXml)
        }
    }

    @Synchronized
    private fun initChipDnaMobile() {
        if (!ChipDnaMobile.isInitialized()) {
            initialiseListeners()
        }
    }

    private fun initialiseListeners() {
        val mExecutors = AppExecutors.getInstance()
        mExecutors.diskIO().execute {
            try {
                var requestParameters = Parameters()
                requestParameters.add(ParameterKeys.Password, "0000")
                val response = ChipDnaMobile.initialize(activity, requestParameters)

                if (response.containsKey(ParameterKeys.Result) && response.getValue(ParameterKeys.Result).equals(
                        "True",
                        ignoreCase = true
                    )
                ) {
                    // ChipDna Mobile has been successfully initialised.
                    e(javaClass.simpleName, "ChipDna Mobile initialised: ")
                    e(
                        javaClass.simpleName,
                        "Version: " + ChipDnaMobile.getSoftwareVersion() + ", Name: " + ChipDnaMobile.getSoftwareVersionName()
                    )
                    // We can start setting our ChipDna Mobile credentials.
                    //                registerListeners();
                    //                setCredentials();

                    requestParameters = Parameters()
                } else {
                    // The password is incorrect, ChipDnaMobile cannot initialise
                    e(javaClass.simpleName, "Failed to initialise ChipDna Mobile")
                    if (response.getValue(ParameterKeys.RemainingAttempts).equals(
                            "0",
                            ignoreCase = true
                        )
                    ) {
                        // If all password attempts have been used, the database is deleted and a new password is required.
                        e(javaClass.simpleName, "Reached password attempt limit")
                    } else {
                        e(
                            javaClass.simpleName,
                            "Password attempts remaining: " + response.getValue(ParameterKeys.RemainingAttempts)
                        )
                    }

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initChipDnaMobile()
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
