package com.rsl.youresto.ui.main_screen.checkout


import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast.LENGTH_SHORT
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.R
import com.rsl.youresto.data.checkout.model.CheckoutModel
import com.rsl.youresto.data.checkout.model.CheckoutTransaction
import com.rsl.youresto.databinding.FragmentCheckoutBinding
import com.rsl.youresto.ui.main_screen.cart.CartViewModel
import com.rsl.youresto.ui.main_screen.cart.CartViewModelFactory
import com.rsl.youresto.ui.main_screen.checkout.events.DrawerEvent
import com.rsl.youresto.ui.main_screen.checkout.events.OnNavigationChangeEvent
import com.rsl.youresto.ui.main_screen.checkout.events.SeatPaymentCompleteEvent
import com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.YoyoWalletFragment
import com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.model.YoyoPaymentAuthModel
import com.rsl.youresto.ui.main_screen.tables_and_tabs.tables.TablesViewModel
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.CHECKOUT_FRAGMENT
import com.rsl.youresto.utils.AppConstants.CHECKOUT_ROW_ID
import com.rsl.youresto.utils.AppConstants.DETAIL_NAVIGATION
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.SEAT_SELECTION_ENABLED
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_NO
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppConstants.YOYO_WALLET
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.custom_dialog.AlertDialogEvent
import com.rsl.youresto.utils.custom_dialog.CustomAlertDialogFragment
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("LogNotTimber")
class CheckoutFragment : Fragment() {

    private lateinit var mBinding: FragmentCheckoutBinding
    private lateinit var mCheckoutViewModel: CheckoutViewModel
    private lateinit var mCartViewModel: CartViewModel
    private lateinit var mTableViewModel: TablesViewModel
    private lateinit var mSharedPrefs: SharedPreferences
    private var mCheckoutRowID = 0
    private var mGroupName = ""
    private var mTableID = ""
    private var mTableNO = 0
    private var mSelectedLocationType: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_checkout, container, false)
        val mView = mBinding.root
        mSharedPrefs = requireActivity().getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)

        val factory = InjectorUtils.provideTablesViewModelFactory(requireActivity())
        mTableViewModel = ViewModelProviders.of(this, factory).get(TablesViewModel::class.java)

        val cartFactory: CartViewModelFactory = InjectorUtils.provideCartViewModelFactory(requireActivity())
        mCartViewModel = ViewModelProviders.of(this, cartFactory).get(CartViewModel::class.java)

        val checkoutFactory: CheckoutViewModelFactory = InjectorUtils.provideCheckoutViewModelFactory(requireActivity())
        mCheckoutViewModel = ViewModelProviders.of(this, checkoutFactory).get(CheckoutViewModel::class.java)

//        mCheckoutRowID = CheckoutFragmentArgs.fromBundle(requireArguments()).checkoutRowId
//        mGroupName = CheckoutFragmentArgs.fromBundle(requireArguments()).groupName

        mSelectedLocationType = mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE, 0)

        if (mSelectedLocationType == SERVICE_DINE_IN) {
            mTableID = mSharedPrefs.getString(SELECTED_TABLE_ID, "")!!
            mTableNO = mSharedPrefs.getInt(SELECTED_TABLE_NO, 0)
        } else if (mSelectedLocationType == AppConstants.SERVICE_QUICK_SERVICE) {
            mTableID = mSharedPrefs.getString(AppConstants.QUICK_SERVICE_TABLE_ID, "")!!
            mTableNO = mSharedPrefs.getInt(AppConstants.QUICK_SERVICE_TABLE_NO, 0)
            mGroupName = "Q"
        }

        e(javaClass.simpleName, "mCheckoutRowID: $mCheckoutRowID")

        getTax()
        setNavigationView()
        getCheckoutModel()
        return mView
    }

    override fun onResume() {
        super.onResume()
//        requireActivity().onBackPressedDispatcher.addCallback(this)

        EventBus.getDefault().post(DrawerEvent(true, javaClass.simpleName))
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().post(DrawerEvent(false, javaClass.simpleName))
    }

    private var mCheckoutModel: CheckoutModel? = null
    private fun getCheckoutModel() {

        mCheckoutViewModel.getCheckoutDataByTableAndGroup(mTableID, mGroupName).observe(viewLifecycleOwner,
            {
                mCheckoutModel = it
            })
    }

    private fun setNavigationView() {
        val navHostFragment = childFragmentManager.findFragmentById(R.id.checkout_host_fragment)

        val navController = navHostFragment!!.findNavController()

        val navOptions = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setEnterAnim(R.anim.item_animation_slide_enter_from_right)
            .setExitAnim(R.anim.item_animation_slide_exit_to_left)
            .setPopUpTo(navController.graph.startDestination, true)
            .build()

        mBinding.navigationView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigate_checkout_details -> {
                    navController
                        .navigate(R.id.checkoutCalculationFragment, null, navOptions)
                }
                R.id.navigate_payment_method -> {
                    navController
                        .navigate(R.id.paymentMethodFragment, null, navOptions)
//                    EventBus.getDefault().post(OpenPaymentMethodEvent(true))
                }
                R.id.navigate_discount -> {
                    checkDiscount().observe(viewLifecycleOwner, { discountApplied ->
                        if (!discountApplied) {

                            navController
                                .navigate(R.id.discountFragment, null, navOptions)
                        }
                    })
                }
            }
            return@setOnNavigationItemSelectedListener true
        }

    }

    private var mCheckoutObserver: Observer<CheckoutModel>? = null

    private fun checkDiscount(): LiveData<Boolean> {

        val mCheckoutDiscountData = MutableLiveData<Boolean>()

        val mCheckoutRowID = mSharedPrefs.getInt(CHECKOUT_ROW_ID, 0)
        val mCheckoutData = mCheckoutViewModel.getCheckoutDataByRowID(mCheckoutRowID)

        mCheckoutObserver = Observer {
            if (it != null) {
                if (it.mCheckoutTransaction[it.mCheckoutTransaction.size - 1].mAmountPaid > BigDecimal(0)) {
                    CustomToast.makeText(
                        requireActivity(),
                        "Some amount is paid already, cannot apply discount now",
                        LENGTH_SHORT
                    ).show()
                    mCheckoutDiscountData.postValue(true)
                } else {
                    mCheckoutDiscountData.postValue(false)
                }

                mCheckoutData.removeObserver(mCheckoutObserver!!)
            }
        }

        mCheckoutData.observe(viewLifecycleOwner, mCheckoutObserver!!)

        return mCheckoutDiscountData
    }

    //TODO: HANDLE BACK PRESS

//    override fun handleOnBackPressed(): Boolean {
//        val mTransactionList = mCheckoutModel!!.mCheckoutTransaction
//
//        var mTransactionStarted = false
//        for (i in 0 until mTransactionList.size)
//            if (mTransactionList[i].mAmountPaid > BigDecimal(0) && !mTransactionList[i].isFullPaid) {
//                mTransactionStarted = true
//                break
//            }
//
//        if (mTransactionStarted) {
//            CustomToast.makeText(
//                requireActivity(),
//                "You have already paid some amount for selected seats, Cannot cancel the transaction now",
//                LENGTH_SHORT
//            ).show()
//        } else {
//
//            val mCustomAlertDialog = CustomAlertDialogFragment.newInstance(
//                1, javaClass.simpleName, R.drawable.ic_delete_forever_primary_36dp,
//                "Cancel Transaction?", "Are you sure you want to cancel this transaction?",
//                "Yes, Cancel", "No, Don't", R.drawable.ic_check_black_24dp, R.drawable.ic_close_black_24dp
//            )
//            mCustomAlertDialog.show(childFragmentManager, AppConstants.CUSTOM_DIALOG_FRAGMENT)
//        }
//        return true
//    }

    @Subscribe
    fun onAlertDialogEvent(mEvent: AlertDialogEvent) {
        if (mEvent.mSource == javaClass.simpleName && mEvent.mActionType) {
            when (mSelectedLocationType) {
                SERVICE_DINE_IN -> when {
                    mSharedPrefs.getBoolean(SEAT_SELECTION_ENABLED, false) -> mCheckoutViewModel.deleteCheckoutRow(mCheckoutRowID).observe(viewLifecycleOwner,
                        {
                            e(javaClass.simpleName, "deleteCheckoutRow $it")
                            when {
                                it > 0 -> {
                                    val action =
                                        CheckoutFragmentDirections.actionCheckoutFragmentToSeatsCheckoutFragment(
                                            mGroupName,
                                            javaClass.simpleName
                                        )
                                    findNavController().navigate(action)
                                }
                            }
                        })
                    else -> mCheckoutViewModel.deleteWithoutSeatSelectionCheckoutRow(mCheckoutRowID).observe(viewLifecycleOwner,
                        {
                            e(javaClass.simpleName, "deleteCheckoutRow $it")
                            when {
                                it > 0 -> {
                                    val action = CheckoutFragmentDirections.actionCheckoutFragmentToCartGroupFragment(
                                        mGroupName,
                                        CHECKOUT_FRAGMENT
                                    )
                                    findNavController().navigate(action)
                                }
                            }
                        })
                }
                else -> mCheckoutViewModel.deleteQuickServiceCheckoutRow(mCheckoutRowID).observe(viewLifecycleOwner,
                    {
                        e(javaClass.simpleName, "deleteCheckoutRow $it")
                        when {
                            it > 0 -> {
                                val action = CheckoutFragmentDirections.actionCheckoutFragmentToCartGroupFragment(
                                    mGroupName,
                                    CHECKOUT_FRAGMENT
                                )
                                findNavController().navigate(action)
                            }
                        }
                    })
            }
        }
    }

    private var mCheckoutTransactionList: ArrayList<CheckoutTransaction>? = null
    private var mCheckoutTransaction: CheckoutTransaction? = null

    @Subscribe
    fun onSeatPaymentCompleted(mEvent: SeatPaymentCompleteEvent) {
        when {
            mEvent.mPaymentComplete && mEvent.mIntentFrom == YoyoWalletFragment::class.java.simpleName -> mCheckoutViewModel.getCheckoutDataByRowID(
                mCheckoutRowID
            ).observe(viewLifecycleOwner, {
                when {
                    it != null -> {
                        mCheckoutModel = it
                        mCheckoutTransactionList = mCheckoutModel!!.mCheckoutTransaction

                        loop@ for (i in 0 until mCheckoutTransactionList!!.size) {
                            when {
                                mCheckoutTransactionList!![i].isSelected && mCheckoutTransactionList!![i].isFullPaid -> {
                                    mCheckoutTransaction = mCheckoutTransactionList!![i]
                                    break@loop
                                }
                            }
                        }

                        for (j in 0 until mCheckoutTransaction!!.mPaymentTransaction.size) {
                            when (YOYO_WALLET) {
                                mCheckoutTransaction!!.mPaymentTransaction[j].mWalletName -> {

                                    val mQRCode = mCheckoutTransaction!!.mPaymentTransaction[j].mWalletQRCode

                                    val mPaymentAuthModel =
                                        YoyoPaymentAuthModel(
                                            mCheckoutTransaction!!,
                                            getDateTime(),
                                            mQRCode,
                                            mTableNO,
                                            mGroupName,
                                            mTaxPercentage
                                        )

                                    val mBasketID = mCheckoutTransaction!!.mPaymentTransaction[j].mReferenceNO

                                    mCheckoutViewModel.callYoyoBasketRegistrationAPI(mPaymentAuthModel, mBasketID)
                                        .observe(viewLifecycleOwner, { response ->
                                            when {
                                                response != null ->
                                                    when (response.mStatus) {
                                                        "COMPLETED" -> {
                                                            e(javaClass.simpleName, "Basket Registration successfully")
                                                            val action =
                                                                CheckoutFragmentDirections.actionCheckoutFragmentToSeatsCheckoutFragment(
                                                                    mGroupName,
                                                                    javaClass.simpleName
                                                                )
                                                            findNavController().navigate(action)

                                                        }
                                                        else -> {
                                                            val mCustomDialogFragment =
                                                                CustomAlertDialogFragment.newInstance(
                                                                    3,
                                                                    javaClass.simpleName,
                                                                    R.drawable.ic_delete_forever_primary_36dp,
                                                                    response.mStatus,
                                                                    response.mMessageID + " : " + response.mStatusMessage,
                                                                    "Okay",
                                                                    "",
                                                                    R.drawable.ic_check_black_24dp,
                                                                    0
                                                                )
                                                            mCustomDialogFragment.show(
                                                                childFragmentManager,
                                                                AppConstants.CUSTOM_DIALOG_FRAGMENT
                                                            )
                                                        }
                                                    }
                                            }
                                        })
                                }
                            }
                        }
                    }
                }
            })
            else -> {
                val action = CheckoutFragmentDirections.actionCheckoutFragmentToSeatsCheckoutFragment(
                    mGroupName,
                    javaClass.simpleName
                )
                findNavController().navigate(action)
            }
        }

    }

    private fun getTax() {
        mCheckoutViewModel.getTaxData().observe(viewLifecycleOwner, {
            for (i in 0 until it.size) mTaxPercentage += it[i].mTaxPercentage
        })
    }

    private var mTaxPercentage = BigDecimal(0)

    private fun getDateTime(): String {
        val today = Calendar.getInstance()
        val mDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
        var mDateTime = mDateFormat.format(today.time)
        mDateTime = mDateTime.replace(" ", "T")
        mDateTime += "Z"
        return mDateTime
    }

    @Subscribe
    fun onNavigationChange(mEvent: OnNavigationChangeEvent) {
        if (mEvent.mSelection == DETAIL_NAVIGATION)
            mBinding.navigationView.selectedItemId = R.id.navigate_checkout_details
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
