package com.rsl.youresto.ui.main_screen.tables_and_tabs.tables


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.R
import com.rsl.youresto.data.database_download.models.TablesModel
import com.rsl.youresto.data.tables.models.ServerTableGroupModel
import com.rsl.youresto.databinding.DialogGroupListBinding
import com.rsl.youresto.databinding.FragmentTablesBinding
import com.rsl.youresto.ui.main_screen.MainScreenActivity
import com.rsl.youresto.ui.main_screen.cart.CartViewModel
import com.rsl.youresto.ui.main_screen.cart.CartViewModelFactory
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModelFactory
import com.rsl.youresto.ui.main_screen.estimate_bill_print.EstimateBillPrint50Activity
import com.rsl.youresto.ui.main_screen.estimate_bill_print.EstimateBillPrint80Activity
import com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab.ShowCartEvent
import com.rsl.youresto.ui.main_screen.tables_and_tabs.tables.events.PrintGroupEvent
import com.rsl.youresto.ui.tab_specific.TablesTabFragment
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.SEAT_SELECTION_GROUP
import com.rsl.youresto.utils.AppConstants.SELECTED_BILL_PRINTER_NAME
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_NO
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppPreferences
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.custom_dialog.CustomProgressDialog
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("LogNotTimber")
class TablesFragment : Fragment() {

    private lateinit var mBinding: FragmentTablesBinding
    private lateinit var mViewModel: TablesViewModel
    private lateinit var mCartViewModel: CartViewModel
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mLocationID: String
    private lateinit var mCheckoutViewModel: CheckoutViewModel

    private val tablesViewModel: NewTablesViewModel by viewModel()
    private val prefs: AppPreferences by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_tables, container, false)
        val view: View = mBinding.root

        mSharedPrefs = requireActivity().getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)

        mLocationID = mSharedPrefs.getString(SELECTED_LOCATION_ID, "")!!

        val factory = InjectorUtils.provideTablesViewModelFactory(requireActivity())
        mViewModel = ViewModelProviders.of(this, factory).get(TablesViewModel::class.java)

        val cartFactory: CartViewModelFactory = InjectorUtils.provideCartViewModelFactory(requireActivity())
        mCartViewModel = ViewModelProviders.of(this, cartFactory).get(CartViewModel::class.java)

        val checkoutFactory: CheckoutViewModelFactory = InjectorUtils.provideCheckoutViewModelFactory(requireActivity())
        mCheckoutViewModel = ViewModelProviders.of(this, checkoutFactory).get(CheckoutViewModel::class.java)

        checkNetworkAndProceed()

        mBinding.buttonRetry.setOnClickListener { checkNetworkAndProceed() }

        mBinding.table = this
        return view
    }

    private fun checkNetworkAndProceed() {

//        mBinding.buttonRetry.isEnabled = false
//
//        mProgressDialog = CustomProgressDialog.newInstance(
//            "Network Connection",
//            "Checking Network Connection, please wait",
//            DIALOG_TYPE_NETWORK
//        )
//        mProgressDialog!!.show(childFragmentManager, AppConstants.CUSTOM_PROGRESS_DIALOG_FRAGMENT)
//        mProgressDialog!!.isCancelable = false
//
//        Network.isNetworkAvailableWithInternetAccess(requireActivity()).observe(viewLifecycleOwner,
//            {
//                if (it) {
//                    mProgressDialog!!.dismiss()
//                    mBinding.buttonRetry.isEnabled = true
//                    showTables()
//                    initViews()
//                } else {
//                    mProgressDialog!!.dismiss()
//                    CustomToast.makeText(
//                        requireActivity(),
//                        "Please check your network connection and try again!",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    mBinding.buttonRetry.isEnabled = true
//                    showRetry()
//                }
//            })

        showTables()
        initViews()
    }

    private fun showRetry() {
        mBinding.textViewNoInternetConnection.visibility = VISIBLE
        mBinding.buttonRetry.visibility = VISIBLE
        mBinding.recyclerViewTables.visibility = GONE
    }

    private fun showTables() {
        mBinding.textViewNoInternetConnection.visibility = GONE
        mBinding.buttonRetry.visibility = GONE
        mBinding.recyclerViewTables.visibility = VISIBLE
    }

    override fun onResume() {
        mSharedPrefs.edit().putInt(AppConstants.LOCATION_SERVICE_TYPE, SERVICE_DINE_IN).apply()
        mSharedPrefs.edit().putString(AppConstants.GROUP_NAME, "").apply()
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, this)
        super.onResume()
    }

    private var mProgressDialog: CustomProgressDialog? = null

    private fun initViews() {

//        mProgressDialog = CustomProgressDialog.newInstance(
//            "Refreshing Tables",
//            "Please Wait..",
//            DIALOG_TYPE_OTHER
//        )
//        mProgressDialog!!.show(childFragmentManager, AppConstants.CUSTOM_PROGRESS_DIALOG_FRAGMENT)
//        mProgressDialog!!.isCancelable = false

        setupTablesRecyclerView()

//        mViewModel.syncTables(mLocationID).observe(viewLifecycleOwner, {
//            when (it) {
//                1 -> mCartViewModel.syncCart(mLocationID).observe(viewLifecycleOwner, { syncCart ->
//                    if (syncCart != null) {
//                        when (syncCart) {
//                            1 -> {
//                                mCheckoutViewModel.paymentSync(mSharedPrefs.getString(SELECTED_LOCATION_ID, "")!!)
//                                Handler().postDelayed({ mProgressDialog!!.dismiss() }, 1000)
//                            }
//                            2 -> {
//                                CustomToast.makeText(
//                                    requireActivity(),
//                                    "Network problem, couldn't refresh carts",
//                                    Toast.LENGTH_SHORT
//                                )
//                                mProgressDialog!!.dismiss()
//                            }
//                        }
//                    }
//                })
//                2 -> {
//                    CustomToast.makeText(requireActivity(), "Network problem, couldn't refresh tables", Toast.LENGTH_SHORT)
//                    mProgressDialog!!.dismiss()
//                }
//            }
//
//        })



    }

    private var mTableAdapter: TableRecyclerAdapter? = null

    private fun setupTablesRecyclerView() {
        requireActivity().registerForContextMenu(mBinding.recyclerViewTables)

//        mViewModel.getTablesData(mLocationID, SERVICE_DINE_IN).observe(viewLifecycleOwner, {
//
//            if (mTableNo.isEmpty()) {
//                mTableAdapter =
//                    TableRecyclerAdapter(ArrayList(it))
//                mBinding.recyclerViewTables.adapter = mTableAdapter
//
//                runLayoutAnimationFallDown(mBinding.recyclerViewTables)
//            } else {
//                val mFilteredTableList: ArrayList<TablesModel> = ArrayList()
//
//                for (i in 0 until it.size) {
//                    if (it[i].mTableNo.toString().contains(mTableNo)) {
//                        mFilteredTableList.add(it[i])
//                    }
//                }
//
//                mTableAdapter =
//                    TableRecyclerAdapter(mFilteredTableList)
//                mBinding.recyclerViewTables.adapter = mTableAdapter
//                runLayoutAnimationFallDown(mBinding.recyclerViewTables)
//            }
//        })

        tablesViewModel.filterText.value = ""

        tablesViewModel.getTablesData(prefs.getSelectedLocation(), "1".toInt()).observe(viewLifecycleOwner) {
            it?.let {
                mTableAdapter =
                    TableRecyclerAdapter(ArrayList(it))
                mBinding.recyclerViewTables.adapter = mTableAdapter
            }
        }
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        val mTable: TablesModel
        try {
            mTable = mTableAdapter!!.getTableLongPressed()
        } catch (e: Exception) {
            Log.d(javaClass.simpleName, e.localizedMessage, e)
            return super.onContextItemSelected(item)
        }

        when (item.itemId) {
            R.id.menu_print_bill -> {
                if (mTable.mGroupList!!.size == 1) {
                    printBill(mTable, mTable.mGroupList!![0])
                } else {
                    openGroupPopup(mTable)
                }
            }
            R.id.menu_checkout -> {
                close(mTable, mTable.mGroupList!![0])
            }
            R.id.menu_clear_table -> {
                checkTableKOT(mTable)
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun close(mTable: TablesModel, mGroup: ServerTableGroupModel) {
        if (mSharedPrefs.getString(SELECTED_BILL_PRINTER_NAME, "") == ""
            || mSharedPrefs.getString(SELECTED_BILL_PRINTER_NAME, "") == "NO_TYPE"
        ) {

            CustomToast.makeText(requireActivity(), "Select bill printer from app settings to proceed", Toast.LENGTH_SHORT).show()
        } else {

            when {
                mTable.mGroupList!!.size > 1 ->
                    CustomToast.makeText(requireActivity(), "Table has multiple groups, please go through normal process", Toast.LENGTH_SHORT).show()
                else -> mCartViewModel.getTableCartData(mTable.mTableNo).observe(viewLifecycleOwner,
                    {
                        when {
                            it.isNotEmpty() -> {
                                var mCheckKOTFlag = true

                                for (i in 0 until it.size) {
                                    if (it[i].mKitchenPrintFlag == 0) {
                                        mCheckKOTFlag = false
                                        break
                                    }
                                }

                                when {
                                    mCheckKOTFlag -> {

                                        val mEditor = mSharedPrefs.edit()
                                        mEditor.putInt(SELECTED_TABLE_NO, mTable.mTableNo)
                                        mEditor.putString(SELECTED_TABLE_ID, mTable.mTableID)
                                        mEditor.apply()

                                        if (mGroup.mGroupName != "NULL") {
                                            val action =
                                                TablesFragmentDirections.actionTablesFragmentToSeatsCheckoutFragment(mGroup.mGroupName, javaClass.simpleName)
                                            findNavController().navigate(action)
                                        } else {
                                            val action =
                                                TablesFragmentDirections.actionTablesFragmentToSeatsCheckoutFragment("A", javaClass.simpleName)
                                            findNavController().navigate(action)
                                        }
                                    }
                                    else -> CustomToast.makeText(requireActivity(), "Items pending in cart to be sent to kitchen", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    })
            }

        }
    }

    private fun printBill(mTable: TablesModel, mGroup: ServerTableGroupModel) {

        if (mSharedPrefs.getString(SELECTED_BILL_PRINTER_NAME, "") == ""
            || mSharedPrefs.getString(SELECTED_BILL_PRINTER_NAME, "") == "NO_TYPE"
        ) {

            CustomToast.makeText(requireActivity(), "Bill Printer not selected", Toast.LENGTH_SHORT).show()
        } else {
            val mEstimateIntent: Intent =
                if (mSharedPrefs.getInt(AppConstants.SELECTED_BILL_PRINT_PAPER_SIZE, 0) == AppConstants.PAPER_SIZE_50) {
                    Intent(requireActivity(), EstimateBillPrint50Activity::class.java)
                } else {
                    Intent(requireActivity(), EstimateBillPrint80Activity::class.java)
                }

            mEstimateIntent.putExtra(AppConstants.TABLE_NO, mTable.mTableNo)
            mEstimateIntent.putExtra(AppConstants.GROUP_NAME, mGroup.mGroupName)
            mEstimateIntent.putExtra(AppConstants.ORDER_NO, mGroup.mCartNO)
            if (mTable.mTableNo != 100)
                mEstimateIntent.putExtra(AppConstants.ORDER_TYPE, 1)
            else
                mEstimateIntent.putExtra(AppConstants.ORDER_TYPE, 2)

            startActivity(mEstimateIntent)
        }
    }

    private var mGroupListDialog: Dialog? = null

    private var mSelectedTable: TablesModel? = null

    private fun openGroupPopup(mTable: TablesModel) {

        mSelectedTable = mTable

        mGroupListDialog = Dialog(requireActivity())

        val mGroupDialogBinding =
            DataBindingUtil.inflate<DialogGroupListBinding>(
                LayoutInflater.from(context),
                R.layout.dialog_group_list,
                null,
                false
            )

        mGroupListDialog!!.setContentView(mGroupDialogBinding.root)

        val mGroupAdapter = PrintGroupRecyclerAdapter(mTable.mGroupList!!)
        mGroupDialogBinding.recyclerViewGroups.adapter = mGroupAdapter

        mGroupListDialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        mGroupListDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        mGroupListDialog!!.show()
    }

    @Subscribe
    fun onPrintGroupClick(mEvent: PrintGroupEvent) {
        printBill(mSelectedTable!!, mEvent.mGroup)
        mGroupListDialog!!.dismiss()
    }

    private var mClearTableObserver: Observer<Int>? = null

    private fun checkTableKOT(mTable: TablesModel) {
        mCartViewModel.getTableCartData(mTable.mTableNo).observe(viewLifecycleOwner, {
            if (it != null) {
                when {
                    it.isEmpty() -> clear(mTable.mTableID)
                    else -> {
                        var mCheckKOTFlag = false

                        for (i in 0 until it.size) {
                            if (it[i].mKitchenPrintFlag == 1) {
                                mCheckKOTFlag = true
                                break
                            }
                        }

                        if (mCheckKOTFlag) {
                            CustomToast.makeText(
                                requireActivity(),
                                "Order already sent to kitchen, table cannot be clear",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            clear(mTable.mTableID)
                        }
                    }
                }
            }
        })
    }

    private fun clear(mTableID: String) {
        val mClearTableData = mViewModel.clearTable(mTableID, requireActivity())
        mClearTableObserver = Observer { integer ->
            if (integer != null) {
                when (integer) {
                    1 -> CustomToast.makeText(requireActivity(), "Table cleared successfully", Toast.LENGTH_SHORT).show()
                    -1 -> CustomToast.makeText(requireActivity(), "Internet not available", Toast.LENGTH_SHORT).show()
                    else -> CustomToast.makeText(requireActivity(), "Something went wrong", Toast.LENGTH_SHORT).show()
                }
                mClearTableData.removeObserver(mClearTableObserver!!)
            }
        }
        mClearTableData.observe(viewLifecycleOwner, mClearTableObserver!!)
    }

    fun onTextChanged(mText: CharSequence) {
//        setupTablesRecyclerView(mText.toString())
        tablesViewModel.filterText.value = mText.toString()
    }

    @Subscribe
    fun onTableClicked(mTable: TablesModel) {

        val mEditor = mSharedPrefs.edit()
        mEditor.putInt(SELECTED_TABLE_NO, mTable.mTableNo)
        mEditor.putString(SELECTED_TABLE_ID, mTable.mTableID)
        mEditor.putString(SEAT_SELECTION_GROUP, "DF")
        mEditor.apply()

        ((parentFragment as NavHostFragment).parentFragment as TablesTabFragment).showProducts()
        if (mTable.mTableNoOfOccupiedChairs > 0) {
            showCart()
        }

//        val action =
//            if (mTable.mTableNoOfOccupiedChairs > 0) TablesFragmentDirections.actionTablesFragmentToMainProductFragment("A","A",javaClass.simpleName)
//            else TablesFragmentDirections.actionTablesFragmentToSeatSelectionFragment(mTable.mTableID, javaClass.simpleName)
//
//        Navigation.findNavController(requireActivity(), R.id.main_screen_host_fragment).navigate(action)
//
//        Utils.hideKeyboardFrom(requireActivity(), mBinding.editTextSearchTables)
    }

    private fun showCart() {
        ((parentFragment as NavHostFragment).parentFragment as TablesTabFragment).showCart()
    }

    @Subscribe
    fun showCartEvent(showCartEvent: ShowCartEvent){
        if (showCartEvent.showCart){
            showCart()
        }
    }

    private var doubleBackToExitPressedOnce = false

    private fun pressAgainMethod() {
        val mDrawerVisibility = (activity as MainScreenActivity).checkNavigationDrawerVisibility()

        if (mDrawerVisibility)
            return

        if (doubleBackToExitPressedOnce) {
            (activity as MainScreenActivity).serverLogout()
            return
        }

        this.doubleBackToExitPressedOnce = true
        if (!requireActivity().isFinishing)
            CustomToast.makeText(requireActivity(), "Press again to exit", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
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
