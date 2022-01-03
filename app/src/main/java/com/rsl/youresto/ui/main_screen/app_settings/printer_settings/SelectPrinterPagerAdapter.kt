package com.rsl.youresto.ui.main_screen.app_settings.printer_settings

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.rsl.youresto.ui.main_screen.app_settings.printer_settings.bluetooth_printer.BluetoothPrinterFragment
import com.rsl.youresto.ui.main_screen.app_settings.printer_settings.network_printer.NetworkPrinterFragment

class SelectPrinterPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> {
                BluetoothPrinterFragment()
            }
            1 -> {
                NetworkPrinterFragment()
            }
            else -> {
                BluetoothPrinterFragment()
            }
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Bluetooth Printer"
            1 -> "Network Printer"
            else -> {
                return "Bluetooth Printer"
            }
        }
    }
}