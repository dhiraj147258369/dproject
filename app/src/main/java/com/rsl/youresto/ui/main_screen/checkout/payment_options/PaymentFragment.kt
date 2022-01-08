package com.rsl.youresto.ui.main_screen.checkout.payment_options

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.rsl.youresto.App
import com.rsl.youresto.databinding.FragmentPaymentBinding
import com.rsl.youresto.ui.main_screen.cart.NewCartViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutDialog
import com.rsl.youresto.ui.main_screen.checkout.SharedCheckoutViewModel
import com.rsl.youresto.ui.main_screen.checkout.bill_print.ShareBillPrint50Activity
import com.rsl.youresto.ui.main_screen.checkout.bill_print.ShareBillPrint80Activity
import com.rsl.youresto.ui.main_screen.checkout.payment_options.events.PaymentCompletedEvent
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.custom_views.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.math.BigDecimal


class PaymentFragment : Fragment() {

    private lateinit var binding: FragmentPaymentBinding
    private lateinit var mSharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPaymentBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val cartViewModel: NewCartViewModel by viewModel()
    private val sharedCheckoutModel by lazy { requireParentFragment().requireParentFragment().getViewModel<SharedCheckoutViewModel>() }
    private val sharedPrefs by inject<SharedPreferences>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mSharedPrefs = requireActivity().getSharedPreferences(
            AppConstants.MY_PREFERENCES,
            Context.MODE_PRIVATE
        )
        setupSpinner()
        setViews()
    }

    private var selectedPaymentMethod = ""

    private fun setupSpinner() {

        lifecycleScope.launch {
            val paymentMethods = withContext(Dispatchers.IO) {
                cartViewModel.getPaymentMethods()
            }

            val payments = ArrayList<String>()
            paymentMethods.map {
                payments.add(it.mPaymentMethodName)
            }

            payments.add("Discount")

            val itemsAdapter =
                ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1, payments)
            binding.paymentMethods.adapter = itemsAdapter

            binding.paymentMethods.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedPaymentMethod = payments[position]
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }


                }
        }

    }

    fun setViews() {

        val mTableID = sharedPrefs.getString(AppConstants.SELECTED_TABLE_ID, "")

        val mOrderTotal = BigDecimal(sharedCheckoutModel.postCheckout.netTotal)
        val mOrderTotalString = String.format("%.2f", mOrderTotal)
        binding.editTextCashValue.setText(mOrderTotalString)
        binding.editTextCashValue.setSelection(mOrderTotalString.length)

        binding.imageViewCancelText.setOnClickListener { binding.editTextCashValue.setText("") }

        binding.buttonCashProceed.setOnClickListener {

            if (binding.editTextCashValue.text.toString().isNotEmpty()) {
                val amount = binding.editTextCashValue.text.toString().toDouble()
                if (!selectedPaymentMethod.contains("Discount", ignoreCase = false) )
                    sharedCheckoutModel.paidAmount += amount

                when {
                    selectedPaymentMethod.contains("Cash", ignoreCase = false) -> sharedCheckoutModel.postCheckout.cashPayment += amount
                    selectedPaymentMethod.contains("Card", ignoreCase = false) -> sharedCheckoutModel.postCheckout.cardPayment += amount
                    selectedPaymentMethod.contains("UPI", ignoreCase = false) -> sharedCheckoutModel.postCheckout.upiPayment += amount
                    selectedPaymentMethod.contains("Banking", ignoreCase = false) -> sharedCheckoutModel.postCheckout.netBanking += amount
                    selectedPaymentMethod.contains("Discount", ignoreCase = false) -> sharedCheckoutModel.postCheckout.disTotal += amount
                }

                if (sharedCheckoutModel.paidAmount >= (sharedCheckoutModel.postCheckout.netTotal - sharedCheckoutModel.postCheckout.disTotal)) {
                    binding.constraintLayoutMain.isVisible = false
                    binding.progress.isVisible = true
                    sharedCheckoutModel.postCheckout.netTotal -= sharedCheckoutModel.postCheckout.disTotal
                    cartViewModel.checkoutOrder(sharedCheckoutModel.postCheckout, mTableID ?: "")
                } else {
                    binding.editTextCashValue.setText("")
                    (requireParentFragment().requireParentFragment() as CheckoutDialog).updateCalculation()
                }

            } else {
                CustomToast.makeText(requireActivity(), "Please Enter Amount", Toast.LENGTH_SHORT).show()
            }


        }

        cartViewModel.checkoutData.observe(viewLifecycleOwner) {event ->
            event?.getContentIfNotHandled()?.let {
                if (it.status) {
                    CustomToast.makeText(requireActivity(), "Order completed!", Toast.LENGTH_SHORT)
                    (requireParentFragment().requireParentFragment() as CheckoutDialog).dismissDialog()
                    printBill()

                    if (!App.isTablet) {
                        EventBus.getDefault().post(PaymentCompletedEvent(true))
                    }
                }
            }
        }
    }

    private fun printBill() {
        val mShareBillPrintIntent =
            when {
                mSharedPrefs.getInt(AppConstants.SELECTED_BILL_PRINT_PAPER_SIZE, 0) == 50 -> Intent(requireActivity(), ShareBillPrint50Activity::class.java)
                else -> Intent(requireActivity(), ShareBillPrint80Activity::class.java)
            }

//        mShareBillPrintIntent.putExtra(AppConstants.TABLE_NO, mTableNO)
//        mShareBillPrintIntent.putExtra(AppConstants.GROUP_NAME, mGroupName)
//        mShareBillPrintIntent.putExtra(AppConstants.ORDER_NO, mCheckoutModel!!.mCartNO)
//        mShareBillPrintIntent.putExtra(AppConstants.API_CART_ID, mCheckoutModel!!.mCartID)
//        mShareBillPrintIntent.putExtra(AppConstants.TABLE_ID, mCheckoutModel!!.mTableID)
//        when {
//            mTableNO != 100 -> mShareBillPrintIntent.putExtra(AppConstants.ORDER_TYPE, 1)
//            else -> mShareBillPrintIntent.putExtra(AppConstants.ORDER_TYPE, 2)
//        }
        startActivity(mShareBillPrintIntent)
    }
    
}