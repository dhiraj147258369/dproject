package com.rsl.foodnairesto.ui.main_screen.checkout.calculation_checkout


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.data.checkout.model.PostCheckout
import com.rsl.foodnairesto.databinding.FragmentCheckoutCalculationBinding
import com.rsl.foodnairesto.ui.main_screen.cart.NewCartViewModel
import com.rsl.foodnairesto.ui.main_screen.checkout.CheckoutDialog
import com.rsl.foodnairesto.ui.main_screen.checkout.SharedCheckoutViewModel
import com.rsl.foodnairesto.utils.AppConstants
import com.rsl.foodnairesto.utils.AppPreferences
import com.rsl.foodnairesto.utils.custom_dialog.CustomAlertDialogFragment
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
            String.format("%.2f",sharedCheckoutModel.postCheckout.disTotal)+"("+String.format("%.2f",sharedCheckoutModel.postCheckout.disTotalPercentage)+"%)"
        binding.textViewTotalAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.netTotal)

//        binding.textViewDiscountAmount.text =
//            String.format("%.2f", sharedCheckoutModel.postCheckout.disTotal)

        binding.textViewPaidAmount.text =
            String.format("%.2f", sharedCheckoutModel.paidAmount)

        binding.textViewPaidUPIAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.upiPayment)
        binding.textViewPaidCashAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.cashPayment)
        binding.textViewPaidCardAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.cardPayment)
        binding.textViewPaidBankAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.netBanking)

        binding.textViewRemainingAmount.text =
            String.format("%.2f", sharedCheckoutModel.postCheckout.netTotal - sharedCheckoutModel.paidAmount - sharedCheckoutModel.postCheckout.disTotal)

        if(sharedCheckoutModel.postCheckout.upiPayment>0) {
            binding.textViewPaidUPIAmount.visibility=View.VISIBLE
            binding.textViewPaidUPILabel.visibility=View.VISIBLE
            binding.viewHorizontalLineUpi.visibility=View.VISIBLE
        }else{
            binding.textViewPaidUPIAmount.visibility=View.GONE
            binding.textViewPaidUPILabel.visibility=View.GONE
            binding.viewHorizontalLineUpi.visibility=View.GONE
        }
        if(sharedCheckoutModel.postCheckout.cashPayment>0) {
            binding.textViewPaidCashAmount.visibility=View.VISIBLE
            binding.textViewPaidCashLabel.visibility=View.VISIBLE
            binding.viewHorizontalLineCash.visibility=View.VISIBLE
        }else{
            binding.textViewPaidCashAmount.visibility=View.GONE
            binding.textViewPaidCashLabel.visibility=View.GONE
            binding.viewHorizontalLineCash.visibility=View.GONE
        }
        if(sharedCheckoutModel.postCheckout.cardPayment>0) {
            binding.textViewPaidCardAmount.visibility=View.VISIBLE
            binding.textViewPaidCardLabel.visibility=View.VISIBLE
            binding.viewHorizontalLineCard.visibility=View.VISIBLE
        }else{
            binding.viewHorizontalLineCard.visibility=View.GONE
            binding.textViewPaidCardLabel.visibility=View.GONE
            binding.textViewPaidCardAmount.visibility=View.GONE
        }
        if(sharedCheckoutModel.postCheckout.netBanking>0) {
            binding.viewHorizontalLineBank.visibility=View.VISIBLE
            binding.textViewPaidBankAmount.visibility=View.VISIBLE
            binding.textViewPaidBankLabel.visibility=View.VISIBLE
        }else{
            binding.viewHorizontalLineBank.visibility=View.GONE
            binding.textViewPaidBankAmount.visibility=View.GONE
            binding.textViewPaidBankLabel.visibility=View.GONE
        }

    }

    private fun initViews() {

        val mTableNo = "Checkout for Table: ${prefs.getSelectedTableNo()}"
        binding.textViewTableNoCheckout.text = mTableNo
        mTableID = prefs.getSelectedTableId()
        Log.e("tableId",mTableID)

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
