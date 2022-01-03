package com.rsl.youresto.ui.main_screen.tables_and_tabs.edit_cart_product.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import com.rsl.youresto.R
import com.rsl.youresto.databinding.DialogEditCartBinding
import com.rsl.youresto.ui.main_screen.tables_and_tabs.edit_cart_product.EditCartProductFragmentArgs

class EditCartDialog(private val cartProductId: String): DialogFragment()  {

    override fun onResume() {
        super.onResume()
        dialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private lateinit var binding: DialogEditCartBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogEditCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHost =
            childFragmentManager.findFragmentById(R.id.editCartHost) as NavHostFragment?
                ?: return

        navHost.navController.setGraph(R.navigation.edit_cart_dialog_nav_graph, EditCartProductFragmentArgs(cartProductId, "").toBundle())
    }

    fun dismissDialog() {
        dismiss()
    }
}