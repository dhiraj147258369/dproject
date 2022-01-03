package com.rsl.youresto.ui.main_screen.quick_service.quick_favorite_item


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.App
import com.rsl.youresto.R
import com.rsl.youresto.databinding.FragmentQuickFavoriteItemBinding
import com.rsl.youresto.ui.main_screen.cart.CartViewModel
import com.rsl.youresto.ui.main_screen.cart.CartViewModelFactory
import com.rsl.youresto.ui.main_screen.cart.NewCartViewModel
import com.rsl.youresto.ui.main_screen.favorite_items.NewFavoriteItemViewModel
import com.rsl.youresto.ui.main_screen.favorite_items.model.FavoriteProductModel
import com.rsl.youresto.ui.main_screen.main_product_flow.NewProductViewModel
import com.rsl.youresto.ui.main_screen.pending_order.PendingOrderFragment
import com.rsl.youresto.ui.main_screen.quick_service.QuickServiceFragmentDirections
import com.rsl.youresto.ui.main_screen.tables_and_tabs.AddToTabDialog
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.QUICK_FAV_ITEM_FRAGMENT
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_CART_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.youresto.utils.AppPreferences
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.custom_views.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class QuickFavoriteItemFragment : Fragment() {

    private lateinit var mBinding: FragmentQuickFavoriteItemBinding
    private lateinit var mCartViewModel: CartViewModel
    private lateinit var mSharedPrefs: SharedPreferences

    private val viewModel: NewFavoriteItemViewModel by viewModel()
    private val productViewModel: NewProductViewModel by viewModel()
    private val cartViewModel: NewCartViewModel by viewModel()
    private val prefs: AppPreferences by inject()
    private var mIntentFrom: String = "A"

    private var mTableID: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_quick_favorite_item, container, false)
        val mView: View = mBinding.root

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        val cartFactory: CartViewModelFactory = InjectorUtils.provideCartViewModelFactory(requireActivity())
        mCartViewModel = ViewModelProviders.of(this, cartFactory).get(CartViewModel::class.java)

        mIntentFrom = requireArguments().getString(AppConstants.INTENT_FROM) ?: ""

        mTableID = mSharedPrefs.getString(AppConstants.SELECTED_TABLE_ID, "")!!

        getFavoriteItems()
        manageCartFabButton()

        return mView
    }

    private fun manageCartFabButton() {
        if(!App.isTablet && mSharedPrefs.getString(QUICK_SERVICE_CART_ID,"") != "") {
            mBinding.cardViewFab.visibility = VISIBLE

            var mCount = 0

            mCartViewModel.getTableCartDataForCartID(mSharedPrefs.getInt(AppConstants.QUICK_SERVICE_TABLE_NO,0),
                mSharedPrefs.getString(QUICK_SERVICE_CART_ID,"")!!).observe(viewLifecycleOwner, {
                if(it.isNotEmpty()) {
                    for(i in it.indices) {
                        mCount++
                    }

                    mBinding.textViewCartItemCount.text = "$mCount"
                }
            })
        }

        mBinding.cardViewFab.setOnClickListener {
            val action = QuickServiceFragmentDirections.actionQuickServiceFragmentToCartGroupFragment("Q",
                QUICK_FAV_ITEM_FRAGMENT)
            findNavController().navigate(action)
        }
    }

    private fun getFavoriteItems() {

        lifecycleScope.launch {
            val locationId = mSharedPrefs.getString(SELECTED_LOCATION_ID, "") ?: ""
            val favorites = withContext(Dispatchers.IO) {
                viewModel.getAllFavorite(locationId)
            }


            val mProductAdapter =
                QuickFavoriteItemGroupAdapter(requireActivity(), ArrayList(favorites))
            mBinding.recyclerViewQuickFavoriteItems.adapter = mProductAdapter
            mBinding.progressbarQuickProducts.visibility = View.GONE
        }
    }

    @Subscribe
    fun onFavoriteClick(product: FavoriteProductModel) {
        if (App.isTablet){

            if (mIntentFrom == PendingOrderFragment::class.simpleName){
                openAddToTab(product)
                return
            }
            lifecycleScope.launch {

                mTableID = mSharedPrefs.getString(AppConstants.SELECTED_TABLE_ID, "") ?: ""

                val cart = withContext(Dispatchers.IO){
                    cartViewModel.getCartByTable(mTableID)
                }

                val table = withContext(Dispatchers.IO){
                    productViewModel.getQuickServiceTable()
                }

                when {
                    table != null -> {
                        prefs.setTable(table.mTableID, table.mTableNo)

                        openAddToTab(product)
                    }
                    cart.isNotEmpty() -> {
                        openAddToTab(product)
                    }
                    else -> {
                        CustomToast.makeText(requireActivity(), "There is no empty tables", Toast.LENGTH_SHORT).show()
                        return@launch
                    }
                }
            }
        }
    }

    private fun openAddToTab(mProduct: FavoriteProductModel) {
        val dialog = AddToTabDialog(mProduct.mGroupID ?: "", mProduct.mCategoryID ?: "", mProduct.mProductID)
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
