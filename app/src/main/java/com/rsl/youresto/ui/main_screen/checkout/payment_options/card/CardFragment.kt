package com.rsl.youresto.ui.main_screen.checkout.payment_options.card


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.provider.UserDictionary.Words.APP_ID
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast.LENGTH_SHORT
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.creditcall.chipdnamobile.*
import com.rsl.youresto.R
import com.rsl.youresto.data.checkout.model.CheckoutModel
import com.rsl.youresto.data.checkout.model.CheckoutTransaction
import com.rsl.youresto.data.checkout.model.PaymentTransaction
import com.rsl.youresto.data.database_download.models.PaymentMethodModel
import com.rsl.youresto.data.tables.models.LocalTableGroupModel
import com.rsl.youresto.databinding.FragmentCardBinding
import com.rsl.youresto.ui.main_screen.app_settings.AppSettingsViewModel
import com.rsl.youresto.ui.main_screen.cart.NewCartViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutDialog
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModelFactory
import com.rsl.youresto.ui.main_screen.checkout.SharedCheckoutViewModel
import com.rsl.youresto.ui.main_screen.checkout.bill_print.ShareBillPrint50Activity
import com.rsl.youresto.ui.main_screen.checkout.bill_print.ShareBillPrint80Activity
import com.rsl.youresto.ui.main_screen.checkout.events.OnNavigationChangeEvent
import com.rsl.youresto.ui.main_screen.checkout.events.OpenPaymentMethodEvent
import com.rsl.youresto.ui.main_screen.checkout.events.SeatPaymentCompleteEvent
import com.rsl.youresto.ui.main_screen.tables_and_tabs.tables.TablesViewModel
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.DIALOG_TYPE_OTHER
import com.rsl.youresto.utils.AppConstants.SELECTED_BILL_PRINTER_NAME
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.youresto.utils.AppExecutors
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.custom_dialog.CustomProgressDialog
import com.rsl.youresto.utils.custom_dialog.CustomProgressTextEvent
import com.rsl.youresto.utils.custom_views.CustomToast
import com.rsl.youresto.utils.printer_utils.ESCUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.OutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("LogNotTimber")
class CardFragment : Fragment() {

    private lateinit var mBinding: FragmentCardBinding
    private lateinit var mCheckoutViewModel: CheckoutViewModel
    private lateinit var mTableViewModel: TablesViewModel
    private lateinit var mAppSettingViewModel: AppSettingsViewModel
    private lateinit var mSharedPrefs: SharedPreferences
    private var mCheckoutRowID = 0
    private var mTableNO = 0
    private var mTableID = ""
    private var mGroupName = ""
    private var TID: String? = null
    private var TK: String? = null
    private var mSelectedLocationType: Int? = null

    private val cartViewModel: NewCartViewModel by viewModel()
    private val sharedCheckoutModel by lazy { requireParentFragment().requireParentFragment().getViewModel<SharedCheckoutViewModel>() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_card, container, false)
        val mView = mBinding.root
        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        mSelectedLocationType = mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE, 0)

        if (mSelectedLocationType == AppConstants.SERVICE_DINE_IN) {
            mTableNO = mSharedPrefs.getInt(AppConstants.SELECTED_TABLE_NO, 0)
            mTableID = mSharedPrefs.getString(AppConstants.SELECTED_TABLE_ID, "") ?: ""
            mGroupName = mSharedPrefs.getString(AppConstants.GROUP_NAME, "")!!
        } else if (mSelectedLocationType == AppConstants.SERVICE_QUICK_SERVICE) {
            mTableNO = mSharedPrefs.getInt(AppConstants.QUICK_SERVICE_TABLE_NO, 0)
            mGroupName = "Q"
        }

        val factory = InjectorUtils.provideTablesViewModelFactory(requireActivity())
        mTableViewModel = ViewModelProviders.of(this, factory).get(TablesViewModel::class.java)

        val checkoutFactory: CheckoutViewModelFactory = InjectorUtils.provideCheckoutViewModelFactory(requireActivity())
        mCheckoutViewModel = ViewModelProviders.of(this, checkoutFactory).get(CheckoutViewModel::class.java)

        val appSettingFactory = InjectorUtils.provideAppSettingsViewModelFactory(requireActivity())
        mAppSettingViewModel = ViewModelProviders.of(this, appSettingFactory).get(AppSettingsViewModel::class.java)

//        mCheckoutRowID = mSharedPrefs.getInt(AppConstants.CHECKOUT_ROW_ID, 0)
//        TID = mSharedPrefs.getString(MERCHANT_TID, "")
//        TK = mSharedPrefs.getString(MERCHANT_TK, "")

//        Handler().postDelayed({ init() },200)
        setViews()
        tryAgain()

//        if (TID!!.isEmpty() || TK!!.isEmpty()) {
//            Toast.makeText(activity, "There are no PinPad credentials presents.", Toast.LENGTH_SHORT).show()
//        }

        return mView
    }

    private var mCheckoutTransactionList: ArrayList<CheckoutTransaction>? = null
    private var mCheckoutTransaction: CheckoutTransaction? = null
    private var mCheckoutModel: CheckoutModel? = null
    private var mLocalTableGroupModel: LocalTableGroupModel? = null
    private var mCardPaymentMethod: PaymentMethodModel? = null
    private var mOrderTotal = BigDecimal(0)
    private var mProgressDialog: CustomProgressDialog? = null

    private fun init() {
        mAppSettingViewModel.getPaymentDevice(mSharedPrefs.getString(SELECTED_LOCATION_ID,"")!!).observe(viewLifecycleOwner,
            {
                if(it != null) {
                    val requestParameters = Parameters()
                    requestParameters.add(ParameterKeys.PinPadName, it.mPinPadName)
                    requestParameters.add(ParameterKeys.PinPadConnectionType, it.mConnectionType)
                    ChipDnaMobile.getInstance().setProperties(requestParameters)
                    showProgressDialog("Processing Payment", "Connecting To Payment Terminal", DIALOG_TYPE_OTHER)
                    connectToPinPad()
                }
            })
    }

    private fun setViews() {

        //progress dialog
        //showProgressDialog("Initialising Payment Gateway", "Please Wait While Payment Gateway is Initialising", DIALOG_TYPE_OTHER)

        mBinding.cardFragment = this

        mOrderTotal = BigDecimal(sharedCheckoutModel.postCheckout.netTotal)
        val mOrderTotalString = String.format("%.2f", mOrderTotal)
        mBinding.editTextCardValue.setText(mOrderTotalString)
        mBinding.editTextCardValue.setSelection(mOrderTotalString.length)

//        mCheckoutViewModel.getCheckoutDataByRowID(mCheckoutRowID).observe(viewLifecycleOwner, {
//            when {
//                it != null -> {
//                    mCheckoutModel = it
//                    mCheckoutTransactionList = mCheckoutModel!!.mCheckoutTransaction
//
//                    loop@ for (i in 0 until mCheckoutTransactionList!!.size) {
//                        when {
//                            mCheckoutTransactionList!![i].isSelected && !mCheckoutTransactionList!![i].isFullPaid -> {
//                                mCheckoutTransaction = mCheckoutTransactionList!![i]
//                                break@loop
//                            }
//                        }
//                    }
//
//                    mOrderTotal = mCheckoutTransaction!!.mAmountRemaining
//
//                    val mOrderTotalString = String.format("%.2f", mOrderTotal)
//                    mBinding.editTextCardValue.setText(mOrderTotalString)
//                }
//            }
//        })

//        mCheckoutViewModel.getPaymentMethods().observe(viewLifecycleOwner, {
//            loop@ for (i in it.indices)
//                when (TYPE_CARD) {
//                    it[i].mPaymentMethodType -> {
//                        mCardPaymentMethod = it[i]
//                        break@loop
//                    }
//                }
//        })
//
//        mCheckoutViewModel.getTableGroupAndSeats(mTableNO, mGroupName).observe(viewLifecycleOwner, {
//            mLocalTableGroupModel = it
//        })

        mBinding.imageViewCancelTextCard.setOnClickListener { mBinding.editTextCardValue.setText("") }

        mBinding.buttonCardProceed.setOnClickListener {
            when {
                mBinding.editTextCardValue.text.toString().isNotEmpty() -> {

//                    val mAmount = BigDecimal(mBinding.editTextCardValue.text.toString())

                    sharedCheckoutModel.postCheckout.cardPayment = sharedCheckoutModel.postCheckout.netTotal

                    cartViewModel.checkoutOrder(sharedCheckoutModel.postCheckout, mTableID)
                }
                else -> CustomToast.makeText(requireActivity(), "Please Enter Amount", LENGTH_SHORT).show()
            }
        }

        cartViewModel.checkoutData.observe(viewLifecycleOwner) {event ->
            event?.getContentIfNotHandled()?.let {
                if (it.status) {
                    CustomToast.makeText(requireActivity(), "Order completed!", LENGTH_SHORT).show()
                    (requireParentFragment().requireParentFragment() as CheckoutDialog).dismissDialog()
                }
            }
        }

        //initial state
        mBinding.editTextCardValue.isEnabled = false
        mBinding.imageViewCancelTextCard.visibility = INVISIBLE

        mBinding.toggleSwitchCard.setOnToggleSwitchChangeListener { position, _ ->
            when (position) {
                0 -> {
                    mBinding.editTextCardValue.isEnabled = false
                    mBinding.imageViewCancelTextCard.visibility = INVISIBLE
                    val mOrderTotalString = String.format("%.2f", mOrderTotal)
                    mBinding.editTextCardValue.setText(mOrderTotalString)
                }
                else -> {
                    mBinding.editTextCardValue.isEnabled = true
                    mBinding.imageViewCancelTextCard.visibility = VISIBLE
                }
            }
        }
    }

    private var mEnteredAmount = BigDecimal(0)

    private fun payEnteredAmount() {

        val mPaymentTransactionList = ArrayList<PaymentTransaction>()
        mPaymentTransactionList.addAll(mCheckoutTransaction!!.mPaymentTransaction)

        mEnteredAmount = BigDecimal(mBinding.editTextCardValue.text.toString())
        mEnteredAmount = mEnteredAmount.setScale(2, RoundingMode.HALF_UP)

        mPaymentTransactionList.add(
            PaymentTransaction(
                "",
                "",
                mCardPaymentMethod!!.mPaymentMethodID,
                mCardPaymentMethod!!.mPaymentMethodName,
                mCardPaymentMethod!!.mPaymentMethodType,
                mEnteredAmount,
                BigDecimal(0),
                BigDecimal(0),
                "",
                "",
                "",
                "",
                "",
                ""
            )
        )

        //update values in checkoutTransaction
        mCheckoutTransaction!!.mAmountPaid = mCheckoutTransaction!!.mAmountPaid + mEnteredAmount
        mCheckoutTransaction!!.isFullPaid = mCheckoutTransaction!!.mAmountPaid >= mCheckoutTransaction!!.mOrderTotal
        mCheckoutTransaction!!.mAmountRemaining =
            mCheckoutTransaction!!.mOrderTotal - mCheckoutTransaction!!.mAmountPaid

        //update values in checkoutModel
        mCheckoutModel!!.mAmountPaid = mCheckoutModel!!.mAmountPaid + mEnteredAmount
        mCheckoutModel!!.mAmountRemaining = mCheckoutModel!!.mOrderTotal - mCheckoutModel!!.mAmountPaid

        //change seat status in table model
        if (mCheckoutTransaction!!.isFullPaid) {
            val mLocalSeatList = mLocalTableGroupModel!!.mSeatList

            for (i in 0 until mLocalSeatList!!.size)
                for (j in 0 until mCheckoutTransaction!!.mSeatList.size)
                    if (mLocalSeatList[i].mSeatNO == mCheckoutTransaction!!.mSeatList[j]) {
                        mLocalSeatList[i].isPaid = true
                        break
                    }

            mLocalTableGroupModel!!.mSeatList = mLocalSeatList

            mCheckoutViewModel.updateTableGroupAndSeats(mLocalTableGroupModel!!)
        }

        mCheckoutTransaction!!.mPaymentTransaction = mPaymentTransactionList

        updateCard()
    }

    private fun updateCard() {
        mCheckoutViewModel.updateCard(mCheckoutModel!!, requireActivity()).observe(viewLifecycleOwner,
            {
                when {
                    it != null ->
                        when {
                            it > -1 -> {
                                when {
                                    mProgressDialog != null -> mProgressDialog!!.dismiss()
                                }

                                log("Full Paid?: ${mCheckoutTransaction!!.isFullPaid}")
                                log("mAmountPaid?: ${mCheckoutTransaction!!.mAmountPaid}")
                                log("mOrderTotal?: ${mCheckoutTransaction!!.mOrderTotal}")

                                when {
                                    ChipDnaMobile.isInitialized() -> ChipDnaMobile.dispose(requestParameters)
                                }

                                when {
                                    mCheckoutTransaction!!.isFullPaid -> if (mSharedPrefs.getString(SELECTED_BILL_PRINTER_NAME, "")!!.isNotEmpty()) {
                                        EventBus.getDefault()
                                            .post(SeatPaymentCompleteEvent(true, mCheckoutRowID, javaClass.simpleName))
                                        val mShareBillPrintIntent =
                                            when {
                                                mSharedPrefs.getInt(AppConstants.SELECTED_BILL_PRINT_PAPER_SIZE, 0) == 50 -> Intent(requireActivity(), ShareBillPrint50Activity::class.java)
                                                else -> Intent(requireActivity(), ShareBillPrint80Activity::class.java)
                                            }

                                        mShareBillPrintIntent.putExtra(AppConstants.TABLE_NO, mTableNO)
                                        mShareBillPrintIntent.putExtra(AppConstants.GROUP_NAME, mGroupName)
                                        mShareBillPrintIntent.putExtra(AppConstants.ORDER_NO, mCheckoutModel!!.mCartNO)
                                        mShareBillPrintIntent.putExtra(AppConstants.API_CART_ID, mCheckoutModel!!.mCartID)
                                        mShareBillPrintIntent.putExtra(AppConstants.TABLE_ID, mCheckoutModel!!.mTableID)
                                        when {
                                            mTableNO != 100 -> mShareBillPrintIntent.putExtra(AppConstants.ORDER_TYPE, 1)
                                            else -> mShareBillPrintIntent.putExtra(AppConstants.ORDER_TYPE, 2)
                                        }
                                        startActivity(mShareBillPrintIntent)
                                    } else {
                                        CustomToast.makeText(requireActivity(), "Bill Printer not selected", LENGTH_SHORT)
                                        EventBus.getDefault()
                                            .post(SeatPaymentCompleteEvent(true, mCheckoutRowID, javaClass.simpleName))
                                    }
                                    else -> EventBus.getDefault().post(OnNavigationChangeEvent(AppConstants.DETAIL_NAVIGATION))
                                }
                            }
                            else -> {
                                if(mProgressDialog != null)
                                    mProgressDialog!!.dismiss()

                                mBinding.constraintLayoutNetworkError.visibility = VISIBLE
                                mBinding.constraintLayoutMain.visibility = GONE
                            }
                        }
                }
            })
    }

    private fun tryAgain() {
        mBinding.buttonTryAgain.setOnClickListener {
            showProgressDialog("Processing Payment", "Please Wait While The Payment is Being Processed", DIALOG_TYPE_OTHER)

            updateCard()

            mBinding.constraintLayoutNetworkError.visibility = GONE
            mBinding.constraintLayoutMain.visibility = VISIBLE
        }
    }

    private fun showProgressDialog(title: String, message: String, mDialogType: Int) {
        mProgressDialog = CustomProgressDialog.newInstance(title, message, mDialogType)
        mProgressDialog!!.show(childFragmentManager, AppConstants.CUSTOM_PROGRESS_DIALOG_FRAGMENT)
        mProgressDialog!!.isCancelable = false
    }

    @Subscribe
    fun onReceiptPrinted(mEvent: ReceiptPrintDoneEvent) {
        payEnteredAmount()
    }

    fun onTextChanged(mText: CharSequence) {
        if (mBinding.editTextCardValue.text.toString().isNotEmpty()) {
            mEnteredAmount = if (mBinding.editTextCardValue.text.toString() == ".") {
                mBinding.editTextCardValue.setText("0.")
                mBinding.editTextCardValue.setSelection(2)
                BigDecimal(0)
            } else {
                BigDecimal(mText.toString().toDouble())
            }


            mEnteredAmount = mEnteredAmount.setScale(2, RoundingMode.HALF_UP)
        }
    }


    override fun onResume() {
        super.onResume()

        // Initialise ChipDna Mobile before starting to interacting with the API. You can check if ChipDna Mobile has been initialised by using isInitialised()
        // It's possible that android may have cleaned up resources while the application has been in the background and needs to be re-initialised.
        mExecutors = AppExecutors.getInstance()
//        initChipDnaMobile()

    }

    @Synchronized
    private fun initChipDnaMobile() {
        if (!ChipDnaMobile.isInitialized()) {
            initialiseListeners()
        } else {
            if (mProgressDialog != null)
                mProgressDialog!!.dismiss()
        }
    }

    private var mPinPadListDialog: Dialog? = null
    private var mPinPadAdapter: PinPadRecyclerAdapter? = null
    private val mPinPadList = ArrayList<PinPadModel>()

    /*private fun deviceSelection() {
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

        mPinPadListDialog!!.window!!.setLayout(MATCH_PARENT, WRAP_CONTENT)
        mPinPadListDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        mPinPadListDialog!!.show()
        mPinPadListDialog!!.setCancelable(false)

        mPinPadDialogBinding.buttonCardCancel.setOnClickListener {
            mPinPadListDialog!!.dismiss()
        }
    }*/

    /*@Subscribe
    fun onPinPadClicked(mPinPad: PinPadModel) {
        val requestParameters = Parameters()
        requestParameters.add(ParameterKeys.PinPadName, mPinPad.pinPadName)
        requestParameters.add(ParameterKeys.PinPadConnectionType, mPinPad.connectionType)
        ChipDnaMobile.getInstance().setProperties(requestParameters)
        mPinPadListDialog!!.dismiss()
        //progress dialog
        showProgressDialog("Processing Payment", "Connecting To Payment Terminal", DIALOG_TYPE_OTHER)
        connectToPinPad()
    }*/

    private fun connectToPinPad() {
        val response = ChipDnaMobile.getInstance().connectAndConfigure(ChipDnaMobile.getInstance().getStatus(null))
        if (response.containsKey(ParameterKeys.Result) && response.getValue(ParameterKeys.Result) == ParameterValues.FALSE) {
            onConnectionFailed()
            CustomToast.makeText(requireActivity(), "Failed to connect with PinPad", LENGTH_SHORT).show()
        }
    }

    private var mExecutors: AppExecutors? = null

    private fun initialiseListeners() {

        mExecutors!!.diskIO().execute {

            val requestParameters = Parameters()
            requestParameters.add(ParameterKeys.Password, "0000")
            val response = ChipDnaMobile.initialize(activity, requestParameters)

            if (response.containsKey(ParameterKeys.Result) && response.getValue(ParameterKeys.Result).equals(
                    "True",
                    ignoreCase = true
                )
            ) {
                // ChipDna Mobile has been successfully initialised.
                log("ChipDna Mobile initialised")
                log("Version: " + ChipDnaMobile.getSoftwareVersion() + ", Name: " + ChipDnaMobile.getSoftwareVersionName())
                // We can start setting our ChipDna Mobile credentials.
                registerListeners()
                setCredentials()
            } else {
                // The password is incorrect, ChipDnaMobile cannot initialise
                log("Failed to initialise ChipDna Mobile")
                if (response.getValue(ParameterKeys.RemainingAttempts).equals("0", ignoreCase = true)) {
                    // If all password attempts have been used, the database is deleted and a new password is required.
                    log("Reached password attempt limit")
                } else {
                    log("Password attempts remaining: " + response.getValue(ParameterKeys.RemainingAttempts))
                }

            }
        }
    }

    private fun setCredentials() {
        // Credentials are set in ChipDnaMobile Status object. It's recommended that you fetch fresh ChipDnaMobile Status object each time you wish to make changes.
        // This ensures the set of properties used is always up to date with the version of properties in ChipDnaMobile
        val statusParameters = ChipDnaMobile.getInstance().getStatus(null)

        // Entering this method means we have successfully initialised ChipDna Mobile and start setting our ChipDna Mobile credentials.
        log("Using TID: $TID, TK: $TK")

        // Credentials are returned to ChipDnaMobile as a set of Parameters
        val requestParameters = Parameters()

        // The credentials consist of a terminal ID and transaction key. This demo application has these hard coded.
        // If you don't have a terminal ID or transaction key to use you can sign up for a test WebMIS account at
        // https://testwebmis.creditcall.com
        requestParameters.add(ParameterKeys.TerminalId, TID)
        requestParameters.add(ParameterKeys.TransactionKey, TK)

        // Set ChipDna Mobile to test mode. This means ChipDna Mobile is running in it's test environment, can configure test devices and perform test transaction.
        // Use test mode while developing your application.
        requestParameters.add(ParameterKeys.Environment, ParameterValues.TestEnvironment)
//        requestParameters.add(ParameterKeys.Environment, ParameterValues.LiveEnvironment)

        // Set the Application Identifier value. This is used by the TMS platform to configure TMS properties specifically for an integrating application.
        requestParameters.add(ParameterKeys.ApplicationIdentifier,
            APP_ID.uppercase(Locale.getDefault())
        )

        // Once all changes have been made a call to .setProperties() is required in order for the changes to take effect.
        // Parameters are passed within this method and added to the ChipDna Mobile status object.
        ChipDnaMobile.getInstance().setProperties(requestParameters)

        requireActivity().runOnUiThread {
            mBinding.buttonCardProceed.isEnabled = true
            log("progress should dismiss")
            if (mProgressDialog!!.isVisible)
                mProgressDialog!!.dismiss()
            // Check if PINPad has already been selected. If so we can enable the connectToPinPadButton.
            if (statusParameters.getValue(ParameterKeys.PinPadName) != null && statusParameters.getValue(ParameterKeys.PinPadName).isNotEmpty()) {
                mBinding.buttonCardProceed.isEnabled = true
            }
        }
    }

    private fun registerListeners() {
        ChipDnaMobile.getInstance().addConnectAndConfigureFinishedListener(ConnectAndConfigureFinishedListener())
        ChipDnaMobile.getInstance().addConfigurationUpdateListener(ConfigurationUpdateListener())
        ChipDnaMobile.getInstance().addDeviceUpdateListener(DeviceUpdateListener())
        ChipDnaMobile.getInstance().addCardDetailsListener(CardDetailsListener())

        val transactionListener = TransactionListener()
        ChipDnaMobile.getInstance().addTransactionUpdateListener(transactionListener)
        ChipDnaMobile.getInstance().addTransactionFinishedListener(transactionListener)
        ChipDnaMobile.getInstance().addDeferredAuthorizationListener(transactionListener)
        ChipDnaMobile.getInstance().addSignatureVerificationListener(transactionListener)
        ChipDnaMobile.getInstance().addVoiceReferralListener(transactionListener)
        ChipDnaMobile.getInstance().addPartialApprovalListener(transactionListener)
        ChipDnaMobile.getInstance().addForceAcceptanceListener(transactionListener)
        ChipDnaMobile.getInstance().addVerifyIdListener(transactionListener)
        ChipDnaMobile.getInstance().addTmsUpdateListener(tmsUpdateListener)
        ChipDnaMobile.getInstance().addSignatureCaptureListener(transactionListener)
        ChipDnaMobile.getInstance().addProcessReceiptFinishedListener(ProcessReceiptListener())
    }

    private inner class ConnectAndConfigureFinishedListener : IConnectAndConfigureFinishedListener {
        override fun onConnectAndConfigureFinished(parameters: Parameters) {
            if (parameters.containsKey(ParameterKeys.Result) && parameters.getValue(ParameterKeys.Result).equals(
                    "True",
                    ignoreCase = true
                )
            ) {
                // Configuration has completed successfully and we are ready to perform transactions.
                log("Ready for transactions")
                EventBus.getDefault().post(CustomProgressTextEvent("Ready for transactions"))

                try {
                    Thread.sleep(1000)
                    log("After 1000")
                    if (mProgressDialog!!.isVisible)
                        mProgressDialog!!.dismiss()
                    //submitTransactionCommandButtonPushed()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

            } else {
                log("Failed to initialise PinPad")
                log(parameters.getValue(ParameterKeys.Error))
                requireActivity().runOnUiThread {
                    if (mProgressDialog!!.isVisible)
                        mProgressDialog!!.dismiss()

                    CustomToast.makeText(
                        requireActivity(),
                        "Could not connect to the device, please make sure device is turn on and try again",
                        LENGTH_SHORT
                    ).show()

                }
            }
        }
    }

    private inner class ConfigurationUpdateListener : IConfigurationUpdateListener {
        override fun onConfigurationUpdateListener(parameters: Parameters) {
            log(parameters.getValue(ParameterKeys.ConfigurationUpdate))
            requireActivity().runOnUiThread {
                EventBus.getDefault()
                    .post(CustomProgressTextEvent(parameters.getValue(ParameterKeys.ConfigurationUpdate)))
            }

        }
    }

    private inner class DeviceUpdateListener : IDeviceUpdateListener {
        override fun onDeviceUpdate(parameters: Parameters) {
            log(parameters.getValue(ParameterKeys.DeviceStatusUpdate))
            try {
                requireActivity().runOnUiThread {
                    EventBus.getDefault()
                        .post(CustomProgressTextEvent(parameters.getValue(ParameterKeys.DeviceStatusUpdate)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    // This is not used in this version of the demo app.
    private inner class CardDetailsListener : ICardDetailsListener {
        override fun onCardDetails(parameters: Parameters) {}
    }

    private inner class TransactionListener : ITransactionUpdateListener, ITransactionFinishedListener,
        IDeferredAuthorizationListener, ISignatureVerificationListener, IVoiceReferralListener,
        IPartialApprovalListener, IForceAcceptanceListener, IVerifyIdListener, ISignatureCaptureListener {
        override fun onSignatureCapture(p0: Parameters?) {
        }

        override fun onTransactionUpdateListener(parameters: Parameters) {
            log("Update " + parameters.getValue(ParameterKeys.TransactionUpdate))
            requireActivity().runOnUiThread {
                EventBus.getDefault()
                    .post(CustomProgressTextEvent(parameters.getValue(ParameterKeys.TransactionUpdate)))
            }
        }

        override fun onTransactionFinishedListener(parameters: Parameters) {
            log("onTransactionFinishedListener =>", parameters)

            submitTransactionCommandButtonPushed()
        }

        override fun onSignatureVerification(parameters: Parameters) {
            log("Signature Check Required")

            if (parameters.getValue(ParameterKeys.ResponseRequired) != ParameterValues.TRUE) {
                // Signature handled on PinPad. No call to ChipDna Mobile required.
                return
            }

            val operatorPinRequired = parameters.getValue(ParameterKeys.OperatorPinRequired) == ParameterValues.TRUE
            val receiptDataXml = parameters.getValue(ParameterKeys.ReceiptData)

            Thread { requestSignatureCheck(operatorPinRequired) }.start()
        }

        override fun onVoiceReferral(parameters: Parameters) {
            log("Voice Referral Check Required")

            if (parameters.getValue(ParameterKeys.ResponseRequired) != ParameterValues.TRUE) {
                // Voice referral handled on PINpad. No call to ChipDna Mobile required.
                return
            }

            val phoneNumber = parameters.getValue(ParameterKeys.ReferralNumber)
            val operatorPinRequired = parameters.getValue(ParameterKeys.OperatorPinRequired) == ParameterValues.TRUE

            Thread { requestVoiceReferral(phoneNumber, operatorPinRequired) }.start()
        }

        /*
            Other ChipDna Mobile Callbacks, not required in this demo.
            You may need to implement some of these depending on what your terminal supports.
          */

        override fun onVerifyId(parameters: Parameters) {

        }

        override fun onDeferredAuthorizationListener(parameters: Parameters) {

        }

        override fun onForceAcceptance(parameters: Parameters) {

        }

        override fun onPartialApproval(parameters: Parameters) {

        }
    }

    private val tmsUpdateListener = ITmsUpdateListener {}

    private inner class ProcessReceiptListener : IProcessReceiptFinishedListener {
        override fun onProcessReceiptFinishedListener(parameters: Parameters) {

        }
    }

    internal enum class TransactionCommand {
        Authorisation, Confirm, Void, TransactionInfo
    }

    companion object {
        private val TRANSACTION_COMMAND_OPTIONS = arrayOf(
            TransactionCommand.Authorisation,
            TransactionCommand.Confirm,
            TransactionCommand.Void,
            TransactionCommand.TransactionInfo
        )

        private const val CURRENCY = "GBP"
    }


    private var currentTransactionCommandOption = TRANSACTION_COMMAND_OPTIONS[0]


    private var mTransactionReference = ""
    private var requestParameters: Parameters? = null

    private fun submitTransactionCommandButtonPushed() {
        log("Starting: $currentTransactionCommandOption")
        EventBus.getDefault().post(CustomProgressTextEvent("Starting: $currentTransactionCommandOption"))

        val mWithoutRound = (mEnteredAmount * BigDecimal(100)).toInt()
        val mWithRound = (mEnteredAmount * BigDecimal(100)).toInt()

        val mTransactionAmount: Int
        mTransactionAmount = (if (mWithRound > mWithoutRound) mWithRound else mWithoutRound)

        // Request Parameters are used as to communicate - with ChipDna Mobile - the parameters needed to complete a given command.
        // They are sent with the method call to ChipDna Mobile.
        requestParameters = Parameters()

        when (currentTransactionCommandOption) {
            TransactionCommand.Authorisation -> {

                // The following parameters are essential for the completion of a transaction.
                // In the current example the parameters are initialised as constants. They will need to be dynamically collected and initialised.
                requestParameters!!.add(ParameterKeys.Amount, mTransactionAmount)
                requestParameters!!.add(ParameterKeys.AmountType, ParameterValues.AmountTypeActual)
                requestParameters!!.add(ParameterKeys.Currency, CURRENCY)

                // The user reference is needed to be to able to access the transaction on WEBMis.
                // The reference should be unique to a transaction, so it is suggested that the reference is generated, similar to the example below.
                mTransactionReference =
                    String.format("CDM-%s", SimpleDateFormat("yy-MM-dd-HH.mm.ss", Locale.ENGLISH).format(Date()))
                requestParameters!!.add(ParameterKeys.UserReference, mTransactionReference)

                requestParameters!!.add(ParameterKeys.TransactionType, ParameterValues.Sale)
                requestParameters!!.add(ParameterKeys.PaymentMethod, ParameterValues.Card)
                doAuthoriseTransaction(requestParameters!!)

                currentTransactionCommandOption = TRANSACTION_COMMAND_OPTIONS[1]
            }
            TransactionCommand.Confirm -> {
                // The following parameters are used to confirm an authorised transaction.
                // The user reference is used to reference the transaction stored on WEBMis.
                requestParameters!!.add(ParameterKeys.UserReference, mTransactionReference)
                requestParameters!!.add(ParameterKeys.Amount, mTransactionAmount)
                requestParameters!!.add(ParameterKeys.TipAmount, null)
                requestParameters!!.add(ParameterKeys.CloseTransaction, ParameterValues.TRUE)
                doConfirmTransaction(requestParameters!!)
                currentTransactionCommandOption = TRANSACTION_COMMAND_OPTIONS[0]
            }
            TransactionCommand.Void -> {
                // The following parameters are used to void an authorised transaction.
                // The user reference is used to reference the transaction stored on WEBMis.
                requestParameters!!.add(ParameterKeys.UserReference, mTransactionReference)
                doVoidTransaction(requestParameters!!)
            }
            TransactionCommand.TransactionInfo -> {
                // The following parameters are used to display information about a transaction.
                // The user reference is used to reference the transaction stored on WEBMis.
                requestParameters!!.add(ParameterKeys.UserReference, mTransactionReference)
                doGetTransactionInformation(requestParameters!!)
            }
        }
    }

    private fun doAuthoriseTransaction(authorisationParameters: Parameters) {
        log("Starting Transaction for amount: $mEnteredAmount")
        requireActivity().runOnUiThread {
            EventBus.getDefault().post(CustomProgressTextEvent("Starting Transaction for amount: $mEnteredAmount"))
        }


        // Use an instance of ChipDnaMobile to begin startTransaction.
        val response = ChipDnaMobile.getInstance().startTransaction(authorisationParameters)

        if (response.containsKey(ParameterKeys.Result) && response.getValue(ParameterKeys.Result) == ParameterValues.FALSE) {
            log("Error: " + response.getValue(ParameterKeys.Error))
        }

        log("doAuthoriseTransaction", response)
    }

    private fun doConfirmTransaction(confirmParameters: Parameters) {
        log("Confirm Transaction")

        mExecutors!!.diskIO().execute {
            val response = ChipDnaMobile.getInstance().confirmTransaction(confirmParameters)
            log("Confirm Transaction Response", response)
            when {
                response.getValue(ParameterKeys.TransactionResult) != null && response.getValue(ParameterKeys.TransactionResult).equals(
                    "APPROVED",
                    ignoreCase = true
                ) -> requireActivity().runOnUiThread {
                    EventBus.getDefault().post(CustomProgressTextEvent("APPROVED"))
                    connect(response.getValue(ParameterKeys.PreformattedCustomerReceipt))

                }
                response.getValue(ParameterKeys.TransactionResult) != null && response.getValue(ParameterKeys.TransactionResult).equals(
                    "RETRY_REQUEST",
                    ignoreCase = true
                ) -> onRetry()
                else -> onDecline()
            }
        }
    }

    private fun doVoidTransaction(voidParameters: Parameters) {
        log("Void Transaction")

        mExecutors!!.diskIO().execute {
            val response = ChipDnaMobile.getInstance().voidTransaction(voidParameters)
            log("Confirm Transaction Response", response)
        }
    }

    private fun doGetTransactionInformation(transactionInfoParameters: Parameters) {
        log("Get Transaction Info")
        mExecutors!!.diskIO().execute {
            val response = ChipDnaMobile.getInstance().getTransactionInformation(transactionInfoParameters)
            log("Confirm Transaction Response", response)
        }
    }

    private inner class AvailablePinPadsListener : IAvailablePinPadsListener {

        @SuppressLint("StaticFieldLeak")
        override fun onAvailablePinPads(parameters: Parameters) {

            val availablePinPadsXml = parameters.getValue(ParameterKeys.AvailablePinPads)

            object : AsyncTask<String, Void, List<PinPadModel>>() {
                override fun doInBackground(vararg params: String): List<PinPadModel> {
                    val availablePinPadsList = ArrayList<PinPadModel>()
                    try {
                        val availablePinPadsHashMap = ChipDnaMobileSerializer.deserializeAvailablePinPads(params[0])

                        for (connectionType in availablePinPadsHashMap.keys) {
                            for (mPinPad in Objects.requireNonNull<ArrayList<String>>(availablePinPadsHashMap[connectionType])) {
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

    private fun requestSignatureCheck(operatorPinRequired: Boolean) {
        requireActivity().runOnUiThread {

            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Please check signature.")
            val input = EditText(activity)

            // If operator PIN is required, Add an extra text field to allow the user to enter the operator PIN.
            if (operatorPinRequired) {
                input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                input.hint = "Operator PIN"
                builder.setView(input)
            }

            builder.setPositiveButton("Accept") { _, _ ->
                val inputStr = input.text.toString()
                when {
                    operatorPinRequired -> // Check we have an operator PIN
                        if (inputStr.isEmpty()) {
                            // No operator PIN found. Send request again.
                            log("Operator PIN required but not entered")
                            requestSignatureCheck(true)
                            return@setPositiveButton
                        }
                }

                val approveSignatureParameters = Parameters()
                approveSignatureParameters.add(ParameterKeys.Result, ParameterValues.TRUE)
                approveSignatureParameters.add(ParameterKeys.OperatorPin, inputStr)

                val response = ChipDnaMobile.getInstance().continueSignatureVerification(approveSignatureParameters)

                if (!response.containsKey(ParameterKeys.Result) || response.getValue(ParameterKeys.Result) == ParameterValues.FALSE) {
                    requestSignatureCheck(operatorPinRequired)
                }

            }

            builder.setNegativeButton("Terminate", getTerminateOnClickListener())

            builder.setNeutralButton("Decline") { _, _ ->
                // The merchant wishes to decline the transaction.
                // No operator PIN is required when declining the transaction.
                val declineParameters = Parameters()
                declineParameters.add(ParameterKeys.Result, ParameterValues.FALSE)
                log("Signature Check Declined")
                val response = ChipDnaMobile.getInstance().continueSignatureVerification(declineParameters)

                if (!response.containsKey(ParameterKeys.Result) || response.getValue(ParameterKeys.Result) == ParameterValues.FALSE) {
                    requestSignatureCheck(operatorPinRequired)
                }
            }

            builder.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun requestVoiceReferral(phoneNumber: String?, operatorPinRequired: Boolean) {
        log("Requesting voice referral")
        requireActivity().runOnUiThread {
            val builder = AlertDialog.Builder(activity)
            val contentView = LinearLayout(activity)
            contentView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            contentView.orientation = LinearLayout.VERTICAL

            builder.setTitle("Voice Referral")
            val requestText = TextView(activity)

            // If returned display phone number for the merchant to call.
            if (phoneNumber != null && phoneNumber.isNotEmpty()) {
                requestText.text = "Please ring your bank: $phoneNumber"
            } else {
                requestText.text = "Please ring your bank."
            }
            contentView.addView(requestText)

            val operatorPinInput = EditText(activity)
            operatorPinInput.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            operatorPinInput.hint = "Operator PIN"

            // If required add an extra text field to allow the user to enter their operator PIN.
            if (operatorPinRequired) {
                val operatorLabel = TextView(activity)
                operatorLabel.text = "Operator PIN:"

                contentView.addView(operatorLabel)
                contentView.addView(operatorPinInput)
            }

            // Add a text field for the merchant to enter the authorization code given to them by the bank.
            val authCodeInput = EditText(activity)
            authCodeInput.inputType = InputType.TYPE_CLASS_TEXT
            authCodeInput.hint = "Auth Code"
            val authCodeLabel = TextView(activity)
            authCodeLabel.text = "Authorization Code:"

            contentView.addView(authCodeLabel)
            contentView.addView(authCodeInput)

            builder.setView(contentView)

            builder.setPositiveButton("Accept") { _, _ ->
                // The bank has given the authorization code and the merchant can proceed with the transaction.
                if (operatorPinRequired) {
                    // If required check we have been given an operator PIN.
                    val inputStr = operatorPinInput.text.toString()
                    if (inputStr.isEmpty()) {
                        log("Operator PIN required but not entered")
                        // If no operator PIN, try again.
                        requestVoiceReferral(phoneNumber, true)
                        return@setPositiveButton
                    }
                }

                val authCodeStr = authCodeInput.text.toString()
                val operatorPinStr = operatorPinInput.text.toString()

                // Check we have an authorization code.
                if (authCodeStr.isEmpty()) {
                    log("Authorization code required and not entered")
                    // If not try again.
                    requestVoiceReferral(phoneNumber, operatorPinRequired)
                    return@setPositiveButton
                }

                val voiceReferralParameters = Parameters()
                voiceReferralParameters.add(ParameterKeys.Result, ParameterValues.TRUE)
                voiceReferralParameters.add(ParameterKeys.AuthCode, authCodeStr)
                voiceReferralParameters.add(ParameterKeys.OperatorPin, operatorPinStr)

                val response = ChipDnaMobile.getInstance().continueVoiceReferral(voiceReferralParameters)

                if (!response.containsKey(ParameterKeys.Result) || response.getValue(ParameterKeys.Result) == ParameterValues.FALSE) {
                    requestVoiceReferral(phoneNumber, operatorPinRequired)
                }
            }


            builder.setNegativeButton("Terminate", getTerminateOnClickListener())

            // The bank has instructed the merchant to decline the transaction. No authorization code or operator PIN is necessary.
            builder.setNeutralButton("Decline") { _, _ ->

                val voiceReferralParameters = Parameters()
                voiceReferralParameters.add(ParameterKeys.Result, ParameterValues.FALSE)

                log("Voice Referral Declined")
                val response = ChipDnaMobile.getInstance().continueVoiceReferral(voiceReferralParameters)

                if (response.containsKey(ParameterKeys.Result) && response.getValue(ParameterKeys.Result) == ParameterValues.FALSE) {
                    log("Error: " + response.getValue(ParameterKeys.Error))
                }

            }

            builder.show()
        }
    }

    private fun getTerminateOnClickListener(): DialogInterface.OnClickListener {
        return DialogInterface.OnClickListener { _, _ ->
            log("ChipDnaMobile.terminateTransaction")
            val response = ChipDnaMobile.getInstance().terminateTransaction(Parameters())

            if (response.containsKey(ParameterKeys.Result) && response.getValue(ParameterKeys.Result) == ParameterValues.FALSE) {
                log("Error: " + response.getValue(ParameterKeys.Error))
            }
            log("Signature Check Terminated")
        }
    }

    private fun onDecline() {
        try {
            requireActivity().runOnUiThread {
                CustomToast.makeText(requireActivity(), "Transaction Declined", LENGTH_SHORT)
                    .show()
                //completed: hide loader
                if (mProgressDialog != null)
                    mProgressDialog!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun onRetry() {
        try {
            requireActivity().runOnUiThread {
                CustomToast.makeText(
                    requireActivity(),
                    "Transaction Failed, Please Retry",
                    LENGTH_SHORT
                ).show()
                //completed: hide loader
                if (mProgressDialog != null)
                    mProgressDialog!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onDestroy() {
        log("onDestroy")
        if (ChipDnaMobile.isInitialized())
            ChipDnaMobile.dispose(requestParameters)
        super.onDestroy()
    }

    private fun onConnectionFailed() {
        try {
            requireActivity().runOnUiThread {
                CustomToast.makeText(
                    requireActivity(),
                    "Connection Failed, Please Retry",
                    LENGTH_SHORT
                ).show()
                //completed: hide loader
                if (mProgressDialog != null)
                    mProgressDialog!!.dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private var mmOutputStream: OutputStream? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mmSocket: BluetoothSocket? = null
    private var mmDevice: BluetoothDevice? = null
    private var mBluetoothDeviceList = java.util.ArrayList<BluetoothDevice>()

    fun connect(mReceipt: String) {
        var printerDeviceCheck = false
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        when {
            mBluetoothAdapter == null && !(requireActivity().isFinishing) -> CustomToast.makeText(
                requireActivity(),
                "No bluetooth device paired",
                LENGTH_SHORT
            ).show()
        }

        if (!mBluetoothAdapter!!.isEnabled) {
            val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetooth, 0)
        }

        val pairedDevices = mBluetoothAdapter!!.bondedDevices

        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                if (device.name.equals(
                        mSharedPrefs.getString(SELECTED_BILL_PRINTER_NAME, "")!!,
                        ignoreCase = true
                    )
                ) {
                    printerDeviceCheck = true
                    mmDevice = device
                }
                mBluetoothDeviceList.add(device)

            }
        }

        if (printerDeviceCheck) {
            openBT()
            beginListenForData(mReceipt)

        } else {
            if (!(requireActivity().isFinishing))
                CustomToast.makeText(requireActivity(), "Printer not found", LENGTH_SHORT).show()
        }

    }

    private fun openBT() {
        try {

            // Standard SerialPortService ID
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            mmSocket = mmDevice!!.createRfcommSocketToServiceRecord(uuid)
            mmSocket!!.connect()
            mmOutputStream = mmSocket!!.outputStream


        } catch (e: Exception) {
            e.printStackTrace()
            EventBus.getDefault().post(ReceiptPrintDoneEvent(false))
        }

    }

    private fun beginListenForData(mReceipt: String) {
        wakeUpPrinter()
        initPrinter()
        printText(mReceipt)
    }

    private fun printText(mReceipt: String) {

        try {
            if (mmOutputStream != null) {
                mmOutputStream!!.write(ESCUtil.alignCenter())
                mmOutputStream!!.write(mReceipt.toByteArray())
                mmOutputStream!!.write("\n\n\n".toByteArray())
                mmOutputStream!!.write(ESCUtil.cutter())

                EventBus.getDefault().post(ReceiptPrintDoneEvent(true))
            }
        } catch (e: IOException) {
            e.printStackTrace()
            EventBus.getDefault().post(ReceiptPrintDoneEvent(false))
        } finally {
            if (mmOutputStream != null)
                mmOutputStream!!.close()
            if (mmSocket != null)
                mmSocket!!.close()

        }

    }

    private fun wakeUpPrinter() {
        val b = ByteArray(3)

        try {
            if (mmOutputStream != null) {
                mmOutputStream!!.write(b)
            }

            Thread.sleep(100L)
        } catch (var2: Exception) {
            var2.printStackTrace()
        }

    }

    private fun initPrinter() {
        val combyte = byteArrayOf(27.toByte(), 64.toByte())

        try {
            if (mmOutputStream != null) {
                mmOutputStream!!.write(combyte)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private val df = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.ENGLISH)
    private val logs = ArrayList<String>()
    private val MAX_LOG = 100
    private val LoggingLock = Any()

    // Logging method which takes the string to be logged and display it in the logger text view.
    private fun log(toLog: String) {
        synchronized(LoggingLock) {
            if (logs.size == MAX_LOG) {
                logs.removeAt(0)
            }

            logs.add(String.format("%s: %s\n", df.format(Date()), toLog))
            Log.e(javaClass.simpleName, toLog)

            val sb = StringBuilder()
            for (log in logs) {
                sb.append(String.format("%s", log))
            }
            val logStr = sb.toString()
            Log.e(javaClass.simpleName, logStr)
        }
    }

    private fun log(title: String, parameters: Parameters?) {
        val formattedLogBuilder = StringBuilder()
        formattedLogBuilder.append(title)

        if (parameters != null) {
            for (parameter in parameters.toList()) {
                formattedLogBuilder.append(String.format("\t[%s]\n", parameter))
            }
        }

        log(formattedLogBuilder.toString())
    }

    @Subscribe
    fun openPaymentMethodFragment(mEvent: OpenPaymentMethodEvent) {
        if (mEvent.mResult) {
            findNavController().popBackStack()
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
