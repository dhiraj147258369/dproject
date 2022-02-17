package com.rsl.foodnairesto.ui.main_screen.main_product_flow


import android.annotation.SuppressLint
import android.graphics.Typeface.DEFAULT
import android.graphics.Typeface.DEFAULT_BOLD
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.data.database_download.models.ProductCategoryModel
import com.rsl.foodnairesto.data.database_download.models.ProductGroupModel
import com.rsl.foodnairesto.data.database_download.models.ProductModel
import com.rsl.foodnairesto.data.database_download.models.TablesModel
import com.rsl.foodnairesto.databinding.FragmentMainProductBinding
import com.rsl.foodnairesto.ui.main_screen.MainScreenActivity
import com.rsl.foodnairesto.ui.main_screen.cart.NewCartViewModel
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.event.MainProductFlowEvent
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.event.MainProductNavigateEvent
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.event.MainProductSearchEvent
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.product.ProductFragment
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.product_category.ProductCategoryFragment
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.product_group.ProductGroupFragment
import com.rsl.foodnairesto.ui.main_screen.quick_service.QuickServiceFragmentDirections
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.AddToTabDialog
import com.rsl.foodnairesto.utils.AppConstants.CATEGORY
import com.rsl.foodnairesto.utils.AppConstants.GROUP
import com.rsl.foodnairesto.utils.AppConstants.INTENT_FROM
import com.rsl.foodnairesto.utils.AppConstants.PRODUCT
import com.rsl.foodnairesto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.foodnairesto.utils.AppConstants.SERVICE_QUICK_SERVICE
import com.rsl.foodnairesto.utils.AppPreferences
import com.rsl.foodnairesto.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("LogNotTimber")
class MainProductFragment : Fragment() {

    private lateinit var mBinding: FragmentMainProductBinding
    private lateinit var mTableID: String
    private var mSelectedLocationType: Int? = null
    private var mIntentFrom: String = "A"

    private var isTablet: Boolean = false

    private val prefs: AppPreferences by inject()
    private val cartViewModel: NewCartViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_product, container, false)
        val mView = mBinding.root

        isTablet = resources.getBoolean(R.bool.isTablet)

        mSelectedLocationType = prefs.getLocationServiceType()
        mIntentFrom = arguments?.getString(INTENT_FROM) ?: ""
        mTableID = prefs.getSelectedTableId()

        (activity as MainScreenActivity?)?.checktablesfrag()
        initViews()
        checkLocationServiceType()
        return mView
    }


    private fun checkLocationServiceType() {
        if (mSelectedLocationType != SERVICE_DINE_IN) {
            mBinding.textViewTableNo.visibility = GONE
            mBinding.textViewGuestsLabel.visibility = GONE
            mBinding.textViewOccupiedChairs.visibility = GONE
        }
    }

    var mGroupID: String = ""

    private fun initViews() {
        val mTableNO = "Table " + prefs.getSelectedTableNo()
        mBinding.textViewTableNo.text = mTableNO


        mBinding.textViewNavigationCategories.setOnClickListener {
            EventBus.getDefault().post(
                MainProductNavigateEvent(
                    CATEGORY,
                    mGroupID
                )
            )
            mBinding.textViewNavigationProducts.visibility = INVISIBLE
            mBinding.imageViewArrowCategory.visibility = INVISIBLE
            mBinding.editTextSearchProducts.setText("")
        }

        mBinding.textViewNavigationGroups.setOnClickListener {
            EventBus.getDefault().post(
                MainProductNavigateEvent(
                    GROUP,
                    ""
                )
            )
            mBinding.textViewNavigationProducts.visibility = INVISIBLE
            mBinding.imageViewArrowCategory.visibility = INVISIBLE
            mBinding.textViewNavigationCategories.visibility = INVISIBLE
            mBinding.imageViewArrowCategory.visibility = INVISIBLE

            mGroupID = ""
            mBinding.editTextSearchProducts.setText("")
        }

        mBinding.textViewOccupiedChairs.setOnClickListener {}

        productSearch()
        Handler(Looper.getMainLooper()).postDelayed({
            redirectTOProducts()
        }, 10)

        if (isTablet) mBinding.constraintLayoutCartBar.visibility = GONE


        lifecycleScope.launch {
            val cartItems = withContext(Dispatchers.IO){
                cartViewModel.getCartByTable(mTableID)
            }

            if (cartItems.isNotEmpty()){
                mBinding.cartProductCount.text = "${cartItems.size}"
            } else {
                mBinding.constraintLayoutCartBar.visibility = GONE
            }
        }

        mBinding.viewCart.setOnClickListener {
            if (!isTablet){
                findNavController().navigate(R.id.cartFragment)
            }
        }
    }

    private fun redirectTOProducts() {
        val mGroupID = ""
        val mCategoryID = ""

        try {

            if (mGroupID != "A" && mCategoryID != "A") {
                val mGroupModel = ProductGroupModel(
                    "",
                    mGroupID,
                    true,
                    "",
                    true,
                    ArrayList()
                )

                EventBus.getDefault().post(mGroupModel)

                val mCategoryModel = ProductCategoryModel(
                    "",
                    mCategoryID,
                    mGroupID,
                    mCategoryActive = true,
                    mDoNotDisplayOn = true,
                    mCategoryImageUrl = "",
                    mCategorySequence = 1,
                    isCompulsory = true,
                    mModifierSelection = 1,
                    mProductList = ArrayList()
                )

                Handler(Looper.getMainLooper()).postDelayed({
                    EventBus.getDefault().post(mCategoryModel)
                }, 50)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun productSearch() {

        val navHostFragment = childFragmentManager.findFragmentById(R.id.main_product_host_fragment)

        mBinding.editTextSearchProducts.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                mBinding.editTextSearchProducts.setText("a")
                Handler(Looper.getMainLooper()).postDelayed({
                    mBinding.editTextSearchProducts.setText("")
                }, 10)
            }
        }

        mBinding.editTextSearchProducts.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                mGroupID = ""

                if (s!!.isNotEmpty()) {
                    navHostFragment?.let { navFragment ->
                        navFragment.childFragmentManager.primaryNavigationFragment?.let {
                            //DO YOUR STUFF
                            when (it.javaClass.simpleName) {
                                ProductGroupFragment::class.java.simpleName -> {
                                    EventBus.getDefault().post(
                                        MainProductSearchEvent(
                                            true,
                                            s.toString()
                                        )
                                    )
                                }
                                ProductCategoryFragment::class.java.simpleName -> {
                                    EventBus.getDefault().post(
                                        MainProductSearchEvent(
                                            true,
                                            s.toString()
                                        )
                                    )
                                }
                                ProductFragment::class.java.simpleName -> {
                                    val productFragment: ProductFragment = it as ProductFragment
                                    productFragment.setupSearch(s.toString())
                                }
                            }

                            e(javaClass.simpleName, "Current Fragment: ${it.javaClass.simpleName}")
                        }
                    }
                }


            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* not required */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                /* not required */
            }

        })
    }


    @Subscribe
    fun onFlowChanged(mEvent: MainProductFlowEvent) {

        e(javaClass.simpleName, "onFlowChanged ${mEvent.mCurrentType}")

        when (mEvent.mCurrentType) {
            GROUP -> {
                mBinding.imageViewArrowGroups.visibility = INVISIBLE
                mBinding.imageViewArrowCategory.visibility = INVISIBLE
                mBinding.textViewNavigationCategories.visibility = INVISIBLE
                mBinding.textViewNavigationProducts.visibility = INVISIBLE
                mBinding.textViewNavigationGroups.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                mBinding.textViewNavigationGroups.typeface = DEFAULT_BOLD
                mBinding.editTextSearchProducts.text.clear()
            }
            CATEGORY -> {
                mBinding.imageViewArrowGroups.visibility = VISIBLE
                mBinding.textViewNavigationCategories.visibility = VISIBLE
                mBinding.textViewNavigationCategories.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                mBinding.textViewNavigationCategories.typeface = DEFAULT_BOLD
                mBinding.textViewNavigationGroups.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlack))
                mBinding.textViewNavigationGroups.typeface = DEFAULT
                mBinding.textViewNavigationProducts.visibility = INVISIBLE
                mBinding.imageViewArrowCategory.visibility = INVISIBLE
                mBinding.editTextSearchProducts.text.clear()
            }
            PRODUCT -> {
                mBinding.imageViewArrowGroups.visibility = VISIBLE
                mBinding.textViewNavigationCategories.visibility = VISIBLE
                mBinding.imageViewArrowCategory.visibility = VISIBLE
                mBinding.textViewNavigationProducts.visibility = VISIBLE
                mBinding.textViewNavigationProducts.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                mBinding.textViewNavigationProducts.typeface = DEFAULT_BOLD
                mBinding.textViewNavigationCategories.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorBlack
                    )
                )
                mBinding.textViewNavigationCategories.typeface = DEFAULT
                mBinding.textViewNavigationGroups.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorBlack))
                mBinding.textViewNavigationGroups.typeface = DEFAULT
            }
        }
    }

    @Subscribe
    fun onProductClicked(mProduct: ProductModel) {
        Utils.hideKeyboardFrom(requireActivity(), mBinding.editTextSearchProducts)

        if (prefs.getLocationServiceType() == SERVICE_DINE_IN) {

            if (isTablet){
                val dialog = AddToTabDialog(mProduct.mGroupID ?: "", mProduct.mCategoryID ?: "", mProduct.mProductID)
                dialog.isCancelable = false
                dialog.show(childFragmentManager, "AddToTabDialog")
            } else {
                val action =
                    MainProductFragmentDirections.actionMainProductFragmentToAddToTabFragment(
                        mProduct.mGroupID ?: "",
                        mProduct.mCategoryID ?: "",
                        mProduct.mProductID
                    )
                findNavController().navigate(action)
            }


        } else if (prefs.getLocationServiceType() == SERVICE_QUICK_SERVICE) {
            if (isTablet){
                //check tableId
                openAddToTab(mProduct)
            } else {
                val action =
                    QuickServiceFragmentDirections.actionQuickServiceFragmentToAddToTabFragment(
                        mProduct.mGroupID ?: "",
                        mProduct.mCategoryID ?: "",
                        mProduct.mProductID
                    )
                findNavController().navigate(action)
            }
        }

    }

    private fun openAddToTab(mProduct: ProductModel) {
        val dialog = AddToTabDialog(mProduct.mGroupID ?: "", mProduct.mCategoryID ?: "", mProduct.mProductID)
        dialog.isCancelable = false
        dialog.show(childFragmentManager, "AddToTabDialog")
    }

    @Subscribe
    fun onTableClicked(mTable: TablesModel) {
        val mTableNO = "Table " + mTable.mTableNo
        mBinding.textViewTableNo.text = mTableNO
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
