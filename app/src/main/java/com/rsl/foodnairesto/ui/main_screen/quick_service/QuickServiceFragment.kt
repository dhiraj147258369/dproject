package com.rsl.foodnairesto.ui.main_screen.quick_service

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.databinding.FragmentQuickBinding
import com.rsl.foodnairesto.ui.main_screen.MainScreenActivity
import com.rsl.foodnairesto.utils.AppConstants
import com.rsl.foodnairesto.utils.AppConstants.INTENT_FROM
import com.rsl.foodnairesto.utils.AppConstants.QUICK_SERVICE_FRAGMENT_TAB_SELECTED

class QuickServiceFragment : Fragment() {
    private lateinit var mBinding: FragmentQuickBinding
    private lateinit var mSharedPrefs: SharedPreferences

    private var mGroupID: String = ""
    private var mCategoryID: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_quick, container, false)
        val mView: View = mBinding.root

        (activity as MainScreenActivity?)?.checkquickservicefrag()
        mSharedPrefs = requireActivity().getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)
        (activity as MainScreenActivity?)?.checkquickservicefrag()

        val mFragmentAdapter = QuickServicePagerAdapter(childFragmentManager, mGroupID, mCategoryID, arguments?.getString(INTENT_FROM) ?: "")
        mBinding.viewpagerQuickService.adapter = mFragmentAdapter

        mBinding.tabsQuickService.setupWithViewPager(mBinding.viewpagerQuickService)
        mBinding.tabsQuickService.getTabAt(mSharedPrefs.getInt(QUICK_SERVICE_FRAGMENT_TAB_SELECTED,0))!!.select()

        return mView
    }


}
