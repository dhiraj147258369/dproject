package com.rsl.youresto.ui.main_screen.pending_order


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.App
import com.rsl.youresto.R
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.database_download.models.LocationModel
import com.rsl.youresto.data.database_download.models.ServerModel
import com.rsl.youresto.databinding.FragmentPendingOrderBinding
import com.rsl.youresto.ui.main_screen.cart.CartViewModel
import com.rsl.youresto.ui.main_screen.cart.CartViewModelFactory
import com.rsl.youresto.ui.main_screen.pending_order.event.PendingOrderDeleteEvent
import com.rsl.youresto.ui.main_screen.pending_order.event.PendingOrderEvent
import com.rsl.youresto.ui.server_login.ServerLoginViewModel
import com.rsl.youresto.utils.Animations.runLayoutAnimationFallDown
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.INTENT_FROM
import com.rsl.youresto.utils.AppConstants.LOCATION_SERVICE_TYPE
import com.rsl.youresto.utils.AppConstants.LOGGED_IN_SERVER_NAME
import com.rsl.youresto.utils.AppConstants.PENDING_ORDER_FRAGMENT
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_CART_ID
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_CART_NO
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_NAME
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_NO
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppConstants.SERVICE_QUICK_SERVICE
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.custom_dialog.AlertDialogEvent
import com.rsl.youresto.utils.custom_dialog.CustomAlertDialogFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

@SuppressLint("LogNotTimber")
class PendingOrderFragment : Fragment()  {

    private lateinit var mBinding: FragmentPendingOrderBinding
    private lateinit var mServerLoginViewModel: ServerLoginViewModel
    private lateinit var mCartViewModel: CartViewModel

    private var mSharedPref: SharedPreferences? = null

    private var mLocationList: ArrayList<String>? = null
    private var mServerList: ArrayList<String>? = null

    private var mLocationID = ""
    private var mServerID = ""

    private var mViewCreated = true

    companion object{
        const val ASCENDING_ORDER = 1
        const val DESCENDING_ORDER = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_pending_order, container, false)
        val mView = mBinding.root

        mSharedPref = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        val factory = InjectorUtils.provideServerLoginViewModelFactory(requireActivity().applicationContext)
        mServerLoginViewModel = ViewModelProviders.of(this, factory).get(ServerLoginViewModel::class.java)

        val cartFactory: CartViewModelFactory = InjectorUtils.provideCartViewModelFactory(requireActivity().applicationContext)
        mCartViewModel = ViewModelProviders.of(this, cartFactory).get(CartViewModel::class.java)

        mLocationID = mSharedPref?.getString(SELECTED_LOCATION_ID, "") ?: ""

//        setUpLocationFilter()
        setUpServerFilter()

        Handler(Looper.getMainLooper()).postDelayed({
            if(mViewCreated) {
                setUpRecyclerView()
                //initOnClickListeners()
            }
        },200)

        return mView
    }

    private var mLocationSelectedListener: AdapterView.OnItemSelectedListener? = null

    private var mLocationModelList: ArrayList<LocationModel>? = ArrayList()
    private var mServerModelList: ArrayList<ServerModel>? = ArrayList()

    private fun initOnClickListeners() {
        mBinding.linearOrderNo.setOnClickListener {
            if(mCartList!!.isNotEmpty()) {
                mOrderNoOrder = if (mOrderNoOrder == DESCENDING_ORDER){
                    mCartList!!.sortWith { c1, c2 ->
                        c1.mCartNO.compareTo(c2.mCartNO)
                    }

                    ASCENDING_ORDER
                } else {
                    mCartList!!.sortWith { c1, c2 ->
                        c2.mCartNO.compareTo(c1.mCartNO)
                    }
                    DESCENDING_ORDER
                }
                mPendingAdapter?.notifyDataSetChanged()
            }

        }

        mBinding.linearTableNo.setOnClickListener {
            if(mCartList!!.isNotEmpty()) {
                mTableOrder = if (mTableOrder == DESCENDING_ORDER){
                    mCartList!!.sortWith { c1, c2 ->
                        c1.mTableNO.compareTo(c2.mTableNO)
                    }

                    ASCENDING_ORDER
                }else{

                    mCartList!!.sortWith { c1, c2 ->
                        c2.mTableNO.compareTo(c1.mTableNO)
                    }

                    DESCENDING_ORDER
                }
                mPendingAdapter?.notifyDataSetChanged()
            }
        }

        mBinding.linearTime.setOnClickListener {
            if(mCartList!!.isNotEmpty()) {
                mTimeOrder = if (mTimeOrder == DESCENDING_ORDER){
                    mCartList!!.sortWith { c1, c2 ->
                        c1.mDateInMillis.compareTo(c2.mDateInMillis)
                    }
                    ASCENDING_ORDER
                }else{
                    mCartList!!.sortWith { c1, c2 ->
                        c2.mDateInMillis.compareTo(c1.mDateInMillis)
                    }
                    DESCENDING_ORDER
                }
                mPendingAdapter?.notifyDataSetChanged()
            }
        }
    }

    private fun setUpLocationFilter() {
        mLocationList = ArrayList()
        mLocationModelList!!.add(LocationModel("","ALL", "1"))
        mLocationList!!.add("ALL")

        mServerLoginViewModel.getLocations().observe(viewLifecycleOwner, {
            when {
                it.isNotEmpty() -> {
                    for (i in it.indices) {
                        mLocationModelList!!.add(it[i])
                        mLocationList!!.add(it[i].mLocationName)
                    }

                    val mLocationAdapter =
                        ArrayAdapter(requireActivity(), R.layout.spinner_item_pending_order, mLocationList!!)
                    mLocationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    mBinding.spinnerLocation.adapter = mLocationAdapter

                    mLocationSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            /*Not required*/
                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            mLocationID = mLocationModelList!![position].mLocationID

                            if(!mViewCreated)
                                setUpRecyclerView()
                        }
                    }

                    var mSelectedLocationPosition = 0
                    for(j in 0 until mLocationList!!.size) {
                        if(mLocationList!![j] == mSharedPref!!.getString(SELECTED_LOCATION_NAME,"")) {
                            mSelectedLocationPosition = j
                            break
                        }
                    }
                    mBinding.spinnerLocation.setSelection(mSelectedLocationPosition)

                    mBinding.spinnerLocation.onItemSelectedListener = mLocationSelectedListener
                }
            }
        })
    }

    private var mOrderNoOrder = DESCENDING_ORDER
    private var mTableOrder = DESCENDING_ORDER
    private var mTimeOrder = DESCENDING_ORDER

    private var mServerSelectedListener: AdapterView.OnItemSelectedListener? = null

    private fun setUpServerFilter() {
        mServerList = ArrayList()
        mServerModelList!!.add(ServerModel("","ALL", "", "1", ArrayList()))
        mServerList!!.add("ALL")

        mServerLoginViewModel.getServers().observe(viewLifecycleOwner, {
            when {
                it.isNotEmpty() -> {
                    for(i in it.indices) {
                        mServerModelList!!.add(it[i])
                        mServerList!!.add(it[i].mServerName)
                    }

                    val mServerAdapter =
                        ArrayAdapter(requireActivity(), R.layout.spinner_item_pending_order, mServerList!!)
                    mServerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    mBinding.spinnerServer.adapter = mServerAdapter

                    mServerSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            /*Not required*/
                        }

                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            mServerID = mServerModelList!![position].mServerID

                            if(!mViewCreated)
                                setUpRecyclerView()
                        }
                    }

                    var mSelectedServerPosition = 0
                    loop@ for(j in 0 until mServerList!!.size) {
                        when {
                            mServerList!![j] == mSharedPref!!.getString(LOGGED_IN_SERVER_NAME,"") -> {
                                mSelectedServerPosition = j
                                break@loop
                            }
                        }
                    }
                    mBinding.spinnerServer.setSelection(mSelectedServerPosition)

                    mBinding.spinnerServer.onItemSelectedListener = mServerSelectedListener
                }
            }
        })
    }

    private var mCartObserver: Observer<List<CartProductModel>>? = null

    private var mCartList: ArrayList<CartProductModel>? =null
    private var mPendingAdapter: PendingOrderAdapter? =null

    private fun setUpRecyclerView() {
        mViewCreated = false

        e(javaClass.simpleName,"location: $mLocationID , Server $mServerID")

        val mCartData = mCartViewModel.getPendingOrderCartData(mLocationID,mServerID)
        mCartObserver = Observer {
            if(it.isNotEmpty()) {
                mCartList = ArrayList(it)

                mCartList!!.sortWith { c1, c2 ->
                    c2.mDateInMillis.compareTo(c1.mDateInMillis)
                }

                e(javaClass.simpleName,"size: ${it.size}")
                mBinding.constraintLayoutNoPendingOrder.visibility = GONE

                mPendingAdapter = PendingOrderAdapter(mCartList!!)
                mBinding.recyclerViewPendingOrder.adapter = mPendingAdapter
                runLayoutAnimationFallDown(mBinding.recyclerViewPendingOrder)

                initOnClickListeners()
                //mCartData.removeObserver(mCartObserver!!)
            } else {
                mBinding.recyclerViewPendingOrder.adapter = null
                mBinding.constraintLayoutNoPendingOrder.visibility = VISIBLE
            }
        }
        mCartData.observe(viewLifecycleOwner,mCartObserver!!)
    }

    @Subscribe
    fun onPendingOrderClick(mEvent: PendingOrderEvent) {
        if(mEvent.mResult) {
            if(mEvent.mCart.mOrderType == SERVICE_DINE_IN) {
                mSharedPref!!.edit().putString(SELECTED_TABLE_ID,mEvent.mCart.mTableID).apply()
                mSharedPref!!.edit().putInt(SELECTED_TABLE_NO,mEvent.mCart.mTableNO).apply()
                mSharedPref!!.edit().putInt(LOCATION_SERVICE_TYPE, SERVICE_DINE_IN).apply()

            } else if(mEvent.mCart.mOrderType == SERVICE_QUICK_SERVICE) {
                mSharedPref!!.edit().putInt(LOCATION_SERVICE_TYPE, SERVICE_QUICK_SERVICE).apply()
                mSharedPref!!.edit().putString(SELECTED_TABLE_ID,mEvent.mCart.mTableID).apply()
                mSharedPref!!.edit().putInt(SELECTED_TABLE_NO,mEvent.mCart.mTableNO).apply()
                mSharedPref!!.edit().putString(QUICK_SERVICE_CART_ID, mEvent.mCart.mCartID).apply()
                mSharedPref!!.edit().putString(QUICK_SERVICE_CART_NO, mEvent.mCart.mCartNO).apply()
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (!App.isTablet) {
                    val action = PendingOrderFragmentDirections.actionPendingOrderFragmentToCartGroupFragment(mEvent.mCart.mGroupName, PENDING_ORDER_FRAGMENT)
                    findNavController().navigate(action)
                } else {
                    if(mEvent.mCart.mOrderType == SERVICE_QUICK_SERVICE) {
                        findNavController().navigate(R.id.quickServiceTabFragment, bundleOf(INTENT_FROM to "PendingOrderFragment"))
                    } else {
                        findNavController().navigate(R.id.tablesTabFragment, bundleOf(INTENT_FROM to "PendingOrderFragment"))
                    }

                }
            },200)
        }
    }

    private var mCart: CartProductModel? = null

    @Subscribe
    fun onLongPressPendingOrder(mEvent: PendingOrderDeleteEvent) {
        if(mEvent.mResult) {
            mCart = mEvent.mCart

            val mCustomDialogFragment = CustomAlertDialogFragment.newInstance(1, javaClass.simpleName, R.drawable.ic_delete_forever_primary_36dp,
                "Delete Order","Are you sure you want to delete this order?",
                "Yes, Delete","No, Don't", R.drawable.ic_check_black_24dp, R.drawable.ic_close_black_24dp)
            mCustomDialogFragment.show(childFragmentManager, AppConstants.CUSTOM_DIALOG_FRAGMENT)
        }
    }

    @Subscribe
    fun onAlertDialogEvent(mEvent: AlertDialogEvent){
        when {
            mEvent.mSource == javaClass.simpleName && mEvent.mActionType -> mCartViewModel.getTableCartDataForCartID(mCart!!.mTableNO, mCart!!.mCartID).observe(viewLifecycleOwner,
                {
                    when {
                        it.isNotEmpty() -> {
                            var mCheckKOT = false
                            loop@ for(element in it) {
                                when (element.mKitchenPrintFlag) {
                                    1 -> {
                                        mCheckKOT = true
                                        break@loop
                                    }
                                }
                            }
                            when {
                                mCheckKOT -> {
                                    val mCustomDialogFragment = CustomAlertDialogFragment.newInstance(1, javaClass.simpleName, R.drawable.ic_delete_forever_primary_36dp,
                                        "Table Occupied","You cannot delete this order.",
                                        "","Okay", 0, R.drawable.ic_close_black_24dp)
                                    mCustomDialogFragment.show(childFragmentManager, AppConstants.CUSTOM_DIALOG_FRAGMENT)
                                }
                                else -> mCartViewModel.deleteCartOnServer(mCart!!).observe(viewLifecycleOwner,
                                    { i ->
                                    when {
                                        i != null && i > 0 -> {
                                            e(javaClass.simpleName, "i: $i product deleted on server")
                                            setUpRecyclerView()
                                        }
                                    }
                                })
                            }
                        }
                    }
                })
        }
    }

    //TODO: HANDLE BACK PRESS
//    override fun handleOnBackPressed(): Boolean {
//        pressAgainMethod()
//        return true
//    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

}
