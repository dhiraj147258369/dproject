package com.rsl.youresto.ui.main_screen.favorite_items


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.rsl.youresto.App
import com.rsl.youresto.R
import com.rsl.youresto.data.database_download.models.ProductModel
import com.rsl.youresto.databinding.FragmentFavoriteCategoryBinding
import com.rsl.youresto.ui.main_screen.favorite_items.model.FavoriteProductModel
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.CATEGORY_ID
import com.rsl.youresto.utils.AppConstants.GROUP_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.youresto.utils.InjectorUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * A simple [Fragment] subclass.
 *
 */
@SuppressLint("LogNotTimber")
class FavoriteCategoryFragment : Fragment() {

    private lateinit var mBinding: FragmentFavoriteCategoryBinding
    private lateinit var mViewModel: FavoriteItemsViewModel
    private var mCategoryID: String? = null
    private lateinit var mSharedPrefs: SharedPreferences
    private var mFavoriteCategoryAdapter: FavoriteCategoryItemsAdapter? = null

    private val viewModel: NewFavoriteItemViewModel by viewModel()

    companion object {
        fun newInstance(mCategoryId: String, mGroupId: String = ""): FavoriteCategoryFragment {
            val mBundle = Bundle()
            mBundle.putString(CATEGORY_ID, mCategoryId)
            mBundle.putString(GROUP_ID, mGroupId)
            val cartGroupFragment = FavoriteCategoryFragment()
            cartGroupFragment.arguments = mBundle
            return cartGroupFragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_favorite_category, container, false)
        val mView: View = mBinding.root

        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        mCategoryID = requireArguments().getString(CATEGORY_ID)
        val mGroupID = requireArguments().getString(GROUP_ID)

        val factory = InjectorUtils.provideFavoriteItemsViewModelFactory(requireActivity())
        mViewModel = ViewModelProviders.of(this, factory).get(FavoriteItemsViewModel::class.java)

        Log.e(javaClass.simpleName, "onCreateView: mGroupID: $mGroupID")

        val locationId = mSharedPrefs.getString(SELECTED_LOCATION_ID, "") ?: ""
        
        lifecycleScope.launch {
            val products = withContext(Dispatchers.IO) {
                viewModel.getProducts(mCategoryID ?: "")
            }

            val favorites = withContext(Dispatchers.IO) {
                viewModel.getFavorites(mCategoryID ?: "", locationId)
            }

            val mProductList: ArrayList<FavoriteProductModel> = getAllProducts(products)
            for (n in mProductList.indices) {
                for (l in favorites.indices) {
                    val mFavoriteProductList = favorites[l].mProductArrayList
                    for (m in mFavoriteProductList.indices) {
                        when (mFavoriteProductList[m].mProductID) {
                            mProductList[n].mProductID
                            -> {
//                                Log.d(javaClass.simpleName,"mFavoriteProductList[m].mProductID: " + mFavoriteProductList[m].mProductID)
                                mProductList[n].isSelected = true
                                mProductList[n].mCategorySequence = favorites[l].mCategorySequence
                            }
                        }
                    }
                }
            }

            val spanCount = if (App.isTablet) 3 else 1

            mFavoriteCategoryAdapter = FavoriteCategoryItemsAdapter(requireActivity(), mProductList)
            mBinding.recyclerViewFavoriteCategory.layoutManager = GridLayoutManager(activity, spanCount)
            mBinding.recyclerViewFavoriteCategory.adapter = mFavoriteCategoryAdapter
        }

        return mView
    }

    private fun getAllProducts(mLocalProductList: List<ProductModel>): ArrayList<FavoriteProductModel> {
        val mProductList = ArrayList<FavoriteProductModel>()


        for (k in mLocalProductList.indices) {

            val mProductModel = FavoriteProductModel(
                mLocalProductList[k].mProductID,
                mLocalProductList[k].mProductName,
                mLocalProductList[k].mProductType,
                mLocalProductList[k].mGroupID ?: "",
                mLocalProductList[k].mCategoryID ?: "",
                "",
                "",
                mLocalProductList[k].mProductImageUrl,
                mLocalProductList[k].mProductSequence,
                0,
                mLocalProductList[k].mDineInPrice.toDouble(),
                mLocalProductList[k].mQuickServicePrice.toDouble(),
                false
            )
            mProductList.add(mProductModel)
        }
        return mProductList
    }

}
