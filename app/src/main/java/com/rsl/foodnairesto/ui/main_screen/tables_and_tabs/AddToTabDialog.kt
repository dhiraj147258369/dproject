package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.databinding.DialogAddToTabBinding
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.AddToTabFragmentArgs

class AddToTabDialog(val groupId: String, val categoryId: String, val productId: String): DialogFragment()  {

    override fun onResume() {
        super.onResume()
        dialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private lateinit var binding: DialogAddToTabBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogAddToTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHost =
            childFragmentManager.findFragmentById(R.id.add_to_tab_host_fragment) as NavHostFragment?
                ?: return

        navHost.navController.setGraph(R.navigation.add_to_tab_dialog_nav_graph, AddToTabFragmentArgs(groupId, categoryId, productId).toBundle())
    }

    fun dismissDialog() {
        dismiss()
    }
}