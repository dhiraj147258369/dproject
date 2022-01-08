package com.rsl.youresto.ui.main_screen.quick_service.quick_favorite_item


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.App
import com.rsl.youresto.R
import com.rsl.youresto.databinding.FragmentQuickFavoriteItemBinding
import com.rsl.youresto.ui.main_screen.cart.NewCartViewModel
import com.rsl.youresto.ui.main_screen.favorite_items.NewFavoriteItemViewModel
import com.rsl.youresto.ui.main_screen.favorite_items.model.FavoriteProductModel
import com.rsl.youresto.ui.main_screen.tables_and_tabs.AddToTabDialog
import com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab.AddToTabFragmentArgs
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class QuickFavoriteItemFragment : Fragment() {

    private lateinit var mBinding: FragmentQuickFavoriteItemBinding

    private val viewModel: NewFavoriteItemViewModel by viewModel()
    private val prefs: AppPreferences by inject()
    private val cartViewModel: NewCartViewModel by viewModel()
    private var mIntentFrom: String = "A"

    private var mTableID: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_quick_favorite_item, container, false)
        val mView: View = mBinding.root

        mIntentFrom = requireArguments().getString(AppConstants.INTENT_FROM) ?: ""

        mTableID = prefs.getSelectedTableId()

        getFavoriteItems()

        return mView
    }

    private fun getFavoriteItems() {

        lifecycleScope.launch {
            val locationId = prefs.getSelectedLocation()
            val favorites = withContext(Dispatchers.IO) {
                viewModel.getAllFavorite(locationId)
            }


            val mProductAdapter =
                QuickFavoriteItemGroupAdapter(requireActivity(), ArrayList(favorites))
            mBinding.recyclerViewQuickFavoriteItems.adapter = mProductAdapter
            mBinding.progressbarQuickProducts.visibility = View.GONE
        }

        if (App.isTablet) mBinding.constraintLayoutCartBar.visibility = View.GONE


        lifecycleScope.launch {
            val cartItems = withContext(Dispatchers.IO){
                cartViewModel.getCartByTable(mTableID)
            }

            if (cartItems.isNotEmpty()){
                mBinding.cartProductCount.text = "${cartItems.size}"
            } else {
                mBinding.constraintLayoutCartBar.visibility = View.GONE
            }
        }

        mBinding.viewCart.setOnClickListener {
            if (!App.isTablet){
                findNavController().navigate(R.id.cartFragment)
            }
        }
    }

    @Subscribe
    fun onFavoriteClick(product: FavoriteProductModel) {
        if (App.isTablet){
            openAddToTab(product)
        } else {
            findNavController().navigate(R.id.addToTabFragment,
                AddToTabFragmentArgs(product.mGroupID, product.mCategoryID, product.mProductID).toBundle())
        }
    }

    private fun openAddToTab(mProduct: FavoriteProductModel) {
        val dialog = AddToTabDialog(mProduct.mGroupID, mProduct.mCategoryID, mProduct.mProductID)
        dialog.isCancelable = false
        dialog.show(childFragmentManager, "AddToTabDialog")
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
