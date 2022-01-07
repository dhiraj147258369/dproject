package com.rsl.youresto.ui.main_screen.checkout.calculation_checkout


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.rsl.youresto.R
import com.rsl.youresto.data.checkout.model.PostCheckout
import com.rsl.youresto.databinding.FragmentCheckoutCalculationBinding
import com.rsl.youresto.ui.main_screen.cart.NewCartViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutDialog
import com.rsl.youresto.ui.main_screen.checkout.SharedCheckoutViewModel
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppPreferences
import com.rsl.youresto.utils.custom_dialog.CustomAlertDialogFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("LogNotTimber")
class CheckoutCalculationFragment : Fragment() {

    private lateinit var binding: FragmentCheckoutCalculationBinding

    private val cartViewModel: NewCartViewModel by viewModel()
    private val prefs: AppPreferences by inject()
    private val sharedCheckoutModel by lazy { requireParentFragment().requireParentFragment().getViewModel<SharedCheckoutViewModel>() }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_checkout_calculation,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private var mTableID = ""

    fun updateViews() {
        binding.textViewSubTotalAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.subTotal)
        binding.textViewTaxAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.cgstPer)
        binding.textViewDiscountAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.disTotal)
        binding.textViewTotalAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.netTotal)

        binding.textViewDiscountAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.disTotal)

        binding.textViewPaidAmount.text =
            String.format("%.2f", sharedCheckoutModel.paidAmount)
        binding.textViewRemainingAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.netTotal - sharedCheckoutModel.paidAmount - sharedCheckoutModel.postCheckout.disTotal)
    }

    private fun initViews() {

        val mTableNo = "Checkout for Table: ${prefs.getSelectedTableNo()}"
        binding.textViewTableNoCheckout.text = mTableNo
        mTableID = prefs.getSelectedTableId()

        cartViewModel.getCarts(mTableID).observe(viewLifecycleOwner){

            if (it.isNotEmpty()) {
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

                sharedCheckoutModel.postCheckout.ids.add(cartList[0].mCartID)
                sharedCheckoutModel.postCheckout.tableOrderId = (cartList[0].tableOrderId)

                (requireParentFragment().requireParentFragment() as CheckoutDialog).updatePaymentFragment()
            }
        }


        binding.textViewClearDiscount.setOnClickListener {
            clearDiscountPopup()
        }

        binding.textViewClearTip.setOnClickListener {
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
}
