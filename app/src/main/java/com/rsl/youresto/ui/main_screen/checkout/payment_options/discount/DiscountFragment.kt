package com.rsl.youresto.ui.main_screen.checkout.payment_options.discount


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
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
import com.rsl.youresto.databinding.FragmentDiscountBinding
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModelFactory
import com.rsl.youresto.ui.main_screen.checkout.events.OnNavigationChangeEvent
import com.rsl.youresto.ui.main_screen.checkout.events.OpenPaymentMethodEvent
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.CHECKOUT_ROW_ID
import com.rsl.youresto.utils.AppConstants.DETAIL_NAVIGATION
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.Utils
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.math.BigDecimal
import java.util.*

/**
 * A simple [Fragment] subclass.
 *
 */
class DiscountFragment : Fragment() {

    private lateinit var mBinding: FragmentDiscountBinding
    private lateinit var mCheckoutViewModel: CheckoutViewModel
    private lateinit var mSharedPrefs: SharedPreferences
    private var mCheckoutRowID = 0
    private var mGroupName = ""
    private var mSelectedLocationType: Int? = null

    companion object {
        private const val CALCULATED_DISCOUNT = "Calculated Discount: "
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_discount, container, false)
        val mView = mBinding.root

        mSharedPrefs =
            requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        mSelectedLocationType = mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE, 0)

        if (mSelectedLocationType == AppConstants.SERVICE_DINE_IN) {
            mGroupName = mSharedPrefs.getString(AppConstants.GROUP_NAME, "")!!
        } else if (mSelectedLocationType == AppConstants.SERVICE_QUICK_SERVICE) {
            mGroupName = "Q"
        }

        val checkoutFactory: CheckoutViewModelFactory =
            InjectorUtils.provideCheckoutViewModelFactory(requireActivity())
        mCheckoutViewModel =
            ViewModelProviders.of(this, checkoutFactory).get(CheckoutViewModel::class.java)

        mCheckoutRowID = mSharedPrefs.getInt(CHECKOUT_ROW_ID, 0)

        getCheckoutData()
        setViews()

        return mView
    }

    private var mCheckoutModel: CheckoutModel? = null
    private var mCheckoutTransactionList: ArrayList<CheckoutTransaction>? = null
    private var mCheckoutTransaction: CheckoutTransaction? = null
    var mDiscountPercent = BigDecimal(0)
    var mDiscountAmount = BigDecimal(0)

    private fun getCheckoutData() {
        mBinding.discountFragment = this

        mCheckoutViewModel.getCheckoutDataByRowID(mCheckoutRowID).observe(viewLifecycleOwner, {
            when {
                it != null -> {
                    mCheckoutModel = it

                    mCheckoutTransactionList = mCheckoutModel!!.mCheckoutTransaction

                    for (i in 0 until mCheckoutTransactionList!!.size) {
                        if (mCheckoutTransactionList!![i].isSelected && !mCheckoutTransactionList!![i].isFullPaid) {
                            mCheckoutTransaction = mCheckoutTransactionList!![i]
                            mDiscountAmount = mCheckoutTransaction!!.mDiscountAmount
                            mDiscountPercent = mCheckoutTransaction!!.mDiscountPercent

                            if(mDiscountAmount > BigDecimal(0)) {
                                mBinding.editTextDiscountValue.setText(String.format(Locale.ENGLISH, mDiscountPercent.toString()))
                                val mDiscountString = CALCULATED_DISCOUNT + getString(R.string.string_currency_sign) + mDiscountAmount
                                mBinding.textViewDiscount.text = mDiscountString
                            }

                            break
                        }
                    }
                }
            }
        })
    }

    private fun setViews() {
        mBinding.toggleSwitchDiscount.setOnToggleSwitchChangeListener { position, _ ->
            if (position == 0) {
                mBinding.textViewSign.text = "%"
                mBinding.editTextDiscountValue.hint = "Enter Discount Percentage"
                mBinding.editTextDiscountValue.setText(
                    String.format(
                        Locale.ENGLISH,
                        mDiscountPercent.toString()
                    )
                )
            } else {
                mBinding.textViewSign.text =
                    resources.getString(R.string.string_currency_sign)
                mBinding.editTextDiscountValue.hint = "Enter Discount Amount"
                mBinding.editTextDiscountValue.setText(
                    String.format(
                        Locale.ENGLISH,
                        mDiscountAmount.toString()
                    )
                )
            }

            val mDiscountString = CALCULATED_DISCOUNT + getString(R.string.string_currency_sign) + mDiscountAmount
            mBinding.textViewDiscount.text = mDiscountString
        }

        mBinding.buttonApplyDiscount.setOnClickListener { discountProceed() }

    }

    private fun discountProceed() {

        Utils.hideKeyboardFrom(requireActivity(), mBinding.editTextDiscountValue)

        val mDiscountAmount: BigDecimal
        val mDiscountPercent: BigDecimal

        var mEnteredAmount = BigDecimal(0)
        if (mBinding.editTextDiscountValue.text.toString().isNotEmpty()) {
            mEnteredAmount =
                BigDecimal(mBinding.editTextDiscountValue.text.toString().toDouble())
        }

        if (mEnteredAmount == BigDecimal(0)) {
            if (mBinding.toggleSwitchDiscount.checkedTogglePosition == 0)
                CustomToast.makeText(
                    requireActivity(),
                    "Enter Discount Percentage",
                    Toast.LENGTH_SHORT
                ).show()
            else
                CustomToast.makeText(requireActivity(), "Enter Discount Amount", Toast.LENGTH_SHORT).show()
            return
        }

        if (mBinding.toggleSwitchDiscount.checkedTogglePosition == 0) {
            mDiscountAmount =
                mEnteredAmount * mCheckoutTransaction!!.mSeatCartTotal / BigDecimal(100)
            mDiscountPercent = mEnteredAmount

            if (mEnteredAmount <= BigDecimal(100)) {
                updateDiscount(mDiscountAmount, mDiscountPercent)
            } else {
                CustomToast.makeText(
                    requireActivity(),
                    "Discount should be less than 100%",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {
            mDiscountAmount = mEnteredAmount

            if (mDiscountAmount < mCheckoutTransaction!!.mSeatCartTotal - mCheckoutTransaction!!.mAmountPaid) {

                mDiscountPercent =
                    (mDiscountAmount * BigDecimal(100)) / mCheckoutTransaction!!.mSeatCartTotal

                updateDiscount(mDiscountAmount, mDiscountPercent)
            } else {
                CustomToast.makeText(
                    requireActivity(),
                    "Discount should be less than remaining amount",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

    }

    private fun updateDiscount(mDiscountAmount: BigDecimal, mDiscountPercent: BigDecimal) {
        val mSeatCartTotal = mCheckoutTransaction!!.mSeatCartTotal - mDiscountAmount

        val mUpdatedTax = mSeatCartTotal * mCheckoutModel!!.mTaxPercent / BigDecimal(100)

        val mOrderTotal = mSeatCartTotal + mUpdatedTax + mCheckoutTransaction!!.mTipAmount

        val mRemainingAmount = mOrderTotal - mCheckoutTransaction!!.mAmountPaid

        //update values in checkoutTransaction
        mCheckoutTransaction!!.mDiscountAmount = mDiscountAmount
        mCheckoutTransaction!!.mDiscountPercent = mDiscountPercent
        mCheckoutTransaction!!.mOrderTotal = mOrderTotal
        mCheckoutTransaction!!.mTaxAmount = mUpdatedTax
        mCheckoutTransaction!!.mAmountRemaining = mRemainingAmount

        //update values in checkoutModel
        mCheckoutModel!!.mDiscountAmount = mCheckoutModel!!.mDiscountAmount + mDiscountAmount
        mCheckoutModel!!.mDiscountPercent = mCheckoutModel!!.mDiscountPercent + mDiscountPercent
        mCheckoutModel!!.mCartTotal = mCheckoutModel!!.mCartTotal - mCheckoutModel!!.mDiscountAmount
        val mCartUpdatedTax =
            mCheckoutModel!!.mCartTotal * mCheckoutModel!!.mTaxPercent / BigDecimal(100)
        mCheckoutModel!!.mTaxAmount = mCartUpdatedTax
        mCheckoutModel!!.mOrderTotal =
            mCheckoutModel!!.mCartTotal + mCartUpdatedTax + mCheckoutModel!!.mTipAmount
        mCheckoutModel!!.mAmountRemaining =
            mCheckoutModel!!.mOrderTotal - mCheckoutModel!!.mAmountPaid


        mCheckoutViewModel.updateDiscount(mCheckoutModel!!)
            .observe(viewLifecycleOwner, {
                if (it != null && it > -1) EventBus.getDefault().post(
                    OnNavigationChangeEvent(
                        DETAIL_NAVIGATION
                    )
                )
            })
    }

    fun onTextChanged(mText: CharSequence) {
        var mEnteredAmount = BigDecimal(0)
        if (mBinding.editTextDiscountValue.text.toString().isNotEmpty()) {
            if (mBinding.editTextDiscountValue.text.toString() == ".") {
                mEnteredAmount = BigDecimal(0)
                mBinding.editTextDiscountValue.setText("0.")
                mBinding.editTextDiscountValue.setSelection(2)
            } else
                mEnteredAmount = BigDecimal(mText.toString())
        }

        val mDiscount: BigDecimal

        val mDiscountString: String

        if (mEnteredAmount > BigDecimal(0)) {
            if (mBinding.toggleSwitchDiscount.checkedTogglePosition == 0) {
                mDiscount = mEnteredAmount * mCheckoutTransaction!!.mSeatCartTotal / BigDecimal(100)
                mDiscountString =
                    CALCULATED_DISCOUNT + getString(R.string.string_currency_sign) + String.format(
                        Locale.ENGLISH,
                        "%.2f",
                        mDiscount
                    )
                mBinding.textViewDiscount.text = mDiscountString
            } else {
                mDiscountString =
                    CALCULATED_DISCOUNT + getString(R.string.string_currency_sign) + String.format(
                        Locale.ENGLISH,
                        "%.2f",
                        mEnteredAmount
                    )
                mBinding.textViewDiscount.text = mDiscountString
            }

        } else {
            mDiscountString = CALCULATED_DISCOUNT + getString(R.string.string_currency_sign) + 0.0
            mBinding.textViewDiscount.text = mDiscountString
        }
    }

    @Subscribe
    fun openPaymentMethodFragment(mEvent: OpenPaymentMethodEvent) {
        if (mEvent.mResult) {
            val action =
                DiscountFragmentDirections.actionDiscountFragmentToPaymentMethodFragment2(mGroupName)
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
