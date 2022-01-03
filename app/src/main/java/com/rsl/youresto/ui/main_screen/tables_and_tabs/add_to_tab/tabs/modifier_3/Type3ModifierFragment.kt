package com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_3


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log.e
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders

import com.rsl.youresto.R
import com.rsl.youresto.data.database_download.models.GenericProducts
import com.rsl.youresto.data.database_download.models.ProductModel
import com.rsl.youresto.data.database_download.models.SubProductCategoryModel
import com.rsl.youresto.databinding.FragmentType3ModifierBinding
import com.rsl.youresto.ui.main_screen.main_product_flow.MainProductViewModel
import com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab.AddToTabFragmentArgs
import com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab.ScrollTo
import com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_2.ModifierClickedEvent
import com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_2.VariantModifierCategoryATTRecyclerAdapter
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.InjectorUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * A simple [Fragment] subclass.
 *
 */
@SuppressLint("LogNotTimber")
class Type3ModifierFragment : Fragment() {

    private lateinit var mBinding: FragmentType3ModifierBinding
    private lateinit var mViewModel: MainProductViewModel
    private lateinit var mSharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_type3_modifier, container, false)
        val mView = mBinding.root

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        val factory = InjectorUtils.provideMainProductViewModelFactory(requireActivity())
        mViewModel = ViewModelProviders.of(this, factory).get(MainProductViewModel::class.java)

        getProduct()

        mBinding.recyclerViewGeneric1.isNestedScrollingEnabled = false
        mBinding.recyclerViewGeneric2.isNestedScrollingEnabled = false
        mBinding.recyclerViewSelectedProducts.isNestedScrollingEnabled = false
        mBinding.recyclerViewVariants.isNestedScrollingEnabled = false
        return mView
    }

    private lateinit var mProductModel : ProductModel
    private lateinit var mSubProductCategoryList : ArrayList<SubProductCategoryModel>

    private fun getProduct(){
        val mProductID = AddToTabFragmentArgs.fromBundle(requireArguments()).productId
        val mCategoryID = AddToTabFragmentArgs.fromBundle(requireArguments()).categoryId
        val mGroupID = AddToTabFragmentArgs.fromBundle(requireArguments()).groupId

        mViewModel.getProductGroupCategory(mGroupID).observe(viewLifecycleOwner, {
            when {
                it != null -> {
                    val mCategoryList = it.mProductCategoryList

                    for (i in 0 until mCategoryList.size){
                        when (mCategoryID) {
                            mCategoryList[i].mCategoryID -> {
                                val mProductList = mCategoryList[i].mProductList
                                for (j in 0 until mProductList!!.size){
                                    when (mProductID) {
                                        mProductList[j].mProductID -> mProductModel = mProductList[j]
                                    }
                                }
                            }
                        }
                    }

                    mSubProductCategoryList = mProductModel.mSubProductCategoryList

                    val mSubProductCategoryAdapter = SubProductCategoryRecyclerAdapter(mSubProductCategoryList)
                    mBinding.recyclerViewGeneric1.adapter = mSubProductCategoryAdapter
                }
            }
        })

        mVariantList = ArrayList()
    }

    fun getSelectedProducts() : ArrayList<SubProductCategoryModel>{
        return mSubProductCategoryList
    }

    @Subscribe
    fun onSubProductClicked(mSubProduct: ProductModel){
        mBinding.cardViewSubProducts.visibility = GONE

        Handler().postDelayed({

            mBinding.cardViewSelectedProducts.visibility = VISIBLE

            val mSelectedProductAdapter = SelectedSubProductRecyclerAdapter(mSubProductCategoryList)
            mBinding.recyclerViewSelectedProducts.adapter = mSelectedProductAdapter

            val mVariantList =
                mSubProduct.mGenericProductList

            if (mVariantList.size > 0)
                mBinding.cardViewVariants.visibility = VISIBLE

            mBinding.textViewVariantTitle.text = mSubProduct.mProductName

            mVariantList.sortWith({ c1, c2 ->
                c1.mDineInPrice.toDouble().compareTo(c2.mDineInPrice.toDouble())
            })

            val mVariantsRecyclerAdapter =
                Type3VariantsRecyclerAdapter(
                    mVariantList
                )
            mBinding.recyclerViewVariants.adapter = mVariantsRecyclerAdapter

        }, 100)

    }

    @Subscribe
    fun onChangedClicked(mEvent: SubProductChangeEvent){
        mBinding.cardViewSelectedProducts.visibility = GONE
        mBinding.cardViewVariants.visibility = GONE
        mBinding.recyclerViewGeneric2.visibility = GONE

        mBinding.cardViewSubProducts.visibility = VISIBLE
    }

    @Subscribe
    fun onSelectedProductClicked(mEvent: SelectedProductClickEvent){

        e(javaClass.simpleName, "onSelectedProductClicked: ")

        val mProductList =
            mEvent.mProductList

        for (j in 0 until mProductList.size){
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
    fun onVariantChecked(mVariant: GenericProducts){

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

        this.mVariant = mVariant

        showModifierViews()

        showModifiers(mVariant)
    }

    private fun showModifiers(mVariant: GenericProducts){

        val mModifierCategoryList = mVariant.mIngredientCategoryList

        mModifierCategoryList.sortWith({ o1, o2 ->  o1.mCategorySequence - o2.mCategorySequence})

        var mSingleSelectionCategoryCount = 0

        for (i in 0 until mModifierCategoryList.size)
            if (mModifierCategoryList[i].mModifierSelection == AppConstants.SINGLE_SELECTION)
                mSingleSelectionCategoryCount++

        val mMultipleSelectionCount = mVariant.mIngredientLimit - mSingleSelectionCategoryCount

        val mModifierCategoryAdapter =
            VariantModifierCategoryATTRecyclerAdapter(requireActivity(), mModifierCategoryList, mMultipleSelectionCount)
        mBinding.recyclerViewGeneric2.adapter = mModifierCategoryAdapter

        val mEditor = mSharedPrefs.edit()
        mEditor.putInt(AppConstants.MULTIPLE_SELECTION_COUNT, 0)
        mEditor.apply()

    }

    private fun showModifierViews(){
        mBinding.recyclerViewGeneric2.visibility = VISIBLE
    }

    @Subscribe
    fun onModifierClicked(mEvent: ModifierClickedEvent){
//        EventBus.getDefault().post(ModifierType2Event(3, mVariant, mVariantList!!))
    }

    @Subscribe
    fun onValidation(mEvent: ScrollTo){
        if (mEvent.mCategory == "TOP"){
            mBinding.scrollViewType3.smoothScrollTo(0,0)
        } else if (mEvent.mCategory == "TOPPING"){
            val height = mBinding.recyclerViewSelectedProducts.height + mBinding.recyclerViewVariants.height
            mBinding.scrollViewType3.smoothScrollTo(0, height)
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
