package com.rsl.youresto.ui.main_screen.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.rsl.youresto.R
import com.rsl.youresto.databinding.DialogCheckoutBinding
import com.rsl.youresto.ui.main_screen.checkout.calculation_checkout.CheckoutCalculationFragment
import com.rsl.youresto.ui.main_screen.checkout.payment_options.PaymentFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class CheckoutDialog: DialogFragment()  {

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private lateinit var binding: DialogCheckoutBinding
    private val sharedCheckoutModel: SharedCheckoutViewModel by viewModel()
    private lateinit var navPaymentHost: NavHostFragment
    private lateinit var navCalculationHost: NavHostFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navPaymentHost =
            childFragmentManager.findFragmentById(R.id.checkout_dialog_host) as NavHostFragment?
                ?: return

        navCalculationHost =
            childFragmentManager.findFragmentById(R.id.calculationHost) as NavHostFragment?
                ?: return
    }

    fun dismissDialog() {
        dismiss()
    }

    fun updatePaymentFragment() {
        getCurrentFragment(navPaymentHost)?.let {
            if (it is PaymentFragment){
                it.setViews()
            }
        }
    }

    fun updateCalculation() {
        getCurrentFragment(navCalculationHost)?.let {
            if (it is CheckoutCalculationFragment){
                it.updateViews()
            }
        }
    }

    private fun getCurrentFragment(host: NavHostFragment): Fragment? {
        return host.childFragmentManager.primaryNavigationFragment
    }
}