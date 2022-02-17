package com.rsl.foodnairesto.ui.main_screen.favorite_items

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.data.database_download.models.FavoriteItemsModel
import com.rsl.foodnairesto.databinding.FragmentFavoriteItemsBinding
import com.rsl.foodnairesto.ui.main_screen.MainScreenActivity
import com.rsl.foodnairesto.ui.main_screen.favorite_items.event.FavoriteItemSelectOrDeSelectEvent
import com.rsl.foodnairesto.ui.main_screen.favorite_items.model.FavoriteProductModel
import com.rsl.foodnairesto.utils.AppConstants
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.foodnairesto.utils.InjectorUtils
import com.rsl.foodnairesto.utils.custom_views.CustomToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.ArrayList


@SuppressLint("LogNotTimber")
class FavoriteItemsFragment : Fragment() {

    private lateinit var mBinding: FragmentFavoriteItemsBinding
    private lateinit var mViewModel: FavoriteItemsViewModel
    private lateinit var mSharedPrefs: SharedPreferences
    private lateinit var mViewPagerAdapter: FavoriteItemsPagerAdapter

    private val viewModel: NewFavoriteItemViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorite_items, container, false)
        val mView: View = mBinding.root

        val factory = InjectorUtils.provideFavoriteItemsViewModelFactory(requireActivity())
        mViewModel = ViewModelProviders.of(this, factory).get(FavoriteItemsViewModel::class.java)

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        getGroupData()
        (activity as MainScreenActivity?)?.checkquickservicefrag()
        mBinding.buttonSaveAndExit.setOnClickListener {
            saveAndExit()
        }

        return mView
    }

    private fun getGroupData() {

        lifecycleScope.launch {
           val categories = withContext(Dispatchers.IO) {
               viewModel.getCategories()
           }

            mViewPagerAdapter = FavoriteItemsPagerAdapter(childFragmentManager, ArrayList(categories))
            mBinding.viewPagerFavItemFragment.adapter = mViewPagerAdapter
            mBinding.tabCategories.setupWithViewPager(mBinding.viewPagerFavItemFragment)

        }

    }

    private fun saveAndExit() {

        viewModel.saveFavorites()

        viewModel.favoriteData.observe(viewLifecycleOwner){ event ->
            event?.getContentIfNotHandled()?.let {
                CustomToast.makeText(requireActivity(), it.data, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Subscribe
    fun onClickFavoriteItem(mEvent: FavoriteItemSelectOrDeSelectEvent) {
        if (mEvent.isSelected) {
            // Insert Favorite Item
            insertFavoriteItem(mEvent.favoriteProductModel)
            Log.e(javaClass.simpleName, "onClickFavoriteItem - insert")
        } else {
            // Delete Favorite Item
            deleteFavoriteItem(mEvent.favoriteProductModel)
            Log.e(javaClass.simpleName, "onClickFavoriteItem - delete")
        }
    }

    private var mUpdateItemObserver: Observer<Int>? = null
    private var mInsertItemObserver: Observer<Long>? = null
    private var mObserverFavoriteItems: Observer<List<FavoriteItemsModel>>? = null

    private fun insertFavoriteItem(mFavoriteProductModel: FavoriteProductModel) {
        val mFavoriteItemsData: LiveData<List<FavoriteItemsModel>> = mViewModel.getFavoriteItemsFromDB(
            mFavoriteProductModel.mCategoryID,
            mSharedPrefs.getString(SELECTED_LOCATION_ID, "")!!
        )
        mObserverFavoriteItems = Observer { favoriteItemsModels ->
            when {
                favoriteItemsModels != null -> {
                    when {
                        favoriteItemsModels.isNotEmpty() -> {
                            favoriteItemsModels[0].mProductArrayList.add(mFavoriteProductModel)
                            Log.d(javaClass.simpleName, "Insert category ID: " + mFavoriteProductModel.mCategoryID)
                            val mUpdateItemData: LiveData<Int> =
                                mViewModel.updateFavoriteItemToDB(
                                    favoriteItemsModels[0], mFavoriteProductModel.mCategoryID, mSharedPrefs.getString(
                                        SELECTED_LOCATION_ID, ""
                                    )!!
                                )
                            mUpdateItemObserver = Observer { integer ->
                                when {
                                    integer != null -> {
                                        when {
                                            integer > 0 -> Log.e(
                                                javaClass.simpleName,
                                                "insertFavoriteItemToDB: Favorite Item updated successfully"
                                            )
                                            else -> Log.e(javaClass.simpleName, "insertFavoriteItemToDB: Favorite Item failed to update")
                                        }
                                        mUpdateItemData.removeObserver(mUpdateItemObserver!!)
                                    }
                                }
                            }
                            mUpdateItemData.observe(viewLifecycleOwner, mUpdateItemObserver!!)
                        }
                        else -> {
                            val mTempList = ArrayList<FavoriteProductModel>()
                            mTempList.add(mFavoriteProductModel)
                            val favoriteItemsModel = FavoriteItemsModel(
                                mFavoriteProductModel.mGroupID,
                                mFavoriteProductModel.mGroupName,
                                mFavoriteProductModel.mCategoryID,
                                mFavoriteProductModel.mCategoryName,
                                mFavoriteProductModel.mCategorySequence,
                                mSharedPrefs.getString(SELECTED_LOCATION_ID, "")!!,
                                mTempList
                            )
                            val mInsertItemData: LiveData<Long> = mViewModel.insertFavoriteItemToDB(favoriteItemsModel)
                            mInsertItemObserver = Observer { aLong ->
                                if (aLong != null) {
                                    if (aLong > -1) {
                                        Log.e(
                                            javaClass.simpleName,
                                            "insertFavoriteItemToDB: Favorite Item inserted successfully"
                                        )
                                    } else {
                                        Log.e(javaClass.simpleName, "insertFavoriteItemToDB: Favorite Item failed to insert")
                                    }
                                    mInsertItemData.removeObserver(mInsertItemObserver!!)
                                }
                            }
                            mInsertItemData.observe(viewLifecycleOwner, mInsertItemObserver!!)
                        }
                    }
                    mFavoriteItemsData.removeObserver(mObserverFavoriteItems!!)
                }
            }
        }
        mFavoriteItemsData.observe(viewLifecycleOwner, mObserverFavoriteItems!!)
    }

    private fun deleteFavoriteItem(mFavoriteProductModel: FavoriteProductModel) {
        val mFavoriteItemsData: LiveData<List<FavoriteItemsModel>> = mViewModel.getFavoriteItemsFromDB(
            mFavoriteProductModel.mCategoryID,
            mSharedPrefs.getString(SELECTED_LOCATION_ID, "")!!
        )
        mObserverFavoriteItems = Observer { favoriteItemsModels ->
            when {
                favoriteItemsModels != null -> {
                    when {
                        favoriteItemsModels.isNotEmpty() -> for (i in favoriteItemsModels.indices) {
                            loop@ for (j in 0 until favoriteItemsModels[i].mProductArrayList.size) {
                                when (favoriteItemsModels[i].mProductArrayList[j].mProductID) {
                                    mFavoriteProductModel.mProductID -> {
                                        favoriteItemsModels[i].mProductArrayList.removeAt(j)

                                        val mUpdateItemData: LiveData<Int> = mViewModel.updateFavoriteItemToDB(
                                            favoriteItemsModels[i], mFavoriteProductModel.mCategoryID,
                                            mSharedPrefs.getString(SELECTED_LOCATION_ID, "")!!
                                        )
                                        mUpdateItemObserver = Observer { integer ->
                                            when {
                                                integer != null -> {
                                                    when {
                                                        integer > 0 -> Log.e(
                                                            javaClass.simpleName,
                                                            "deleteFavoriteItemFromDB: Favorite Item deleted successfully"
                                                        )
                                                        else -> Log.e(
                                                            javaClass.simpleName,
                                                            "deleteFavoriteItemFromDB: Favorite Item failed to delete"
                                                        )
                                                    }
                                                    mUpdateItemData.removeObserver(mUpdateItemObserver!!)
                                                }
                                            }
                                        }
                                        mUpdateItemData.observe(viewLifecycleOwner, mUpdateItemObserver!!)
                                        break@loop
                                    }
                                }
                            }
                        }
                    }
                    mFavoriteItemsData.removeObserver(mObserverFavoriteItems!!)
                }
            }
        }
        mFavoriteItemsData.observe(viewLifecycleOwner, mObserverFavoriteItems!!)
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
