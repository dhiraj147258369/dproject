package com.rsl.youresto.ui.tab_specific

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.rsl.youresto.R
import com.rsl.youresto.databinding.FragmentQuickServiceTabBinding
import com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab.ShowCartEvent
import com.rsl.youresto.utils.AppConstants.INTENT_FROM
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class QuickServiceTabFragment : Fragment() {

    private lateinit var binding: FragmentQuickServiceTabBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentQuickServiceTabBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val intentFrom = arguments?.getString(INTENT_FROM) ?: ""

        if (intentFrom == "PendingOrderFragment") {
            showProductsAndCart()
        }
    }

    private fun showCart() {
        binding.cartHostFragment.visibility = View.VISIBLE

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.cart_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController
        val graphInflater = navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.cart_nav_graph)
        navController.graph = navGraph
    }

    private fun showProductsAndCart() {
        showCart()

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.product_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController
        val graphInflater = navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.quick_service_tab_nav)

        navController.setGraph(navGraph, arguments)
    }

    fun hideCart() {
        binding.cartHostFragment.visibility = View.GONE
    }

    @Subscribe
    fun showCartEvent(showCartEvent: ShowCartEvent){
        if (showCartEvent.showCart){
            showCart()
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