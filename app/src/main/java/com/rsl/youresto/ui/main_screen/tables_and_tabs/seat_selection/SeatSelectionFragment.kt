package com.rsl.youresto.ui.main_screen.tables_and_tabs.seat_selection


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast.LENGTH_SHORT
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.rsl.youresto.R
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.main_login.network.LOG_TAG
import com.rsl.youresto.data.tables.models.LocalTableGroupModel
import com.rsl.youresto.data.tables.models.LocalTableSeatModel
import com.rsl.youresto.data.tables.models.ServerTableSeatModel
import com.rsl.youresto.databinding.FragmentSeatSelectionBinding
import com.rsl.youresto.ui.main_screen.cart.CartViewModel
import com.rsl.youresto.ui.main_screen.cart.CartViewModelFactory
import com.rsl.youresto.ui.main_screen.tables_and_tabs.seat_selection.event.GuestCountSelectionEvent
import com.rsl.youresto.ui.main_screen.tables_and_tabs.tables.TablesViewModel
import com.rsl.youresto.ui.main_screen.tables_and_tabs.tables.TablesViewModelFactory
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.SEAT_SELECTION_ENABLED
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_TABLE_NO
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.custom_dialog.CustomProgressDialog
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


@SuppressLint("LogNotTimber")
class SeatSelectionFragment : Fragment() {

    private lateinit var mBinding: FragmentSeatSelectionBinding
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mTableViewModel: TablesViewModel
    private lateinit var mCartViewModel: CartViewModel
    private lateinit var mIntentFrom: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_seat_selection, container, false)
        val mView = mBinding.root

        mSharedPrefs = requireActivity().getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)

        val factory: TablesViewModelFactory = InjectorUtils.provideTablesViewModelFactory(requireActivity())
        mTableViewModel = ViewModelProviders.of(this, factory).get(TablesViewModel::class.java)

        val cartFactory: CartViewModelFactory = InjectorUtils.provideCartViewModelFactory(requireActivity())
        mCartViewModel = ViewModelProviders.of(this, cartFactory).get(CartViewModel::class.java)

        mIntentFrom = SeatSelectionFragmentArgs.fromBundle(requireArguments()).intentFrom

        initialiseDataAndViews()

        return mView
    }

    private var mTableGroupList = ArrayList<LocalTableGroupModel>()
    private var mTableNO: Int = 0
    private var mTableID: String? = null
    private var mLocationID: String? = null
    private var isSeatSelectionEnabled: Boolean = false

    private val mGroupList = mutableListOf("A", "B", "C", "D", "E")

    private fun initialiseDataAndViews() {

        mTableNO = mSharedPrefs.getInt(SELECTED_TABLE_NO, 0)
        mTableID = mSharedPrefs.getString(SELECTED_TABLE_ID, "")
        mLocationID = mSharedPrefs.getString(SELECTED_LOCATION_ID, "")
        isSeatSelectionEnabled = mSharedPrefs.getBoolean(SEAT_SELECTION_ENABLED, false)

        when {
            isSeatSelectionEnabled -> checkIfSeatsAlreadyStored()
            else -> initViewsWithoutSeatSelection()
        }


        mBinding.checkBoxSelectAllSeats.setOnCheckedChangeListener { _, isChecked ->
            when {
                !isChecked -> {
                    for (k in mTableSeatListTop.indices) {
                        mTableSeatListTop[k].isSelected = false
                        mTableSeatListTop[k].mGroup = "Z"
                    }
                    for (k in mTableSeatListBottom.indices) {
                        mTableSeatListBottom[k].isSelected = false
                        mTableSeatListBottom[k].mGroup = "Z"
                    }
                }
                else -> {
                    for (k in mTableSeatListTop.indices) {
                        mTableSeatListTop[k].isSelected = true
                        mTableSeatListTop[k].mGroup = "A"
                    }
                    for (k in mTableSeatListBottom.indices) {
                        mTableSeatListBottom[k].isSelected = true
                        mTableSeatListBottom[k].mGroup = "A"
                    }
                }
            }


            when {
                mSeatAdapterTop != null -> mSeatAdapterTop!!.notifyDataSetChanged()
            }

            when {
                mSeatAdapterBottom != null -> mSeatAdapterBottom!!.notifyDataSetChanged()
            }
        }
    }

    private var mGuestAdapter: GuestCountSelectionAdapter? = null
    private var mGuestList: ArrayList<Int>? = null

    private fun initViewsWithoutSeatSelection() {
        mBinding.constraintLayoutSeatSelection.visibility = GONE
        mBinding.constraintLayoutWithoutSeatSelection.visibility = VISIBLE

        mGuestList = ArrayList()

        mTableViewModel.getNoOfChairsForTable(mTableID).observe(viewLifecycleOwner, {
            if (it != null && it > 0) {
                for (i in 0 until it) {
                    mGuestList!!.add(i + 1)
                }

                mGuestAdapter = GuestCountSelectionAdapter(mGuestList!!)
                mBinding.recyclerViewGuestsWithoutSeatSelection.adapter = mGuestAdapter

                checkIfGuestAlreadySelected()
            }
        })
    }

    private fun checkIfGuestAlreadySelected() {
        mTableViewModel.getTableGroupsAndSeats(mTableID!!).observe(viewLifecycleOwner, {
            mTableGroupList = ArrayList()
            if (it.isEmpty()) seatsArePresent = false
            else {
                seatsArePresent = true
                mTableGroupList.addAll(it)
                mGuestList = ArrayList()
                for (i in it.indices) {
                    for (j in 0 until it[i].mSeatList!!.size) {
                        mGuestList!!.add(it[i].mSeatList!![j].mSeatNO)
                    }
                }
                mGuestAdapter!!.onGuestCountClick(mGuestList!!.size)
            }
            proceedWithoutSeatSelection()
        })
    }

    private fun proceedWithoutSeatSelection() {
        mBinding.textViewGuestsProceedWithoutSeatSelection.setOnClickListener {
            if (mGuestCount > 0) {
                when {
                    seatsArePresent -> mTableViewModel.deleteTableGroups(mTableID!!).observe(viewLifecycleOwner,
                        {
                            when {
                                it > -1 -> for (i in mTableGroupList.indices) {
                                    val mTableGroup = getGroupWithoutSeatSelection(mTableGroupList[i].mGroupName!!)
                                    when {
                                        mTableGroup != null -> mTableViewModel.storeTableGroups(mTableGroup, mTableID!!)
                                    }
                                    when (i) {
                                        mTableGroupList.size - 1 -> {
                                            val action =
                                                SeatSelectionFragmentDirections.actionSeatSelectionFragmentToMainProductFragment(
                                                    "A",
                                                    "A",
                                                    javaClass.simpleName
                                                )
                                            navController.navigate(action)
                                        }
                                    }
                                }
                            }
                        })
                    else -> {
                        mTableGroupList.clear()
                        val mSeatList = ArrayList<LocalTableSeatModel>()
                        for (i in 0 until mGuestCount) {
                            mSeatList.add(
                                LocalTableSeatModel(
                                    i + 1,
                                    "Z",
                                    mTableNO,
                                    mTableID,
                                    isSelected = true,
                                    isPaid = false
                                )
                            )
                        }

                        mTableGroupList.add(
                            LocalTableGroupModel(
                                "Z",
                                true,
                                mTableNO,
                                mTableID,
                                mLocationID,
                                mSeatList
                            )
                        )

                        for (i in mTableGroupList.indices) {
                            val mTableGroup = getGroupWithoutSeatSelection(mTableGroupList[i].mGroupName!!)
                            when {
                                mTableGroup != null -> mTableViewModel.storeTableGroups(mTableGroup, mTableID!!)
                            }

                            when (i) {
                                mTableGroupList.size - 1 -> {
                                    val action =
                                        SeatSelectionFragmentDirections.actionSeatSelectionFragmentToMainProductFragment(
                                            "A",
                                            "A",
                                            javaClass.simpleName
                                        )
                                    navController.navigate(action)
                                }
                            }
                        }
                    }
                }
            } else CustomToast.makeText(
                requireActivity(),
                "Please select number of guests to proceed",
                LENGTH_SHORT
            ).show()
        }
    }

    private fun getGroupWithoutSeatSelection(mGroupName: String): LocalTableGroupModel? {
        val mSeatList = ArrayList<LocalTableSeatModel>()

        for (i in 0 until mGuestCount) {
            mSeatList.add(LocalTableSeatModel(i + 1, mGroupName, mTableNO, mTableID, isSelected = true, isPaid = false))
        }

        e(LOG_TAG, "getGroup: seat size: " + mSeatList.size)

        return if (mSeatList.size > 0)
            LocalTableGroupModel(mGroupName, true, mTableNO, mTableID, mLocationID, mSeatList)
        else null
    }

    private var mGuestCount = 0

    @Subscribe
    fun onGuestCountClick(mEvent: GuestCountSelectionEvent) {
        if (mEvent.mResult) {
            mGuestCount = mEvent.mGuestCount
        }
    }

    private var seatsArePresent = false

    private var mCartSeatList: ArrayList<ServerTableSeatModel>? = null

    private fun checkIfSeatsAlreadyStored() {
        mBinding.constraintLayoutSeatSelection.visibility = VISIBLE
        mBinding.constraintLayoutWithoutSeatSelection.visibility = GONE

        mTableViewModel.getTableGroupsAndSeats(mTableID!!).observe(viewLifecycleOwner, {
            mTableGroupList = ArrayList()
            when {
                it.isEmpty() -> seatsArePresent = false
                else -> {
                    seatsArePresent = true
                    mTableGroupList.addAll(it)

                    val mEditor = mSharedPrefs.edit()
                    mEditor.putString(AppConstants.SEAT_SELECTION_GROUP, it[0].mGroupName)
                    mEditor.apply()
                }
            }

            mCartSeatList = ArrayList()

            when {
                seatsArePresent -> mCartViewModel.getCartDataByTable(mTableNO).observe(viewLifecycleOwner,
                    { cartList ->

                    for (element in cartList) {
                        val tempSeatList = element.mAssignedSeats

                        when (mCartSeatList!!.size) {
                            0 -> mCartSeatList!!.addAll(tempSeatList!!)
                            else -> for (j in 0 until tempSeatList!!.size) {

                                var mHasSeat = false
                                for (k in 0 until mCartSeatList!!.size) {
                                    when (mCartSeatList!![k].mSeatNO) {
                                        tempSeatList[j].mSeatNO -> mHasSeat = true
                                    }
                                }

                                when {
                                    !mHasSeat -> mCartSeatList!!.add(tempSeatList[j])
                                }

                            }
                        }
                    }

                    addTableGroup()
                    showSeats()

                })
                else -> {
                    addTableGroup()
                    showSeats()
                }
            }
        })

        proceed()
    }

    private var mTableGroupAdapter: TableGroupRecyclerAdapter? = null
    private var mSelectedGroup: String = "A"

    private fun addTableGroup() {

        when {
            !seatsArePresent -> mTableGroupList.add(
                LocalTableGroupModel(
                    "A",
                    true,
                    mTableNO,
                    mTableID,
                    mLocationID,
                    ArrayList()
                )
            )
            else -> for (i in mTableGroupList.indices)
                when (i) {
                    0 -> {
                        mSelectedGroup = mTableGroupList[i].mGroupName!!
                        mTableGroupList[i].isSelected = true
                    }
                    else -> mTableGroupList[i].isSelected = false
                }
        }

        mTableGroupList.sortBy { it.mGroupName }

        //related check all
        //group already exist, so will check if already existed group has products
        when {
            mTableGroupList.size > 1 -> mBinding.checkBoxSelectAllSeats.isEnabled = false
        }

        mTableGroupAdapter = TableGroupRecyclerAdapter(mTableGroupList)
        mBinding.recyclerViewAddedGroups.adapter = mTableGroupAdapter

        mBinding.textViewAddGroup.setOnClickListener {

            //group already exist, so will check if already existed group has products
            when {
                selectedChairs >= mNoOfChairs -> {
                    CustomToast.makeText(
                        requireActivity(),
                        "All seats are selected already",
                        LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                else -> loop2@ for (i in 0 until mGroupList.size) {
                    var mOccupied = false
                    var mGroupNotExists = false
                    loop@ for (j in 0 until mTableGroupList.size) {
                        when (mTableGroupList[j].mGroupName) {
                            mGroupList[i] -> {

                                //group already exist, so will check if already existed group has products
                                val mSeatList = mTableGroupList[j].mSeatList

                                when {
                                    mSeatList!!.size > 0 ->
                                        loop1@ for (k in 0 until mCartSeatList!!.size)
                                            for (l in 0 until mSeatList.size) {
                                                when (mCartSeatList!![k].mSeatNO) {
                                                    mSeatList[l].mSeatNO -> {
                                                        mOccupied = true
                                                        break@loop1
                                                    }
                                                    else -> mOccupied = false
                                                }
                                            }
                                    else -> mOccupied = false
                                }


                                mGroupNotExists = false
                                break@loop
                            }
                            else -> {
                                mOccupied = true
                                mGroupNotExists = true
                            }
                        }
                    }

                    e(javaClass.simpleName, "Group ${mGroupList[i]}: mOccupied: $mOccupied")

                    when {
                        !mOccupied -> {
                            CustomToast.makeText(
                                requireActivity(),
                                "Cannot add new group.\nGroup ${mGroupList[i]} hasn't been occupied yet, \n" +
                                        "please add some product to Group ${mGroupList[i]} first",
                                LENGTH_SHORT
                            ).show()

                            break@loop2
                        }
                        mGroupNotExists -> {

                            mTableGroupList.add(
                                LocalTableGroupModel(
                                    mGroupList[i], true, mTableNO, mTableID,
                                    mLocationID, ArrayList()
                                )
                            )

                            val mEditor = mSharedPrefs.edit()
                            mEditor.putString(AppConstants.SEAT_SELECTION_GROUP, mGroupList[i])
                            mEditor.apply()

                            for (j in 0 until mTableGroupList.size) {

                                when (j) {
                                    mTableGroupList.size - 1 -> {
                                        mTableGroupList[j].isSelected = true
                                        mSeatAdapterTop!!.setSeatGroup(mTableGroupList[j].mGroupName!!)
                                        mSeatAdapterBottom!!.setSeatGroup(mTableGroupList[j].mGroupName!!)
                                        mSelectedGroup = mTableGroupList[j].mGroupName!!
                                    }
                                    else -> mTableGroupList[j].isSelected = false
                                }
                            }

                            mTableGroupAdapter!!.notifyDataSetChanged()

                            break@loop2
                        }
                        i == 4 -> CustomToast.makeText(
                            requireActivity(),
                            "Cannot add more than 5 groups!",
                            LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private var mTableSeatListTop = ArrayList<LocalTableSeatModel>()
    private var mTableSeatListBottom = ArrayList<LocalTableSeatModel>()

    private var mSeatAdapterTop: TableSeatRecyclerAdapter? = null
    private var mSeatAdapterBottom: TableSeatRecyclerAdapter? = null

    private var mNoOfChairs = 0

    private fun showSeats() {
        val mTableString = "Table $mTableNO"
        mBinding.textViewTable.text = mTableString

        mTableSeatListTop = ArrayList()
        mTableSeatListBottom = ArrayList()

        mSeatAdapterTop = TableSeatRecyclerAdapter(mTableSeatListTop)
        mBinding.recyclerViewDiningTableRow1.adapter = mSeatAdapterTop

        mSeatAdapterBottom = TableSeatRecyclerAdapter(mTableSeatListBottom)
        mBinding.recyclerViewDiningTableRow2.adapter = mSeatAdapterBottom

        mTableViewModel.getNoOfChairsForTable(mTableID).observe(viewLifecycleOwner, {

            mNoOfChairs = it

            if (mNoOfChairs % 2 == 0) {
                for (i in 1..mNoOfChairs / 2) {
                    mTableSeatListTop.add(
                        LocalTableSeatModel(
                            i, "", mTableNO, mTableID,
                            isSelected = false,
                            isPaid = false
                        )
                    )
                    mTableSeatListBottom.add(
                        LocalTableSeatModel(
                            i + mNoOfChairs / 2,
                            "",
                            mTableNO,
                            mTableID,
                            isSelected = false,
                            isPaid = false
                        )
                    )
                    mSeatAdapterTop!!.notifyDataSetChanged()
                    mSeatAdapterBottom!!.notifyDataSetChanged()
                }
            } else {
                for (i in 1..mNoOfChairs / 2 + 1) {
                    mTableSeatListTop.add(
                        LocalTableSeatModel(
                            i, "", mTableNO, mTableID,
                            isSelected = false,
                            isPaid = false
                        )
                    )
                    mSeatAdapterTop!!.notifyDataSetChanged()
                }
                for (i in mNoOfChairs / 2 + 2..mNoOfChairs) {
                    mTableSeatListBottom.add(
                        LocalTableSeatModel(
                            i, "", mTableNO, mTableID,
                            isSelected = false,
                            isPaid = false
                        )
                    )
                    mSeatAdapterBottom!!.notifyDataSetChanged()

                }
            }


            if (mTableSeatListTop.size + mTableSeatListBottom.size < 3) {

                val mTextParams = mBinding.textViewTable.layoutParams
                mTextParams.width = 200
                mBinding.textViewTable.layoutParams = mTextParams
            }


            showSeatsIfPresent()
        })

    }

    private var selectedChairs = 0

    private fun showSeatsIfPresent() {
        when {
            seatsArePresent -> {
                for (i in mTableGroupList.indices) {

                    val mSeatStoredList = mTableGroupList[i].mSeatList

                    for (j in mSeatStoredList!!.indices) {
                        for (k in mTableSeatListTop.indices) {
                            when (mSeatStoredList[j].mSeatNO) {
                                mTableSeatListTop[k].mSeatNO -> {
                                    e(javaClass.simpleName, " mSeatStoredList[j].isPaid: ${mSeatStoredList[j].isPaid}")
                                    mTableSeatListTop[k].mGroup = mSeatStoredList[j].mGroup
                                    mTableSeatListTop[k].isSelected = true
                                    mTableSeatListTop[k].isPaid = mSeatStoredList[j].isPaid
                                    selectedChairs++
                                }
                            }
                        }

                        for (k in mTableSeatListBottom.indices) {
                            when (mSeatStoredList[j].mSeatNO) {
                                mTableSeatListBottom[k].mSeatNO -> {
                                    mTableSeatListBottom[k].mGroup = mSeatStoredList[j].mGroup
                                    mTableSeatListBottom[k].isSelected = true
                                    mTableSeatListBottom[k].isPaid = mSeatStoredList[j].isPaid
                                    selectedChairs++
                                }
                            }
                        }

                    }
                }

                for (j in 0 until mCartSeatList!!.size) {

                    for (k in mTableSeatListTop.indices) {
                        when (mCartSeatList!![j].mSeatNO) {
                            mTableSeatListTop[k].mSeatNO -> mTableSeatListTop[k].isOccupied =
                                true
                        }
                    }

                    for (k in mTableSeatListBottom.indices) {
                        when (mCartSeatList!![j].mSeatNO) {
                            mTableSeatListBottom[k].mSeatNO -> mTableSeatListBottom[k].isOccupied =
                                true
                        }
                    }

                }

                mSeatAdapterTop!!.notifyDataSetChanged()
                mSeatAdapterBottom!!.notifyDataSetChanged()
            }
        }
    }

    private fun proceed() {
        mBinding.textViewGuestsProceed.setOnClickListener {
            var mSeatCount = 0
            for (i in mTableSeatListTop.indices)
                when {
                    mTableSeatListTop[i].isSelected -> mSeatCount++
                }
            for (i in mTableSeatListBottom.indices)
                when {
                    mTableSeatListBottom[i].isSelected -> mSeatCount++
                }
            e(javaClass.simpleName, "seatsAre: $seatsArePresent")
            when {
                mSeatCount > 0 ->
                    when {
                        seatsArePresent ->
                            mTableViewModel.deleteTableGroups(mTableID!!).observe(viewLifecycleOwner,
                                {
                                    when {
                                        it > -1 -> {

                                            for (i in mTableGroupList.indices) {
                                                val mTableGroup = getGroup(mTableGroupList[i].mGroupName!!)



                                                when {
                                                    mTableGroup != null -> mTableViewModel.storeTableGroups(
                                                        mTableGroup,
                                                        mTableID!!
                                                    )
                                                }

                                                when (i) {
                                                    mTableGroupList.size - 1 -> {
                                                        Handler()
                                                            .postDelayed({
                                                                when {
                                                                    mProgressDialog != null -> mProgressDialog!!.dismiss()
                                                                }

                                                                val action =
                                                                    SeatSelectionFragmentDirections.actionSeatSelectionFragmentToMainProductFragment(
                                                                        "A",
                                                                        "A",
                                                                        javaClass.simpleName
                                                                    )
                                                                navController.navigate(action)
                                                            }, 3000)
                                                    }
                                                }

                                            }
                                            updateGroup()
                                        }
                                    }

                                })
                        else -> {
                            for (i in mTableGroupList.indices) {
                                val mTableGroup = getGroup(mTableGroupList[i].mGroupName!!)
                                when {
                                    mTableGroup != null -> mTableViewModel.storeTableGroups(mTableGroup, mTableID!!)
                                }

                                when (i) {
                                    mTableGroupList.size - 1 -> {
                                        val action =
                                            SeatSelectionFragmentDirections.actionSeatSelectionFragmentToMainProductFragment(
                                                "A",
                                                "A",
                                                javaClass.simpleName
                                            )
                                        navController.navigate(action)
                                    }
                                }
                            }

                            if (mTableGroupList.size > 1) {
                                updateGroup()
                            }
                        }
                    }
                else -> CustomToast.makeText(
                    requireActivity(),
                    "Please select seats to start taking order",
                    LENGTH_SHORT
                ).show()
            }
        }
    }

    private var mCartData: LiveData<List<CartProductModel>>? = null
    private var mCartObserver: Observer<List<CartProductModel>>? = null

    private var mProgressDialog: CustomProgressDialog? = null

    private fun updateGroup() {
        mProgressDialog = CustomProgressDialog.newInstance(
            "Update",
            "Updating group and seats, please wait",
            AppConstants.DIALOG_TYPE_NETWORK
        )
        mProgressDialog!!.show(childFragmentManager, AppConstants.CUSTOM_PROGRESS_DIALOG_FRAGMENT)
        mProgressDialog!!.isCancelable = false

        val mGroupList = ArrayList<LocalTableGroupModel>()

        var mSeats = 0
        for (j in mTableGroupList.indices) {
            mGroupList.add(getGroup(mTableGroupList[j].mGroupName!!)!!)
            mSeats += mTableGroupList[j].mSeatList!!.size
        }

        mTableViewModel.getLocalTableData(mTableID!!).observe(viewLifecycleOwner, {


            it.mTableNoOfOccupiedChairs = mSeats

            val mServerGroupList = it.mGroupList
            for (i in 0 until mGroupList.size) {

                for (j in 0 until mServerGroupList!!.size) {
                    e(javaClass.simpleName, "mGroupList[i].mGroupName: ${mGroupList[i].mGroupName}")
                    e(javaClass.simpleName, "mServerGroupList[j].mGroupName: ${mServerGroupList[j].mGroupName}")
                    when (mGroupList[i].mGroupName) {
                        mServerGroupList[j].mGroupName -> {
                            val mServerSeatList = ArrayList<ServerTableSeatModel>()
                            e(javaClass.simpleName, "mSeatList.size: ${mGroupList[i].mSeatList!!.size}")
                            for (k in 0 until mGroupList[i].mSeatList!!.size) {
                                mServerSeatList.add(ServerTableSeatModel(mGroupList[i].mSeatList!![k].mSeatNO))
                            }

                            mServerGroupList[j].mSeatList = mServerSeatList
                        }
                    }
                }

            }

            e(javaClass.simpleName, "mGroupList.size: ${mGroupList.size}")
            e(javaClass.simpleName, "mServerGroupList.size: ${mServerGroupList!!.size}")

            it.mGroupList = mServerGroupList

            mTableViewModel.updateTableData(it, mSelectedGroup)
        })

    }

    private fun getGroup(mGroupName: String): LocalTableGroupModel? {
        val mSeatList = ArrayList<LocalTableSeatModel>()

        for (i in mTableSeatListTop.indices)
            if (mTableSeatListTop[i].isSelected && mTableSeatListTop[i].mGroup == mGroupName)
                mSeatList.add(mTableSeatListTop[i])


        for (i in mTableSeatListBottom.indices)
            if (mTableSeatListBottom[i].isSelected && mTableSeatListBottom[i].mGroup == mGroupName)
                mSeatList.add(mTableSeatListBottom[i])


        e(LOG_TAG, "getGroup: groupName size: $mGroupName")
        e(LOG_TAG, "getGroup: seat size: " + mSeatList.size)

        return if (mSeatList.size > 0)
            LocalTableGroupModel(mGroupName, true, mTableNO, mTableID, mLocationID, mSeatList)
        else null
    }

    @Subscribe
    fun tableSeatClicked(mTableSeat: LocalTableSeatModel) {

        when {
            mTableSeat.isOccupied -> {
                CustomToast.makeText(
                    requireActivity(),
                    "Product is assigned to this seat already, cannot make change to this now",
                    LENGTH_SHORT
                ).show()
                return
            }
            mTableSeat.isSelected -> selectedChairs--
            else -> selectedChairs++
        }

        loop@ for (i in mTableSeatListTop.indices) {
            when (mTableSeatListTop[i].mSeatNO) {
                mTableSeat.mSeatNO -> {
                    mTableSeatListTop[i].isSelected = !mTableSeat.isSelected
                    when {
                        mTableSeat.isSelected -> mTableSeatListTop[i].mGroup = mSelectedGroup
                        else -> mTableSeatListTop[i].mGroup = ""
                    }
                    mSeatAdapterTop!!.notifyItemChanged(i)
                    break@loop
                }
            }
        }

        loop@ for (i in mTableSeatListBottom.indices) {
            when (mTableSeatListBottom[i].mSeatNO) {
                mTableSeat.mSeatNO -> {
                    mTableSeatListBottom[i].isSelected = !mTableSeat.isSelected
                    when {
                        mTableSeat.isSelected -> mTableSeatListBottom[i].mGroup = mSelectedGroup
                        else -> mTableSeatListBottom[i].mGroup = ""
                    }
                    mSeatAdapterBottom!!.notifyItemChanged(i)
                    break@loop
                }
            }
        }

    }

    @Subscribe
    fun tableGroupClicked(mTableGroup: LocalTableGroupModel) {
        for (j in 0 until mTableGroupList.size) {
            mTableGroupList[j].isSelected = mTableGroupList[j].mGroupName == mTableGroup.mGroupName
            mSelectedGroup = mTableGroup.mGroupName!!
        }

        val mEditor = mSharedPrefs.edit()
        mEditor.putString(AppConstants.SEAT_SELECTION_GROUP, mSelectedGroup)
        mEditor.apply()

        mTableGroupAdapter!!.notifyDataSetChanged()
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
//        if (!mDrawerVisibility) {
//            when (mIntentFrom) {
//                TablesFragment::class.java.simpleName -> navController.navigate(R.id.action_seatSelectionFragment_to_tablesFragment)
//                MainProductFragment::class.java.simpleName -> {
//                    val action =
//                        SeatSelectionFragmentDirections.actionSeatSelectionFragmentToMainProductFragment(
//                            "A",
//                            "A",
//                            javaClass.simpleName
//                        )
//                    navController.navigate(action)
//                }
//            }
//        }
//
//        return true
//    }

    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        navController = Navigation.findNavController(view)
    }
}
