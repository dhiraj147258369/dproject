package com.rsl.youresto.ui.main_screen.checkout.payment_options.tip


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
import com.rsl.youresto.data.database_download.models.PaymentMethodModel
import com.rsl.youresto.databinding.FragmentTipBinding
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModelFactory
import com.rsl.youresto.ui.main_screen.checkout.events.OnNavigationChangeEvent
import com.rsl.youresto.ui.main_screen.checkout.events.OpenPaymentMethodEvent
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.DETAIL_NAVIGATION
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.Utils
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class TipFragment : Fragment() {

    private lateinit var mBinding: FragmentTipBinding
    private lateinit var mCheckoutViewModel: CheckoutViewModel
    private lateinit var mSharedPrefs: SharedPreferences
    private var mCheckoutRowID = 0

    companion object{
        private const val CALCULATED_TIP = "Calculated Tip: "
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_tip, container, false)
        val mView = mBinding.root

        mSharedPrefs = requireActivity().getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        val checkoutFactory: CheckoutViewModelFactory = InjectorUtils.provideCheckoutViewModelFactory(requireActivity())
        mCheckoutViewModel = ViewModelProviders.of(this, checkoutFactory).get(CheckoutViewModel::class.java)

        mCheckoutRowID = mSharedPrefs.getInt(AppConstants.CHECKOUT_ROW_ID, 0)

        getCheckoutData()
        setViews()

        return mView
    }

    private var mCheckoutModel: CheckoutModel? = null
    private var mCheckoutTransactionList: ArrayList<CheckoutTransaction>? = null
    private var mCheckoutTransaction: CheckoutTransaction? = null
    private var mTipPaymentMethod: PaymentMethodModel? = null
    var mTipPercent = BigDecimal(0)
    var mTipAmount = BigDecimal(0)

    private fun getCheckoutData() {
        mBinding.tipFragment = this

        mCheckoutViewModel.getCheckoutDataByRowID(mCheckoutRowID).observe(viewLifecycleOwner, {
            when {
                it != null -> {
                    mCheckoutModel = it

                    mCheckoutTransactionList = mCheckoutModel!!.mCheckoutTransaction

                    loop@ for (i in 0 until mCheckoutTransactionList!!.size) {
                        when {
                            mCheckoutTransactionList!![i].isSelected && !mCheckoutTransactionList!![i].isFullPaid -> {
                                mCheckoutTransaction = mCheckoutTransactionList!![i]
                                mTipAmount = mCheckoutTransaction!!.mTipAmount
                                mTipPercent = mCheckoutTransaction!!.mTipPercent

                                if(mTipAmount > BigDecimal(0)) {
                                    mBinding.editTextTipValue.setText(String.format(Locale.ENGLISH, mTipPercent.toString()))
                                    val mDiscountString = CALCULATED_TIP + getString(R.string.string_currency_sign) + mTipAmount
                                    mBinding.textViewTip.text = mDiscountString
                                }
                                break@loop
                            }
                        }
                    }
                }
            }
        })
    }

    private fun setViews(){
        mBinding.toggleSwitchTip.setOnToggleSwitchChangeListener { position, _ ->
            when (position) {
                0 -> {
                    mBinding.textViewSign.text = "%"
                    mBinding.editTextTipValue.hint = "Enter Tip Percentage"
                    mBinding.editTextTipValue.setText(String.format(Locale.ENGLISH, mTipPercent.toString()))
                }
                else -> {
                    mBinding.textViewSign.text = resources.getString(R.string.string_currency_sign)
                    mBinding.editTextTipValue.hint = "Enter Tip Amount"
                    mBinding.editTextTipValue.setText(String.format(Locale.ENGLISH, mTipAmount.toString()))
                }
            }

            val mTipString = "Tip: " + getString(R.string.string_currency_sign) + mTipAmount
            mBinding.textViewTip.text = mTipString
        }

        mBinding.buttonApplyTip.setOnClickListener { tipProceed() }

        mCheckoutViewModel.getPaymentMethods().observe(viewLifecycleOwner, {
            loop@ for (i in it.indices)
                when (it[i].mPaymentMethodType) {
                    AppConstants.TYPE_CASH -> {
                        mTipPaymentMethod = it[i]
                        break@loop
                    }
                }
        })
    }

    private fun tipProceed() {
        Utils.hideKeyboardFrom(requireActivity(), mBinding.editTextTipValue)

        val mTipAmount: BigDecimal
        val mTipPercent: BigDecimal

        var mEnteredAmount = BigDecimal(0)
        if (mBinding.editTextTipValue.text.toString().isNotEmpty()) {
            mEnteredAmount = BigDecimal(mBinding.editTextTipValue.text.toString().toDouble())
        }

        if (mEnteredAmount <= BigDecimal(0)) {
            if (mBinding.toggleSwitchTip.checkedTogglePosition == 0)
                CustomToast.makeText(requireActivity(), "Enter valid tip percentage", Toast.LENGTH_SHORT).show()
            else
                CustomToast.makeText(requireActivity(), "Enter valid tip amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (mBinding.toggleSwitchTip.checkedTogglePosition == 0) {
            mTipAmount = mEnteredAmount * mCheckoutTransaction!!.mSeatCartTotal / BigDecimal(100)
            mTipPercent = mEnteredAmount
            updateTip(mTipAmount, mTipPercent)
        } else {
            mTipAmount = mEnteredAmount
            mTipPercent = (mTipAmount * BigDecimal(100)) / mCheckoutTransaction!!.mSeatCartTotal
            updateTip(mTipAmount, mTipPercent)
        }

    }

    private fun updateTip(mTipAmount: BigDecimal, mTipPercent: BigDecimal) {

        val mOrderTotal = mCheckoutTransaction!!.mOrderTotal + mTipAmount

        val mRemainingAmount = mOrderTotal - (mCheckoutTransaction!!.mAmountPaid)

        //update values in checkoutTransaction
        mCheckoutTransaction!!.mTipAmount = mTipAmount
        mCheckoutTransaction!!.mTipPercent = mTipPercent
        mCheckoutTransaction!!.mOrderTotal = mOrderTotal
//        mCheckoutTransaction!!.mAmountPaid = mCheckoutTransaction!!.mAmountPaid + mTipAmount
        mCheckoutTransaction!!.mAmountRemaining = mRemainingAmount

        //update values in checkoutModel
        mCheckoutModel!!.mTipAmount = mCheckoutModel!!.mTipAmount + mTipAmount
        mCheckoutModel!!.mTipPercent = mCheckoutModel!!.mTipPercent + mTipPercent

//        mCheckoutModel!!.mAmountPaid = mCheckoutModel!!.mAmountPaid + mTipAmount

        mCheckoutModel!!.mOrderTotal = mCheckoutModel!!.mOrderTotal + mTipAmount
        mCheckoutModel!!.mAmountRemaining = mCheckoutModel!!.mOrderTotal - mCheckoutModel!!.mAmountPaid


        mCheckoutViewModel.updateTip(mCheckoutModel!!)
            .observe(viewLifecycleOwner, {
                if (it != null && it > -1) EventBus.getDefault().post(OnNavigationChangeEvent(DETAIL_NAVIGATION))
            })
    }

    fun onTextChanged(mText: CharSequence) {
        var mEnteredAmount = BigDecimal(0)
        if (mBinding.editTextTipValue.text.toString().isNotEmpty()) {
            if (mBinding.editTextTipValue.text.toString() == ".") {
                mEnteredAmount = BigDecimal(0)
                mBinding.editTextTipValue.setText("0.")
                mBinding.editTextTipValue.setSelection(2)
            } else
                mEnteredAmount = BigDecimal(mText.toString().toDouble())
        }

        val mTip : BigDecimal

        val mTipString: String

        if (mEnteredAmount > BigDecimal(0)) {
            if (mBinding.toggleSwitchTip.checkedTogglePosition == 0) {
                mTip = mEnteredAmount * mCheckoutTransaction!!.mSeatCartTotal / BigDecimal(100)
                mTipString = CALCULATED_TIP + getString(R.string.string_currency_sign) + String.format(
                    Locale.ENGLISH,
                    "%.2f",
                    mTip
                )
                mBinding.textViewTip.text = mTipString
            } else {
                mTipString = CALCULATED_TIP + getString(R.string.string_currency_sign) + String.format(
                    Locale.ENGLISH,
                    "%.2f",
                    mEnteredAmount
                )
                mBinding.textViewTip.text = mTipString
            }

        } else {
            mTipString = CALCULATED_TIP + getString(R.string.string_currency_sign) + 0.00
            mBinding.textViewTip.text = mTipString
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
