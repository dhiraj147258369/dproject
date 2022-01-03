package com.rsl.youresto.ui.main_screen.cart

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.rsl.youresto.data.tables.models.ServerTableGroupModel

class CartGroupPagerAdapter(fm: FragmentManager, private var mGroupModelList: ArrayList<ServerTableGroupModel>, var mGroupNameSelected: String): FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val mGroup: ServerTableGroupModel = mGroupModelList[position]
        return CartFragment.newInstance(mGroup.mGroupName, mGroupNameSelected)
    }

    override fun getCount(): Int {
        return mGroupModelList.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        val mGroup: ServerTableGroupModel = mGroupModelList[position]
        return "Group "+ mGroup.mGroupName
    }
}