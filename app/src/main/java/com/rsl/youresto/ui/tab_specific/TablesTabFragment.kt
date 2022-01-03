package com.rsl.youresto.ui.tab_specific

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.rsl.youresto.R
import com.rsl.youresto.databinding.FragmentTablesTabBinding
import com.rsl.youresto.utils.AppConstants.INTENT_FROM


class TablesTabFragment : Fragment() {

    private lateinit var binding: FragmentTablesTabBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTablesTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null){
            val intentFrom = arguments?.getString(INTENT_FROM) ?: ""
            if (intentFrom === "PendingOrderFragment") {
                showCart()
            }
        }
    }

    fun showProducts() {
        binding.productHostFragment.visibility = VISIBLE
    }

    fun showCart() {
        binding.cartHostFragment.visibility = VISIBLE
        binding.productHostFragment.visibility = VISIBLE

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.cart_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController
        val graphInflater = navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.cart_nav_graph)
        navController.graph = navGraph
    }

    fun hideCart() {
        binding.cartHostFragment.visibility = GONE
        binding.productHostFragment.visibility = GONE
    }
}