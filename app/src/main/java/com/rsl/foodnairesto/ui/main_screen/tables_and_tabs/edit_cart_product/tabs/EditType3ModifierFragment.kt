package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.edit_cart_product.tabs


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.data.cart.models.CartProductModel
import com.rsl.foodnairesto.data.database_download.models.GenericProducts
import com.rsl.foodnairesto.data.database_download.models.ProductModel
import com.rsl.foodnairesto.data.database_download.models.SubProductCategoryModel
import com.rsl.foodnairesto.databinding.FragmentEditType3ModifierBinding
import com.rsl.foodnairesto.ui.main_screen.cart.CartViewModel
import com.rsl.foodnairesto.ui.main_screen.cart.CartViewModelFactory
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.MainProductViewModel
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_2.ModifierClickedEvent
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_2.VariantModifierCategoryATTRecyclerAdapter
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_3.*
import com.rsl.foodnairesto.utils.AppConstants
import com.rsl.foodnairesto.utils.InjectorUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.math.BigDecimal

/**
 * A simple [Fragment] subclass.
 *
 */
@SuppressLint("LogNotTimber")
class EditType3ModifierFragment : Fragment() {

    private lateinit var mBinding: FragmentEditType3ModifierBinding
    private lateinit var mViewModel: MainProductViewModel
    private lateinit var mCartViewModel: CartViewModel
    private lateinit var mSharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_type3_modifier, container, false)
        val mView = mBinding.root

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        val factory = InjectorUtils.provideMainProductViewModelFactory(requireActivity())
        mViewModel = ViewModelProviders.of(this, factory).get(MainProductViewModel::class.java)

        val cartFactory: CartViewModelFactory = InjectorUtils.provideCartViewModelFactory(requireActivity())
        mCartViewModel = ViewModelProviders.of(this, cartFactory).get(CartViewModel::class.java)

        getProduct()

        return mView
    }

    private lateinit var mProductModel: ProductModel
    private lateinit var mSubProductCategoryList: ArrayList<SubProductCategoryModel>
    private lateinit var mCartProductModel: CartProductModel

    private fun getProduct() {

        val mCartProductID = requireArguments().getString(AppConstants.API_CART_PRODUCT_ID)!!

        mCartViewModel.getCartProductByID(mCartProductID).observe(viewLifecycleOwner, {

            mCartProductModel = it

            mSubProductCategoryList = mCartProductModel.mSubProductCategoryList!!

            val mSubProductCategoryAdapter = SubProductCategoryRecyclerAdapter(mSubProductCategoryList)
            mBinding.recyclerViewGeneric1.adapter = mSubProductCategoryAdapter

        })

        mVariantList = ArrayList()
    }

    @Subscribe
    fun onSubProductClicked(mSubProduct: ProductModel) {
        mBinding.cardViewSubProducts.visibility = GONE


        Handler().postDelayed({

        mBinding.cardViewSelectedProducts.visibility = VISIBLE
        val mSelectedProductAdapter = SelectedSubProductRecyclerAdapter(mSubProductCategoryList)
        mBinding.recyclerViewSelectedProducts.adapter = mSelectedProductAdapter

        if(mSubProduct.mProductType == 2){

            val mVariantList =
                mSubProduct.mGenericProductList


            mVariantList.sortWith({ c1, c2 ->
                c1.mDineInPrice.toDouble().compareTo(c2.mDineInPrice.toDouble())
            })

            val mVariantsRecyclerAdapter = Type3VariantsRecyclerAdapter(mVariantList)
            mBinding.recyclerViewVariants.adapter = mVariantsRecyclerAdapter

            mBinding.cardViewVariants.visibility = VISIBLE

            mBinding.textViewVariantTitle.text = mSubProduct.mProductName

        }
        }, 200)
    }

    @Subscribe
    fun onChangedClicked(mEvent: SubProductChangeEvent) {
        mBinding.cardViewSubProducts.visibility = VISIBLE
        mBinding.cardViewSelectedProducts.visibility = GONE
        mBinding.cardViewVariants.visibility = GONE
        mBinding.recyclerViewGeneric2.visibility = GONE
    }

    fun getSelectedProducts() : ArrayList<SubProductCategoryModel>{
        return mSubProductCategoryList
    }

    @Subscribe
    fun onSelectedProductClicked(mEvent: SelectedProductClickEvent) {

        Log.e(javaClass.simpleName, "onSelectedProductClicked: ")

        val mProductList =
            mEvent.mProductList

        for (j in 0 until mProductList.size) {
            if (mProductList[j].mProductID == mEvent.mSelectedProductID)
                mProductModel = mProductList[j]
        }

        val mVariantList =
            mProductModel.mGenericProductList

        mBinding.cardViewSubProducts.visibility = GONE
        mBinding.cardViewSelectedProducts.visibility = VISIBLE

        if (mVariantList.size > 0)
            mBinding.cardViewVariants.visibility = VISIBLE

        mVariantList.sortWith({ c1, c2 ->
            c1.mDineInPrice.toDouble().compareTo(c2.mDineInPrice.toDouble())
        })

        val mVariantsRecyclerAdapter =
            Type3VariantsRecyclerAdapter(
                mVariantList
            )
        mBinding.recyclerViewVariants.adapter = mVariantsRecyclerAdapter
    }

    private var mVariant: GenericProducts? = null
    private var mVariantList: ArrayList<GenericProducts>? = null

    @Subscribe
    fun onVariantChecked(mVariant: GenericProducts) {

        Handler().postDelayed({

            if (mVariantList!!.size > 0){

                var mHasCategoryVariant = true
                for (i in 0 until mVariantList!!.size){
                    if (mVariantList!![i].mCategoryID != mVariant.mCategoryID){
                        mHasCategoryVariant = false
                    }else{
                        mVariantList!![i] = mVariant
                        mHasCategoryVariant = true
                        break
                    }
                }

                if (!mHasCategoryVariant) mVariantList!!.add(mVariant)

            }else mVariantList!!.add(mVariant)

            showModifierViews()

            showModifiers(mVariant)
        }, 100)


    }

    @Subscribe
    fun onModifierClicked(mEvent: ModifierClickedEvent) {
//        EventBus.getDefault().post(ModifierType2Event(3, mVariant!!, mVariantList))
    }

    private fun showModifiers(mVariant: GenericProducts) {

        val mModifierCategoryList = if (mVariant.mGenericProductID == mCartProductModel.mProductID)
            mCartProductModel.mEditModifierList!! else mVariant.mIngredientCategoryList

        this.mVariant = mVariant
        this.mVariant!!.mIngredientCategoryList = mModifierCategoryList

        mModifierCategoryList.sortWith({ o1, o2 -> o1.mCategorySequence - o2.mCategorySequence })

        var mSingleSelectionCategoryCount = 0
        var mMultipleSelectionCategoryCount = BigDecimal(0)

        for (i in 0 until mModifierCategoryList.size)
            if (mModifierCategoryList[i].mModifierSelection == AppConstants.SINGLE_SELECTION) {
                mSingleSelectionCategoryCount++
            }else if (mModifierCategoryList[i].mModifierSelection == AppConstants.MULTIPLE_SELECTION){
                val mModifierList = mModifierCategoryList[i].mIngredientsList

                for (j in 0 until mModifierList!!.size){
                    if (mModifierList[j].isSelected){
                        mMultipleSelectionCategoryCount += mModifierList[j].mIngredientQuantity
                    }
                }
            }

        val mMultipleSelectionCount = mVariant.mIngredientLimit - mSingleSelectionCategoryCount

        val mModifierCategoryAdapter =
            VariantModifierCategoryATTRecyclerAdapter(
                requireActivity(),
                mModifierCategoryList,
                mMultipleSelectionCount
            )
        mBinding.recyclerViewGeneric2.adapter = mModifierCategoryAdapter

        val mEditor = mSharedPrefs.edit()
        mEditor.putInt(AppConstants.MULTIPLE_SELECTION_COUNT, mMultipleSelectionCategoryCount.toInt())
        mEditor.apply()

    }

    private fun showModifierViews() {
        mBinding.recyclerViewGeneric2.visibility = VISIBLE
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
