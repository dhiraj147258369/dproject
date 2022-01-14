package com.rsl.youresto.ui.main_screen.tables_and_tabs.tables


import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.App
import com.rsl.youresto.R
import com.rsl.youresto.data.database_download.models.TablesModel
import com.rsl.youresto.databinding.FragmentTablesBinding
import com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab.ShowCartEvent
import com.rsl.youresto.ui.tab_specific.TablesTabFragment
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppConstants.SERVICE_QUICK_SERVICE
import com.rsl.youresto.utils.AppPreferences
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("LogNotTimber")
class TablesFragment : Fragment() {

    private lateinit var mBinding: FragmentTablesBinding
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mLocationID: String



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
        var mLocationType = mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE,0)



        showTables()
        initViews()

        mBinding.table = this
        return view
    }

    private fun showTables() {
        mBinding.textViewNoInternetConnection.visibility = GONE
        mBinding.buttonRetry.visibility = GONE
        mBinding.recyclerViewTables.visibility = VISIBLE
    }

    override fun onResume() {
        mSharedPrefs.edit().putInt(AppConstants.LOCATION_SERVICE_TYPE, SERVICE_DINE_IN).apply()
        super.onResume()
    }

    private fun initViews() {
        setupTablesRecyclerView()
    }

    private var mTableAdapter: TableRecyclerAdapter? = null

    private fun setupTablesRecyclerView() {

        tablesViewModel.filterText.value = ""

        tablesViewModel.getTablesData(prefs.getSelectedLocation(), "1".toInt()).observe(viewLifecycleOwner) {
            it?.let {
                mTableAdapter =
                    TableRecyclerAdapter(ArrayList(it))
                mBinding.recyclerViewTables.adapter = mTableAdapter
            }
        }
    }

    fun onTextChanged(mText: CharSequence) {
        tablesViewModel.filterText.value = mText.toString()
    }

    @Subscribe
    fun onTableClicked(mTable: TablesModel) {

        prefs.setTable(mTable.mTableID, mTable.mTableNo)

        if (App.isTablet){
            ((parentFragment as NavHostFragment).parentFragment as TablesTabFragment).showProducts()
            if (mTable.mTableNoOfOccupiedChairs > 0) {
                showCart()
            }
        } else {
            findNavController().navigate(R.id.mainProductFragment)
        }
    }

    private fun showCart() {
        ((parentFragment as NavHostFragment).parentFragment as TablesTabFragment).showCart()
    }

    @Subscribe
    fun showCartEvent(showCartEvent: ShowCartEvent){
        if (showCartEvent.showCart){
            showCart()
        }

        onTextChanged("a")
        onTextChanged("")
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
