package com.rsl.youresto.ui.main_screen.cart

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log.e
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast.LENGTH_SHORT
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.rsl.youresto.R
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.database_download.models.KitchenModel
import com.rsl.youresto.data.database_download.models.TablesModel
import com.rsl.youresto.data.tables.models.ServerTableGroupModel
import com.rsl.youresto.data.tables.models.ServerTableSeatModel
import com.rsl.youresto.databinding.FragmentCartGroupBinding
import com.rsl.youresto.ui.main_screen.app_settings.AppSettingsViewModel
import com.rsl.youresto.ui.main_screen.cart.event.CartFragmentTouchEvent
import com.rsl.youresto.ui.main_screen.cart.event.LogwoodEvent
import com.rsl.youresto.ui.main_screen.cart.event.RefreshCartEvent
import com.rsl.youresto.ui.main_screen.checkout.bill_print.BillPrintEvent
import com.rsl.youresto.ui.main_screen.checkout.events.DrawerEvent
import com.rsl.youresto.ui.main_screen.estimate_bill_print.EstimateBillPrint50Activity
import com.rsl.youresto.ui.main_screen.estimate_bill_print.EstimateBillPrint80Activity
import com.rsl.youresto.ui.main_screen.kitchen_print.KitchenPrint50Activity
import com.rsl.youresto.ui.main_screen.kitchen_print.KitchenPrint80Activity
import com.rsl.youresto.ui.main_screen.kitchen_print.event.KitchenActivityCreatedEvent
import com.rsl.youresto.ui.main_screen.kitchen_print.event.KitchenBundleEvent
import com.rsl.youresto.ui.main_screen.kitchen_print.event.KitchenPrintDoneEvent
import com.rsl.youresto.ui.main_screen.kitchen_print.model.SingleKOTModel
import com.rsl.youresto.ui.main_screen.tables_and_tabs.tables.TablesViewModel
import com.rsl.youresto.utils.Animations
import com.rsl.youresto.utils.Animations.rotateAntiClockwise
import com.rsl.youresto.utils.Animations.rotateClockwise
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.DIALOG_TYPE_OTHER
import com.rsl.youresto.utils.AppConstants.ENABLE_KITCHEN_PRINT
import com.rsl.youresto.utils.AppConstants.ENABLE_LOGWOOD
import com.rsl.youresto.utils.AppConstants.GROUP_NAME
import com.rsl.youresto.utils.AppConstants.LOCATION_SERVICE_TYPE
import com.rsl.youresto.utils.AppConstants.LOGGED_IN_SERVER_NAME
import com.rsl.youresto.utils.AppConstants.NO_TYPE
import com.rsl.youresto.utils.AppConstants.ORDER_NO
import com.rsl.youresto.utils.AppConstants.ORDER_TYPE
import com.rsl.youresto.utils.AppConstants.PAPER_SIZE_50
import com.rsl.youresto.utils.AppConstants.PAPER_SIZE_80
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_CART_ID
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_TABLE_ID
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_TABLE_NO
import com.rsl.youresto.utils.AppConstants.SELECTED_BILL_PRINT_PAPER_SIZE
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_NO
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppConstants.SERVICE_QUICK_SERVICE
import com.rsl.youresto.utils.AppConstants.TABLE_NO
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.Network
import com.rsl.youresto.utils.Utils.disableAllViews
import com.rsl.youresto.utils.custom_dialog.AlertDialogEvent
import com.rsl.youresto.utils.custom_dialog.CustomAlertDialogFragment
import com.rsl.youresto.utils.custom_dialog.CustomProgressDialog
import com.rsl.youresto.utils.custom_views.CustomToast
import com.rsl.youresto.utils.logwood.Logwood
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("LogNotTimber")
class CartGroupFragment : Fragment() {

    private lateinit var mBinding: FragmentCartGroupBinding
    private var mGroupNameSelected: String = ""
    private var mTableGroupObserver: Observer<TablesModel>? = null
    private lateinit var mTableViewModel: TablesViewModel
    private lateinit var mCartViewModel: CartViewModel
    private lateinit var mAppSettingsViewModel: AppSettingsViewModel
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mTableID: String
    private var mTableNO: Int = 0
    private var mOpenFrom = ""
    private var mServerName = ""
    private var mSelectedLocationType: Int? = null
    private var mProgressDialog: CustomProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_cart_group, container, false)
        val mView = mBinding.root

        val factory = InjectorUtils.provideTablesViewModelFactory(requireActivity())
        mTableViewModel = ViewModelProviders.of(this, factory).get(TablesViewModel::class.java)

        val cartFactory: CartViewModelFactory =
            InjectorUtils.provideCartViewModelFactory(requireActivity())
        mCartViewModel = ViewModelProviders.of(this, cartFactory).get(CartViewModel::class.java)

        val appSettingFactory = InjectorUtils.provideAppSettingsViewModelFactory(requireActivity())
        mAppSettingsViewModel =
            ViewModelProviders.of(this, appSettingFactory).get(AppSettingsViewModel::class.java)

//        mOpenFrom = CartGroupFragmentArgs.fromBundle(requireArguments()).openFrom

        mSharedPrefs =
            requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        mServerName = mSharedPrefs.getString(LOGGED_IN_SERVER_NAME, "")!!
        mSelectedLocationType = mSharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0)

        if (mSelectedLocationType == SERVICE_DINE_IN) {
            mTableID = mSharedPrefs.getString(SELECTED_TABLE_ID, "")!!
            mTableNO = mSharedPrefs.getInt(SELECTED_TABLE_NO, 0)

//            mGroupNameSelected = CartGroupFragmentArgs.fromBundle(requireArguments()).groupName
        } else if (mSelectedLocationType == SERVICE_QUICK_SERVICE) {
            mTableID = mSharedPrefs.getString(QUICK_SERVICE_TABLE_ID, "")!!
            mTableNO = mSharedPrefs.getInt(QUICK_SERVICE_TABLE_NO, 0)
            mCartID = mSharedPrefs.getString(QUICK_SERVICE_CART_ID, "")
            mGroupNameSelected = "Q"
        }

        EventBus.getDefault().post(DrawerEvent(false, javaClass.simpleName))
        mBinding.imageViewAction.setOnClickListener {
            if (mBinding.constraintLayoutActionOptions.isVisible) {
                hideActionBarOptions()
            } else {
                showActionBarOptions()
            }
        }

        mBinding.imageViewAddProduct.setOnClickListener {

            val mEditor = mSharedPrefs.edit()
            mEditor.putString(AppConstants.SEAT_SELECTION_GROUP, mGroupNameSelected)
            mEditor.apply()

            if (mSelectedLocationType == SERVICE_DINE_IN) {
                val action =
                    CartGroupFragmentDirections.actionCartGroupFragmentToMainProductFragment(
                        "A",
                        "A",
                        javaClass.simpleName
                    )
                findNavController().navigate(action)
            } else if (mSelectedLocationType == SERVICE_QUICK_SERVICE) {
                val action =
                    CartGroupFragmentDirections.actionCartGroupFragmentToQuickServiceFragment(
                        "A",
                        "A"
                    )
                findNavController().navigate(action)

                /*Navigation.findNavController(requireActivity(), R.id.main_screen_host_fragment)
                    .navigate(R.id.quickServiceFragment)*/
            }
        }

        e(javaClass.simpleName, "Group Name: $mGroupNameSelected")

//        checkIfProductSentToKOT()
//        getTableGroups()
//        handleActionOptionsClick()

        mView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN && !mActionBarHiddenState)
                hideActionBarOptions()
            true
        }

        return mView
    }

    private fun checkIfProductSentToKOT() {
        if (mSharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0) == SERVICE_DINE_IN) {
            mCartViewModel.getCartData(mTableNO, mGroupNameSelected).observe(viewLifecycleOwner, {
                if (it.isNotEmpty()) {
                    for (i in it.indices) {
                        if (it[i].mKitchenPrintFlag == 1) {
                            mBinding.constraintLayoutOptionRepeatOrder.visibility = VISIBLE
                            break
                        }
                    }
                }
            })
        }
    }

    private var mActionBarHiddenState = true

    private fun showProgressDialog() {
        //progress dialog
        mProgressDialog = CustomProgressDialog.newInstance(
            "Connecting Logwood !",
            "Please wait while order send to kitchen",
            DIALOG_TYPE_OTHER
        )
        mProgressDialog!!.show(childFragmentManager, AppConstants.CUSTOM_PROGRESS_DIALOG_FRAGMENT)
        mProgressDialog!!.isCancelable = false
    }

    private fun hideActionBarOptions() {
        rotateAntiClockwise(mBinding.imageViewAction)
        mBinding.constraintLayoutActionOptions.visibility = INVISIBLE
        mBinding.viewBackgroundBlur.visibility = INVISIBLE
        Animations.slideDown(mBinding.constraintLayoutActionOptions)
        mActionBarHiddenState = true
        disableAllViews(mBinding.viewpagerCart, false)
        EventBus.getDefault().post(CartFragmentTouchEvent(false))
        EventBus.getDefault().post(DrawerEvent(false, javaClass.simpleName))
        mBinding.imageViewAddProduct.isEnabled = true
    }

    private fun showActionBarOptions() {
        rotateClockwise(mBinding.imageViewAction)
        mBinding.constraintLayoutActionOptions.visibility = VISIBLE
        mBinding.viewBackgroundBlur.visibility = VISIBLE
        Animations.slideUp(mBinding.constraintLayoutActionOptions)
        mActionBarHiddenState = false
        disableAllViews(mBinding.viewpagerCart, true)
        EventBus.getDefault().post(CartFragmentTouchEvent(true))
        EventBus.getDefault().post(DrawerEvent(true, javaClass.simpleName))
        mBinding.imageViewAddProduct.isEnabled = false
    }

    private fun handleActionOptionsClick() {
        mBinding.constraintLayoutOptionClose.setOnClickListener {
            Network.isNetworkAvailableWithInternetAccess(requireActivity()).observe(viewLifecycleOwner,
                {
                    when {
                        it -> close()
                        else -> CustomToast.makeText(requireActivity(), getString(R.string.network_error), LENGTH_SHORT).show()
                    }
                })

        }
        mBinding.constraintLayoutOptionDone.setOnClickListener {
            Network.isNetworkAvailableWithInternetAccess(requireActivity())
                .observe(viewLifecycleOwner, { internet ->
                    when {
                        internet ->
                            when {
                                mSharedPrefs.getBoolean(
                                    ENABLE_KITCHEN_PRINT,
                                    false
                                ) -> mAppSettingsViewModel.getAllKitchenPrinters().observe(
                                    viewLifecycleOwner,
                                    {
                                        when {
                                            it.isNotEmpty() -> {
                                                var ifAnyPrinterSelected = false
                                                for (i in it.indices) {
                                                    when {
                                                        it[i].mSelectedKitchenPrinterName != NO_TYPE && it[i].mSelectedKitchenPrinterName != "" -> ifAnyPrinterSelected =
                                                            true
                                                    }
                                                }

                                                when {
                                                    !ifAnyPrinterSelected -> CustomToast.makeText(
                                                        requireActivity(),
                                                        "Select all kitchen printers from App settings -> Printer Settings",
                                                        LENGTH_SHORT
                                                    ).show()
                                                    else -> sendToKOT()
                                                }
                                            }
                                        }
                                    })
                                !mSharedPrefs.getBoolean(
                                    ENABLE_KITCHEN_PRINT,
                                    false
                                ) && !mSharedPrefs.getBoolean(
                                    ENABLE_LOGWOOD,
                                    false
                                ) -> CustomToast.makeText(
                                    requireActivity(),
                                    "Either enable kitchen print or logwood from app settings to proceed",
                                    LENGTH_SHORT
                                ).show()
                                else -> sendToKOT()
                            }
                        else -> CustomToast.makeText(
                            requireActivity(),
                            getString(R.string.network_error),
                            LENGTH_SHORT
                        ).show()
                    }
                })
        }

        mBinding.constraintLayoutOptionPrint.setOnClickListener {
            printEstimateBill()
            hideActionBarOptions()
        }

        mBinding.constraintLayoutOptionRepeatOrder.setOnClickListener {
            val action = CartGroupFragmentDirections.actionCartGroupFragmentToRepeatOrderFragment(
                mGroupNameSelected
            )
            findNavController().navigate(action)
        }
    }

    private fun printEstimateBill() {
        if (mSharedPrefs.getString(AppConstants.SELECTED_BILL_PRINTER_NAME, "") == ""
            || mSharedPrefs.getString(AppConstants.SELECTED_BILL_PRINTER_NAME, "") == "NO_TYPE"
        ) {

            CustomToast.makeText(requireActivity(), "Bill Printer not selected", LENGTH_SHORT).show()
        } else {
            if (mSharedPrefs.getInt(SELECTED_BILL_PRINT_PAPER_SIZE, 0) == PAPER_SIZE_50) {
                val mEstimate50Intent = Intent(requireActivity(), EstimateBillPrint50Activity::class.java)
                mEstimate50Intent.putExtra(TABLE_NO, mTableNO)
                mEstimate50Intent.putExtra(GROUP_NAME, mGroupNameSelected)
                mEstimate50Intent.putExtra(ORDER_NO, mCartNO)
                mEstimate50Intent.putExtra(AppConstants.API_CART_ID, mCartID)
                if (mTableNO != 100)
                    mEstimate50Intent.putExtra(ORDER_TYPE, 1)
                else
                    mEstimate50Intent.putExtra(ORDER_TYPE, 2)

                startActivity(mEstimate50Intent)
            } else {
                val mEstimate80Intent = Intent(requireActivity(), EstimateBillPrint80Activity::class.java)
                mEstimate80Intent.putExtra(TABLE_NO, mTableNO)
                mEstimate80Intent.putExtra(GROUP_NAME, mGroupNameSelected)
                mEstimate80Intent.putExtra(ORDER_NO, mCartNO)
                mEstimate80Intent.putExtra(AppConstants.API_CART_ID, mCartID)
                if (mTableNO != 100)
                    mEstimate80Intent.putExtra(ORDER_TYPE, 1)
                else
                    mEstimate80Intent.putExtra(ORDER_TYPE, 2)

                startActivity(mEstimate80Intent)
            }
        }
    }

    private var mTableGroupList: ArrayList<ServerTableGroupModel> = ArrayList()

    private fun initialiseTabs() {

        val mGroupPagerAdapter =
            CartGroupPagerAdapter(childFragmentManager, mTableGroupList, mGroupNameSelected)
        mBinding.viewpagerCart.adapter = mGroupPagerAdapter
        mBinding.tabsCartGroup.setupWithViewPager(mBinding.viewpagerCart)
        setCartGroup(mGroupNameSelected)


        mBinding.viewpagerCart.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) = Unit

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) = Unit

            override fun onPageSelected(position: Int) {
                onGroupChangeOnCartTab(position)
            }

        })
    }

    private fun setCartGroup(groupName: String) {
        var mGroupName = groupName
        mGroupName = if (mGroupName.equals("Z", ignoreCase = true) || mGroupName.equals(
                "Q",
                ignoreCase = true
            )
        ) "A" else mGroupName

        var mGroupPosition = 0

        for (i in 0 until mTableGroupList.size) {
            if (mTableGroupList[i].mGroupName == mGroupName) {
                mGroupPosition = i
                break
            }
        }


        mBinding.viewpagerCart.currentItem = mGroupPosition
        e(javaClass.simpleName, "mGroupPosition: $mGroupPosition")
        onGroupChangeOnCartTab(mGroupPosition)
    }

    private var mCartNO: String? = null
    private var mCovers: Int = 0

    private fun getTableGroups() {
        mTableGroupList.clear()

        when (mSelectedLocationType) {
            SERVICE_DINE_IN -> {
                val mTableGroupData = mTableViewModel.getTable(mTableID)
                mTableGroupObserver = Observer {
                    when {
                        it != null -> {

                            loop@ for (i in 0 until it.mGroupList!!.size) {
                                when (mGroupNameSelected) {
                                    it.mGroupList!![i].mGroupName -> {
                                        mCartNO = it.mGroupList!![i].mCartNO
                                        mCovers = it.mGroupList!![i].mSeatList!!.size
                                        break@loop
                                    }
                                }
                            }

                            mTableGroupList.addAll(it.mGroupList!!)

                            val tempList = mTableGroupList.sortedBy { group -> group.mGroupName }

                            mTableGroupList.clear()
                            mTableGroupList.addAll(tempList)

                            initialiseTabs()
                            mTableGroupData.removeObserver(mTableGroupObserver!!)
                        }
                    }
                }
                mTableGroupData.observe(viewLifecycleOwner, mTableGroupObserver!!)

            }
            SERVICE_QUICK_SERVICE -> if (mSharedPrefs.getString(QUICK_SERVICE_CART_ID, "") != "") {

                val mCartData = mCartViewModel.getTableCartDataForCartID(
                    mSharedPrefs.getInt(QUICK_SERVICE_TABLE_NO, 0),
                    mSharedPrefs.getString(QUICK_SERVICE_CART_ID, "")!!
                )

                mCartObserver = Observer {
                    when {
                        it.isNotEmpty() -> {
                            var mGroupTotal = BigDecimal(0.0)
                            for (i in it.indices) {
                                e(
                                    javaClass.simpleName,
                                    "mProductTotalPrice: ${it[i].mProductTotalPrice}"
                                )
                                mGroupTotal += it[i].mProductTotalPrice
                            }

                            mCartNO = it[0].mCartNO

                            e(javaClass.simpleName, "mGroupTotal: $mGroupTotal")
                            e(javaClass.simpleName, "mCartNO: $mCartNO")

                            val mServerTableSeatList = ArrayList<ServerTableSeatModel>()
                            mServerTableSeatList.add(ServerTableSeatModel(1))

                            val mServerTableGroupModel = ServerTableGroupModel(
                                mSharedPrefs.getString(QUICK_SERVICE_CART_ID, ""), mCartNO, "", "Q",
                                mTableNO, mTableID, mServerTableSeatList, mGroupTotal, true
                            )

                            mTableGroupList.add(mServerTableGroupModel)

                            e(javaClass.simpleName, "mTableGroupList: ${mTableGroupList.size}")

                            initialiseTabs()

                            mCartData.removeObserver(mCartObserver!!)
                        }
                    }
                }
                mCartData.observe(viewLifecycleOwner, mCartObserver!!)
            }
        }
    }

    private var mKitchenList = ArrayList<KitchenModel>()
    private var mSingleKOTList = ArrayList<SingleKOTModel>()
    private var mCartProductList = ArrayList<CartProductModel>()
    private var mCartID: String? = null

    private var mCartObserver: Observer<List<CartProductModel>>? = null
    private var mKitchenObserver: Observer<List<KitchenModel>>? = null

    private fun sendToKOT() {
        mCartProductList = ArrayList()
        mSingleKOTList = ArrayList()
        mKitchenList = ArrayList()
        mSerial = -1
        mKitchenUpdateResult = -1
        isLogwoodTriggered = false

        hideActionBarOptions()

        when {
            mSharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0) == SERVICE_DINE_IN -> {

                //DINE IN FLOW

                mCartProductList = ArrayList()

                val mCartData = mCartViewModel.getCartData(mTableNO, mGroupNameSelected)
                mCartObserver = Observer {
                    when {
                        it.isNotEmpty() -> {
                            mCartProductList.addAll(it)

                            val mKitchenData = mAppSettingsViewModel.getAllKitchenPrinters()
                            mKitchenObserver = Observer { kitchen ->
                                when {
                                    kitchen.isNotEmpty() -> {
                                        mKitchenList.addAll(kitchen)

                                        var mKitchenProductCount = 0

                                        for (element in it) {
                                            mCartID = it[0].mCartID
                                            when (element.mKitchenPrintFlag) {
                                                0 -> mKitchenProductCount++
                                            }
                                        }

                                        e(
                                            javaClass.simpleName,
                                            "mKitchenProductCount: $mKitchenProductCount"
                                        )

                                        when {
                                            mKitchenProductCount > 0 -> {
                                                // New items to send to kitchen
                                                var mSerialNO = -1
                                                mSingleKOTList = ArrayList()
                                                for (i in 0 until mKitchenList.size) {
                                                    e(
                                                        javaClass.simpleName,
                                                        "kitchen paper size: ${mKitchenList[i].mSelectedKitchenPrinterSize} " +
                                                                "printer type ${mKitchenList[i].mPrinterType}"
                                                    )

                                                    val mProductList = ArrayList<CartProductModel>()
                                                    for (j in it.indices) {
                                                        when {
                                                            it[j].mKitchenPrintFlag == 0 && mKitchenList[i].mKitchenID == it[j].mPrinterID -> mProductList.add(it[j])
                                                        }
                                                        when {
                                                            j == it.size - 1 && mProductList.size > 0 -> {
                                                                mSerialNO++
                                                                mSingleKOTList.add(
                                                                    SingleKOTModel(
                                                                        mSerialNO,
                                                                        mKitchenList[i],
                                                                        mProductList
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                val mRequiredPrinterList = ArrayList<KitchenModel>()

                                                for (l in 0 until mCartProductList.size) {
                                                    for (i in 0 until mKitchenList.size) {
                                                        when {
                                                            mCartProductList[l].mPrinterID == mKitchenList[i].mKitchenID
                                                                    && !mRequiredPrinterList.contains(
                                                                mKitchenList[i]
                                                            ) -> mRequiredPrinterList.add(
                                                                mKitchenList[i]
                                                            )
                                                        }
                                                    }

                                                }

                                                var mAllPrinterSelected = true
                                                for (i in 0 until mRequiredPrinterList.size) {
                                                    when {
                                                        mRequiredPrinterList[i].mSelectedKitchenPrinterName == NO_TYPE && mRequiredPrinterList[i].mLogWoodServerIP.isEmpty() -> mAllPrinterSelected =
                                                            false
                                                    }
                                                }

                                                when {
                                                    mAllPrinterSelected -> openPrintLayout()
                                                    else -> CustomToast.makeText(
                                                        requireActivity(),
                                                        "Please select printer or logwood for all Kitchens",
                                                        LENGTH_SHORT
                                                    ).show()
                                                }


                                            }
                                            else -> {
                                                val mCustomDialogFragment =
                                                    CustomAlertDialogFragment.newInstance(
                                                        1,
                                                        javaClass.simpleName,
                                                        R.drawable.ic_delete_forever_primary_36dp,
                                                        "Do you want to re-print kot?",
                                                        "All items are already sent to kitchen",
                                                        "Yes, Print",
                                                        "No, Don't",
                                                        R.drawable.ic_check_black_24dp,
                                                        R.drawable.ic_close_black_24dp
                                                    )
                                                mCustomDialogFragment.show(
                                                    childFragmentManager,
                                                    AppConstants.CUSTOM_DIALOG_FRAGMENT
                                                )
                                            }
                                        }
                                        mKitchenData.removeObserver(mKitchenObserver!!)
                                    }
                                }
                            }
                            mKitchenData.observe(viewLifecycleOwner, mKitchenObserver!!)
                            mCartData.removeObserver(mCartObserver!!)
                        }
                    }
                }
                mCartData.observe(viewLifecycleOwner, mCartObserver!!)

            }
            mSharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0) == SERVICE_QUICK_SERVICE -> {

                //QUICK SERVICE FLOW

                val mCartData = mCartViewModel.getCartDataWithCartID(mCartID!!)
                mCartObserver = Observer {
                    when {
                        it.isNotEmpty() -> {
                            mCartProductList.addAll(it)

                            val mKitchenData = mAppSettingsViewModel.getAllKitchenPrinters()
                            mKitchenObserver = Observer { kitchen ->
                                when {
                                    kitchen.isNotEmpty() -> {
                                        mKitchenList.addAll(kitchen)

                                        var mKitchenProductCount = 0

                                        for (element in it) {
                                            mCartID = it[0].mCartID
                                            when (element.mKitchenPrintFlag) {
                                                0 -> mKitchenProductCount++
                                            }
                                        }

                                        e(
                                            javaClass.simpleName,
                                            "mKitchenProductCount: $mKitchenProductCount"
                                        )

                                        when {
                                            mKitchenProductCount > 0 -> {
                                                // New items to send to kitchen
                                                var mSerialNO = -1
                                                mSingleKOTList = ArrayList()
                                                for (i in 0 until mKitchenList.size) {
                                                    e(
                                                        javaClass.simpleName,
                                                        "kitchen paper size: ${mKitchenList[i].mSelectedKitchenPrinterSize} " +
                                                                "printer type ${mKitchenList[i].mPrinterType}"
                                                    )
                                                    val mProductList = ArrayList<CartProductModel>()
                                                    for (j in it.indices) {
                                                        when {
                                                            it[j].mKitchenPrintFlag == 0 && mKitchenList[i].mKitchenID == it[j].mPrinterID -> mProductList.add(
                                                                it[j]
                                                            )
                                                        }
                                                        when {
                                                            j == it.size - 1 && mProductList.size > 0 -> {
                                                                mSerialNO++
                                                                mSingleKOTList.add(
                                                                    SingleKOTModel(
                                                                        mSerialNO,
                                                                        mKitchenList[i],
                                                                        mProductList
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                }

                                                var mKotPrinterEnableCount = 0

                                                for (k in 0 until mSingleKOTList.size) {

                                                    when {
                                                        mSingleKOTList[k].mKitchenModel.mSelectedKitchenPrinterName.isNotEmpty() -> mKotPrinterEnableCount++
                                                    }
                                                }

                                                when (mKotPrinterEnableCount) {
                                                    mSingleKOTList.size -> openPrintLayout()
                                                    else -> CustomToast.makeText(
                                                        requireActivity(),
                                                        "Please Select Printer for all Kitchens",
                                                        LENGTH_SHORT
                                                    ).show()
                                                }


                                            }
                                            else -> {
                                                val mCustomDialogFragment =
                                                    CustomAlertDialogFragment.newInstance(
                                                        1,
                                                        javaClass.simpleName,
                                                        R.drawable.ic_delete_forever_primary_36dp,
                                                        "Do you want to re-print kot?",
                                                        "All items are already sent to kitchen",
                                                        "Yes, Print",
                                                        "No, Don't",
                                                        R.drawable.ic_check_black_24dp,
                                                        R.drawable.ic_close_black_24dp
                                                    )

                                                mCustomDialogFragment.show(
                                                    childFragmentManager,
                                                    AppConstants.CUSTOM_DIALOG_FRAGMENT
                                                )
                                            }
                                        }
                                        mKitchenData.removeObserver(mKitchenObserver!!)
                                    }
                                }
                            }
                            mKitchenData.observe(viewLifecycleOwner, mKitchenObserver!!)
                            mCartData.removeObserver(mCartObserver!!)
                        }
                    }
                }
                mCartData.observe(viewLifecycleOwner, mCartObserver!!)


            }
        }
    }

    private var mSerial = -1

    private fun openPrintLayout() {
        Network.isNetworkAvailableWithInternetAccess(requireActivity()).observe(viewLifecycleOwner,
            {
                if (it) {
                    e(javaClass.simpleName, "openPrintLayout()")
                    e(javaClass.simpleName, "mSingleKOTList size: ${mSingleKOTList.size}")
                    e(javaClass.simpleName, "mSerial: $mSerial")

                    mSerial++
                    if (mSingleKOTList.size > 0 && mSerial < mSingleKOTList.size) {
                        e(javaClass.simpleName, "openPrintLayout() mSerial: $mSerial")
                        val mKitchenPrintIntent: Intent

                        e(
                            javaClass.simpleName,
                            "openPrintLayout() mSerialll: ${mSingleKOTList[mSerial].mKitchenModel.mSelectedKitchenPrinterSize} " +
                                    "print type: ${mSingleKOTList[mSerial].mKitchenModel.mPrinterType} serial: ${mSingleKOTList[mSerial].mSerialNO}"
                        )

                        if (mSingleKOTList[mSerial].mKitchenModel.mSelectedKitchenPrinterSize == PAPER_SIZE_50
                            && mSingleKOTList[mSerial].mKitchenModel.mPrinterType != 0
                        ) {

                            e(javaClass.simpleName, "Intent")
                            mKitchenPrintIntent = Intent(requireActivity(), KitchenPrint50Activity::class.java)
                            startActivity(mKitchenPrintIntent)

                        } else if (mSingleKOTList[mSerial].mKitchenModel.mSelectedKitchenPrinterSize == PAPER_SIZE_80
                            && mSingleKOTList[mSerial].mKitchenModel.mPrinterType != 0
                        ) {

                            mKitchenPrintIntent = Intent(activity, KitchenPrint80Activity::class.java)
                            startActivity(mKitchenPrintIntent)

                        } else {
                            e(javaClass.simpleName, "openPrintLayout ELSE, Send via LOGWOOD")
                            showProgressDialog()
                            sendOrderToLogwood()
                        }
                    }
                } else {
                    CustomToast.makeText(requireActivity(), getString(R.string.network_error), LENGTH_SHORT)
                        .show()
                }
            })


    }

    private var isLogwoodTriggered: Boolean = false

    @Subscribe
    fun onKitchenActivityCreated(mEvent: KitchenActivityCreatedEvent) {
        if (mEvent.mResult) {
            e(javaClass.simpleName, "onKitchenActivityCreated, mSerial : $mSerial")
            e(
                javaClass.simpleName,
                "onKitchenActivityCreated, mSingleKOTList.size : ${mSingleKOTList.size}"
            )
            if (mSerial < mSingleKOTList.size) {
                val mKitchenBundle =
                    KitchenBundleEvent(
                        true,
                        mSerial,
                        mGroupNameSelected,
                        mCartID!!,
                        mSingleKOTList[mSerial]
                    )
                Handler().postDelayed({

                    e(
                        javaClass.simpleName,
                        "logwood ip: ${mSingleKOTList[mSerial].mKitchenModel.mLogWoodServerIP}"
                    )

                    if (mSingleKOTList[mSerial].mKitchenModel.mLogWoodServerIP.isNotEmpty()) {
                        e(javaClass.simpleName, "calling logwood")
                        isLogwoodTriggered = true
                        sendOrderToLogwood()
                    }

                    EventBus.getDefault().post(mKitchenBundle)

                }, 200)

            }
        }
    }

    private fun sendOrderToLogwood() {
        e(javaClass.simpleName, "called logwood")

        val now = Calendar.getInstance()
        val simpleDateFormat = SimpleDateFormat("hh:mm:dd:MM:yyyy", Locale.ENGLISH)
        val nowTime = simpleDateFormat.format(now.time)

        val mStringBuilder = StringBuilder()
        for (i in 0 until mSingleKOTList[mSerial].mProductList.size) {
            var mProductName = mSingleKOTList[mSerial].mProductList[i].mProductName
            val mProductQuantity = mSingleKOTList[mSerial].mProductList[i].mProductQuantity

            when {
                mProductName.length > 20 -> mProductName = mProductName.substring(0, 19)
            }

            mStringBuilder.append("PLU,")
                .append(mProductQuantity)
                .append(",")
                .append(mProductName)
                .append(",1,0\n")

            val mIngredientsList = mSingleKOTList[mSerial].mProductList[i].mShowModifierList

            when {
                mIngredientsList!!.size > 0 -> for (k in mIngredientsList.indices) {
                    var mIngredientName = mIngredientsList[k].mIngredientName
                    val mIngredientQty = mIngredientsList[k].mIngredientQuantity.toString()
                    when {
                        mIngredientName.length > 20 -> mIngredientName =
                            mIngredientName.substring(0, 19)
                    }
                    mStringBuilder.append("SI,")
                        .append(mIngredientQty)
                        .append(",")
                        .append(mIngredientName)
                        .append(",0,\n")
                }
            }
        }

        val mServerIP = mSingleKOTList[mSerial].mKitchenModel.mLogWoodServerIP
        val mSocketPort = mSingleKOTList[mSerial].mKitchenModel.mLogWoodServerPort

        var mCartNoTrim = ""
        when (mCartNO!!.length) {
            7 -> mCartNoTrim = mCartNO!!.substring(2, 6)
            8 -> mCartNoTrim = mCartNO!!.substring(3, 7)
            9 -> mCartNoTrim = mCartNO!!.substring(4, 8)
            10 -> mCartNoTrim = mCartNO!!.substring(5, 9)
            11 -> mCartNoTrim = mCartNO!!.substring(6, 10)
        }

        var mUserDetailString = ""
        if (mCartNO!!.isNotEmpty()) mUserDetailString =
            "$mTableNO,$mServerName,0,$mCovers,$mCartNoTrim,1,M,0"
        else e(javaClass.simpleName, "kotPrint: mOrderId is null")

        val mOrder = "$&,\n $nowTime,\n $mUserDetailString\n $mStringBuilder"
        e(javaClass.simpleName, "sendOrderToLogWood: logwood params: $mOrder")
        when {
            mServerIP != "" && mSocketPort != "" -> {
                val mLogwoodData = Logwood(requireActivity())
                mLogwoodData.sendData(mOrder, mServerIP, mSocketPort)
            }
            else -> {
                CustomToast.makeText(
                    requireActivity(),
                    "Please check logwood Id and port in app settings",
                    LENGTH_SHORT
                )
                    .show()
                e(javaClass.simpleName, "sendOrderToLogWood: Server IP is null")
            }
        }
    }

    @Subscribe
    fun onLogwoodResponse(mEvent: LogwoodEvent) {
        if (mEvent.mResult) {
            e(javaClass.simpleName, "change kitchen flag 2")
            changeKitchenFlag()
        } else {
            CustomToast.makeText(requireActivity(), mEvent.mMessage, LENGTH_SHORT).show()
            mProgressDialog!!.dismiss()
        }
    }

    @Subscribe
    fun onKitchenPrintDone(mEvent: KitchenPrintDoneEvent) {
        when {
            mEvent.mResult ->
                when {
                    mSerial + 1 < mSingleKOTList.size -> if (!isLogwoodTriggered) {
                        changeKitchenFlag()
                    } else {
                        when {
                            mKitchenUpdateResult > -1 -> Handler().postDelayed(
                                { openPrintLayout() },
                                500
                            )
                        }
                    }
                    else -> // Print Complete
                        when {
                            !isLogwoodTriggered -> {
                                changeKitchenFlag()
                            }
                        }
                }
            else -> CustomToast.makeText(
                requireActivity(),
                "Cannot Print, Printer might be offline!",
                LENGTH_SHORT
            ).show()
        }
    }

    @Subscribe
    fun isBillPrintDone(mEvent: BillPrintEvent) {
        if (!mEvent.mBillPrintDone) {
            CustomToast.makeText(
                requireActivity(),
                "Cannot Print, Printer might be offline!",
                LENGTH_SHORT
            )
        }
    }

    private var mKitchenUpdateResult = -1

    private fun changeKitchenFlag() {

        e(javaClass.simpleName, "Cart ID: $mCartID")

        requireActivity().runOnUiThread {
            mCartViewModel.changeKOTFlag(
                mSingleKOTList[mSerial].mKitchenModel.mKitchenID,
                mCartID!!
            )
                .observe(viewLifecycleOwner, {
                    if (it != null && it > -1) {
                        mKitchenUpdateResult = it
                        e(
                            javaClass.simpleName,
                            "Kitchen flag updated to 1 for printer id ${mSingleKOTList[mSerial].mKitchenModel.mKitchenID}"
                        )

                        isLogwoodTriggered = false

                        Handler().postDelayed({ openPrintLayout() }, 500)

                        EventBus.getDefault().post(RefreshCartEvent(true, mGroupNameSelected))
                    }
                })
        }
    }

    @Subscribe
    fun onAlertDialogEvent(mEvent: AlertDialogEvent) {
        when {
            mEvent.mSource == javaClass.simpleName && mEvent.mActionType -> if (mEvent.mActionID == 1) {
                var mSerialNO = -1
                mSingleKOTList = ArrayList()
                for (i in 0 until mKitchenList.size) {
                    val mProductList = ArrayList<CartProductModel>()
                    e(javaClass.simpleName, "mCartProductList.size --- ${mCartProductList.size}")
                    for (j in 0 until mCartProductList.size) {
                        when (mKitchenList[i].mKitchenID) {
                            mCartProductList[j].mPrinterID -> mProductList.add(
                                mCartProductList[j]
                            )
                        }
                        when {
                            j == mCartProductList.size - 1 && mProductList.size > 0 -> {
                                mSerialNO++
                                mSingleKOTList.add(
                                    SingleKOTModel(
                                        mSerialNO,
                                        mKitchenList[i],
                                        mProductList
                                    )
                                )
                            }
                        }
                    }
                }
                openPrintLayout()
            }
        }
    }

    private fun close() {

        when {
            mSharedPrefs.getString(
                AppConstants.SELECTED_BILL_PRINTER_NAME,
                ""
            ) == "" || mSharedPrefs.getString(
                AppConstants.SELECTED_BILL_PRINTER_NAME,
                ""
            ) == "NO_TYPE" -> CustomToast.makeText(
                requireActivity(),
                "Select bill printer from app settings to proceed",
                LENGTH_SHORT
            )
                .show()
            else ->
                when (mSelectedLocationType) {
                    SERVICE_DINE_IN -> {
                        val mCartData = mCartViewModel.getCartData(mTableNO, mGroupNameSelected)
                        mCartObserver = Observer {
                            when {
                                it != null -> {
                                    when {
                                        it.isNotEmpty() -> {
                                            var mKotSentProduct = true
                                            loop@ for (element in it) {
                                                when (element.mKitchenPrintFlag) {
                                                    0 -> {
                                                        mKotSentProduct = false
                                                        break@loop
                                                    }
                                                }
                                            }

                                            when {
                                                mKotSentProduct -> if (!mGroupNameSelected.equals("NULL")) {
                                                    val action =
                                                        CartGroupFragmentDirections.actionCartGroupFragmentToSeatsCheckoutFragment(
                                                            mGroupNameSelected,
                                                            javaClass.simpleName
                                                        )
                                                    findNavController().navigate(action)
                                                } else {
                                                    val action =
                                                        CartGroupFragmentDirections.actionCartGroupFragmentToSeatsCheckoutFragment(
                                                            "A",
                                                            javaClass.simpleName
                                                        )
                                                    findNavController().navigate(action)
                                                }
                                                else -> CustomToast.makeText(
                                                    requireActivity(),
                                                    "Items pending in cart to be sent to kitchen!",
                                                    LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                    mCartData.removeObserver(mCartObserver!!)
                                }
                            }

                        }
                        mCartData.observe(viewLifecycleOwner, mCartObserver!!)
                    }
                    SERVICE_QUICK_SERVICE -> {

                        val mCartData = mCartViewModel.getCartDataWithCartID(mCartID!!)
                        mCartObserver = Observer {
                            when {
                                it != null -> {
                                    when {
                                        it.isNotEmpty() -> {
                                            var mKotSentProduct = true
                                            loop@ for (element in it) {
                                                when (element.mKitchenPrintFlag) {
                                                    0 -> {
                                                        mKotSentProduct = false
                                                        break@loop
                                                    }
                                                }
                                            }

                                            when {
                                                mKotSentProduct ->
                                                    when {
                                                        !mGroupNameSelected.equals("NULL") -> {
                                                            val action =
                                                                CartGroupFragmentDirections.actionCartGroupFragmentToSeatsCheckoutFragment(
                                                                    mGroupNameSelected,
                                                                    javaClass.simpleName
                                                                )
                                                            findNavController().navigate(action)
                                                        }
                                                        else -> {
                                                            val action =
                                                                CartGroupFragmentDirections.actionCartGroupFragmentToSeatsCheckoutFragment(
                                                                    "A",
                                                                    javaClass.simpleName
                                                                )
                                                            findNavController().navigate(action)
                                                        }
                                                    }
                                                else -> CustomToast.makeText(
                                                    requireActivity(),
                                                    "Items pending in cart to be sent to kitchen!",
                                                    LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                    mCartData.removeObserver(mCartObserver!!)
                                }
                            }

                        }
                        mCartData.observe(viewLifecycleOwner, mCartObserver!!)

                    }
                }
        }

    }

    fun onGroupChangeOnCartTab(mPosition: Int) {
        mGroupNameSelected = mTableGroupList[mPosition].mGroupName

        when (mSelectedLocationType) {
            SERVICE_DINE_IN -> mCartViewModel.getCartData(
                mTableNO,
                mTableGroupList[mPosition].mGroupName
            ).observeForever {
                when {
                    it.isNotEmpty() -> {
                        val mCartNOString = "Cart ID: ${it[0].mCartNO}"
                        mBinding.textViewCartOrderNo.text = mCartNOString

                        var mCartTotal = BigDecimal(0.0)
                        for (element in it)
                            mCartTotal += element.mProductTotalPrice

                        mBinding.textViewCartTotalCartAmount.text =
                            String.format(Locale.ENGLISH, "%.2f", mCartTotal)
                    }
                    else -> mBinding.textViewCartTotalCartAmount.text =
                        String.format(Locale.ENGLISH, "%.2f", 0.0)
                }
            }
            SERVICE_QUICK_SERVICE -> mCartViewModel.getTableCartDataForCartID(
                mTableNO,
                mSharedPrefs.getString(QUICK_SERVICE_CART_ID, "")!!
            )
                .observeForever {
                    when {
                        it.isNotEmpty() -> {
                            val mCartNOString = "Cart ID: ${it[0].mCartNO}"
                            mBinding.textViewCartOrderNo.text = mCartNOString

                            var mCartTotal = BigDecimal(0.0)
                            for (element in it)
                                mCartTotal += element.mProductTotalPrice

                            mBinding.textViewCartTotalCartAmount.text =
                                String.format(Locale.ENGLISH, "%.2f", mCartTotal)
                        }
                        else -> mBinding.textViewCartTotalCartAmount.text =
                            String.format(Locale.ENGLISH, "%.2f", 0.0)
                    }
                }
        }
    }

    //TODO: HANDLE BACK PRESS
//    override fun handleOnBackPressed(): Boolean {
//
//        val mDrawerVisibility = (activity as MainScreenActivity).checkNavigationDrawerVisibility()
//
//        if (!mDrawerVisibility) {
//            if (!mActionBarHiddenState) {
//                hideActionBarOptions()
//                return true
//            }
//
//            if (mSelectedLocationType == SERVICE_DINE_IN) {
//                when (mOpenFrom) {
//                    AppConstants.PENDING_ORDER_FRAGMENT -> {
//                        val action =
//                            CartGroupFragmentDirections.actionCartGroupFragmentToPendingOrderFragment()
//                        findNavController().navigate(action)
//                    }
//                    else -> {
//                        val action = CartGroupFragmentDirections
//                            .actionCartGroupFragmentToMainProductFragment(
//                                "A",
//                                "A",
//                                javaClass.simpleName
//                            )
//                        findNavController().navigate(action)
//                    }
//                }
//            } else if (mSelectedLocationType == SERVICE_QUICK_SERVICE) {
//                if (mOpenFrom == AppConstants.PENDING_ORDER_FRAGMENT) {
//                    val action =
//                        CartGroupFragmentDirections.actionCartGroupFragmentToPendingOrderFragment()
//                    findNavController().navigate(action)
//                } else {
//                    val action =
//                        CartGroupFragmentDirections.actionCartGroupFragmentToQuickServiceFragment(
//                            "A",
//                            "A"
//                        )
//                    findNavController().navigate(action)
//                }
//            }
//        }
//
//        return true
//    }

    @Subscribe
    fun onCartProductNameClick(mEvent: EditCartProductClickEvent) {
        val action =
            CartGroupFragmentDirections.actionCartGroupFragmentToEditCartProductFragment(
                mEvent.mCartModel.mCartProductID,
                mGroupNameSelected
            )
        findNavController().navigate(action)
    }

    //TODO: HANDLE BACK PRESS
//    override fun onResume() {
//        requireActivity().onBackPressedDispatcher.addCallback(this)
//        super.onResume()
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
