package com.rsl.youresto.ui.main_screen.main_product_flow.product_group


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.rsl.youresto.R
import com.rsl.youresto.data.database_download.models.ProductGroupModel
import com.rsl.youresto.databinding.FragmentProductGroupBinding
import com.rsl.youresto.ui.main_screen.main_product_flow.MainProductViewModel
import com.rsl.youresto.ui.main_screen.main_product_flow.NewProductViewModel
import com.rsl.youresto.ui.main_screen.main_product_flow.event.MainProductFlowEvent
import com.rsl.youresto.ui.main_screen.main_product_flow.event.MainProductSearchEvent
import com.rsl.youresto.ui.main_screen.main_product_flow.event.MainProductStoreIDEvent
import com.rsl.youresto.utils.Animations
import com.rsl.youresto.utils.AppConstants.CATEGORY
import com.rsl.youresto.utils.AppConstants.GROUP
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.Utils
import com.rsl.youresto.utils.Utils.dpToPx
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.viewModel


class ProductGroupFragment : Fragment() {

    private lateinit var mBinding: FragmentProductGroupBinding
    private lateinit var mMainProductViewModel: MainProductViewModel


    private var isTablet: Boolean = false
    private val productViewModel: NewProductViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_product_group, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        isTablet = resources.getBoolean(R.bool.isTablet)

        val factory = InjectorUtils.provideMainProductViewModelFactory(requireActivity())
        mMainProductViewModel = ViewModelProviders.of(this, factory).get(MainProductViewModel::class.java)

        setupProductGroups()
    }

    private fun setupProductGroups() {

        val layoutManager = (mBinding.recyclerViewProductGroups.layoutManager as GridLayoutManager)

        mBinding.recyclerViewProductGroups.post {
            val width = mBinding.recyclerViewProductGroups.width

            if(isTablet){
                layoutManager.spanCount=width / Utils.dpToPx(180)
            }else{
                layoutManager.spanCount=2

            }

            //layoutManager.spanCount = width / dpToPx(100)
        }

        productViewModel.getProductGroups().observe(viewLifecycleOwner) {
            val mGroupAdapter =
                ProductGroupRecyclerAdapter(
                    ArrayList(it), requireActivity()
                )
            mBinding.recyclerViewProductGroups.adapter = mGroupAdapter
            Animations.runGridLayoutAnimationFallDown(mBinding.recyclerViewProductGroups)
        }

        EventBus.getDefault().post(MainProductFlowEvent(GROUP))
    }

    @Subscribe
    fun onProductGroupClicked(mGroup: ProductGroupModel) {
        EventBus.getDefault().post(MainProductStoreIDEvent(mGroup.mGroupID))

        val action =
            ProductGroupFragmentDirections.actionProductGroupFragmentToProductCategoryFragment(
                mGroup.mGroupID
            )
        findNavController().navigate(action)

        EventBus.getDefault().post(MainProductFlowEvent(CATEGORY))
    }

    @SuppressLint("LogNotTimber")
    @Subscribe
    fun onProductSearch(mEvent: MainProductSearchEvent) {
        if (mEvent.mSearchStarted) {
            Log.e(javaClass.simpleName, "onProductSearch: ${mEvent.mText}")
            val action =
                ProductGroupFragmentDirections.actionProductGroupFragmentToProductFragment(
                    "", "", mEvent.mText
                )
            findNavController().navigate(action)
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
