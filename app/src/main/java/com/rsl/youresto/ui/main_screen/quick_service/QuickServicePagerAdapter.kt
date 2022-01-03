package com.rsl.youresto.ui.main_screen.quick_service

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.rsl.youresto.ui.main_screen.quick_service.quick_favorite_item.QuickFavoriteItemFragment
import com.rsl.youresto.ui.main_screen.main_product_flow.MainProductFragment
import com.rsl.youresto.utils.AppConstants.CATEGORY_ID
import com.rsl.youresto.utils.AppConstants.GROUP_ID
import com.rsl.youresto.utils.AppConstants.INTENT_FROM

class QuickServicePagerAdapter(fm: FragmentManager, val mGroupId: String, val mCategoryId: String, val intentFrom: String) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> {
                val mBundle = Bundle()
                mBundle.putString(INTENT_FROM, intentFrom)
                val fragment = QuickFavoriteItemFragment()
                fragment.arguments = mBundle
                fragment
            }
            1 -> {
                val mBundle = Bundle()
                mBundle.putString(INTENT_FROM, intentFrom)
                mBundle.putString(GROUP_ID, mGroupId)
                mBundle.putString(CATEGORY_ID, mCategoryId)
                val mMainProductFragment = MainProductFragment()
                mMainProductFragment.arguments = mBundle
                mMainProductFragment
            }
            else -> {
                val mBundle = Bundle()
                mBundle.putString(INTENT_FROM, intentFrom)
                val fragment = QuickFavoriteItemFragment()
                fragment.arguments = mBundle
                fragment
            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Favorite"
            1 -> "All Products"
            else -> {
                return "Favorite"
            }
        }
    }
}