package com.rsl.foodnairesto.ui.main_screen.order_history.order_history_list

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.rsl.foodnairesto.App
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.data.database_download.models.ReportModel
import com.rsl.foodnairesto.data.database_download.models.ServerModel
import com.rsl.foodnairesto.data.main_login.network.LOG_TAG
import com.rsl.foodnairesto.databinding.FragmentOrderHistoryListBinding
import com.rsl.foodnairesto.ui.main_screen.order_history.NewOrdersViewModel
import com.rsl.foodnairesto.ui.main_screen.order_history.OrderHistoryViewModel
import com.rsl.foodnairesto.ui.main_screen.order_history.event.OrderHistoryListEvent
import com.rsl.foodnairesto.ui.main_screen.order_history.model.LocationTypeModel
import com.rsl.foodnairesto.ui.main_screen.order_history.order_history_cart.OrderHistoryCartFragment
import com.rsl.foodnairesto.ui.server_login.ServerLoginViewModel
import com.rsl.foodnairesto.utils.*
import com.rsl.foodnairesto.utils.custom_dialog.CustomProgressDialog
import com.rsl.foodnairesto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("LogNotTimber")
class OrderHistoryListFragment : Fragment() {
    private lateinit var mBinding: FragmentOrderHistoryListBinding
    private lateinit var mOrderHistoryViewModel: OrderHistoryViewModel
    private val serverViewModel: ServerLoginViewModel by viewModel()

    private var mFromDateInMillis: Long = 0
    private var mToDateInMillis: Long = 0
    private var mSharedPref: SharedPreferences? = null

    private var mHour: Int = 0
    private var mMinute: Int = 0
    private var mFlagToPopulateList = 0

    private val ordersViewModel: NewOrdersViewModel by viewModel()

    private val prefs: AppPreferences by inject()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_history_list, container, false)
        val mView = mBinding.root

        mSharedPref = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        val factory = InjectorUtils.provideOrderHistoryViewModelFactory(requireActivity())
        mOrderHistoryViewModel = ViewModelProviders.of(this, factory).get(OrderHistoryViewModel::class.java)

        val mIntentFrom = ""

        editTextDateFrom()
        editTextDateTo()
      //  setUpLocationFilter()
      //  setUpServerFilter()
          setUpServerFilterD()

        mBinding.serverNameTv?.text = prefs.getServerName()

        if (mIntentFrom != OrderHistoryCartFragment::class.java.simpleName)
            checkForUpdateData()
        else {
            mBinding.constraintLayoutMainOrderHistory.visibility = VISIBLE
            initialiseDates()
        }

        mBinding.fragment = this

        return mView
    }

    var mDateTime = ""

    private var mProgressDialog: CustomProgressDialog? = null

    private fun checkForUpdateData() {


        CustomToast.makeText(requireActivity(), "Updating orders in the background", Toast.LENGTH_LONG).show()

//        mProgressDialog = CustomProgressDialog.newInstance(
//            "Updating Reports",
//            "Please Wait..",
//            DIALOG_TYPE_OTHER
//        )
//        mProgressDialog!!.show(childFragmentManager, AppConstants.CUSTOM_PROGRESS_DIALOG_FRAGMENT)
//        mProgressDialog!!.isCancelable = true

        ordersViewModel.getCartReportsFromNetwork()

        initialiseDates()

        ordersViewModel.reportData.observe(viewLifecycleOwner) { event ->
            event?.getContentIfNotHandled()?.let {
                if (it.status){
                    initialiseDates()
                }
                mProgressDialog?.dismiss()
            }
        }
    }

    private fun initialiseDates() {
        val today = Date()
        val format = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
        var mStringDateFrom = format.format(today)

        val format2 = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.ENGLISH)
        val mStringDateTo = format2.format(today)

        mStringDateFrom = "$mStringDateFrom 00:00"

        mBinding.editTextActiveFrom.setText(mStringDateFrom)
        mBinding.editTextActiveTo.setText(mStringDateTo)

        mFromDateInMillis = changeDateTimeInMillis(mStringDateFrom)
        mToDateInMillis = Utils.getDate("dd-MM-yyyy HH:mm:ss")!!.time

        setupOrderListRecyclerView("")   //1
    }

    private fun editTextDateFrom() {
        mBinding.editTextActiveFrom.setOnClickListener { showDatePicker(mBinding.editTextActiveFrom) }
    }

    private fun editTextDateTo() {
        mBinding.editTextActiveTo.setOnClickListener { showDatePicker(mBinding.editTextActiveTo) }
    }

    private fun showDatePicker(mEditText: EditText) {
        val c = Calendar.getInstance()
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireActivity(), R.style.DateDialogTheme,
            { _, year, monthOfYear, dayOfMonth ->
                val day = String.format(Locale.ENGLISH, "%02d", dayOfMonth)
                val month = String.format(Locale.ENGLISH, "%02d", monthOfYear + 1)
                val mDate = "$day-$month-$year"
                showTimePicker(mDate, mEditText)
            }, mYear, mMonth, mDay
        )
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        if (mEditText == mBinding.editTextActiveTo) {
            datePickerDialog.datePicker.minDate = mFromDateInMillis
        }
        datePickerDialog.show()
    }

    private fun showTimePicker(mDate: String, mEditText: EditText) {
        val c = Calendar.getInstance()
        mHour = c.get(Calendar.HOUR_OF_DAY)
        mMinute = c.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            activity, R.style.DateDialogTheme,
            { _, hourOfDay, minute ->
                val hours = String.format(Locale.ENGLISH, "%02d", hourOfDay)
                val minutes = String.format(Locale.ENGLISH, "%02d", minute)
                mHour = hourOfDay
                mMinute = minute
                val mTime = "$mDate $hours:$minutes"
                mEditText.setText(mTime)
                setDate()
            }, mHour, mMinute, false
        )
        timePickerDialog.show()
    }

    private fun setDate() {
        if ((mBinding.editTextActiveFrom.text.toString() == "") or (mBinding.editTextActiveTo.text.toString() == ""))
            CustomToast.makeText(requireActivity(), "Please select date first", Toast.LENGTH_LONG).show()
        else {
            mBinding.progressBarBillReportList.visibility = VISIBLE
            mBinding.textViewNoOrders.visibility = GONE

            mFromDateInMillis = changeDateTimeInMillis(mBinding.editTextActiveFrom.text.toString())
            mToDateInMillis = changeDateTimeInMillis(mBinding.editTextActiveTo.text.toString())

            if (mFromDateInMillis < mToDateInMillis) {
                mBinding.textViewUpdatingData.visibility = GONE
                setupOrderListRecyclerView("")
            } else {
                mBinding.progressBarBillReportList.visibility = INVISIBLE
                mBinding.textViewUpdatingData.visibility = VISIBLE
                mBinding.textViewUpdatingData.text = "To date/time should be greater than From date/time"
            }
        }
    }

    private var mReportData: LiveData<List<ReportModel>>? = null
    private var mReportObserver: Observer<List<ReportModel>>? = null

    private fun setupOrderListRecyclerView(mSearchText: String)
    {

        Log.e("RUN","1");
        e(javaClass.simpleName, "mLocationTypeID: $mLocationTypeID mServerName: $mServerName")
        e(javaClass.simpleName, "mFromDateInMillis: $mFromDateInMillis mToDateInMillis: $mToDateInMillis")

        mBinding.progressBarBillReportList.visibility = VISIBLE

        mReportData = mOrderHistoryViewModel.getReportDataForTimeStamp(
            mFromDateInMillis,
            mToDateInMillis,
            mLocationTypeID,
            mServerName
        )

        mReportObserver = Observer { reportModels ->
            mBinding.progressBarBillReportList.visibility = GONE
            e(javaClass.simpleName, "reportModels: ${reportModels.size}")
            when {
                reportModels != null ->
                    when {
                        reportModels.isNotEmpty() -> {
                            val mReportList = ArrayList<ReportModel>()

                            when {
                                mSearchText.isEmpty() -> mReportList.addAll(reportModels)
                                else -> for (i in reportModels.indices) {
                                    when {
                                        reportModels[i].mCartNO.contains(mSearchText, ignoreCase = true) -> mReportList.add(reportModels[i])
                                    }
                                }
                            }

                            mReportList.sortWith { p2, p1 ->
                                p1.mDateTimeInTimeStamp.time.toDouble()
                                    .compareTo(p2.mDateTimeInTimeStamp.time.toDouble())
                            }

                            mBinding.textViewNoOrders.visibility = GONE
                            val mListAdapter = OrderHistoryListAdapter(mReportList)


                            val x = 0;
                        //    for(x in 0 until mReportList.size){
                                e("cart no "+x,mReportList[x].mCartNO)
                                e("server name "+x,mReportList[x].mServerName)
                                e("id "+x,mReportList[x].id)
                                e("cart id "+x,mReportList[x].mCartID)
                                e("date time "+x,mReportList[x].mDateTime)
                                e("discount type "+x,mReportList[x].mDiscountType)
                                e("pay type type "+x,mReportList[x].mPaymentSelectionType)
                                e("resto id "+x,mReportList[x].mRestaurantID)
                                e("resto id "+x,mReportList[x].mPaymentList.toString())
                          //  }

                            mBinding.recyclerViewOrderHistory.adapter = mListAdapter
                            Animations.runLayoutAnimationFallDown(mBinding.recyclerViewOrderHistory)
                        }
                        else -> {
                            mBinding.textViewNoOrders.visibility = VISIBLE
                            mBinding.recyclerViewOrderHistory.adapter = null
                        }
                    }
                else -> {
                    mBinding.textViewNoOrders.visibility = VISIBLE
                    mBinding.textViewNoOrders.text = "Oops! Something went wrong."
                }
            }
        }
        mReportData!!.observe(viewLifecycleOwner, mReportObserver!!)
    }

    private var mLocationSelectedListener: AdapterView.OnItemSelectedListener? = null

    private var mLocationModelList: ArrayList<LocationTypeModel>? = ArrayList()
    private var mServerModelList: ArrayList<ServerModel>? = ArrayList()

    private var mLocationList: ArrayList<String>? = null
    private var mServerList: ArrayList<String>? = null

    private var mLocationTypeID = 0
    private var mServerName = ""

    private fun setUpLocationFilter() {
        mFlagToPopulateList++
        mLocationList = ArrayList()
        mLocationModelList!!.add(LocationTypeModel(0, "ALL"))
        mLocationModelList!!.add(LocationTypeModel(1, "Dine In"))
        mLocationModelList!!.add(
            LocationTypeModel(
                2,
                "Quick Service"
            )
        )

        mLocationModelList!!.add(LocationTypeModel(3, "Delivery"))
        mLocationList!!.add("ALL")
        mLocationList!!.add("Dine In")
        mLocationList!!.add("Quick Service")
        mLocationList!!.add("Delivery")

        val mLocationAdapter =
            ArrayAdapter(requireActivity(), R.layout.spinner_item_pending_order, mLocationList!!)
        mLocationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mBinding.spinnerLocation.adapter = mLocationAdapter

        mLocationSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                e("TAG", "onItemSelected: $mLocationTypeID")
                mLocationTypeID = mLocationModelList!![position].mLocationTypeID

                if (mFlagToPopulateList >= 2)
                    setupOrderListRecyclerView("")
            }
        }

        var mSelectedLocationPosition = 0
        for (j in 0 until mLocationModelList!!.size) {
            if (mLocationModelList!![j].mLocationTypeID == mSharedPref!!.getInt(
                    AppConstants.LOCATION_SERVICE_TYPE,
                    0
                )
            ) {
                mSelectedLocationPosition = j
                break
            }
        }

        mBinding.spinnerLocation.setSelection(mSelectedLocationPosition)

        mBinding.spinnerLocation.onItemSelectedListener = mLocationSelectedListener
    }

    private var mServerSelectedListener: AdapterView.OnItemSelectedListener? = null

    private fun setUpServerFilterD(){
        mServerList = ArrayList()
//        mServerModelList!!.add(ServerModel("", "ALL", "1", "","", ArrayList()))
//        mServerList!!.add("ALL")

        serverViewModel.getServers().observe(viewLifecycleOwner) {


            when {
                it.isNotEmpty() -> {
                    for (i in it.indices) {
                        mServerModelList!!.add(it[i])
                        mServerList!!.add(it[i].mServerName)
                    }

                    mServerName = prefs.getServerName();
                    Log.e("SERVER A",mServerName);
                    setupOrderListRecyclerView("")
                }
            }
        }
    }

//    private fun setUpServerFilter() {
//        mServerList = ArrayList()
//        mServerModelList!!.add(ServerModel("", "ALL", "1", "","", ArrayList()))
//        mServerList!!.add("ALL")
//
//
//        serverViewModel.getServers().observe(viewLifecycleOwner) {
//            when {
//                it.isNotEmpty() -> {
//                    for (i in it.indices) {
//                        mServerModelList!!.add(it[i])
//                        mServerList!!.add(it[i].mServerName)
//                        Log.e("SERVER NAME " + i, mServerModelList!!.get(i).mServerName.toString())
//                    }
//
//                    val mServerAdapter =
//                        ArrayAdapter(
//                            requireActivity(),
//                            R.layout.spinner_item_pending_order,
//                            mServerList!!
//                        )
//
//                    mServerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                    mBinding.spinnerServer.adapter = mServerAdapter
//
//                    mServerSelectedListener = object : AdapterView.OnItemSelectedListener {
//                        override fun onNothingSelected(parent: AdapterView<*>?) {
//                        }
//
//                        override fun onItemSelected(
//                            parent: AdapterView<*>?,
//                            view: View?,
//                            position: Int,
//                            id: Long
//                        ) {
//
//                            mServerName = mServerModelList!![position].mServerName
//
//                            when {
//                                mFlagToPopulateList >= 1 -> {
//                                    setupOrderListRecyclerView("")
//                                    mFlagToPopulateList++
//                                }
//                            }
//                        }
//                    }
//
////                    var mSelectedServerPosition = 0
////                    loop@ for (j in 0 until mServerList!!.size) {
////                        when {
////                            mServerList!![j] == mSharedPref!!.getString(AppConstants.LOGGED_IN_SERVER_NAME, "") -> {
////                                mSelectedServerPosition = j
////                                break@loop
////                            }
////                        }
////                    }
////                    mBinding.spinnerServer.setSelection(mSelectedServerPosition)
//
//                    mBinding.spinnerServer.onItemSelectedListener = mServerSelectedListener
//                }
//            }
//        }
//    }

    fun onTextChanged(mText: CharSequence) {
        setupOrderListRecyclerView(mText.toString())
    }

    private fun changeDateTimeInMillis(date: String): Long {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        var mDate: Date? = null
        try {
            mDate = sdf.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return Objects.requireNonNull<Date>(mDate).time
    }

    @Subscribe
    fun onOrderClick(mEvent: OrderHistoryListEvent) {
        if (mEvent.mResult) {
            mBinding.editTextSearchOrderNo.text.clear()

            if (!App.isTablet){
                val action =
                    OrderHistoryListFragmentDirections.actionOrderHistoryListFragmentToOrderHistoryCartFragment(mEvent.mOrder.id)
                findNavController().navigate(action)
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
