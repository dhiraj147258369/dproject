package com.rsl.foodnairesto.ui.main_screen.app_settings

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.rsl.foodnairesto.ui.main_screen.app_settings.general_settings.GeneralSettingFragment
import com.rsl.foodnairesto.ui.main_screen.app_settings.printer_settings.PrinterSettingFragment

class SettingsPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> {
                GeneralSettingFragment()
            }
            1 -> {
                PrinterSettingFragment()
            }
            else -> {
                GeneralSettingFragment()
            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "General Settings"
            1 -> "Printer Settings"
            else -> {
                return "General Settings"
            }
        }
    }
}