package com.rsl.foodnairesto.ui.main_screen.checkout.payment_options

import android.graphics.Color
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
import com.rsl.foodnairesto.App
import com.rsl.foodnairesto.databinding.FragmentPaymentBinding
import com.rsl.foodnairesto.ui.main_screen.cart.NewCartViewModel
import com.rsl.foodnairesto.ui.main_screen.checkout.CheckoutDialog
import com.rsl.foodnairesto.ui.main_screen.checkout.SharedCheckoutViewModel
import com.rsl.foodnairesto.ui.main_screen.checkout.payment_options.events.PaymentCompletedEvent
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.ShowCartEvent
import com.rsl.foodnairesto.utils.AppConstants
import com.rsl.foodnairesto.utils.AppPreferences
import com.rsl.foodnairesto.utils.custom_views.CustomToast
import com.rsl.foodnairesto.utils.new_print.BillPrint
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
    private var selectedDiscountVia = ""

    private fun setupSpinner() {

        lifecycleScope.launch {
            val paymentMethods = withContext(Dispatchers.IO) {
                cartViewModel.getPaymentMethods()
            }

            val payments = ArrayList<String>()

            val discountVia = ArrayList<String>()
            discountVia.add("Amount")
            discountVia.add("Percentage")

            paymentMethods.map {
                payments.add(it.mPaymentMethodName)
            }

//            payments.add("Discount")

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


            val disItemAdapter=ArrayAdapter(requireActivity(), android.R.layout.simple_list_item_1,discountVia )
            binding.discountMenthods.adapter=disItemAdapter

            binding.discountMenthods.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        selectedDiscountVia = discountVia[position]
                        if(selectedDiscountVia.equals("Percentage")){
                            binding.editTextDisValue.hint="%"
                        }else{
                            binding.editTextDisValue.hint="₹"
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {

                    }


                }

        }

    }

    fun setViews() {

        binding.editTextDicountNote.setHintTextColor(Color.GRAY)
        binding.editTextDisValue.hint="₹"
        val mOrderTotal = BigDecimal(sharedCheckoutModel.postCheckout.netTotal)
        val mOrderTotalString = String.format("%.2f", mOrderTotal)
        binding.editTextCashValue.setText(mOrderTotalString)
        binding.editTextCashValue.setSelection(mOrderTotalString.length)

        binding.imageViewCancelText.setOnClickListener { binding.editTextCashValue.setText("") }
        binding.imageDisCancelText.setOnClickListener { binding.editTextDisValue.setText("") }

        binding.buttonCashProceed.setOnClickListener {

            if (binding.editTextCashValue.text.toString().isNotEmpty()) {

                if (sharedCheckoutModel.postCheckout.disTotal <= sharedCheckoutModel.postCheckout.netTotal * 30 / 100) {

                    runthis()
                } else {
                    if (binding.editTextDicountNote.text.toString().isNotEmpty()) {

                        sharedCheckoutModel.postCheckout.discountNote=binding.editTextDicountNote.text.toString()
                        runthis()
                    } else {
                        CustomToast.makeText(requireActivity(), "Please Enter Discount Note", Toast.LENGTH_SHORT).show()
                    }

                }

            } else {
                CustomToast.makeText(requireActivity(), "Please Enter Amount", Toast.LENGTH_SHORT).show()
            }


        }

        binding.applyDiscountButton.setOnClickListener {

            
            var tmDiscount = 0.0

            if (binding.editTextDisValue.text.toString().isNotEmpty()){
                val discnt = binding.editTextDisValue.text.toString().toDouble()
                if (selectedDiscountVia.equals("Percentage")) {

                    tmDiscount = sharedCheckoutModel.postCheckout.netTotal * discnt / 100
                } else {
                    tmDiscount = discnt
                }
            if (tmDiscount > sharedCheckoutModel.postCheckout.netTotal * 90 / 100) {
                CustomToast.makeText(
                    requireActivity(),
                    "Cannot add discount more than 90% of the net price!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (tmDiscount > sharedCheckoutModel.postCheckout.netTotal * 30 / 100) {
                    binding.editTextDicountNote.setHintTextColor(Color.RED)
                } else {
                    binding.editTextDicountNote.setHintTextColor(Color.GRAY)
                }

                if (sharedCheckoutModel.postCheckout.disTotal <= sharedCheckoutModel.postCheckout.netTotal * 30 / 100
                    &&
                    tmDiscount <= sharedCheckoutModel.postCheckout.netTotal * 30 / 100
                ) {
                    sharedCheckoutModel.postCheckout.disTotal = tmDiscount
                    sharedCheckoutModel.postCheckout.disTotalPercentage =
                        (sharedCheckoutModel.postCheckout.disTotal * 100) / sharedCheckoutModel.postCheckout.netTotal
                    var price=String.format("%.2f", sharedCheckoutModel.postCheckout.netTotal - sharedCheckoutModel.paidAmount - sharedCheckoutModel.postCheckout.disTotal)
                    binding.editTextCashValue.setText(price.toString())
                    (requireParentFragment().requireParentFragment() as CheckoutDialog).updateCalculation()

                } else {
                    if (binding.editTextDicountNote.text.toString().isNotEmpty()) {

                        sharedCheckoutModel.postCheckout.disTotal = tmDiscount


                        sharedCheckoutModel.postCheckout.disTotalPercentage =
                            (sharedCheckoutModel.postCheckout.disTotal * 100) / sharedCheckoutModel.postCheckout.netTotal

                            var price=String.format("%.2f", sharedCheckoutModel.postCheckout.netTotal - sharedCheckoutModel.paidAmount - sharedCheckoutModel.postCheckout.disTotal)
                        binding.editTextCashValue.setText(price.toString())
                        (requireParentFragment().requireParentFragment() as CheckoutDialog).updateCalculation()

                    } else {
                        binding.editTextDicountNote.setHintTextColor(Color.RED)
                        CustomToast.makeText(
                            requireActivity(),
                            "Please Enter Discount Note",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        }else{
                CustomToast.makeText(
                    requireActivity(),
                    "Please Enter Discount amount/percentage",
                    Toast.LENGTH_SHORT
                ).show()
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

    fun runthis(){
        val amount = binding.editTextCashValue.text.toString().toDouble()

        if ((amount > (sharedCheckoutModel.postCheckout.netTotal - sharedCheckoutModel.paidAmount - sharedCheckoutModel.postCheckout.disTotal))) {
            CustomToast.makeText(
                requireActivity(),
                "Please Enter Correct Amount",
                Toast.LENGTH_SHORT
            ).show()
        } else {

            if (!selectedPaymentMethod.contains("Discount", ignoreCase = false))
                sharedCheckoutModel.paidAmount += amount

            when {
                selectedPaymentMethod.contains(
                    "Cash",
                    ignoreCase = false
                ) -> sharedCheckoutModel.postCheckout.cashPayment += amount
                selectedPaymentMethod.contains(
                    "Card",
                    ignoreCase = false
                ) -> sharedCheckoutModel.postCheckout.cardPayment += amount
                selectedPaymentMethod.contains(
                    "UPI",
                    ignoreCase = false
                ) -> sharedCheckoutModel.postCheckout.upiPayment += amount
                selectedPaymentMethod.contains(
                    "Banking",
                    ignoreCase = false
                ) -> sharedCheckoutModel.postCheckout.netBanking += amount
                selectedPaymentMethod.contains(
                    "Discount",
                    ignoreCase = false
                ) -> sharedCheckoutModel.postCheckout.disTotal += amount
            }

            if (sharedCheckoutModel.paidAmount >= (sharedCheckoutModel.postCheckout.netTotal - sharedCheckoutModel.postCheckout.disTotal)) {
                binding.constraintLayoutMain.isVisible = false
                binding.progress.isVisible = true
                sharedCheckoutModel.postCheckout.netTotal -= sharedCheckoutModel.postCheckout.disTotal

                cartViewModel.checkoutOrder(
                    sharedCheckoutModel.postCheckout,
                    sharedCheckoutModel.postCheckout.orderId
                )
            } else {
                binding.editTextCashValue.setText("")
                (requireParentFragment().requireParentFragment() as CheckoutDialog).updateCalculation()
            }
        }
//            }else{
//                    CustomToast.makeText(requireActivity(), "Please Enter Discount Note", Toast.LENGTH_SHORT).show()
//            }
    }
    
}