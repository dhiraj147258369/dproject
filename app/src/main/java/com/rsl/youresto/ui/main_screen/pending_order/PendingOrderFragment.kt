package com.rsl.youresto.ui.main_screen.pending_order


import android.annotation.SuppressLint
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
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.App
import com.rsl.youresto.R
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.database_download.models.ServerModel
import com.rsl.youresto.databinding.FragmentPendingOrderBinding
import com.rsl.youresto.ui.main_screen.cart.NewCartViewModel
import com.rsl.youresto.ui.main_screen.pending_order.event.PendingOrderDeleteEvent
import com.rsl.youresto.ui.main_screen.pending_order.event.PendingOrderEvent
import com.rsl.youresto.ui.server_login.ServerLoginViewModel
import com.rsl.youresto.utils.Animations.runLayoutAnimationFallDown
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.INTENT_FROM
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppConstants.SERVICE_QUICK_SERVICE
import com.rsl.youresto.utils.AppPreferences
import com.rsl.youresto.utils.custom_dialog.AlertDialogEvent
import com.rsl.youresto.utils.custom_dialog.CustomAlertDialogFragment
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("LogNotTimber")
class PendingOrderFragment : Fragment()  {

    private lateinit var mBinding: FragmentPendingOrderBinding
    private val serverViewModel: ServerLoginViewModel by viewModel()
    private val cartViewModel: NewCartViewModel by viewModel()
    private val prefs: AppPreferences by inject()

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

        mLocationID = prefs.getSelectedLocation()

        setUpServerFilter()

        Handler(Looper.getMainLooper()).postDelayed({
            if(mViewCreated) {
                setUpRecyclerView()
                //initOnClickListeners()
            }
        },200)

        cartViewModel.syncCarts()

        return mView
    }

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

    private var mOrderNoOrder = DESCENDING_ORDER
    private var mTableOrder = DESCENDING_ORDER
    private var mTimeOrder = DESCENDING_ORDER

    private var mServerSelectedListener: AdapterView.OnItemSelectedListener? = null

    private fun setUpServerFilter() {
        mServerList = ArrayList()
        mServerModelList!!.add(ServerModel("","ALL", "", "1", ArrayList()))
        mServerList!!.add("ALL")

        serverViewModel.getServers().observe(viewLifecycleOwner, {
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

//                    var mSelectedServerPosition = 0
//                    loop@ for(j in 0 until mServerList!!.size) {
//                        when {
//                            mServerList!![j] == mSharedPref!!.getString(LOGGED_IN_SERVER_NAME,"") -> {
//                                mSelectedServerPosition = j
//                                break@loop
//                            }
//                        }
//                    }
//                    mBinding.spinnerServer.setSelection(mSelectedServerPosition)

                    mBinding.spinnerServer.onItemSelectedListener = mServerSelectedListener
                }
            }
        })
    }

    private var mCartList: ArrayList<CartProductModel>? =null
    private var mPendingAdapter: PendingOrderAdapter? =null

    private fun setUpRecyclerView() {
        mViewCreated = false

        cartViewModel.getPendingOrderCartData().observe(viewLifecycleOwner) {
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
    }

    @Subscribe
    fun onPendingOrderClick(mEvent: PendingOrderEvent) {
        if(mEvent.mResult) {
            if(mEvent.mCart.mOrderType == SERVICE_DINE_IN) {
                prefs.setTable(mEvent.mCart.mTableID, mEvent.mCart.mTableNO)
                prefs.setSelectedLocationType(SERVICE_DINE_IN)

            } else if(mEvent.mCart.mOrderType == SERVICE_QUICK_SERVICE) {
                prefs.setTable(mEvent.mCart.mTableID, mEvent.mCart.mTableNO)
                prefs.setSelectedLocationType(SERVICE_QUICK_SERVICE)
                prefs.setQuickServiceCartId(mEvent.mCart.mCartID)
            }

            Handler(Looper.getMainLooper()).postDelayed({
                if (!App.isTablet) {
                    findNavController().navigate(R.id.cartFragment)
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
            mEvent.mSource == javaClass.simpleName && mEvent.mActionType -> {

                mCart?.let { cart ->
                    if (cart.mOrderType == SERVICE_QUICK_SERVICE){
                        cartViewModel.deleteCart(null, cart.mCartID)
                    } else {
                        cartViewModel.deleteCart(cart.mTableID, null)
                    }
                }


            }
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
