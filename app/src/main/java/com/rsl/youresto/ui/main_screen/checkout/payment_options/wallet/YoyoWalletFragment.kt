package com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet


import android.annotation.SuppressLint
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
import androidx.navigation.fragment.findNavController
import com.google.zxing.integration.android.IntentIntegrator
import com.rsl.youresto.R
import com.rsl.youresto.data.checkout.model.CheckoutModel
import com.rsl.youresto.data.checkout.model.CheckoutTransaction
import com.rsl.youresto.data.checkout.model.PaymentTransaction
import com.rsl.youresto.data.database_download.models.PaymentMethodModel
import com.rsl.youresto.data.tables.models.LocalTableGroupModel
import com.rsl.youresto.databinding.FragmentYoyoWalletBinding
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModelFactory
import com.rsl.youresto.ui.main_screen.checkout.bill_print.ShareBillPrint50Activity
import com.rsl.youresto.ui.main_screen.checkout.bill_print.ShareBillPrint80Activity
import com.rsl.youresto.ui.main_screen.checkout.events.OnNavigationChangeEvent
import com.rsl.youresto.ui.main_screen.checkout.events.OpenPaymentMethodEvent
import com.rsl.youresto.ui.main_screen.checkout.events.SeatPaymentCompleteEvent
import com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.model.YoyoPaymentAuthModel
import com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.model.YoyoResponseModel
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.DIALOG_TYPE_OTHER
import com.rsl.youresto.utils.AppConstants.SEAT_SELECTION_ENABLED
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppConstants.TYPE_WALLET
import com.rsl.youresto.utils.AppConstants.YOYO_WALLET
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.custom_dialog.AlertDialogEvent
import com.rsl.youresto.utils.custom_dialog.CustomAlertDialogFragment
import com.rsl.youresto.utils.custom_dialog.CustomProgressDialog
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("LogNotTimber")
class YoyoWalletFragment : Fragment() {

    private lateinit var mBinding: FragmentYoyoWalletBinding
    private lateinit var mCheckoutViewModel: CheckoutViewModel
    private var mCheckoutRowID = 0
    private lateinit var mSharedPrefs: SharedPreferences
    private var mTableNO = 0
    private var mGroupName = ""
    private var mSelectedLocationType: Int? = null

    private var mCheckoutTransactionList: ArrayList<CheckoutTransaction>? = null
    private var mCheckoutTransaction: CheckoutTransaction? = null
    private var mCheckoutModel: CheckoutModel? = null
    private var mLocalTableGroupModel: LocalTableGroupModel? = null
    private var mWalletPaymentMethod: PaymentMethodModel? = null
    private var mOrderTotal = BigDecimal(0)

    private var qrScan: IntentIntegrator? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        e(javaClass.simpleName, "onCreate Called")

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_yoyo_wallet, container, false)
        val mView = mBinding.root

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        mSelectedLocationType = mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE, 0)

        if (mSelectedLocationType == SERVICE_DINE_IN) {
            mTableNO = mSharedPrefs.getInt(AppConstants.SELECTED_TABLE_NO, 0)
            mGroupName = mSharedPrefs.getString(AppConstants.GROUP_NAME, "")!!
        } else if (mSelectedLocationType == AppConstants.SERVICE_QUICK_SERVICE) {
            mTableNO = mSharedPrefs.getInt(AppConstants.QUICK_SERVICE_TABLE_NO, 0)
            mGroupName = "Q"
        }

        val checkoutFactory: CheckoutViewModelFactory = InjectorUtils.provideCheckoutViewModelFactory(requireActivity())
        mCheckoutViewModel = ViewModelProviders.of(this, checkoutFactory).get(CheckoutViewModel::class.java)

        mCheckoutRowID = mSharedPrefs.getInt(AppConstants.CHECKOUT_ROW_ID, 0)

        qrScan = IntentIntegrator(requireActivity())

        setViews()
        getTax()
        tryAgain()

        return mView
    }

    @Subscribe
    fun onQRCodeScanned(mEvent: QRCodeScannedEvent) {
        if (mEvent.mResult) {
            mQRCode = mEvent.mQRCode
            callPaymentAuthYoyoApi()
        } else {
            val mCustomDialogFragment = CustomAlertDialogFragment.newInstance(
                4, javaClass.simpleName, R.drawable.ic_delete_forever_primary_36dp,
                "Error", "No Result Found",
                "Okay", "", R.drawable.ic_check_black_24dp, 0
            )
            mCustomDialogFragment.show(childFragmentManager, AppConstants.CUSTOM_DIALOG_FRAGMENT)
        }
    }

    private fun setViews() {
        mCheckoutViewModel.getCheckoutDataByRowID(mCheckoutRowID).observe(viewLifecycleOwner, {
            when {
                it != null -> {
                    mCheckoutModel = it
                    mCheckoutTransactionList = mCheckoutModel!!.mCheckoutTransaction

                    loop@ for (i in 0 until mCheckoutTransactionList!!.size) {
                        when {
                            mCheckoutTransactionList!![i].isSelected && !mCheckoutTransactionList!![i].isFullPaid -> {
                                mCheckoutTransaction = mCheckoutTransactionList!![i]
                                break@loop
                            }
                        }
                    }

                    mOrderTotal = mCheckoutTransaction!!.mAmountRemaining.setScale(2, RoundingMode.HALF_UP)

                    val mOrderTotalString = String.format("%.2f", mOrderTotal)
                    mBinding.editTextWalletValue.setText(mOrderTotalString)
                    mBinding.editTextWalletValue.setSelection(mOrderTotalString.length)
                }
            }
        })

        mCheckoutViewModel.getPaymentMethods().observe(viewLifecycleOwner, {
            loop@ for (i in it.indices)
                when (TYPE_WALLET) {
                    it[i].mPaymentMethodType -> {
                        mWalletPaymentMethod = it[i]
                        break@loop
                    }
                }
        })

        mCheckoutViewModel.getTableGroupAndSeats(mTableNO, mGroupName).observe(viewLifecycleOwner, {
            mLocalTableGroupModel = it
        })

        mBinding.imageViewCancelText.setOnClickListener { mBinding.editTextWalletValue.setText("") }

        mBinding.buttonStartScanning.setOnClickListener {
            mCheckoutViewModel.checkInternet(requireActivity()).observe(viewLifecycleOwner, { bool ->
                when {
                    bool ->
                        when {
                            mBinding.editTextWalletValue.text.toString().isNotEmpty() -> qrScan!!.initiateScan()
                            else -> CustomToast.makeText(requireActivity(), "Please Enter Amount", Toast.LENGTH_SHORT).show()
                        }
                    else -> CustomToast.makeText(requireActivity(), "Internet not available", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun getDateTime(): String {
        val today = Calendar.getInstance()
        val mDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        var mDateTime = mDateFormat.format(today.time)
        mDateTime = mDateTime.replace(" ", "T")
        mDateTime += "Z"
        return mDateTime
    }

    private var mEnteredAmount = BigDecimal(0)
    private var mProgressDialog: CustomProgressDialog? = null

    private fun createPaymentTransaction() {
        mEnteredAmount = mOrderTotal
        //mEnteredAmount = mEnteredAmount.setScale(2, RoundingMode.DOWN)

        val mPaymentTransactionList = ArrayList<PaymentTransaction>()
        mPaymentTransactionList.addAll(mCheckoutTransaction!!.mPaymentTransaction)

        e(javaClass.simpleName, "Wallet mEnteredAmount: $mEnteredAmount")

        mPaymentTransactionList.add(
            PaymentTransaction(
                "",
                "",
                mWalletPaymentMethod!!.mPaymentMethodID,
                mWalletPaymentMethod!!.mPaymentMethodName,
                mWalletPaymentMethod!!.mPaymentMethodType,
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

        mCheckoutTransaction!!.mPaymentTransaction = mPaymentTransactionList

        e(javaClass.simpleName, "mPaymentTransaction size ${mCheckoutTransaction!!.mPaymentTransaction.size}")

        //qrScan!!.initiateScan()
    }

    private var mTaxPercentage = BigDecimal(0)

    private fun getTax() {
        mCheckoutViewModel.getTaxData().observe(viewLifecycleOwner, {
            for (i in 0 until it.size) mTaxPercentage += it[i].mTaxPercentage

            mTaxPercentage = mTaxPercentage.setScale(2, RoundingMode.DOWN)
        })
    }

    private var mQRCode: String = ""

    private var mDialogClickAction: Int? = 0

    @SuppressLint("LogNotTimber")
    private fun callPaymentAuthYoyoApi() {

        createPaymentTransaction()

        e(javaClass.simpleName, "createPaymentTransaction QR Code: $mQRCode")

        e(javaClass.simpleName, "mPaymentTransaction size ${mCheckoutTransaction!!.mPaymentTransaction.size}")

        for (i in 0 until mCheckoutTransaction!!.mPaymentTransaction.size) {
            if (mCheckoutTransaction!!.mPaymentTransaction[i].mWalletName == "")
                e(
                    javaClass.simpleName,
                    "createPaymentTransaction mTransactionAmount: ${mCheckoutTransaction!!.mPaymentTransaction[i].mTransactionAmount}"
                )
        }

        //progress dialog
        showProgressDialog("Processing Payment", "Please Wait While The Payment is Being Processed", DIALOG_TYPE_OTHER)

        val mPaymentAuthModel =
            YoyoPaymentAuthModel(mCheckoutTransaction!!, getDateTime(), mQRCode, mTableNO, mGroupName, mTaxPercentage)

        mCheckoutViewModel.callYoyoPaymentAuthAPI(mPaymentAuthModel).observe(viewLifecycleOwner, {
            if (it != null) {
                if (it.mStatus == "COMPLETED") {
                    updateWallet(it, it)
                    /*mCheckoutViewModel.callYoyoBasketRegistrationAPI(mPaymentAuthModel, it)
                        .observe(viewLifecycleOwner, Observer { response ->
                            if (response != null) {
                                if (response.mStatus == "COMPLETED") {
                                    e(javaClass.simpleName, "Basket Registration successfully")

                                    updateWallet(it, response)

                                } else {
                                    if (mProgressDialog != null)
                                        mProgressDialog!!.dismiss()

                                    val mCustomDialogFragment = CustomAlertDialogFragment.newInstance(
                                        3, javaClass.simpleName, R.drawable.ic_delete_forever_primary_36dp,
                                        response.mStatus, response.mMessageID + " : " + response.mStatusMessage,
                                        "Okay", "", R.drawable.ic_check_black_24dp, 0
                                    )
                                    mCustomDialogFragment.show(
                                        childFragmentManager,
                                        AppConstants.CUSTOM_DIALOG_FRAGMENT
                                    )
                                }
                            }
                        })*/
                } else {
                    if (mProgressDialog != null)
                        mProgressDialog!!.dismiss()

                    val mCustomDialogFragment = CustomAlertDialogFragment.newInstance(
                        1, javaClass.simpleName, R.drawable.ic_delete_forever_primary_36dp,
                        it.mStatus, it.mMessageID + " : " + it.mStatusMessage,
                        "Okay", "", R.drawable.ic_check_black_24dp, 0
                    )
                    mCustomDialogFragment.show(childFragmentManager, AppConstants.CUSTOM_DIALOG_FRAGMENT)
                }
            }
        })

    }

    private fun updateWallet(it: YoyoResponseModel, response: YoyoResponseModel) {
        for (i in 0 until mCheckoutTransaction!!.mPaymentTransaction.size) {
            if (mCheckoutTransaction!!.mPaymentTransaction[i].mPaymentMethodType == mWalletPaymentMethod!!.mPaymentMethodType &&
                mCheckoutTransaction!!.mPaymentTransaction[i].mWalletName == ""
            ) {

                mCheckoutTransaction!!.mPaymentTransaction[i].mWalletName = YOYO_WALLET
                mCheckoutTransaction!!.mPaymentTransaction[i].mReferenceNO = it.mBasketID
                mCheckoutTransaction!!.mPaymentTransaction[i].mTransactionID = it.mTransactionID
                mCheckoutTransaction!!.mPaymentTransaction[i].mWalletQRCode = mQRCode

                break
            }
        }

        //update values in checkoutTransaction
        mCheckoutTransaction!!.mAmountPaid = mCheckoutTransaction!!.mAmountPaid + mEnteredAmount
        mCheckoutTransaction!!.isFullPaid = mCheckoutTransaction!!.mAmountPaid >= mCheckoutTransaction!!.mOrderTotal
        mCheckoutTransaction!!.mAmountRemaining =
            mCheckoutTransaction!!.mOrderTotal - mCheckoutTransaction!!.mAmountPaid

        //update values in checkoutModel
        mCheckoutModel!!.mAmountPaid = mCheckoutModel!!.mAmountPaid + mEnteredAmount
        mCheckoutModel!!.mAmountRemaining = mCheckoutModel!!.mOrderTotal - mCheckoutModel!!.mAmountPaid

        //change seat status in table model
        when {
            mSelectedLocationType == SERVICE_DINE_IN && mSharedPrefs.getBoolean(SEAT_SELECTION_ENABLED,false) -> if (mCheckoutTransaction!!.isFullPaid) {
                val mLocalSeatList = mLocalTableGroupModel!!.mSeatList!!

                for (i in 0 until mLocalSeatList.size)
                    for (j in 0 until mCheckoutTransaction!!.mSeatList.size)
                        if (mLocalSeatList[i].mSeatNO == mCheckoutTransaction!!.mSeatList[j]) {
                            mLocalSeatList[i].isPaid = true
                            break
                        }
                mLocalTableGroupModel!!.mSeatList = mLocalSeatList
                mCheckoutViewModel.updateTableGroupAndSeats(mLocalTableGroupModel!!)
            }
        }

        updateWallet(response)
    }

    private var mTryAgainYoyoResponseModel: YoyoResponseModel? = null

    private fun updateWallet(response: YoyoResponseModel) {
        mTryAgainYoyoResponseModel = response

        mCheckoutViewModel.updateWallet(mCheckoutModel!!, requireActivity()).observe(viewLifecycleOwner,
            {
                when {
                    it != null -> if(it > -1) {
                        mDialogClickAction = if (mCheckoutTransaction!!.isFullPaid) 1 else 0

                        when {
                            mProgressDialog != null -> mProgressDialog!!.dismiss()
                        }

                        val mCustomDialogFragment = CustomAlertDialogFragment.newInstance(
                            2, javaClass.simpleName, R.drawable.ic_delete_forever_primary_36dp,
                            response.mStatus, response.mMessageID + " : " + response.mStatusMessage,
                            "Okay", "", R.drawable.ic_check_black_24dp, 0
                        )
                        mCustomDialogFragment.show(childFragmentManager, AppConstants.CUSTOM_DIALOG_FRAGMENT)
                    } else {
                        when {
                            mProgressDialog != null -> mProgressDialog!!.dismiss()
                        }

                        mBinding.constraintLayoutNetworkError.visibility = View.VISIBLE
                        mBinding.constraintLayoutMain.visibility = View.GONE
                    }
                }
            })
    }

    private fun tryAgain() {
        mBinding.buttonTryAgain.setOnClickListener {
            showProgressDialog("Processing Payment", "Please Wait While The Payment is Being Processed", DIALOG_TYPE_OTHER)

            updateWallet(mTryAgainYoyoResponseModel!!)

            mBinding.constraintLayoutNetworkError.visibility = View.GONE
            mBinding.constraintLayoutMain.visibility = View.VISIBLE
        }
    }

    private fun showProgressDialog(title: String, message: String, mDialogType: Int) {
        mProgressDialog = CustomProgressDialog.newInstance(title, message, mDialogType)
        mProgressDialog!!.show(childFragmentManager, AppConstants.CUSTOM_PROGRESS_DIALOG_FRAGMENT)
        mProgressDialog!!.isCancelable = false
    }

    @SuppressLint("LogNotTimber")
    @Subscribe
    fun onAlertDialogEvent(mEvent: AlertDialogEvent) {
        when {
            mEvent.mSource == javaClass.simpleName && mEvent.mActionType ->
                when (mEvent.mActionID) {
                    1 -> {
                        // TODO Need to delete wallet payment transaction because yoyo xml failed
                        EventBus.getDefault().post(OnNavigationChangeEvent(AppConstants.DETAIL_NAVIGATION))
                        e(javaClass.simpleName, "onAlertDialogEvent 1")
                    }
                    2 ->
                        when (mDialogClickAction) {
                            1 -> {
                                e(javaClass.simpleName, "Seat Payment Complete")
                                when {
                                    mSharedPrefs.getString(AppConstants.SELECTED_BILL_PRINTER_NAME, "")!!.isNotEmpty() -> {
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
                                    }
                                    else -> {
                                        CustomToast.makeText(requireActivity(), "Bill Printer not selected", Toast.LENGTH_SHORT)
                                        EventBus.getDefault()
                                            .post(SeatPaymentCompleteEvent(true, mCheckoutRowID, javaClass.simpleName))
                                    }
                                }
                            }
                            else -> {
                                e(javaClass.simpleName, "Seat Payment fail")
                                EventBus.getDefault().post(OnNavigationChangeEvent(AppConstants.DETAIL_NAVIGATION))
                            }
                        }
                    3 -> {
                        e(javaClass.simpleName, "onAlertDialogEvent 2")
                        EventBus.getDefault().post(OnNavigationChangeEvent(AppConstants.DETAIL_NAVIGATION))
                    }
                    4 -> {
                        e(javaClass.simpleName, "onAlertDialogEvent 3")
                        EventBus.getDefault().post(OnNavigationChangeEvent(AppConstants.DETAIL_NAVIGATION))
                    }
                }
        }
    }

    @Subscribe
    fun openPaymentMethodFragment(mEvent: OpenPaymentMethodEvent) {
        if(mEvent.mResult) {
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
