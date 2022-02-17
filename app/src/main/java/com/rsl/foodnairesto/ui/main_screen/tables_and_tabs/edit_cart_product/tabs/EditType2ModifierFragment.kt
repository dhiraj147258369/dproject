package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.edit_cart_product.tabs


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.data.cart.models.CartProductModel
import com.rsl.foodnairesto.data.database_download.models.IngredientsModel
import com.rsl.foodnairesto.data.database_download.models.ProductModel
import com.rsl.foodnairesto.databinding.FragmentEditType2ModifierBinding
import com.rsl.foodnairesto.ui.main_screen.cart.CartViewModel
import com.rsl.foodnairesto.ui.main_screen.cart.CartViewModelFactory
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.MainProductViewModel
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.NewProductViewModel
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_2.ModifierClickedEvent
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_2.ModifierType2Event
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_2.Type2VariantsRecyclerAdapter
import com.rsl.foodnairesto.utils.AppConstants
import com.rsl.foodnairesto.utils.AppConstants.API_CART_PRODUCT_ID
import com.rsl.foodnairesto.utils.InjectorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * A simple [Fragment] subclass.
 *
 */
@SuppressLint("LogNotTimber")
class EditType2ModifierFragment : Fragment() {

    private lateinit var mBinding: FragmentEditType2ModifierBinding
    private lateinit var mCartViewModel: CartViewModel
    private lateinit var mViewModel: MainProductViewModel
    private lateinit var mSharedPrefs: SharedPreferences
    private val productViewModel: NewProductViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_type2_modifier, container, false)
        val mView = mBinding.root

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        val factory = InjectorUtils.provideMainProductViewModelFactory(requireActivity())
        mViewModel = ViewModelProviders.of(this, factory).get(MainProductViewModel::class.java)

        val cartFactory: CartViewModelFactory = InjectorUtils.provideCartViewModelFactory(requireActivity())
        mCartViewModel = ViewModelProviders.of(this, cartFactory).get(CartViewModel::class.java)

        getProduct()
        initViews()
        return mView
    }

    private lateinit var mCartProductModel: CartProductModel
    private lateinit var mProductModel: ProductModel

    private fun setupIngredients() {
        lifecycleScope.launch {

            val ingredients = withContext(Dispatchers.IO) {
                ArrayList(productViewModel.getProductIngredients(mProductModel.mIngredientsList))
            }

            mCartProductModel.mShowModifierList?.map {
                for (ingredient in ingredients){
                    if (ingredient.mIngredientID == it.mIngredientID){
                        ingredient.isSelected = true
                        mVariantList.add(ingredient)
                        break
                    }
                }
            }

            EventBus.getDefault().post(ModifierType2Event(2, null, mVariantList))

            val mVariantsRecyclerAdapter =
                Type2VariantsRecyclerAdapter(
                    ingredients
                )
            mBinding.recyclerViewGeneric1.adapter = mVariantsRecyclerAdapter
        }
    }

    private fun getProduct() {


        val mCartProductID = requireArguments().getString(API_CART_PRODUCT_ID)!!

        lifecycleScope.launch {
            mCartProductModel = withContext(Dispatchers.IO) {
                productViewModel.getCartProductByID(mCartProductID)
            }

            productViewModel.getProduct(mCartProductModel.mProductID ?: "").observe(viewLifecycleOwner) {
                mProductModel = it
                setupIngredients()
            }
        }
    }

    private fun initViews() {
        mBinding.textViewChangeVariant.setOnClickListener {
            hideModifierViews()
        }
    }

    private var mVariant: IngredientsModel? = null
    private var mVariantList = ArrayList<IngredientsModel>()

    @Subscribe
    fun onVariantChecked(mVariant: IngredientsModel) {

        if (!mVariantList.contains(mVariant)) mVariantList.add(mVariant) else mVariantList.remove(mVariant)

        Handler(Looper.getMainLooper()).postDelayed({
            this.mVariant = mVariant
            mBinding.textViewSelectedVariant.text = mVariant.mIngredientName

            EventBus.getDefault().post(ModifierType2Event(2, mVariant, mVariantList))
        }, 100)

    }

    @Subscribe
    fun onModifierClicked(mEvent: ModifierClickedEvent) {
//        EventBus.getDefault().post(ModifierType2Event(2, mVariant!!, mVariantList))
    }


    private fun showModifierViews() {
        e(javaClass.simpleName, "showModifierViews:" )
        mBinding.textViewSelectedVariant.visibility = View.VISIBLE
        mBinding.textViewChangeVariant.visibility = View.VISIBLE
        mBinding.cardViewGenericProducts.visibility = View.GONE
        mBinding.recyclerViewGeneric2.visibility = View.VISIBLE
    }

    private fun hideModifierViews() {
        e(javaClass.simpleName, "hideModifierViews:" )
        mBinding.textViewSelectedVariant.visibility = View.GONE
        mBinding.textViewChangeVariant.visibility = View.GONE
        mBinding.recyclerViewGeneric2.visibility = View.GONE

        mBinding.cardViewGenericProducts.visibility = View.VISIBLE
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
