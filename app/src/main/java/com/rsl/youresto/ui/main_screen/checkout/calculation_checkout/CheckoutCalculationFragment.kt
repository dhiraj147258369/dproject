package com.rsl.youresto.ui.main_screen.checkout.calculation_checkout


import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.R
import com.rsl.youresto.data.checkout.model.CheckoutTransaction
import com.rsl.youresto.data.checkout.model.PostCheckout
import com.rsl.youresto.databinding.FragmentCheckoutCalculationBinding
import com.rsl.youresto.ui.main_screen.cart.NewCartViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutDialog
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModelFactory
import com.rsl.youresto.ui.main_screen.checkout.SharedCheckoutViewModel
import com.rsl.youresto.ui.main_screen.checkout.events.OpenPaymentMethodEvent
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.CHECKOUT_ROW_ID
import com.rsl.youresto.utils.AppConstants.GROUP_NAME
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.SEAT_SELECTION_ENABLED
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.custom_dialog.AlertDialogEvent
import com.rsl.youresto.utils.custom_dialog.CustomAlertDialogFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("LogNotTimber")
class CheckoutCalculationFragment : Fragment() {

    private lateinit var mBinding: FragmentCheckoutCalculationBinding
    private lateinit var mCheckoutViewModel: CheckoutViewModel
    private lateinit var mSharedPrefs: SharedPreferences
    private var mCheckoutRowID = 0
    private var mGroupName = ""
    private var isSeatSelectionEnabled = false

    private val cartViewModel: NewCartViewModel by viewModel()
    private val sharedCheckoutModel by lazy { requireParentFragment().requireParentFragment().getViewModel<SharedCheckoutViewModel>() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_checkout_calculation,
            container,
            false
        )
        val mView = mBinding.root

        mSharedPrefs = requireActivity().getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)
        mGroupName = mSharedPrefs.getString(GROUP_NAME, "")!!

        val checkoutFactory: CheckoutViewModelFactory =
            InjectorUtils.provideCheckoutViewModelFactory(requireActivity())
        mCheckoutViewModel =
            ViewModelProviders.of(this, checkoutFactory).get(CheckoutViewModel::class.java)

        mCheckoutRowID = mSharedPrefs.getInt(CHECKOUT_ROW_ID, 0)

        isSeatSelectionEnabled = mSharedPrefs.getBoolean(SEAT_SELECTION_ENABLED, false)

        initViews()
        return mView
    }

    private var mTableID = ""

    fun updateViews() {
        mBinding.textViewSubTotalAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.subTotal)
        mBinding.textViewTaxAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.cgstPer)
        mBinding.textViewDiscountAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.disTotal)
        mBinding.textViewTotalAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.netTotal)

        mBinding.textViewDiscountAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.disTotal)

        mBinding.textViewPaidAmount.text =
            String.format("%.2f", sharedCheckoutModel.paidAmount)
        mBinding.textViewRemainingAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.netTotal - sharedCheckoutModel.paidAmount - sharedCheckoutModel.postCheckout.disTotal)
    }

    private fun initViews() {

        mTableID = mSharedPrefs.getString(AppConstants.SELECTED_TABLE_ID, "")!!

        cartViewModel.getCarts(mTableID).observe(viewLifecycleOwner){

            val cartList = ArrayList(it)

            var subTotal = 0.0
            var taxTotal = 0.0
            for (cart in cartList){
                subTotal += cart.mProductTotalPrice.toDouble()
                taxTotal += (cart.mProductTotalPrice.toDouble() * cart.taxPercentage) /100
            }

            val orderTotal = subTotal + taxTotal - sharedCheckoutModel.postCheckout.disTotal

            val postCheckout = PostCheckout().apply {
                orderId = cartList[0].mCartID
                this.subTotal = subTotal
                cgstPer = taxTotal
                netTotal = orderTotal
            }

            sharedCheckoutModel.postCheckout = postCheckout

            updateViews()

//            mBinding.textViewTipAmount.text =
//                String.format("%.2f", mCheckoutTransaction.mTipAmount)

            sharedCheckoutModel.postCheckout.ids.add(cartList[0].mCartID)
            sharedCheckoutModel.postCheckout.tableOrderId = (cartList[0].tableOrderId)

            (requireParentFragment().requireParentFragment() as CheckoutDialog).updatePaymentFragment()
        }

        e(javaClass.simpleName, "mCheckoutRowID: $mCheckoutRowID")
//        mCheckoutViewModel.getCheckoutDataByRowID(mCheckoutRowID).observe(viewLifecycleOwner, {
//
//            when {
//                it != null -> {
//                    val mTransactionList = it.mCheckoutTransaction
//
//                    var mCheckoutTransaction: CheckoutTransaction? = null
//                    loop@ for (i in 0 until mTransactionList.size) {
//                        when {
//                            mTransactionList[i].isSelected && !mTransactionList[i].isFullPaid -> {
//                                mCheckoutTransaction = mTransactionList[i]
//                                break@loop
//                            }
//                        }
//                    }
//
//                    if (mCheckoutTransaction != null) {
//
//
//                        //discount percent
//                        when {
//                            mCheckoutTransaction.mDiscountPercent > BigDecimal(0) -> {
//                                val mDiscountPercentString =
//                                    "Discount(" + String.format(
//                                        "%.2f",
//                                        mCheckoutTransaction.mDiscountPercent
//                                    ) + "%)"
//                                mBinding.textViewDiscountLabel.text = mDiscountPercentString
//                            }
//                        }
//
//                        //tip percent
//                        when {
//                            mCheckoutTransaction.mTipPercent > BigDecimal(0) -> {
//                                val mTipPercentString =
//                                    "Tip(" + String.format(
//                                        "%.2f",
//                                        mCheckoutTransaction.mTipPercent
//                                    ) + "%)"
//                                mBinding.textViewTipLabel.text = mTipPercentString
//                            }
//                        }
//
//                        when {
//                            mCheckoutTransaction.mDiscountAmount > BigDecimal(0) &&
//                                    mCheckoutTransaction.mAmountPaid == BigDecimal(0) -> mBinding.textViewClearDiscount.visibility = VISIBLE
//                            else -> mBinding.textViewClearDiscount.visibility = GONE
//                        }
//
//                        when {
//                            mCheckoutTransaction.mTipAmount > BigDecimal(0) &&
//                                    mCheckoutTransaction.mAmountPaid == BigDecimal(0) -> mBinding.textViewClearTip.visibility = VISIBLE
//                            else -> mBinding.textViewClearTip.visibility = GONE
//                        }
//
//
//                        //service charge related
//                        when {
//                            mCheckoutTransaction.mServiceChargeAmount > BigDecimal(0) -> {
//                                val mServiceChargeString =
//                                    "Service Charge(" + mCheckoutTransaction.mServiceChargePercent + "%)"
//                                mBinding.textViewServiceChargeLabel.text = mServiceChargeString
//                                mBinding.textViewServiceChargeAmount.text = String.format(
//                                    "%.2f",
//                                    mCheckoutTransaction.mServiceChargeAmount
//                                )
//                                mBinding.textViewServiceChargeAmount.visibility = VISIBLE
//                                mBinding.textViewServiceChargeLabel.visibility = VISIBLE
//                                mBinding.viewHorizontalLine9.visibility = VISIBLE
//                            }
//
//                        }
//
//                        //tax related
//                        when {
//                            mCheckoutTransaction.mTaxAmount > BigDecimal(0) -> {
//                                mBinding.textViewTaxAmount.text =
//                                    String.format("%.2f", mCheckoutTransaction.mTaxAmount)
//                                val mTaxes = StringBuilder()
//                                mTaxes.append("(")
//                                for (i in 0 until it.mTaxList.size) {
//                                    when (i) {
//                                        it.mTaxList.size - 1 -> mTaxes.append(it.mTaxList[i].mTaxName).append(
//                                            " - "
//                                        ).append(it.mTaxList[i].mTaxPercentage).append(
//                                            "%"
//                                        ).append(")")
//                                        else -> mTaxes.append(it.mTaxList[i].mTaxName).append(" - ").append(
//                                            it.mTaxList[i].mTaxPercentage
//                                        ).append(
//                                            "%"
//                                        ).append(", ")
//                                    }
//                                }
//                                val mTaxLabel = "Tax$mTaxes"
//                                mBinding.textViewTaxLabel.text = mTaxLabel
//
//                            }
//                            else -> {
//                                //hide tax label if 0
//                                mBinding.textViewTaxAmount.visibility = GONE
//                                mBinding.textViewTaxLabel.visibility = GONE
//                                mBinding.viewHorizontalLine2.visibility = GONE
//                            }
//                        }
//
//                        //seat related
//                        when {
//                            isSeatSelectionEnabled -> {
//                                val mSeats = StringBuilder()
//                                mSeats.append("(").append(it.mGroupName).append(": ")
//                                for (i in 0 until mCheckoutTransaction.mSeatList.size) {
//                                    when (i) {
//                                        mCheckoutTransaction.mSeatList.size - 1 ->
//                                            mSeats.append("S").append(mCheckoutTransaction.mSeatList[i]).append(
//                                                ")"
//                                            )
//                                        else -> mSeats.append("S").append(mCheckoutTransaction.mSeatList[i]).append(
//                                            ", "
//                                        )
//                                    }
//                                }
//                                mBinding.textViewSeatsCheckout.text = mSeats.toString()
//                            }
//                            else -> mBinding.textViewSeatsCheckout.text = "(${it.mGroupName})"
//                        }
//                    }
//                    val mTableNO = "Table: " + it.mTableNO
//                    mBinding.textViewTableNoCheckout.text = mTableNO
//                }
//            }
//
//        })

        mBinding.textViewClearDiscount.setOnClickListener {
            clearDiscountPopup()
        }

        mBinding.textViewClearTip.setOnClickListener {
            clearTipPopup()
        }
    }

    private var mCustomAlertDialog: CustomAlertDialogFragment? = null

    private fun clearDiscountPopup() {
        mCustomAlertDialog = CustomAlertDialogFragment.newInstance(
            1,
            javaClass.simpleName,
            R.drawable.ic_delete_forever_primary_36dp,
            "Clear Discount?",
            "Are you sure you want to clear the discount on this transaction?",
            "Yes, Clear",
            "No, Don't",
            R.drawable.ic_check_black_24dp,
            R.drawable.ic_close_black_24dp
        )
        mCustomAlertDialog!!.show(childFragmentManager, AppConstants.CUSTOM_DIALOG_FRAGMENT)
    }

    private fun clearTipPopup() {
        mCustomAlertDialog = CustomAlertDialogFragment.newInstance(
            2,
            javaClass.simpleName,
            R.drawable.ic_delete_forever_primary_36dp,
            "Clear Tip?",
            "Are you sure you want to clear the Tip on this transaction?",
            "Yes, Clear",
            "No, Don't",
            R.drawable.ic_check_black_24dp,
            R.drawable.ic_close_black_24dp
        )
        mCustomAlertDialog!!.show(childFragmentManager, AppConstants.CUSTOM_DIALOG_FRAGMENT)
    }

    @Subscribe
    fun onAlertDialogEvent(mEvent: AlertDialogEvent) {
        if (mEvent.mActionID == 1) {
            clearDiscount()
        } else if (mEvent.mActionID == 2) {
            clearTip()
        }
    }

    private fun clearTip() {
        mCheckoutViewModel.getCheckoutDataByRowIDWithoutObserving(mCheckoutRowID).observe(viewLifecycleOwner,
            {
                when {
                    it != null -> {
                        val mCheckoutModel = it

                        val mCheckoutTransactionList = mCheckoutModel.mCheckoutTransaction

                        var mCheckoutTransaction: CheckoutTransaction? = null

                        loop@ for (i in 0 until mCheckoutTransactionList.size) {
                            when {
                                mCheckoutTransactionList[i].isSelected && !mCheckoutTransactionList[i].isFullPaid -> {
                                    mCheckoutTransaction = mCheckoutTransactionList[i]
                                    break@loop
                                }
                            }
                        }

                        val mOrderTotal =
                            mCheckoutTransaction!!.mOrderTotal - mCheckoutTransaction.mTipAmount

                        val mRemainingAmount = mOrderTotal - (mCheckoutTransaction.mAmountPaid)

                        //update values in checkoutModel
                        mCheckoutModel.mTipAmount =
                            mCheckoutModel.mTipAmount - mCheckoutTransaction.mTipAmount
                        mCheckoutModel.mTipPercent =
                            mCheckoutModel.mTipPercent - mCheckoutTransaction.mTipPercent

                        mCheckoutModel.mOrderTotal =
                            mCheckoutModel.mOrderTotal - mCheckoutTransaction.mTipAmount
                        mCheckoutModel.mAmountRemaining =
                            mCheckoutModel.mOrderTotal - mCheckoutModel.mAmountPaid

                        //update values in checkoutTransaction
                        mCheckoutTransaction.mTipAmount = BigDecimal(0)
                        mCheckoutTransaction.mTipPercent = BigDecimal(0)
                        mCheckoutTransaction.mOrderTotal = mOrderTotal
                        mCheckoutTransaction.mAmountRemaining = mRemainingAmount


                        mCheckoutViewModel.updateTip(mCheckoutModel)
                            .observe(viewLifecycleOwner, { tip ->
                                if (tip != null && tip > -1) mCustomAlertDialog!!.dismiss()
                            })
                    }
                }
            })
    }

    private fun clearDiscount() {
        mCheckoutViewModel.getCheckoutDataByRowIDWithoutObserving(mCheckoutRowID).observe(viewLifecycleOwner,
            {
                if (it != null) {
                    val mCheckoutModel = it

                    val mCheckoutTransactionList = mCheckoutModel.mCheckoutTransaction

                    var mCheckoutTransaction: CheckoutTransaction? = null

                    for (i in 0 until mCheckoutTransactionList.size) {
                        if (mCheckoutTransactionList[i].isSelected && !mCheckoutTransactionList[i].isFullPaid) {
                            mCheckoutTransaction = mCheckoutTransactionList[i]
                            break
                        }
                    }

                    val mSeatCartTotal =
                        mCheckoutTransaction!!.mSeatCartTotal

                    val mUpdatedTax = mSeatCartTotal * mCheckoutModel.mTaxPercent / BigDecimal(100)

                    val mOrderTotal = mSeatCartTotal + mUpdatedTax + mCheckoutTransaction.mTipAmount

                    val mRemainingAmount = mOrderTotal - mCheckoutTransaction.mAmountPaid

                    //update values in checkoutModel
                    mCheckoutModel.mDiscountAmount =
                        mCheckoutModel.mDiscountAmount - mCheckoutTransaction.mDiscountAmount
                    mCheckoutModel.mDiscountPercent =
                        mCheckoutModel.mDiscountPercent - mCheckoutTransaction.mDiscountPercent
                    mCheckoutModel.mCartTotal =
                        mCheckoutModel.mCartTotal + mCheckoutModel.mDiscountAmount
                    val mCartUpdatedTax =
                        mCheckoutModel.mCartTotal * mCheckoutModel.mTaxPercent / BigDecimal(100)
                    mCheckoutModel.mTaxAmount = mCartUpdatedTax
                    mCheckoutModel.mOrderTotal =
                        mCheckoutModel.mCartTotal + mCartUpdatedTax + mCheckoutModel.mTipAmount
                    mCheckoutModel.mAmountRemaining =
                        mCheckoutModel.mOrderTotal - mCheckoutModel.mAmountPaid

                    //update values in checkoutTransaction
                    mCheckoutTransaction.mDiscountAmount = BigDecimal(0)
                    mCheckoutTransaction.mDiscountPercent = BigDecimal(0)
                    mCheckoutTransaction.mOrderTotal = mOrderTotal
                    mCheckoutTransaction.mTaxAmount = mUpdatedTax
                    mCheckoutTransaction.mAmountRemaining = mRemainingAmount


                    mCheckoutViewModel.updateDiscount(mCheckoutModel)
                        .observe(viewLifecycleOwner, { update ->
                            if (update != null && update > -1) mCustomAlertDialog!!.dismiss()
                        })
                }
            })
    }

    @Subscribe
    fun openPaymentMethodFragment(mEvent: OpenPaymentMethodEvent) {
        if (mEvent.mResult) {
            val action =
                CheckoutCalculationFragmentDirections.actionCheckoutCalculationFragmentToPaymentMethodFragment(
                    mGroupName
                )
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
