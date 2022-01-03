package com.rsl.youresto.ui.main_screen.app_settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.rsl.youresto.databinding.DialogPrinterSettingsBinding

class PrinterSettingsDialog: DialogFragment()  {

    override fun onResume() {
        super.onResume()
        dialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private lateinit var binding: DialogPrinterSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogPrinterSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
}