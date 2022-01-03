package com.rsl.youresto.utils.custom_dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.rsl.youresto.R
import com.rsl.youresto.databinding.DialogCustomProgressBinding
import com.rsl.youresto.utils.AppConstants.DIALOG_TYPE
import com.rsl.youresto.utils.AppConstants.DIALOG_TYPE_NETWORK
import com.rsl.youresto.utils.AppConstants.LOADER_TEXT
import com.rsl.youresto.utils.AppConstants.LOADER_TITLE_TEXT
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class CustomProgressDialog : DialogFragment(){

    private lateinit var mBinding : DialogCustomProgressBinding
    private var mLoaderTitleText: String ? = null
    private var mLoaderText: String ? = null

    companion object{
        fun newInstance(mLoaderTitleText: String, mLoaderText: String, mDialogType : Int): CustomProgressDialog{
            val mBundle = Bundle()
            mBundle.putString(LOADER_TITLE_TEXT, mLoaderTitleText)
            mBundle.putString(LOADER_TEXT, mLoaderText)
            mBundle.putInt(DIALOG_TYPE, mDialogType)
            val mCustomDialogFragment = CustomProgressDialog()
            mCustomDialogFragment.arguments = mBundle
            return mCustomDialogFragment
        }
    }

    override fun onResume() {
        super.onResume()
        dialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)

        val mDialogType = requireArguments().getInt(DIALOG_TYPE)

        if (mDialogType != DIALOG_TYPE_NETWORK) dialog!!.window!!.setWindowAnimations(R.style.DialogScaleAnimation)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.dialog_custom_progress, container, false)
        val mView = mBinding.root

        if (arguments != null) {
            mLoaderTitleText = requireArguments().getString(LOADER_TITLE_TEXT)
            mLoaderText = requireArguments().getString(LOADER_TEXT)
        }

        setViews()

        return mView
    }

    private fun setViews() {
        mBinding.textViewDialogTitle.text = mLoaderTitleText
        mBinding.textViewDialogText.text = mLoaderText
    }

    @Subscribe
    fun onTextChanged(mEvent: CustomProgressTextEvent){
        mBinding.textViewDialogText.text = mEvent.mLoaderText
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
