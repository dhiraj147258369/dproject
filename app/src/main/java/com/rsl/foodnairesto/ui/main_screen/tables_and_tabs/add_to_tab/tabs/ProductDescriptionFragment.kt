package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.databinding.FragmentProductDescriptionBinding
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.NewProductViewModel
import com.rsl.foodnairesto.utils.AppConstants
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductDescriptionFragment : Fragment() {

    private lateinit var mBinding: FragmentProductDescriptionBinding
    private val productViewModel: NewProductViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_product_description, container, false)
        val mView = mBinding.root

        setupDescription()
        return mView
    }

    private fun setupDescription() {

        val mProductID = arguments?.getString(AppConstants.PRODUCT_ID)

        productViewModel.getProduct(mProductID?: "").observe(viewLifecycleOwner) {

            if (!it.mProductDescription.isNullOrBlank()){
                mBinding.imageViewWatermark.visibility = GONE
                mBinding.textViewMessage.visibility = GONE
                mBinding.textViewDescription.text = it.mProductDescription
            }

        }
    }

}
