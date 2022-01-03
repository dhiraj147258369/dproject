package com.rsl.youresto.ui.main_screen.favorite_items

import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.rsl.youresto.data.database_download.models.ProductCategoryModel

class FavoriteItemsPagerAdapter(fm: FragmentManager, private val categories: ArrayList<ProductCategoryModel>):
    FragmentStatePagerAdapter(fm) {

    private var mCurrentFragment: Fragment? = null

    override fun getItem(position: Int): Fragment {
        val favoriteItemsModel = categories[position]
        return FavoriteCategoryFragment.newInstance(favoriteItemsModel.mCategoryID, favoriteItemsModel.mGroupID)
    }

    override fun getCount() = categories.size

    override fun getPageTitle(position: Int): CharSequence {
        val favoriteItemsModel = categories[position]
        return favoriteItemsModel.mCategoryName
    }

    override fun setPrimaryItem(container: View, position: Int, mObject: Any) {
        if (getCurrentFragment() !== mObject) {
            mCurrentFragment = mObject as Fragment
        }
        super.setPrimaryItem(container, position, mObject)
    }

    private fun getCurrentFragment(): Fragment {
        return mCurrentFragment!!
    }
}