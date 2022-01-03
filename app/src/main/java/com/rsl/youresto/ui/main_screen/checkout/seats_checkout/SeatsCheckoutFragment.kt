package com.rsl.youresto.ui.main_screen.checkout.seats_checkout

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast.LENGTH_SHORT
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.R
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.checkout.model.CheckoutModel
import com.rsl.youresto.data.checkout.model.CheckoutTransaction
import com.rsl.youresto.data.checkout.model.SeatPriceModel
import com.rsl.youresto.data.database_download.models.TaxModel
import com.rsl.youresto.data.tables.models.LocalTableGroupModel
import com.rsl.youresto.data.tables.models.LocalTableSeatModel
import com.rsl.youresto.databinding.FragmentSeatsCheckoutBinding
import com.rsl.youresto.ui.main_screen.MainScreenActivity
import com.rsl.youresto.ui.main_screen.cart.CartGroupFragment
import com.rsl.youresto.ui.main_screen.cart.CartViewModel
import com.rsl.youresto.ui.main_screen.cart.CartViewModelFactory
import com.rsl.youresto.ui.main_screen.checkout.CheckoutFragment
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModelFactory
import com.rsl.youresto.ui.main_screen.checkout.bill_print.BillPrintEvent
import com.rsl.youresto.ui.main_screen.checkout.events.DrawerEvent
import com.rsl.youresto.ui.main_screen.checkout.seats_checkout.event.SeatsCheckoutEvent
import com.rsl.youresto.ui.main_screen.tables_and_tabs.tables.TablesViewModel
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.AUTO_LOGOUT_ENABLED
import com.rsl.youresto.utils.AppConstants.DIALOG_TYPE_OTHER
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_CART_ID
import com.rsl.youresto.utils.AppConstants.SEAT_SELECTION_ENABLED
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_NO
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppConstants.SERVICE_QUICK_SERVICE
import com.rsl.youresto.utils.AppConstants.ZERO_SEAT_LIST_COUNT
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.Network
import com.rsl.youresto.utils.custom_dialog.CustomProgressDialog
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("LogNotTimber")
class SeatsCheckoutFragment : Fragment() {

    private lateinit var mBinding: FragmentSeatsCheckoutBinding
    private var mGroupName: String? = null
    private var mTableNO: Int = 0
    private var mTableID = ""
    private var mLocationID = ""
    private var isSeatSelectionEnabled = false
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mTableViewModel: TablesViewModel
    private lateinit var mCartViewModel: CartViewModel
    private lateinit var mCheckoutViewModel: CheckoutViewModel
    private var mSelectedLocationType: Int? = null
    private var mIntentFrom: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_seats_checkout, container, false)
        val mView = mBinding.root

        val factory = InjectorUtils.provideTablesViewModelFactory(requireActivity())
        mTableViewModel = ViewModelProviders.of(this, factory).get(TablesViewModel::class.java)

        val cartFactory: CartViewModelFactory = InjectorUtils.provideCartViewModelFactory(requireActivity())
        mCartViewModel = ViewModelProviders.of(this, cartFactory).get(CartViewModel::class.java)

        val checkoutFactory: CheckoutViewModelFactory = InjectorUtils.provideCheckoutViewModelFactory(requireActivity())
        mCheckoutViewModel = ViewModelProviders.of(this, checkoutFactory).get(CheckoutViewModel::class.java)

        mGroupName = SeatsCheckoutFragmentArgs.fromBundle(requireArguments())
            .groupName

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        mSelectedLocationType = mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE, 0)
        mLocationID = mSharedPrefs.getString(SELECTED_LOCATION_ID, "")!!
//        mIntentFrom = SeatsCheckoutFragmentArgs.fromBundle(requireArguments()).INTENTFROM

        if (mSelectedLocationType == SERVICE_DINE_IN) {
            mTableID = mSharedPrefs.getString(SELECTED_TABLE_ID, "")!!
            mTableNO = mSharedPrefs.getInt(SELECTED_TABLE_NO, 0)
            isSeatSelectionEnabled = mSharedPrefs.getBoolean(SEAT_SELECTION_ENABLED, false)

            if (isSeatSelectionEnabled) {
                setUpRecyclerView()
            } else {
                mBinding.constraintLayoutCheckoutSeat.visibility = GONE

                mProgressDialog = CustomProgressDialog.newInstance(
                    "Calculating",
                    "Please Wait..",
                    DIALOG_TYPE_OTHER
                )
                mProgressDialog!!.isCancelable = false
                mProgressDialog!!.show(childFragmentManager, AppConstants.CUSTOM_PROGRESS_DIALOG_FRAGMENT)

                mTableViewModel.getTable(mTableID).observe(viewLifecycleOwner, {
                    if (it != null) {
                        e(javaClass.simpleName, "Intent: $mIntentFrom")
//                        if (mIntentFrom == CheckoutFragment::class.java.simpleName)
                            proceedWithoutSeatSelection(it.mTableNoOfOccupiedChairs)
                    }
                })
            }

        } else if (mSelectedLocationType == SERVICE_QUICK_SERVICE) {
            mTableID = mSharedPrefs.getString(AppConstants.QUICK_SERVICE_TABLE_ID, "")!!
            mTableNO = mSharedPrefs.getInt(AppConstants.QUICK_SERVICE_TABLE_NO, 0)

            mBinding.constraintLayoutCheckoutSeat.visibility = GONE

            mProgressDialog = CustomProgressDialog.newInstance(
                "Calculating",
                "Please Wait..",
                DIALOG_TYPE_OTHER
            )
            mProgressDialog!!.show(childFragmentManager, AppConstants.CUSTOM_PROGRESS_DIALOG_FRAGMENT)
            mProgressDialog!!.isCancelable = false

            proceedForQuickService()
        }

        initViews()
        tryAgain()

        return mView
    }

    override fun onResume() {
//        requireActivity().onBackPressedDispatcher.addCallback(this)
        EventBus.getDefault().post(DrawerEvent(true, javaClass.simpleName))
        super.onResume()
    }

    private var mSeatCartTotal = BigDecimal(0)
    private var mCartTotal = BigDecimal(0)
    private var mTaxPercentage = BigDecimal(0)
    private val mTaxList = ArrayList<TaxModel>()

    private fun initViews() {
        mSharedPrefs.edit().putInt(ZERO_SEAT_LIST_COUNT, 0).apply()

        // FOR SEAT SELECTION
        mBinding.checkBoxSelectAllSeats.setOnCheckedChangeListener(mSelectAllListener)

        mBinding.buttonCheckoutGuestsProceed.setOnClickListener {

            val mSeatList = ArrayList<Int>()
            var mSeatCount = 0
            for (i in 0 until mSeatListWithPrice.size) {
                when {
                    mSeatListWithPrice[i].isSelected && !mSeatListWithPrice[i].mPaid -> {
                        mSeatCount++
                        mSeatList.add(mSeatListWithPrice[i].mSeatNo)
                        mSeatCartTotal += mSeatListWithPrice[i].mSeatTotal
                    }
                }
                mCartTotal += mSeatListWithPrice[i].mSeatTotal
            }
            when {
                mSeatCount > 0 -> proceed(mSeatList)
                else -> CustomToast.makeText(requireActivity(), "Please select seats first", LENGTH_SHORT).show()
            }

        }

        //getTax Data
        mCheckoutViewModel.getTaxData().observe(viewLifecycleOwner, {
            for (element in it) mTaxPercentage += element.mTaxPercentage
            mTaxList.addAll(it)
        })

        //TODO: CHECK THIS
//        mBinding.buttonCheckoutSettleLater.setOnClickListener {
//            handleOnBackPressed()
//        }

        //check if the payment is completed
        when {
            mSelectedLocationType == SERVICE_DINE_IN && isSeatSelectionEnabled -> {
                val mCheckoutData = mCheckoutViewModel.getCheckoutDataByTableAndGroup(mTableID, mGroupName!!)
                mCheckoutObserver = Observer {
                    when {
                        it != null -> {
                            e(javaClass.simpleName, "mAmountPaid : ${it.mAmountPaid}")
                            e(javaClass.simpleName, "mOrderTotal : ${it.mOrderTotal}")
                            when {
                                it.mAmountPaid >= it.mOrderTotal || (it.mOrderTotal - it.mAmountPaid) < BigDecimal(0.05) -> {
                                    mOrderCompleted = true
                                    mCheckoutModel = it
                                    if (mIntentFrom == CartGroupFragment::class.java.simpleName || mIntentFrom == CheckoutFragment::class.java.simpleName) {
                                        submitOrder(mCheckoutModel!!)
                                    }
                                }
                            }
                        }
                    }
                    mCheckoutData.removeObserver(mCheckoutObserver!!)
                }
                mCheckoutData.observe(viewLifecycleOwner, mCheckoutObserver!!)
            }
        }
    }

    private var mOrderCompleted = false
    private var mCheckoutModel: CheckoutModel? = null

    private var mCheckoutObserver: Observer<CheckoutModel>? = null
    private var mCheckoutUpdateObserver: Observer<Int>? = null

    private fun proceed(mSeatList: ArrayList<Int>) {
        //Completed: calculate tax
        val mCheckoutData = mCheckoutViewModel.getCheckoutDataByTableAndGroup(mTableID, mGroupName!!)

        mCheckoutObserver = Observer {
            when (it) {
                null -> insertCheckout(mSeatList, mGroupName!!, mSeatCartTotal)
                else -> {
                    mSeatCartTotal = mSeatCartTotal.setScale(2, RoundingMode.HALF_UP)
                    e(javaClass.simpleName, "mSeatCartTotal $mSeatCartTotal")
                    e(javaClass.simpleName, "mTaxPercentage $mTaxPercentage")
                    var mTaxAmount = mSeatCartTotal * (mTaxPercentage.divide(BigDecimal(100), 2, RoundingMode.DOWN))
                    e(javaClass.simpleName, "mTaxAmount1 $mTaxAmount")
                    mTaxAmount = mTaxAmount.setScale(2, RoundingMode.HALF_UP)
                    e(javaClass.simpleName, "mTaxAmount2 $mTaxAmount")
                    var mOrderTotal = mSeatCartTotal + mTaxAmount
                    mOrderTotal = mOrderTotal.setScale(2, RoundingMode.HALF_UP)
                    e(javaClass.simpleName, "mOrderTotal $mOrderTotal")


                    val mCheckoutTransaction = CheckoutTransaction(
                        mSeatList, true, false, mSeatCartTotal,
                        mTaxAmount, BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0),
                        BigDecimal(0), BigDecimal(0), "", BigDecimal(0), BigDecimal(0),
                        mOrderTotal, BigDecimal(0), mOrderTotal, Date(), false, ArrayList()
                    )


                    mCartTotal -= it.mDiscountAmount
                    mCartTotal = mCartTotal.setScale(2, RoundingMode.HALF_UP)
                    val mTax = mCartTotal * (mTaxPercentage.divide(BigDecimal(100), 2, RoundingMode.DOWN))
                    val mGrandTotal = (mCartTotal + mTax).setScale(2, RoundingMode.DOWN)

                    it.mCartTotal = mCartTotal
                    it.mTaxAmount = mTax
                    it.mOrderTotal = mGrandTotal
                    it.mAmountRemaining = mGrandTotal - it.mAmountPaid

                    val mCheckoutTransactionList = it.mCheckoutTransaction

                    mCheckoutTransactionList.add(mCheckoutTransaction)

                    it.mCheckoutTransaction = mCheckoutTransactionList

                    val mCheckoutUpdateData = mCheckoutViewModel.updateCheckout(it)

                    mCheckoutUpdateObserver = Observer { mUpdate ->
                        if (mUpdate > -1) {
                            storeCheckoutRowID(mUpdate, mGroupName!!)
                            val action =
                                SeatsCheckoutFragmentDirections.actionSeatsCheckoutFragmentToCheckoutFragment(
                                    mUpdate,
                                    mGroupName!!
                                )
                            findNavController().navigate(action)
                        }
                        mCheckoutUpdateData.removeObserver(mCheckoutUpdateObserver!!)
                    }

                    mCheckoutUpdateData.observe(viewLifecycleOwner, mCheckoutUpdateObserver!!)
                }
            }

            mCheckoutData.removeObserver(mCheckoutObserver!!)
        }
        mCheckoutData.observe(viewLifecycleOwner, mCheckoutObserver!!)

    }

    private var mCheckoutInsertObserver: Observer<Int>? = null

    private fun proceedWithoutSeatSelection(mOccupiedChairs: Int) {

        e(javaClass.simpleName, "proceedWithoutSeatSelection, mOccupiedChairs: $mOccupiedChairs")

        val mSeatList = ArrayList<Int>()
        for (i in 0 until mOccupiedChairs) {
            mSeatList.add(i + 1)
        }

        var mSubTotal = BigDecimal(0)

        val mCheckoutData = mCheckoutViewModel.getCheckoutDataByTableAndGroupWithoutObserving(mTableID, "Z")

        mCheckoutObserver = Observer {
            when {
                it != null -> {
                    when {
                        it.mAmountPaid >= it.mOrderTotal -> {
                            e(javaClass.simpleName, "Without seat selection submit order")
                            submitOrder(it)
                        }
                        else -> {

                            mCartViewModel.getCartData(mTableNO, "Z").observe(viewLifecycleOwner,
                                { mCartProductList ->

                                    for (element in mCartProductList) mSubTotal += element.mProductTotalPrice
                                    mSubTotal = mSubTotal.setScale(2, RoundingMode.DOWN)

                                    mCartTotal = mSubTotal

                                    mCartTotal -= it.mDiscountAmount
                                    mCartTotal = mCartTotal.setScale(2, RoundingMode.HALF_UP)
                                    val mTax = mCartTotal * (mTaxPercentage.divide(BigDecimal(100), 2, RoundingMode.DOWN))
                                    val mGrandTotal = (mCartTotal + mTax).setScale(2, RoundingMode.DOWN)

                                    it.mCartTotal = mCartTotal
                                    it.mTaxAmount = mTax
                                    it.mOrderTotal = mGrandTotal
                                    it.mAmountRemaining = mGrandTotal - it.mAmountPaid

                                    val mCheckoutTransaction = CheckoutTransaction(
                                        mSeatList, true, false, mSubTotal,
                                        mTax, BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0),
                                        BigDecimal(0), BigDecimal(0), "", BigDecimal(0), BigDecimal(0),
                                        mGrandTotal, BigDecimal(0), mGrandTotal, Date(), false, ArrayList()
                                    )

                                    val mCheckoutTransactionList = it.mCheckoutTransaction

                                    mCheckoutTransactionList.add(mCheckoutTransaction)

                                    it.mCheckoutTransaction = mCheckoutTransactionList

                                    val mCheckoutUpdateData = mCheckoutViewModel.updateCheckout(it)

                                    mCheckoutInsertObserver = Observer { mInsert ->
                                        storeCheckoutRowID(mInsert, "Z")
                                        when {
                                            mProgressDialog != null -> mProgressDialog!!.dismiss()
                                        }
                                        val action =
                                            SeatsCheckoutFragmentDirections.actionSeatsCheckoutFragmentToCheckoutFragment(
                                                mInsert,
                                                "Z"
                                            )
                                        findNavController().navigate(action)
                                        mCheckoutUpdateData.removeObserver(mCheckoutInsertObserver!!)
                                    }

                                    mCheckoutUpdateData.observe(viewLifecycleOwner, mCheckoutInsertObserver!!)
                                })
                        }
                    }
                    mCheckoutData.removeObserver(mCheckoutObserver!!)
                }
                else -> mCartViewModel.getCartData(mTableNO, "Z").observe(viewLifecycleOwner,
                    { mCartProductList ->
                        for (i in 0 until mCartProductList.size) mSubTotal += mCartProductList[i].mProductTotalPrice
                        mSubTotal = mSubTotal.setScale(2, RoundingMode.DOWN)
                        insertCheckout(mSeatList, "Z", mSubTotal)
                    })
            }
        }

        mCheckoutData.observe(viewLifecycleOwner, mCheckoutObserver!!)
    }

    private fun proceedForQuickService() {

        e(javaClass.simpleName, "proceedForQuickService")

        mCheckoutViewModel.getCheckoutDataByCartID(
            mSharedPrefs.getString(QUICK_SERVICE_CART_ID, "")!!
        )
            .observe(viewLifecycleOwner, {
                when {
                    it != null -> {
                        e(javaClass.simpleName, "Quick Service checkout row found")
                        when {
                            it.mAmountPaid >= it.mOrderTotal -> {
                                e(javaClass.simpleName, "Quick Service checkout row found 2")
                                mOrderCompleted = true
                                mCheckoutModel = it

                                if (mIntentFrom == CartGroupFragment::class.java.simpleName || mIntentFrom == CheckoutFragment::class.java.simpleName) {
                                    submitOrder(mCheckoutModel!!)
                                }
                            }
                        }
                    }
                    else -> {
                        e(javaClass.simpleName, "Quick Service no checkout row found")
                        val mSeatList = ArrayList<Int>()
                        mSeatList.add(1)

                        mCartViewModel.getCartDataWithCartID(
                            mSharedPrefs.getString(QUICK_SERVICE_CART_ID, "")!!
                        )
                            .observe(viewLifecycleOwner, { mCartProductList ->
                                when {
                                    mCartProductList.isNotEmpty() -> {
                                        var mSubTotal = BigDecimal(0)
                                        for (element in mCartProductList) mSubTotal += element.mProductTotalPrice

                                        mCheckoutViewModel.getTaxData().observe(viewLifecycleOwner,
                                            { taxList ->

                                            mSubTotal = mSubTotal.setScale(2, RoundingMode.DOWN)

                                            var mTaxPercentage = BigDecimal(0)
                                            for (i in 0 until taxList.size) mTaxPercentage += taxList[i].mTaxPercentage

                                            var mTaxAmount =
                                                mSubTotal * (mTaxPercentage.divide(
                                                    BigDecimal(100),
                                                    2,
                                                    RoundingMode.DOWN
                                                ))
                                            mTaxAmount = mTaxAmount.setScale(2, RoundingMode.DOWN)
                                            var mOrderTotal = mSubTotal + mTaxAmount
                                            mOrderTotal = mOrderTotal.setScale(2, RoundingMode.DOWN)

                                            val mCheckoutTransaction = CheckoutTransaction(
                                                mSeatList, true, false, mSubTotal,
                                                mTaxAmount, BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0),
                                                BigDecimal(0), BigDecimal(0), "", BigDecimal(0), BigDecimal(0),
                                                mOrderTotal, BigDecimal(0), mOrderTotal, Date(), false, ArrayList()
                                            )

                                            val mCheckoutTransactionList = ArrayList<CheckoutTransaction>()
                                            mCheckoutTransactionList.add(mCheckoutTransaction)

                                            val mCheckout = CheckoutModel(
                                                mTableNO,
                                                mTableID,
                                                mGroupName!!,
                                                mCartProductList[0].mCartID,
                                                mCartProductList[0].mCartNO, "",
                                                mSubTotal, mTaxAmount, mTaxPercentage, ArrayList(taxList),
                                                BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0),
                                                BigDecimal(0), BigDecimal(0), "", BigDecimal(0), BigDecimal(0),
                                                mOrderTotal,
                                                SERVICE_QUICK_SERVICE, 1, 0, BigDecimal(0), mOrderTotal, Date(),
                                                mCheckoutTransactionList
                                            )

                                            val mInsertData = mCheckoutViewModel.insertCheckoutRow(mCheckout)

                                            mCheckoutInsertObserver = Observer { mInsert ->
                                                storeCheckoutRowID(mInsert, mGroupName!!)
                                                if (mProgressDialog != null)
                                                    mProgressDialog!!.dismiss()
                                                val action =
                                                    SeatsCheckoutFragmentDirections.actionSeatsCheckoutFragmentToCheckoutFragment(
                                                        mInsert,
                                                        mGroupName!!
                                                    )
                                                findNavController().navigate(action)
                                                mInsertData.removeObserver(mCheckoutInsertObserver!!)
                                            }

                                            mInsertData.observe(viewLifecycleOwner, mCheckoutInsertObserver!!)
                                        })
                                    }
                                }
                            })
                    }
                }
            })

    }

    private fun insertCheckout(mSeatList: ArrayList<Int>, groupName: String, total: BigDecimal) {

        var mTotal = total

        mCartViewModel.getCartData(mTableNO, groupName).observe(viewLifecycleOwner, {

            var mSubTotal = BigDecimal(0)
            for (i in 0 until it.size) mSubTotal += it[i].mProductTotalPrice

            mCheckoutViewModel.getTaxData().observe(viewLifecycleOwner, { taxList ->

                var mTaxPercentage = BigDecimal(0)
                for (i in 0 until taxList.size) mTaxPercentage += taxList[i].mTaxPercentage

                mSubTotal = mSubTotal.setScale(2, RoundingMode.HALF_UP)

                e(javaClass.simpleName, "mSubTotal: $mSubTotal")

                var mTaxAmount = mSubTotal * (mTaxPercentage.divide(BigDecimal(100), 2, RoundingMode.DOWN))

                mTaxAmount = mTaxAmount.setScale(2, RoundingMode.HALF_UP)

                e(javaClass.simpleName, "mTaxAmount: $mTaxAmount")

                var mOrderTotal = mSubTotal + mTaxAmount

                mOrderTotal = mOrderTotal.setScale(2, RoundingMode.HALF_UP)

                e(javaClass.simpleName, "mOrderTotal: $mOrderTotal")

                //seat related
                var mSeatTaxAmount = mTotal * (mTaxPercentage.divide(BigDecimal(100), 2, RoundingMode.DOWN))
                mSeatTaxAmount = mSeatTaxAmount.setScale(2, RoundingMode.HALF_UP)


                var mSeatOrderTotal = mTotal + mSeatTaxAmount
                mSeatOrderTotal = mSeatOrderTotal.setScale(2, RoundingMode.HALF_UP)

                //rounding everything before inserting
//                mSeatCartTotal = mSeatCartTotal.setScale(2, RoundingMode.HALF_UP)
                mTotal = mTotal.setScale(2, RoundingMode.HALF_UP)

                val mCheckoutTransaction = CheckoutTransaction(
                    mSeatList, true, false, mTotal,
                    mSeatTaxAmount, BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0),
                    BigDecimal(0), BigDecimal(0), "", BigDecimal(0), BigDecimal(0),
                    mSeatOrderTotal, BigDecimal(0), mSeatOrderTotal, Date(), false, ArrayList()
                )

                val mCheckoutTransactionList = ArrayList<CheckoutTransaction>()
                mCheckoutTransactionList.add(mCheckoutTransaction)

                mTaxAmount = mTaxAmount.setScale(2, RoundingMode.DOWN)

                val mCheckout = CheckoutModel(
                    mTableNO,
                    mTableID,
                    groupName,
                    it[0].mCartID,
                    it[0].mCartNO, "",
                    mSubTotal, mTaxAmount, mTaxPercentage, ArrayList(taxList),
                    BigDecimal(0), BigDecimal(0), BigDecimal(0), BigDecimal(0),
                    BigDecimal(0), BigDecimal(0), "", BigDecimal(0), BigDecimal(0),
                    mOrderTotal,
                    //todo:check this
                    1, 1, 0, BigDecimal(0), mOrderTotal, Date(),
                    mCheckoutTransactionList
                )

                val mInsertData = mCheckoutViewModel.insertCheckoutRow(mCheckout)

                mCheckoutInsertObserver = Observer { mInsert ->
                    storeCheckoutRowID(mInsert, groupName)
                    val action =
                        SeatsCheckoutFragmentDirections.actionSeatsCheckoutFragmentToCheckoutFragment(
                            mInsert,
                            groupName
                        )
                    findNavController().navigate(action)
                    mInsertData.removeObserver(mCheckoutInsertObserver!!)
                }

                mInsertData.observe(viewLifecycleOwner, mCheckoutInsertObserver!!)
            })

        })
    }

    private fun storeCheckoutRowID(mCheckoutRowID: Int, mGroupName: String) {
        val mEditor = mSharedPrefs.edit()
        mEditor.putInt(AppConstants.CHECKOUT_ROW_ID, mCheckoutRowID)
        mEditor.putString(AppConstants.GROUP_NAME, mGroupName)
        mEditor.apply()
    }

    private var mSelectAllListener =
        CompoundButton.OnCheckedChangeListener { _, isChecked ->
            var mSeatCount = 0
            for (i in 0 until mSeatListWithPrice.size) {
                mSeatListWithPrice[i].isSelected = isChecked

                if (mSeatListWithPrice[i].isSelected && !mSeatListWithPrice[i].mPaid)
                    mSeatCount++
            }

            if (mSeatAdapter != null)
                mSeatAdapter!!.setSelectedSeat(mSeatCount)
        }

    private var mTableObserver: Observer<List<LocalTableGroupModel>>? = null


    private fun setUpRecyclerView() {
        val mTableData = mCheckoutViewModel.getTableGroupsAndSeats(mTableNO, mLocationID)

        mTableObserver = Observer {
            if (it.isNotEmpty()) {
                for (i in 0 until it.size) {
                    if (mGroupName == it[i].mGroupName) {
                        assignPriceToSeats(ArrayList(it[i].mSeatList!!))
                        break
                    }
                }
                mTableData.removeObserver(mTableObserver!!)
            }
        }
        mTableData.observe(viewLifecycleOwner, mTableObserver!!)
    }

    private var mCartObserver: Observer<List<CartProductModel>>? = null
    private var mSeatListWithPrice = ArrayList<SeatPriceModel>()
    private var mSeatAdapter: SeatsCheckoutAdapter? = null
    private var mCartID = ""
    private var mCartNO = ""
    private var mZeroSeatCount = 0

    private fun assignPriceToSeats(mTableSeatList: ArrayList<LocalTableSeatModel>) {

        val mCartData = mCartViewModel.getCartData(mTableNO, mGroupName!!)

        mSeatListWithPrice = ArrayList()
        mCartObserver = Observer {

            //add seats which has products assigned to it, calculate it's total
            for (i in it.indices) {
                val cartSeatList = it[i].mAssignedSeats
                mCartID = it[i].mCartID
                mCartNO = it[i].mCartNO

                for (j in 0 until mTableSeatList.size) {
                    loop@ for (k in 0 until cartSeatList!!.size) {
                        when (mTableSeatList[j].mSeatNO) {
                            cartSeatList[k].mSeatNO -> {

                                var mRepeatSeat = false
                                when {
                                    mSeatListWithPrice.size > 0 -> loop1@ for (l in 0 until mSeatListWithPrice.size) {
                                        when (mSeatListWithPrice[l].mSeatNo) {
                                            cartSeatList[k].mSeatNO -> {

                                                val mSeatTotal = mSeatListWithPrice[l].mSeatTotal +
                                                        it[i].mProductTotalPrice.divide(
                                                            BigDecimal(cartSeatList.size),
                                                            4,
                                                            RoundingMode.DOWN
                                                        )

                                                e(
                                                    javaClass.simpleName,
                                                    "mSeatTotal ${cartSeatList[k].mSeatNO}: $mSeatTotal"
                                                )
                                                val mSeatPriceModel = SeatPriceModel(
                                                    cartSeatList[k].mSeatNO,
                                                    mSeatTotal,
                                                    false, mTableSeatList[j].isPaid
                                                )
                                                mSeatListWithPrice[l] = mSeatPriceModel
                                                mRepeatSeat = true
                                                break@loop1
                                            }
                                            else -> mRepeatSeat = false
                                        }
                                    }
                                    else -> mRepeatSeat = false
                                }

                                when {
                                    !mRepeatSeat -> {

                                        val mSeatTotal =
                                            it[i].mProductTotalPrice.divide(
                                                BigDecimal(cartSeatList.size),
                                                4,
                                                RoundingMode.DOWN
                                            )

                                        e(javaClass.simpleName, "mSeatTotal ${cartSeatList[k].mSeatNO}: $mSeatTotal")

                                        mSeatListWithPrice.add(
                                            SeatPriceModel(
                                                cartSeatList[k].mSeatNO,
                                                mSeatTotal,
                                                false, mTableSeatList[j].isPaid
                                            )
                                        )
                                    }
                                }
                                break@loop
                            }
                        }
                    }
                }
            }

            //add seats which has no product assigned to it
            var mPaidSeats = 0
            for (j in 0 until mTableSeatList.size) {
                var mHasSeat = false
                loop@ for (l in 0 until mSeatListWithPrice.size) {
                    when (mTableSeatList[j].mSeatNO) {
                        mSeatListWithPrice[l].mSeatNo -> {
                            mHasSeat = true
                            break@loop
                        }
                        else -> mHasSeat = false
                    }
                }

                when {
                    !mHasSeat -> {
                        mSeatListWithPrice.add(
                            SeatPriceModel(
                                mTableSeatList[j].mSeatNO,
                                BigDecimal(0),
                                false, mPaid = true
                            )
                        )
                        mPaidSeats++
                    }
                }
            }

            //filling out the difference if any because of the splitting the seats
            var mCartTotal = BigDecimal(0)
            for (element in it) {
                mCartTotal += element.mProductTotalPrice.setScale(2, RoundingMode.HALF_UP)
            }

            var mSeatTotal = BigDecimal(0)
            for (l in 0 until mSeatListWithPrice.size) {
                mSeatListWithPrice[l].mSeatTotal = mSeatListWithPrice[l].mSeatTotal.setScale(2, RoundingMode.DOWN)
                mSeatTotal += mSeatListWithPrice[l].mSeatTotal
                //seats which are already paid
                when {
                    mSeatListWithPrice[l].mPaid && mSeatListWithPrice[l].mSeatTotal > BigDecimal(0) -> mPaidSeats++
                }
                when {
                    mSeatListWithPrice[l].mSeatTotal <= BigDecimal(0) -> mZeroSeatCount++
                }
            }

            mSharedPrefs.edit().putInt(ZERO_SEAT_LIST_COUNT, mZeroSeatCount).apply()

            e(javaClass.simpleName, "mCartTotal before  : $mCartTotal")
            mCartTotal = mCartTotal.setScale(2, RoundingMode.DOWN)
            mSeatTotal = mSeatTotal.setScale(2, RoundingMode.DOWN)

            e(javaClass.simpleName, "mCartTotal after : $mCartTotal")
            e(javaClass.simpleName, "mSeatTotal : $mSeatTotal")

            val mDifference = mCartTotal - mSeatTotal

            e(javaClass.simpleName, "mDifference : $mDifference")

            when {
                mDifference > BigDecimal(0) -> {

                    val mCount = (mDifference / BigDecimal(0.01)).toInt()

                    when {
                        mCount > 0 && mCount <= mSeatListWithPrice.size -> {

                            e(javaClass.simpleName, "mCount : $mCount")
                            for (l in (mSeatListWithPrice.size - mCount) until mSeatListWithPrice.size) {
                                mSeatListWithPrice[l].mSeatTotal = mSeatListWithPrice[l].mSeatTotal + BigDecimal(0.01)
                            }

                        }
                    }

                }

                //set seats to adapter
            }

            val mFinalSeatListWithPrice = ArrayList<SeatPriceModel>()

            for (l in 0 until mSeatListWithPrice.size) {
                when {
                    mSeatListWithPrice[l].mSeatTotal > BigDecimal(0.00) -> mFinalSeatListWithPrice.add(
                        mSeatListWithPrice[l]
                    )
                }
            }
            mFinalSeatListWithPrice.sortWith({ o1, o2 -> o1.mSeatNo - o2.mSeatNo })

            for (l in 0 until mSeatListWithPrice.size) {
                when {
                    mSeatListWithPrice[l].mSeatTotal <= BigDecimal(0.00) -> mFinalSeatListWithPrice.add(
                        mSeatListWithPrice[l]
                    )
                }
            }

            //set seats to adapter
            mSeatAdapter = SeatsCheckoutAdapter(requireActivity(), mFinalSeatListWithPrice, mPaidSeats)
            mBinding.recyclerViewSeats.adapter = mSeatAdapter

        }

        mCartData.observe(viewLifecycleOwner, mCartObserver!!)
    }

    private var mProgressDialog: CustomProgressDialog? = null
    private var mTryAgainCheckoutModel: CheckoutModel? = null

    private fun tryAgain() {
        mBinding.buttonTryAgain.setOnClickListener {
            submitOrder(mTryAgainCheckoutModel!!)

            mBinding.constraintLayoutNetworkError.visibility = GONE
            mBinding.constraintLayoutCheckoutSeat.visibility = VISIBLE
        }
    }

    private fun submitOrder(mCheckout: CheckoutModel) {

        mTryAgainCheckoutModel = mCheckout

        try {
            if (mProgressDialog != null)
                mProgressDialog!!.dismiss()

            e(javaClass.simpleName, "mCheckout.mCartID: ${mCheckout.mCartID} mCheckout.mTableID: ${mCheckout.mTableID}")

            mBinding.constraintLayoutCheckoutSeat.visibility = GONE
            //progress dialog
            mProgressDialog = CustomProgressDialog.newInstance(
                "Submitting Order",
                "Please Wait While The Order is Being Submitted",
                DIALOG_TYPE_OTHER
            )
            mProgressDialog!!.show(childFragmentManager, AppConstants.CUSTOM_PROGRESS_DIALOG_FRAGMENT)
            mProgressDialog!!.isCancelable = false
        } catch (e: Exception) {
            e.printStackTrace()
        }


        Network.isNetworkAvailableWithInternetAccess(requireContext()).observe(viewLifecycleOwner, {
            when {
                it != null ->
                    when {
                        it -> mCheckoutViewModel.submitOrder(mCheckout).observe(viewLifecycleOwner,
                            { orderResponse ->
                            when {
                                orderResponse != null -> when {
                                    orderResponse > 0 -> {
                                        EventBus.getDefault().post(DrawerEvent(false, "A"))
                                        if (mProgressDialog != null)
                                            mProgressDialog!!.dismiss()

                                        if (mSharedPrefs.getBoolean(AUTO_LOGOUT_ENABLED, false))
                                            (activity as MainScreenActivity).serverLogout()
                                        else
                                            findNavController()
                                                .navigate(SeatsCheckoutFragmentDirections.actionSeatsCheckoutFragmentToTablesFragment())
                                    }
                                    orderResponse == -1 -> {
                                        if (mProgressDialog != null)
                                            mProgressDialog!!.dismiss()

                                        mBinding.constraintLayoutNetworkError.visibility = VISIBLE
                                        mBinding.constraintLayoutCheckoutSeat.visibility = GONE
                                    }
                                }
                            }
                        })
                        else -> {
                            mProgressDialog!!.dismiss()
                            CustomToast.makeText(requireActivity(), getString(R.string.network_error), LENGTH_SHORT).show()
                            mBinding.constraintLayoutNetworkError.visibility = VISIBLE
                            mBinding.constraintLayoutCheckoutSeat.visibility = GONE
                        }
                    }

            }
        })
    }

    @Subscribe
    fun onAllSeatsSelected(mEvent: SeatsCheckoutEvent) {
        mBinding.checkBoxSelectAllSeats.setOnCheckedChangeListener(null)
        mBinding.checkBoxSelectAllSeats.isChecked = mEvent.isAllSelected
        mBinding.checkBoxSelectAllSeats.setOnCheckedChangeListener(mSelectAllListener)
    }

    @Subscribe
    fun isBillPrintDone(mEvent: BillPrintEvent) {

        e(javaClass.simpleName, "isBillPrintDone : $mOrderCompleted")

        if (!mEvent.mBillPrintDone) {
            CustomToast.makeText(requireActivity(), "Printer Not Found", LENGTH_SHORT)
        }

        if (mOrderCompleted) {
            submitOrder(mCheckoutModel!!)
        }

        if (!isSeatSelectionEnabled && mSelectedLocationType != SERVICE_QUICK_SERVICE) {
            proceedWithoutSeatSelection(0)
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

    //TODO: HANDLE BACK PRESS
//    override fun handleOnBackPressed(): Boolean {
//        val mDrawerVisibility = (activity as MainScreenActivity).checkNavigationDrawerVisibility()
//
//        if (!mDrawerVisibility){
//            val action = SeatsCheckoutFragmentDirections.actionSeatsCheckoutFragmentToCartGroupFragment(
//                mGroupName!!,
//                SEATS_CHECKOUT_FRAGMENT
//            )
//            findNavController().navigate(action)
//        }
//
//
//        return true
//    }

}
