package com.rsl.youresto.ui.main_screen.main_product_flow.product


import android.annotation.SuppressLint
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
import com.rsl.youresto.databinding.FragmentProductBinding
import com.rsl.youresto.ui.main_screen.main_product_flow.MainProductViewModel
import com.rsl.youresto.ui.main_screen.main_product_flow.NewProductViewModel
import com.rsl.youresto.ui.main_screen.main_product_flow.event.MainProductNavigateEvent
import com.rsl.youresto.utils.Animations
import com.rsl.youresto.utils.AppConstants.CATEGORY
import com.rsl.youresto.utils.AppConstants.GROUP
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.Utils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("LogNotTimber")
class ProductFragment : Fragment() {

    private lateinit var mBinding: FragmentProductBinding
    private lateinit var mMainViewModel: MainProductViewModel
    private var mGroupID: String? = null
    private var mCategoryID: String? = null
    private var mText: String? = null

    private var isTablet: Boolean = false
    private val productViewModel: NewProductViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_product, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isTablet = resources.getBoolean(R.bool.isTablet)

        val factory = InjectorUtils.provideMainProductViewModelFactory(requireActivity())
        mMainViewModel = ViewModelProviders.of(this, factory).get(MainProductViewModel::class.java)

        mGroupID = ProductFragmentArgs.fromBundle(
            requireArguments()
        ).groupId
        mCategoryID = ProductFragmentArgs.fromBundle(
            requireArguments()
        ).categoryId
        mText = ProductFragmentArgs.fromBundle(
            requireArguments()
        ).text

        setupSearch(mText ?: "")
        setupProducts()
    }

    fun setupSearch(text: String) {
        productViewModel.filterText.value = text
    }

    private fun setupProducts() {

        val layoutManager = (mBinding.recyclerViewProduct.layoutManager as GridLayoutManager)
        mBinding.recyclerViewProduct.post {
            val width = mBinding.recyclerViewProduct.width
//            layoutManager.spanCount = width / Utils.dpToPx(150)
            if(isTablet){
                layoutManager.spanCount =3
            }else{
                layoutManager.spanCount =2
            }
        }

        productViewModel.getProducts(mCategoryID ?: "").observe(viewLifecycleOwner) {
            val productAdapter = ProductRecyclerAdapter(requireActivity(), ArrayList(it))
            mBinding.recyclerViewProduct.adapter = productAdapter
            Animations.runGridLayoutAnimationFallDown(mBinding.recyclerViewProduct)
        }

    }

    @Subscribe
    fun onNavigationEvent(mEvent: MainProductNavigateEvent) {
        if (mEvent.mClickedType == CATEGORY) {
            val mAction = ProductFragmentDirections.actionProductFragmentToProductCategoryFragment(mGroupID!!)
            findNavController().navigate(mAction)
        } else if (mEvent.mClickedType == GROUP) {
            findNavController().navigate(R.id.action_productFragment_to_productGroupFragment)
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
