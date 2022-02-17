package com.rsl.foodnairesto.ui.main_screen.app_settings.general_settings


import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.databinding.FragmentGeneralSettingBinding
import com.rsl.foodnairesto.utils.AppConstants.AUTO_LOGOUT_ENABLED
import com.rsl.foodnairesto.utils.AppConstants.MY_PREFERENCES
import com.rsl.foodnairesto.utils.custom_views.CustomToast

@SuppressLint("LogNotTimber")
class GeneralSettingFragment : Fragment() {

    private lateinit var mBinding: FragmentGeneralSettingBinding
    private var mSharedPref: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_general_setting, container, false)
        val mView = mBinding.root

        mSharedPref = requireActivity().getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)

        autoLogOut()

        return mView
    }

    private fun autoLogOut() {
        if (mSharedPref!!.getBoolean(AUTO_LOGOUT_ENABLED, false)) {
            mBinding.checkboxAutoLogout.setOnCheckedChangeListener(null)
            mBinding.checkboxAutoLogout.isChecked = true
        } else {
            mBinding.checkboxAutoLogout.setOnCheckedChangeListener(null)
            mBinding.checkboxAutoLogout.isChecked = false
        }
        mBinding.checkboxAutoLogout.setOnCheckedChangeListener(mAutoLogoutListener)
    }

    private val mAutoLogoutListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        val mEditor = mSharedPref!!.edit()
        if (isChecked) {
            mEditor.putBoolean(AUTO_LOGOUT_ENABLED, true)
            CustomToast.makeText(requireActivity(), "Auto Logout Enabled", Toast.LENGTH_SHORT).show()
        } else {
            mEditor.putBoolean(AUTO_LOGOUT_ENABLED, false)
            CustomToast.makeText(requireActivity(), "Auto Logout Disabled", Toast.LENGTH_SHORT).show()
        }

        mEditor.apply()
    }
}
