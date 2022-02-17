package com.rsl.foodnairesto.ui.main_screen.app_settings.printer_settings


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.databinding.FragmentSelectPrinterBinding
import com.rsl.foodnairesto.ui.main_screen.app_settings.printer_settings.event.OpenSettingEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class SelectPrinterFragment : Fragment() {

    private lateinit var mBinding: FragmentSelectPrinterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_select_printer, container, false)
        val mView: View = mBinding.root

        val mFragmentAdapter = SelectPrinterPagerAdapter(childFragmentManager)
        mBinding.viewpagerSelectPrinter.adapter = mFragmentAdapter

        mBinding.tabsSelectPrinter.setupWithViewPager(mBinding.viewpagerSelectPrinter)

        return mView
    }

    @Subscribe
    fun openSettingsFragment(mEvent: OpenSettingEvent) {
        val action = SelectPrinterFragmentDirections.actionSelectPrinterFragmentToSettingsFragment(mEvent.mTabNo)
        findNavController().navigate(action)
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
