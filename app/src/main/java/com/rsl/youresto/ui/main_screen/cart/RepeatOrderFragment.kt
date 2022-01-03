package com.rsl.youresto.ui.main_screen.cart


import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.R
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.databinding.FragmentRepeatOrderBinding
import com.rsl.youresto.ui.main_screen.cart.event.RepeatOrderEvent
import com.rsl.youresto.utils.Animations
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.Network
import com.rsl.youresto.utils.custom_dialog.CustomProgressDialog
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class RepeatOrderFragment : Fragment() {
    private lateinit var mBinding: FragmentRepeatOrderBinding
    private lateinit var mCartViewModel: CartViewModel
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mTableID: String
    private var mTableNO: Int = 0
    private var mSelectedLocationType: Int? = null
    private var mGroupName: String? = null
    private var mCartID: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_repeat_order, container, false)
        val mView = mBinding.root

        mSharedPrefs =
            requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        val cartFactory: CartViewModelFactory =
            InjectorUtils.provideCartViewModelFactory(requireActivity())
        mCartViewModel = ViewModelProviders.of(this, cartFactory).get(CartViewModel::class.java)

        mSelectedLocationType = mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE, 0)

        if (mSelectedLocationType == AppConstants.SERVICE_DINE_IN) {
            mTableID = mSharedPrefs.getString(AppConstants.SELECTED_TABLE_ID, "")!!
            mTableNO = mSharedPrefs.getInt(AppConstants.SELECTED_TABLE_NO, 0)

            mGroupName = RepeatOrderFragmentArgs.fromBundle(requireArguments()).groupName
        } else if (mSelectedLocationType == AppConstants.SERVICE_QUICK_SERVICE) {
            mTableID = mSharedPrefs.getString(AppConstants.QUICK_SERVICE_TABLE_ID, "")!!
            mTableNO = mSharedPrefs.getInt(AppConstants.QUICK_SERVICE_TABLE_NO, 0)
            mCartID = mSharedPrefs.getString(AppConstants.QUICK_SERVICE_CART_ID, "")
            mGroupName = "Q"
        }

        getCartDetails()
        init()

        return mView
    }

    private fun init() {
        mBinding.checkBoxSelectAllSeats.setOnCheckedChangeListener(mSelectAllCheckBoxListener)

        mBinding.buttonRepeatOrder.setOnClickListener {
            repeatOrder()
        }
    }

    private var mCartProductList: ArrayList<CartProductModel>? = null
    private var mRepeatCartAdapter: RepeatOrderCartAdapter? = null
    private var mOldCartSize = 0

    private fun getCartDetails() {
        when (mSelectedLocationType) {
            AppConstants.SERVICE_DINE_IN -> mCartViewModel.getCartWithoutLiveData(
                mTableID,
                mGroupName!!
            ).observe(viewLifecycleOwner, {

                mOldCartSize = it.size
                mCartProductList = ArrayList()

                for (i in it.indices) {
                    if (it[i].mKitchenPrintFlag == 1) {
                        mCartProductList!!.add(it[i])
                    }
                }

                mRepeatCartAdapter = RepeatOrderCartAdapter(requireActivity(), mCartProductList!!)
                mBinding.recyclerViewRepeatOrder.adapter = mRepeatCartAdapter
                Animations.runLayoutAnimationFallDown(mBinding.recyclerViewRepeatOrder)
            })
            AppConstants.SERVICE_QUICK_SERVICE -> mCartViewModel.getCartDataWithCartID(mCartID!!).observe(
                viewLifecycleOwner,
                {

                    mOldCartSize = it.size
                    mCartProductList = ArrayList()

                    for (i in it.indices) {
                        if (it[i].mKitchenPrintFlag == 1) {
                            mCartProductList!!.add(it[i])
                        }
                    }

                    mRepeatCartAdapter = RepeatOrderCartAdapter(requireActivity(), mCartProductList!!)
                    mBinding.recyclerViewRepeatOrder.adapter = mRepeatCartAdapter
                    Animations.runLayoutAnimationFallDown(mBinding.recyclerViewRepeatOrder)
                })
        }
    }

    private val mSelectAllCheckBoxListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        if(isChecked) {
            for(i in mCartProductList!!.indices) {
                mCartProductList!![i].isSelectedForRepeatOrder = true
            }
            mRepeatCartAdapter!!.notifyDataSetChanged()
        } else {
            for(i in mCartProductList!!.indices) {
                mCartProductList!![i].isSelectedForRepeatOrder = false
            }
            mRepeatCartAdapter!!.notifyDataSetChanged()
        }
    }

    private fun repeatOrder() {
        Network.isNetworkAvailableWithInternetAccess(requireActivity()).observe(viewLifecycleOwner,
            {
                when {
                    it -> {
                        val mFinalProductList: ArrayList<CartProductModel> = ArrayList()
                        val mProductList = mRepeatCartAdapter!!.getCartUpdatedList()
                        mProductList.indices.forEach { i ->
                            when {
                                mProductList[i].isSelectedForRepeatOrder -> mFinalProductList.add(mProductList[i])
                            }
                        }

                        val now = Calendar.getInstance()
                        val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH)
                        val nowTime = simpleDateFormat.format(now.time)

                        mFinalProductList.indices.forEach { j ->
                            mFinalProductList[j].mSequenceNO = mOldCartSize + j + 1
                            mFinalProductList[j].mID = 0
                            mFinalProductList[j].mCartProductID = ""
                            mFinalProductList[j].mDate = nowTime
                            mFinalProductList[j].mDateInMillis = Date()
                            mFinalProductList[j].mKitchenPrintFlag = 0
                        }

                        when {
                            mFinalProductList.size > 0 -> {
                                showProgressDialog()

                                mCartViewModel.insertBulkCartProduct(mFinalProductList).observe(viewLifecycleOwner,
                                    { longs ->
                                    when {
                                        longs.isNotEmpty() -> {
                                            for(k in longs.indices) {
                                                for(l in mFinalProductList.indices) {
                                                    if(k == l) {
                                                        mFinalProductList[l].mID = longs[k].toInt()
                                                        break
                                                    }
                                                }
                                            }

                                            mCartViewModel.submitRepeatOrderItemsToServer(mFinalProductList).observe(viewLifecycleOwner,
                                                { integer ->
                                                when {
                                                    integer != null -> {
                                                        when {
                                                            mProgressDialog != null -> mProgressDialog!!.dismiss()
                                                        }
                                                        when {
                                                            integer > -1 -> when (mSelectedLocationType) {
                                                                AppConstants.SERVICE_DINE_IN -> {
                                                                    val action = RepeatOrderFragmentDirections.actionRepeatOrderFragmentToCartGroupFragment(mGroupName!!, javaClass.simpleName)
                                                                    findNavController().navigate(action)
                                                                }
                                                            }
                                                            else -> CustomToast.makeText(requireActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                                                        }
                                                    }
                                                }
                                            })
                                        }
                                    }
                                })
                            }
                            else -> CustomToast.makeText(requireActivity(),"Select product to continue", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> CustomToast.makeText(requireActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show()
                }
            })
    }

    private var mProgressDialog: CustomProgressDialog? = null

    private fun showProgressDialog() {
        mProgressDialog = CustomProgressDialog.newInstance("Submitting Product", "Please Wait..", AppConstants.DIALOG_TYPE_OTHER)
        mProgressDialog!!.show(childFragmentManager, AppConstants.CUSTOM_PROGRESS_DIALOG_FRAGMENT)
        mProgressDialog!!.isCancelable = false
    }

    //TODO: HANDLE BACKPRESS
//    override fun handleOnBackPressed(): Boolean {
//
//        val mDrawerVisibility = (activity as MainScreenActivity).checkNavigationDrawerVisibility()
//
//        if (!mDrawerVisibility) {
//            val action = RepeatOrderFragmentDirections.actionRepeatOrderFragmentToCartGroupFragment(mGroupName!!, javaClass.simpleName)
//            findNavController().navigate(action)
//        }
//
//        return true
//    }

    @Subscribe
    fun onProductClick(mEvent: RepeatOrderEvent) {
        var mFlag = false
        for(i in mEvent.mProductList.indices) {
            if(mEvent.mProductList[i].isSelectedForRepeatOrder) {
                mFlag = true
                break
            }
        }
        mBinding.checkBoxSelectAllSeats.isChecked = mFlag
    }

//    override fun onResume() {
//        super.onResume()
//        requireActivity().onBackPressedDispatcher.addCallback(this)
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
