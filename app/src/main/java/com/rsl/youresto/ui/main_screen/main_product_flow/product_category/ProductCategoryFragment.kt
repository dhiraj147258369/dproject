package com.rsl.youresto.ui.main_screen.main_product_flow.product_category


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.rsl.youresto.R
import com.rsl.youresto.data.database_download.models.ProductCategoryModel
import com.rsl.youresto.databinding.FragmentProductCategoryBinding
import com.rsl.youresto.ui.main_screen.MainScreenActivity
import com.rsl.youresto.ui.main_screen.main_product_flow.MainProductViewModel
import com.rsl.youresto.ui.main_screen.main_product_flow.NewProductViewModel
import com.rsl.youresto.ui.main_screen.main_product_flow.event.MainProductFlowEvent
import com.rsl.youresto.ui.main_screen.main_product_flow.event.MainProductNavigateEvent
import com.rsl.youresto.ui.main_screen.main_product_flow.event.MainProductSearchEvent
import com.rsl.youresto.utils.Animations
import com.rsl.youresto.utils.AppConstants.GROUP
import com.rsl.youresto.utils.AppConstants.PRODUCT
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.Utils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProductCategoryFragment : Fragment() {

    private lateinit var mBinding: FragmentProductCategoryBinding
    private lateinit var mMainProductViewModel: MainProductViewModel
    private var mGroupID: String? = null

    private var isTablet: Boolean = false
    private val productViewModel: NewProductViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_product_category, container, false)

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isTablet = resources.getBoolean(R.bool.isTablet)

        val factory = InjectorUtils.provideMainProductViewModelFactory(requireActivity())
        mMainProductViewModel = ViewModelProviders.of(this, factory).get(MainProductViewModel::class.java)

        mGroupID = ProductCategoryFragmentArgs.fromBundle(
            requireArguments()
        ).groupId

        setupProductCategories()
    }

    private fun setupProductCategories() {

        //for any reason if the groupID is empty
        when {
            mGroupID!!.isEmpty() -> {
                val mActivity = activity as MainScreenActivity
                mGroupID = mActivity.mGroupID
            }
        }

        val layoutManager = (mBinding.recyclerViewProductCategories.layoutManager as GridLayoutManager)
        mBinding.recyclerViewProductCategories.post {
            val width = mBinding.recyclerViewProductCategories.width
            if(isTablet){

                layoutManager.spanCount=width / Utils.dpToPx(180)
            }else{
                layoutManager.spanCount=2

            }
           // layoutManager.spanCount = width / Utils.dpToPx(100)
        }

        productViewModel.getProductCategories(mGroupID).observe(viewLifecycleOwner) {
            val categoryAdapter =
                ProductCategoryRecyclerAdapter(
                    ArrayList(it)
                )
            mBinding.recyclerViewProductCategories.adapter = categoryAdapter
            Animations.runGridLayoutAnimationFallDown(mBinding.recyclerViewProductCategories)
        }
    }

    @Subscribe
    fun onCategoryClicked(mCategory: ProductCategoryModel) {
        val action =
            ProductCategoryFragmentDirections.actionProductCategoryFragmentToProductFragment(
                mCategory.mGroupID, mCategory.mCategoryID, ""
            )
        findNavController().navigate(action)

        EventBus.getDefault().post(MainProductFlowEvent(PRODUCT))
    }

    @Subscribe
    fun onProductSearch(mEvent: MainProductSearchEvent) {
        if (mEvent.mSearchStarted) {
            val action =
                ProductCategoryFragmentDirections.actionProductCategoryFragmentToProductFragment(
                    "", "", mEvent.mText
                )
            findNavController().navigate(action)
        }
    }

    @Subscribe
    fun onNavigationEvent(mEvent: MainProductNavigateEvent) {
        if (mEvent.mClickedType == GROUP) {
            findNavController().navigate(R.id.action_productCategoryFragment_to_productGroupFragment)
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
