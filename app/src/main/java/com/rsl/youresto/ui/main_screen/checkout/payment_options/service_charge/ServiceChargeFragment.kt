package com.rsl.youresto.ui.main_screen.checkout.payment_options.service_charge


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController

import com.rsl.youresto.R
import com.rsl.youresto.data.checkout.model.CheckoutModel
import com.rsl.youresto.data.checkout.model.CheckoutTransaction
import com.rsl.youresto.databinding.FragmentServiceChargeBinding
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModelFactory
import com.rsl.youresto.ui.main_screen.checkout.events.OnNavigationChangeEvent
import com.rsl.youresto.ui.main_screen.checkout.events.OpenPaymentMethodEvent
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.DETAIL_NAVIGATION
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.Utils
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 *
 */
class ServiceChargeFragment : Fragment() {

    private lateinit var mBinding: FragmentServiceChargeBinding
    private lateinit var mCheckoutViewModel: CheckoutViewModel
    private lateinit var mSharedPrefs: SharedPreferences
    private var mCheckoutRowID = 0

    companion object{
        private const val SERVICE_CHARGE = "Service Charge: "
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_service_charge, container, false)
        val mView = mBinding.root

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)
        val checkoutFactory: CheckoutViewModelFactory = InjectorUtils.provideCheckoutViewModelFactory(requireActivity())
        mCheckoutViewModel = ViewModelProviders.of(this, checkoutFactory).get(CheckoutViewModel::class.java)

        mCheckoutRowID = mSharedPrefs.getInt(AppConstants.CHECKOUT_ROW_ID, 0)

        setViews()

        return mView
    }

    private var mCheckoutModel: CheckoutModel? = null
    private var mCheckoutTransactionList: ArrayList<CheckoutTransaction>? = null
    private var mCheckoutTransaction: CheckoutTransaction? = null
    private fun setViews() {

        mBinding.serviceCharge = this

        mCheckoutViewModel.getCheckoutDataByRowID(mCheckoutRowID).observe(viewLifecycleOwner, {
            if (it != null) {
                mCheckoutModel = it

                mCheckoutTransactionList = mCheckoutModel!!.mCheckoutTransaction

                for (i in 0 until mCheckoutTransactionList!!.size) {
                    if (mCheckoutTransactionList!![i].isSelected && !mCheckoutTransactionList!![i].isFullPaid) {
                        mCheckoutTransaction = mCheckoutTransactionList!![i]
                        break
                    }
                }
            }
        })

        val mServiceCharge = SERVICE_CHARGE + getString(R.string.string_currency_sign) + "0.00"
        mBinding.textViewServiceCharge.text = mServiceCharge

        mBinding.toggleSwitchServiceCharge.setOnToggleSwitchChangeListener { position, _ ->
            if (position == 0) {
                mBinding.textViewSign.text = "%"
                mBinding.editTextServiceChargeValue.hint = "Enter Service Charge Percentage"
            } else {
                mBinding.textViewSign.text = resources.getString(R.string.string_currency_sign)
                mBinding.editTextServiceChargeValue.hint = "Enter Service Charge Amount"
            }

            mBinding.editTextServiceChargeValue.setText("")
            val mDiscountString = SERVICE_CHARGE + getString(R.string.string_currency_sign) + 0.00
            mBinding.textViewServiceCharge.text = mDiscountString
        }

        mBinding.buttonApplyServiceCharge.setOnClickListener { serviceChargeProceed() }

    }

    private fun serviceChargeProceed() {
        Utils.hideKeyboardFrom(requireActivity(), mBinding.editTextServiceChargeValue)

        val mServiceChargeAmount: BigDecimal
        val mServiceChargePercent: BigDecimal

        var mEnteredAmount = BigDecimal(0)
        if (mBinding.editTextServiceChargeValue.text.toString().isNotEmpty()) {
            mEnteredAmount = BigDecimal(mBinding.editTextServiceChargeValue.text.toString().toDouble())
        }

        if (mEnteredAmount == BigDecimal(0)) {
            CustomToast.makeText(requireActivity(), "Enter amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (mBinding.toggleSwitchServiceCharge.checkedTogglePosition == 0) {
            mServiceChargeAmount = mEnteredAmount * mCheckoutTransaction!!.mSeatCartTotal / BigDecimal(100)
            mServiceChargePercent = mEnteredAmount

            if (mEnteredAmount > BigDecimal(0)) {
                updateServiceCharge(mServiceChargeAmount, mServiceChargePercent)
            } else {
                CustomToast.makeText(requireActivity(), "Service Charge should be less than 100%", Toast.LENGTH_SHORT).show()
            }

        } else {
            mServiceChargeAmount = mEnteredAmount

            if (mServiceChargeAmount < mCheckoutTransaction!!.mSeatCartTotal - mCheckoutTransaction!!.mAmountPaid) {

                mServiceChargePercent = (mServiceChargeAmount * BigDecimal(100)) / mCheckoutTransaction!!.mSeatCartTotal

                updateServiceCharge(mServiceChargeAmount, mServiceChargePercent)
            } else {
                CustomToast.makeText(
                    requireActivity(),
                    "Service Charge should be less than remaining amount",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun updateServiceCharge(mServiceChargeAmount: BigDecimal, mServiceChargePercent: BigDecimal) {

        val mOrderTotal = mCheckoutTransaction!!.mOrderTotal + mServiceChargeAmount

        val mRemainingAmount = mOrderTotal - mCheckoutTransaction!!.mAmountPaid

        //update values in checkoutTransaction
        mCheckoutTransaction!!.mServiceChargeAmount = mServiceChargeAmount
        mCheckoutTransaction!!.mServiceChargePercent = mServiceChargePercent
        mCheckoutTransaction!!.mOrderTotal = mOrderTotal
        mCheckoutTransaction!!.mAmountRemaining = mRemainingAmount

        //update values in checkoutModel
        mCheckoutModel!!.mServiceChargeAmount = mCheckoutModel!!.mServiceChargeAmount + mServiceChargeAmount
        mCheckoutModel!!.mServiceChargePercent = mCheckoutModel!!.mServiceChargePercent + mServiceChargePercent

        mCheckoutModel!!.mOrderTotal = mCheckoutModel!!.mOrderTotal + mServiceChargeAmount
        mCheckoutModel!!.mAmountRemaining = mCheckoutModel!!.mOrderTotal - mCheckoutModel!!.mAmountPaid


        mCheckoutViewModel.updateServiceCharge(mCheckoutModel!!)
            .observe(viewLifecycleOwner, {
                if (it != null && it > -1) EventBus.getDefault().post(OnNavigationChangeEvent(DETAIL_NAVIGATION))
            })
    }

    fun onTextChanged(mText: CharSequence) {
        var mEnteredAmount = BigDecimal(0)
        if (mBinding.editTextServiceChargeValue.text.toString().isNotEmpty()) {
            if (mBinding.editTextServiceChargeValue.text.toString() == ".") {
                mEnteredAmount = BigDecimal(0)
                mBinding.editTextServiceChargeValue.setText("0.")
                mBinding.editTextServiceChargeValue.setSelection(2)
            } else
                mEnteredAmount = BigDecimal(mText.toString().toDouble())
        }

        val mDiscount: BigDecimal

        val mDiscountString: String

        if (mEnteredAmount > BigDecimal(0)) {
            if (mBinding.toggleSwitchServiceCharge.checkedTogglePosition == 0) {
                mDiscount = mEnteredAmount * mCheckoutTransaction!!.mSeatCartTotal / BigDecimal(100)
                mDiscountString = SERVICE_CHARGE + getString(R.string.string_currency_sign) + String.format(
                    Locale.ENGLISH,
                    "%.2f",
                    mDiscount
                )
                mBinding.textViewServiceCharge.text = mDiscountString
            } else {
                mDiscountString = SERVICE_CHARGE + getString(R.string.string_currency_sign) + String.format(
                    Locale.ENGLISH,
                    "%.2f",
                    mEnteredAmount
                )
                mBinding.textViewServiceCharge.text = mDiscountString
            }

        } else {
            mDiscountString = SERVICE_CHARGE + getString(R.string.string_currency_sign) + 0.0
            mBinding.textViewServiceCharge.text = mDiscountString
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
