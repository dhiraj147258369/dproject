package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.databinding.FragmentProductAllergenBinding
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.AddToTabFragmentArgs
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.MainProductViewModel
import com.rsl.foodnairesto.utils.InjectorUtils

class ProductAllergenFragment : Fragment() {

    private lateinit var mBinding: FragmentProductAllergenBinding
    private lateinit var mViewModel: MainProductViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_product_allergen, container, false)
        val mView = mBinding.root

        val factory = InjectorUtils.provideMainProductViewModelFactory(requireActivity())
        mViewModel = ViewModelProviders.of(this, factory).get(MainProductViewModel::class.java)

        setupAllergens()
        return mView
    }

    private fun setupAllergens() {

        val mProductID = AddToTabFragmentArgs.fromBundle(requireArguments()).productId
        val mCategoryID = AddToTabFragmentArgs.fromBundle(requireArguments()).categoryId
        val mGroupID = AddToTabFragmentArgs.fromBundle(requireArguments()).groupId

        val mAllergenList: ArrayList<String> = ArrayList()

        mViewModel.getProductGroupCategory(mGroupID).observe(viewLifecycleOwner, {
            when {
                it != null -> {
                    val mCategoryList = it.mProductCategoryList
                    categoryLoop@
                    for (i in 0 until mCategoryList.size) {
                        when (mCategoryID) {
                            mCategoryList[i].mCategoryID -> {
                                val mProductList = mCategoryList[i].mProductList
                                for (j in 0 until mProductList!!.size) {
                                    when (mProductID) {
                                        mProductList[j].mProductID -> {
                                            mAllergenList.addAll(mProductList[j].mAllergenList)
                                            break@categoryLoop
                                        }
                                    }
                                }
                            }
                        }
                    }

                    mViewModel.getAllergens(mAllergenList).observe(viewLifecycleOwner,
                        { allergenModels ->
                        when {
                            allergenModels.isNotEmpty() -> {
                                mBinding.textViewAllergenLabel.visibility = VISIBLE
                                mBinding.imageViewWatermark.visibility = GONE
                                mBinding.textViewMessage.visibility = GONE
                                val mAllergenAdapter = AllergenRecyclerAdapter(ArrayList(allergenModels))
                                mBinding.recyclerViewAllergen.adapter = mAllergenAdapter
                            }
                        }
                    })

                }
            }
        })
    }
}
