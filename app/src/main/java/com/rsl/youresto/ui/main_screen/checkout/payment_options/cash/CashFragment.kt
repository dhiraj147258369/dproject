package com.rsl.youresto.ui.main_screen.checkout.payment_options.cash

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
import com.rsl.youresto.R
import com.rsl.youresto.data.checkout.model.CheckoutModel
import com.rsl.youresto.data.checkout.model.CheckoutTransaction
import com.rsl.youresto.data.checkout.model.PaymentTransaction
import com.rsl.youresto.data.database_download.models.PaymentMethodModel
import com.rsl.youresto.data.tables.models.LocalTableGroupModel
import com.rsl.youresto.databinding.FragmentCashBinding
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
import com.rsl.youresto.ui.main_screen.checkout.payment_options.events.CurrencyNoteClickEvent
import com.rsl.youresto.ui.main_screen.tables_and_tabs.tables.TablesViewModel
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.DETAIL_NAVIGATION
import com.rsl.youresto.utils.AppConstants.DIALOG_TYPE_OTHER
import com.rsl.youresto.utils.AppConstants.GROUP_NAME
import com.rsl.youresto.utils.AppConstants.ORDER_NO
import com.rsl.youresto.utils.AppConstants.ORDER_TYPE
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_BILL_PRINTER_NAME
import com.rsl.youresto.utils.AppConstants.SELECTED_BILL_PRINT_PAPER_SIZE
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_NO
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppConstants.SERVICE_QUICK_SERVICE
import com.rsl.youresto.utils.AppConstants.TABLE_ID
import com.rsl.youresto.utils.AppConstants.TABLE_NO
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.custom_dialog.CustomProgressDialog
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("LogNotTimber")
class CashFragment : Fragment() {

    private lateinit var mBinding: FragmentCashBinding
    private lateinit var mCheckoutViewModel: CheckoutViewModel
    private lateinit var mTableViewModel: TablesViewModel
    private lateinit var mSharedPrefs: SharedPreferences
    private var mCheckoutRowID = 0
    private var mTableNO = 0
    private var mTableID: String? = null
    private var mGroupName = ""
    private var mLocationID = ""
    private var mSelectedLocationType: Int? = null

    private val cartViewModel: NewCartViewModel by viewModel()
    private val sharedCheckoutModel by lazy { requireParentFragment().requireParentFragment().getViewModel<SharedCheckoutViewModel>() }

    companion object{
        private const val CHANGE = "Change: "
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_cash, container, false)
        val mView = mBinding.root
        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        mSelectedLocationType = mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE, 0)

        if (mSelectedLocationType == SERVICE_DINE_IN) {
            mTableNO = mSharedPrefs.getInt(SELECTED_TABLE_NO, 0)
            mTableID = mSharedPrefs.getString(SELECTED_TABLE_ID, "")
            mGroupName = mSharedPrefs.getString(GROUP_NAME, "")!!
        } else if (mSelectedLocationType == SERVICE_QUICK_SERVICE) {
            mTableNO = mSharedPrefs.getInt(AppConstants.QUICK_SERVICE_TABLE_NO, 0)
            mTableID = mSharedPrefs.getString(QUICK_SERVICE_TABLE_ID, "")
            mGroupName = "Q"
        }

        mLocationID = mSharedPrefs.getString(AppConstants.SELECTED_LOCATION_ID, "")!!

        val factory = InjectorUtils.provideTablesViewModelFactory(requireActivity())
        mTableViewModel = ViewModelProviders.of(this, factory).get(TablesViewModel::class.java)

        val checkoutFactory: CheckoutViewModelFactory = InjectorUtils.provideCheckoutViewModelFactory(requireActivity())
        mCheckoutViewModel = ViewModelProviders.of(this, checkoutFactory).get(CheckoutViewModel::class.java)

//        mCheckoutRowID = mSharedPrefs.getInt(CHECKOUT_ROW_ID, 0)

        setRecyclerView()
        setViews()
        tryAgain()

        return mView
    }

    private var mCheckoutTransactionList: ArrayList<CheckoutTransaction>? = null
    private var mCheckoutTransaction: CheckoutTransaction? = null
    private var mCheckoutModel: CheckoutModel? = null
    private var mLocalTableGroupModel: LocalTableGroupModel? = null
    private var mCashPaymentMethod: PaymentMethodModel? = null
    private var mOrderTotal = BigDecimal(0)

    private fun setViews() {
        mBinding.cashFragment = this

        val mChangeString = CHANGE + getString(R.string.string_currency_sign) + 0.00
        mBinding.textViewChange.text = mChangeString

        e("TAG", "setViews: ${sharedCheckoutModel.postCheckout}" )

        mOrderTotal = BigDecimal(sharedCheckoutModel.postCheckout.netTotal)
        val mOrderTotalString = String.format("%.2f", mOrderTotal)
        mBinding.editTextCashValue.setText(mOrderTotalString)
        mBinding.editTextCashValue.setSelection(mOrderTotalString.length)

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
//                    e(javaClass.simpleName, "mOrderTotal: $mOrderTotal")
//
//                    val mOrderTotalString = String.format("%.2f", mOrderTotal)
//                    mBinding.editTextCashValue.setText(mOrderTotalString)
//                    mBinding.editTextCashValue.setSelection(mOrderTotalString.length)
//                }
//            }
//        })

//        mCheckoutViewModel.getPaymentMethods().observe(viewLifecycleOwner, {
//            loop@ for (i in 0 until it.size)
//                when (TYPE_CASH) {
//                    it[i].mPaymentMethodType -> {
//                        mCashPaymentMethod = it[i]
//                        break@loop
//                    }
//                }
//        })
//
//        when (mSelectedLocationType) {
//            SERVICE_DINE_IN -> mCheckoutViewModel.getTableGroupAndSeats(mTableNO, mGroupName).observe(viewLifecycleOwner,
//                {
//                    mLocalTableGroupModel = it
//                })
//            SERVICE_QUICK_SERVICE -> {
//                val mSeatList = ArrayList<LocalTableSeatModel>()
//                mSeatList.add(LocalTableSeatModel(1, mGroupName, mTableNO, mTableID, true, isPaid = false))
//                mLocalTableGroupModel = LocalTableGroupModel(mGroupName, true, mTableNO, mTableID, mLocationID, mSeatList)
//            }
//        }

        mBinding.imageViewCancelText.setOnClickListener { mBinding.editTextCashValue.setText("") }

        mBinding.buttonCashProceed.setOnClickListener {
//            showProgressDialog("Network Connection", "Checking Network Connection, please wait", DIALOG_TYPE_NETWORK)


            sharedCheckoutModel.postCheckout.cashPayment = sharedCheckoutModel.postCheckout.netTotal

            cartViewModel.checkoutOrder(sharedCheckoutModel.postCheckout, mTableID ?: "")

        }


        cartViewModel.checkoutData.observe(viewLifecycleOwner) {event ->
            event?.getContentIfNotHandled()?.let {
                if (it.status) {
                    CustomToast.makeText(requireActivity(), "Order completed!", Toast.LENGTH_SHORT)
                    (requireParentFragment().requireParentFragment() as CheckoutDialog).dismissDialog()
                }
            }
        }
    }

    private fun showProgressDialog(title: String, message: String, mDialogType: Int) {
        mProgressDialog = CustomProgressDialog.newInstance(title, message, mDialogType)
        mProgressDialog!!.show(childFragmentManager, AppConstants.CUSTOM_PROGRESS_DIALOG_FRAGMENT)
        mProgressDialog!!.isCancelable = false
    }

    private var mProgressDialog: CustomProgressDialog? = null

    private fun tryAgain() {
        mBinding.buttonTryAgain.setOnClickListener {
            showProgressDialog("Processing Payment", "Please Wait While The Payment is Being Processed", DIALOG_TYPE_OTHER)

            updateCash()

            mBinding.constraintLayoutNetworkError.visibility = View.GONE
            mBinding.constraintLayoutMain.visibility = View.VISIBLE
        }
    }

    private fun updateCash() {
        mCheckoutViewModel.updateCash(mCheckoutModel!!, requireActivity()).observe(viewLifecycleOwner,
            {
                when {
                    it != null -> if (it > -1) {
                        when {
                            mProgressDialog != null -> mProgressDialog!!.dismiss()
                        }

                        e(javaClass.simpleName, "mOrderTotal: mOrderTotal: ${mCheckoutTransaction!!.mOrderTotal}")
                        e(javaClass.simpleName, "mOrderTotal: mAmountPaid: ${mCheckoutTransaction!!.mAmountPaid}")
                        e(javaClass.simpleName, "mOrderTotal: isFullPaid: ${mCheckoutTransaction!!.isFullPaid}")

                        when {
                            mCheckoutTransaction!!.isFullPaid -> if (mSharedPrefs.getString(SELECTED_BILL_PRINTER_NAME, "")!!.isNotEmpty()) {
                                EventBus.getDefault()
                                    .post(SeatPaymentCompleteEvent(true, mCheckoutRowID, javaClass.simpleName))
                                val mShareBillPrintIntent =
                                    when {
                                        mSharedPrefs.getInt(SELECTED_BILL_PRINT_PAPER_SIZE, 0) == 50 -> Intent(requireActivity(), ShareBillPrint50Activity::class.java)
                                        else -> Intent(requireActivity(), ShareBillPrint80Activity::class.java)
                                    }

                                mShareBillPrintIntent.putExtra(TABLE_NO, mTableNO)
                                mShareBillPrintIntent.putExtra(GROUP_NAME, mGroupName)
                                mShareBillPrintIntent.putExtra(ORDER_NO, mCheckoutModel!!.mCartNO)
                                mShareBillPrintIntent.putExtra(AppConstants.API_CART_ID, mCheckoutModel!!.mCartID)
                                mShareBillPrintIntent.putExtra(TABLE_ID, mCheckoutModel!!.mTableID)
                                when {
                                    mTableNO != 100 -> mShareBillPrintIntent.putExtra(ORDER_TYPE, 1)
                                    else -> mShareBillPrintIntent.putExtra(ORDER_TYPE, 2)
                                }
                                startActivity(mShareBillPrintIntent)
                            } else {
                                CustomToast.makeText(requireActivity(), "Bill Printer not selected", Toast.LENGTH_SHORT)
                                EventBus.getDefault()
                                    .post(SeatPaymentCompleteEvent(true, mCheckoutRowID, javaClass.simpleName))
                            }
                            else -> EventBus.getDefault().post(OnNavigationChangeEvent(DETAIL_NAVIGATION))
                        }
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

    private fun payEnteredAmount() {

        mChange = mChange.setScale(2, RoundingMode.HALF_UP)

        //progress dialog
        showProgressDialog("Processing Payment", "Please Wait While The Payment is Being Processed", DIALOG_TYPE_OTHER)

        var mEnteredAmount = BigDecimal(mBinding.editTextCashValue.text.toString())
        mEnteredAmount = mEnteredAmount.setScale(2, RoundingMode.DOWN)

        var mTransactionAmount = mEnteredAmount - mChange
        mTransactionAmount = mTransactionAmount.setScale(2, RoundingMode.DOWN)


        val mPaymentTransactionList = ArrayList<PaymentTransaction>()
        mPaymentTransactionList.addAll(mCheckoutTransaction!!.mPaymentTransaction)

        mPaymentTransactionList.add(
            PaymentTransaction(
                "",
                "",
                mCashPaymentMethod!!.mPaymentMethodID,
                mCashPaymentMethod!!.mPaymentMethodName,
                mCashPaymentMethod!!.mPaymentMethodType,
                mTransactionAmount,
                mEnteredAmount,
                mChange,
                "",
                "",
                "",
                "",
                "",
                ""
            )
        )

        //update values in checkoutTransaction
        mCheckoutTransaction!!.mAmountPaid = mCheckoutTransaction!!.mAmountPaid + mTransactionAmount
        e(javaClass.simpleName, "mAmountPaid: ${mCheckoutTransaction!!.mAmountPaid}")
        e(javaClass.simpleName, "mOrderTotal: ${mCheckoutTransaction!!.mOrderTotal}")
        mCheckoutTransaction!!.isFullPaid = mCheckoutTransaction!!.mAmountPaid >= mCheckoutTransaction!!.mOrderTotal
        mCheckoutTransaction!!.mAmountRemaining =
            mCheckoutTransaction!!.mOrderTotal - mCheckoutTransaction!!.mAmountPaid

        //update values in checkoutModel
        mCheckoutModel!!.mAmountPaid = mCheckoutModel!!.mAmountPaid + mEnteredAmount - mChange
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

        updateCash()
    }

    private var mChange = BigDecimal(0)
    fun onTextChanged(mText: CharSequence) {
        var mEnteredAmount = BigDecimal(0)
        if (mBinding.editTextCashValue.text.toString().isNotEmpty()) {
            if (mBinding.editTextCashValue.text.toString() == ".") {
                mEnteredAmount = BigDecimal(0)
                mBinding.editTextCashValue.setText("0.")
                mBinding.editTextCashValue.setSelection(2)
            } else
                mEnteredAmount = BigDecimal(mText.toString().toDouble())


            mEnteredAmount = mEnteredAmount.setScale(2, RoundingMode.DOWN)
        }

        mChange = BigDecimal(0)
        val mChangeString: String

        if (mEnteredAmount > BigDecimal(0) && mEnteredAmount > mCheckoutTransaction!!.mAmountRemaining) {
            mChange = mEnteredAmount - mCheckoutTransaction!!.mAmountRemaining
            mChangeString =
                CHANGE + getString(R.string.string_currency_sign) + String.format(Locale.ENGLISH, "%.2f", mChange)
            mBinding.textViewChange.text = mChangeString
        } else {
            mChangeString =
                CHANGE + getString(R.string.string_currency_sign) + String.format(Locale.ENGLISH, "%.2f", mChange)
            mBinding.textViewChange.text = mChangeString
        }

        mChange = mChange.setScale(2, RoundingMode.DOWN)
    }

    private fun setRecyclerView() {
        val mNoteArray = arrayOf(1, 2, 5, 10, 20, 50, 100)
        val mNotesList = ArrayList(listOf(*mNoteArray))
        val mCurrencyAdapter = CurrencyNoteAdapter(mNotesList)
        mBinding.recyclerViewCurrencyNotes.adapter = mCurrencyAdapter
    }

    @Subscribe
    fun onNoteClicked(mEvent: CurrencyNoteClickEvent) {
        var mEnteredCash = 0.0
        if (mBinding.editTextCashValue.text.isNotEmpty())
            mEnteredCash = mBinding.editTextCashValue.text.toString().toDouble()


        mBinding.editTextCashValue.setText((mEnteredCash + mEvent.mNoteAmount).toString())
        mBinding.editTextCashValue.setSelection(mBinding.editTextCashValue.text.toString().length)
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
