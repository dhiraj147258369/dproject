package com.rsl.youresto.ui.main_screen.checkout.payment_options

import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.rsl.youresto.ui.main_screen.checkout.payment_options.events.PaymentCompletedEvent
import com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab.ShowCartEvent
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppPreferences
import com.rsl.youresto.utils.custom_views.CustomToast
import com.rsl.youresto.utils.new_print.BillPrint
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
    private val prefs: AppPreferences by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                    cartViewModel.checkoutOrder(sharedCheckoutModel.postCheckout, sharedCheckoutModel.postCheckout.orderId)
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
                    printBill()

                    Handler(Looper.getMainLooper()).postDelayed({
                        cartViewModel.deleteCart(sharedCheckoutModel.postCheckout.orderId)
                        (requireParentFragment().requireParentFragment() as CheckoutDialog).dismissDialog()
                        if (!App.isTablet) {
                            EventBus.getDefault().post(PaymentCompletedEvent(true))
                        } else {
                            EventBus.getDefault().post(ShowCartEvent(false))
                        }
                    }, 3000)
                }
            }
        }
    }

    private fun printBill() {
        if (prefs.getSelectedBillPrinterName().isNotBlank()) {
            val print = BillPrint(lifecycleScope, requireActivity(), sharedCheckoutModel.postCheckout.orderId)
            print.finalBill = true
            print.sharedViewModel = sharedCheckoutModel
            if (prefs.getSelectedBillPrinterPaperSize() == AppConstants.PAPER_SIZE_50) print.print50() else print.print80()
        }else {
            CustomToast.makeText(requireActivity(), "Please select a bill printer from settings", Toast.LENGTH_SHORT).show()
        }
    }
    
}