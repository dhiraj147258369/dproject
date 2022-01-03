package com.rsl.youresto.ui.main_screen.main_product_flow


import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.graphics.Typeface.DEFAULT
import android.graphics.Typeface.DEFAULT_BOLD
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.R
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.database_download.models.ProductCategoryModel
import com.rsl.youresto.data.database_download.models.ProductGroupModel
import com.rsl.youresto.data.database_download.models.ProductModel
import com.rsl.youresto.data.database_download.models.TablesModel
import com.rsl.youresto.data.main_product.models.CartBarModel
import com.rsl.youresto.data.tables.models.ServerTableGroupModel
import com.rsl.youresto.databinding.FragmentMainProductBinding
import com.rsl.youresto.ui.main_screen.cart.CartViewModel
import com.rsl.youresto.ui.main_screen.cart.CartViewModelFactory
import com.rsl.youresto.ui.main_screen.cart.NewCartViewModel
import com.rsl.youresto.ui.main_screen.main_product_flow.event.CartBarGroupEvent
import com.rsl.youresto.ui.main_screen.main_product_flow.event.MainProductFlowEvent
import com.rsl.youresto.ui.main_screen.main_product_flow.event.MainProductNavigateEvent
import com.rsl.youresto.ui.main_screen.main_product_flow.event.MainProductSearchEvent
import com.rsl.youresto.ui.main_screen.main_product_flow.product.ProductFragment
import com.rsl.youresto.ui.main_screen.main_product_flow.product_category.ProductCategoryFragment
import com.rsl.youresto.ui.main_screen.main_product_flow.product_group.ProductGroupFragment
import com.rsl.youresto.ui.main_screen.pending_order.PendingOrderFragment
import com.rsl.youresto.ui.main_screen.quick_service.QuickServiceFragmentDirections
import com.rsl.youresto.ui.main_screen.tables_and_tabs.AddToTabDialog
import com.rsl.youresto.ui.main_screen.tables_and_tabs.tables.TablesViewModel
import com.rsl.youresto.utils.AppConstants.CATEGORY
import com.rsl.youresto.utils.AppConstants.GROUP
import com.rsl.youresto.utils.AppConstants.INTENT_FROM
import com.rsl.youresto.utils.AppConstants.LOCATION_SERVICE_TYPE
import com.rsl.youresto.utils.AppConstants.MAIN_PRODUCT_FRAGMENT
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.PRODUCT
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_CART_ID
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_TABLE_NO
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_NO
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppConstants.SERVICE_QUICK_SERVICE
import com.rsl.youresto.utils.AppPreferences
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.Utils
import com.rsl.youresto.utils.custom_views.CustomToast
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
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mViewModel: TablesViewModel
    private lateinit var mCartViewModel: CartViewModel
    private lateinit var mTableID: String
    private var mSelectedLocationType: Int? = null
    private var mIntentFrom: String = "A"

    private var isTablet: Boolean = false
    private val prefs: AppPreferences by inject()
    private val productViewModel: NewProductViewModel by viewModel()
    private val cartViewModel: NewCartViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_product, container, false)
        val mView = mBinding.root

        isTablet = resources.getBoolean(R.bool.isTablet)

        val factory = InjectorUtils.provideTablesViewModelFactory(requireActivity())
        mViewModel = ViewModelProviders.of(this, factory).get(TablesViewModel::class.java)

        val cartFactory: CartViewModelFactory = InjectorUtils.provideCartViewModelFactory(requireActivity())
        mCartViewModel = ViewModelProviders.of(this, cartFactory).get(CartViewModel::class.java)

        mSharedPrefs = requireActivity().getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)

        mSelectedLocationType = mSharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0)

//        mIntentFrom = if (MainProductFragmentArgs.fromBundle(requireArguments()).intentFrom != null)
//            MainProductFragmentArgs.fromBundle(requireArguments()).intentFrom ?: ""
//        else
//            requireArguments().getString(INTENT_FROM) ?: ""

        mIntentFrom = arguments?.getString(INTENT_FROM) ?: ""

        e(javaClass.simpleName, "mIntentFrom: $mIntentFrom")

        mTableID = mSharedPrefs.getString(SELECTED_TABLE_ID, "") ?: ""

//        if (mSelectedLocationType == SERVICE_DINE_IN) {
//            mTableID = mSharedPrefs.getString(SELECTED_TABLE_ID, "")!!
//            getCartBarData()
//        } else if (mSelectedLocationType == SERVICE_QUICK_SERVICE) {
//            mTableID = mSharedPrefs.getString(QUICK_SERVICE_TABLE_ID, "")!!
//            getCartBarDataForQuickService()
//        }

        initViews()
        checkLocationServiceType()
        return mView
    }

    private var mTableGroupObserver: Observer<TablesModel>? = null
    private var mTableGroupList: ArrayList<ServerTableGroupModel> = ArrayList()

    private var mCartData: LiveData<List<CartProductModel>>? = null
    private var mCartObserver: Observer<List<CartProductModel>>? = null

    private var mCartList: ArrayList<CartProductModel> = ArrayList()

    private fun getCartBarDataForQuickService() {
        mTableGroupList.clear()
        mCartList.clear()

        if (mSharedPrefs.getString(QUICK_SERVICE_CART_ID, "") != "") {
            val mTableGroupData = mViewModel.getTable(mTableID)
            mTableGroupObserver = Observer {
                if (it != null) {

                    mTableGroupList.addAll(it.mGroupList!!)

                    mCartData = mCartViewModel.getTableCartDataForCartID(
                        mSharedPrefs.getInt(QUICK_SERVICE_TABLE_NO, 0),
                        mSharedPrefs.getString(QUICK_SERVICE_CART_ID, "")!!
                    )

                    mCartObserver = Observer { cartProductModel ->
                        if (cartProductModel.isNotEmpty()) {
                            mCartList.addAll(cartProductModel)
                            setCartBarRecycler()
                            mCartData!!.removeObserver(mCartObserver!!)
                        }
                    }
                    mCartData!!.observe(viewLifecycleOwner, mCartObserver!!)

                    mTableGroupData.removeObserver(mTableGroupObserver!!)
                }
            }
            mTableGroupData.observe(viewLifecycleOwner, mTableGroupObserver!!)

        }

    }

    private fun getCartBarData() {
        mTableGroupList.clear()
        mCartList.clear()

        val mTableGroupData = mViewModel.getTable(mTableID)
        mTableGroupObserver = Observer {
            if (it != null) {

                mTableGroupList.addAll(it.mGroupList!!)

                mCartData = mCartViewModel.getTableCartData(mSharedPrefs.getInt(SELECTED_TABLE_NO, 0))
                mCartObserver = Observer { cartProductModel ->
                    if (cartProductModel.isNotEmpty()) {
                        mCartList.addAll(cartProductModel)
                        setCartBarRecycler()
                        mCartData!!.removeObserver(mCartObserver!!)
                    }
                }
                mCartData!!.observe(viewLifecycleOwner, mCartObserver!!)

                mTableGroupData.removeObserver(mTableGroupObserver!!)
            }
        }
        mTableGroupData.observe(viewLifecycleOwner, mTableGroupObserver!!)
    }

    private var mCartItemCount: Int = 0
    private var mCartBarList: ArrayList<CartBarModel> = ArrayList()

    private fun setCartBarRecycler() {
        mCartBarList.clear()

        for (i in 0 until mTableGroupList.size) {
            mCartItemCount = 0
            for (j in 0 until mCartList.size) {
                if (mTableGroupList[i].mGroupName == mCartList[j].mGroupName) {
                    mCartItemCount++
                }
                if (j == mCartList.size - 1 && mCartItemCount > 0) {
                    val mCartBarModel = CartBarModel(
                        mTableGroupList[i].mCartID,
                        mTableGroupList[i].mCartNO,
                        mTableGroupList[i].mTableOccupiedID,
                        mTableGroupList[i].mGroupName,
                        mTableGroupList[i].mSeatList,
                        mTableGroupList[i].mGroupTotal,
                        mCartItemCount
                    )
                    mCartBarList.add(mCartBarModel)
                }
            }
        }

        val tempList = mCartBarList.sortedBy { group -> group.mGroupName }

        mCartBarList.clear()
        mCartBarList.addAll(tempList)

        val mCartBarAdapter = CartBarGroupAdapter(mCartBarList)
        mBinding.recyclerViewCartBar.adapter = mCartBarAdapter
    }

    private fun checkLocationServiceType() {
        if (mSharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0) != SERVICE_DINE_IN) {
            mBinding.textViewTableNo.visibility = GONE
            mBinding.textViewGuestsLabel.visibility = GONE
            mBinding.textViewOccupiedChairs.visibility = GONE
        }
    }

    var mGroupID: String = ""

    private fun initViews() {
        val mTableNO = "Table " + mSharedPrefs.getInt(SELECTED_TABLE_NO, 0)
        mBinding.textViewTableNo.text = mTableNO

        mViewModel.getTableGroupsAndSeats(mTableID).observe(viewLifecycleOwner, {
            if (it != null) {
                var mSeatCount = 0
                for (element in it) mSeatCount += element.mSeatList!!.size
                mBinding.textViewOccupiedChairs.text = mSeatCount.toString()
            }
        })

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

        mBinding.textViewOccupiedChairs.setOnClickListener {
            val action = MainProductFragmentDirections.actionMainProductFragmentToSeatSelectionFragment(
                mTableID,
                javaClass.simpleName
            )
            findNavController().navigate(action)
        }

        productSearch()
        Handler().postDelayed({
            redirectTOProducts()
        }, 10)


    }

    private fun redirectTOProducts() {
        val mGroupID = ""
        val mCategoryID = ""

        if(mSelectedLocationType == SERVICE_QUICK_SERVICE) {
//            mGroupID = requireArguments().getString(AppConstants.GROUP_ID)!!
//            mCategoryID = requireArguments().getString(AppConstants.CATEGORY_ID)!!
        } else {
//            mGroupID = MainProductFragmentArgs.fromBundle(requireArguments()).groupId!!
//            mCategoryID = MainProductFragmentArgs.fromBundle(requireArguments()).categoryId!!
        }
        try {
//            val mGroupID = MainProductFragmentArgs.fromBundle(requireArguments()).GROUPID
//            val mCategoryID = MainProductFragmentArgs.fromBundle(requireArguments()).CategoryID

            e(javaClass.simpleName, "mGroupID: $mGroupID && mCategoryID: $mCategoryID")

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

                Handler().postDelayed({
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
                Handler().postDelayed({
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
                mBinding.textViewNavigationGroups.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorRed))
                mBinding.textViewNavigationGroups.typeface = DEFAULT_BOLD
                mBinding.editTextSearchProducts.text.clear()
            }
            CATEGORY -> {
                mBinding.imageViewArrowGroups.visibility = VISIBLE
                mBinding.textViewNavigationCategories.visibility = VISIBLE
                mBinding.textViewNavigationCategories.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorRed))
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
                mBinding.textViewNavigationProducts.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorRed))
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

                    if (mIntentFrom == PendingOrderFragment::class.simpleName){
                        openAddToTab(mProduct)
                        return
                    }

                lifecycleScope.launch {

                    mTableID = mSharedPrefs.getString(SELECTED_TABLE_ID, "") ?: ""

                    val cart = withContext(Dispatchers.IO){
                        cartViewModel.getCartByTable(mTableID)
                    }

                    val table = withContext(Dispatchers.IO){
                        productViewModel.getQuickServiceTable()
                    }

                    when {
                        table != null -> {
                            prefs.setTable(table.mTableID, table.mTableNo)
                            openAddToTab(mProduct)
                        }
                        cart.isNotEmpty() -> {
                            openAddToTab(mProduct)
                        }
                        else -> {
                            CustomToast.makeText(requireActivity(), "There is no empty tables", Toast.LENGTH_SHORT).show()
                            return@launch
                        }
                    }
                }

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
    fun onCartBarGroupClicked(mEvent: CartBarGroupEvent) {
        if (mEvent.mResult) {
            if (mSelectedLocationType == SERVICE_DINE_IN) {
                val action = MainProductFragmentDirections.actionMainProductFragmentToCartGroupFragment(
                    mEvent.mCartBarModel.mGroupName,
                    MAIN_PRODUCT_FRAGMENT
                )
                findNavController().navigate(action)
            } else if (mSelectedLocationType == SERVICE_QUICK_SERVICE) {
                val action = QuickServiceFragmentDirections.actionQuickServiceFragmentToCartGroupFragment(
                    mEvent.mCartBarModel.mGroupName,
                    MAIN_PRODUCT_FRAGMENT
                )
                findNavController().navigate(action)
            }
        }
    }

    @Subscribe
    fun onTableClicked(mTable: TablesModel) {
        val mTableNO = "Table " + mTable.mTableID
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

    //TODO: HANDLE BACK PRESS
//    override fun handleOnBackPressed(): Boolean {
//        val mDrawerVisibility = (activity as MainScreenActivity).checkNavigationDrawerVisibility()
//
//        if (!mDrawerVisibility){
//            when (mIntentFrom) {
//                TablesFragment::class.java.simpleName -> findNavController().navigate(R.id.action_mainProductFragment_to_tablesFragment)
//                SeatSelectionFragment::class.java.simpleName -> {
//                    val action = MainProductFragmentDirections.actionMainProductFragmentToSeatSelectionFragment(
//                        mTableID,
//                        TablesFragment::class.java.simpleName
//                    )
//                    findNavController().navigate(action)
//                }
//                CartGroupFragment::class.java.simpleName -> findNavController().navigate(R.id.action_mainProductFragment_to_tablesFragment)
//            }
//
//            if (mSelectedLocationType == SERVICE_QUICK_SERVICE) pressAgainMethod()
//        }
//
//        return true
//    }

}
