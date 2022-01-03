package com.rsl.youresto.utils.custom_dialog


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.rsl.youresto.R
import com.rsl.youresto.databinding.FragmentCustomDialogBinding
import com.rsl.youresto.utils.AppConstants.DIALOG_ID
import com.rsl.youresto.utils.AppConstants.DIALOG_IMAGE
import com.rsl.youresto.utils.AppConstants.DIALOG_MESSAGE
import com.rsl.youresto.utils.AppConstants.DIALOG_NEGATIVE_BUTTON
import com.rsl.youresto.utils.AppConstants.DIALOG_NEGATIVE_IMAGE
import com.rsl.youresto.utils.AppConstants.DIALOG_POSITIVE_BUTTON
import com.rsl.youresto.utils.AppConstants.DIALOG_POSITIVE_IMAGE
import com.rsl.youresto.utils.AppConstants.DIALOG_SOURCE
import com.rsl.youresto.utils.AppConstants.DIALOG_TITLE
import org.greenrobot.eventbus.EventBus

class CustomAlertDialogFragment : DialogFragment() {

    private lateinit var mBinding: FragmentCustomDialogBinding
    private var mDialogID: Int? = null
    private var mSource: String? = null
    private var mDialogImage: Int? = null
    private var mDialogTitle: String? = null
    private var mDialogMessage: String? = null
    private var mDialogPositiveButton: String? = null
    private var mDialogPositiveImage: Int? = null
    private var mDialogNegativeButton: String? = null
    private var mDialogNegativeImage: Int? = null

    companion object {
        fun newInstance(
            mDialogID: Int, mSource: String, mDialogImage: Int, mTitle: String, mMessage: String, mPositiveButtonText: String,
            mNegativeButtonText: String, mPositiveImage: Int, mNegativeImage: Int
        ): CustomAlertDialogFragment {
            val mBundle = Bundle()
            mBundle.putInt(DIALOG_ID, mDialogID)
            mBundle.putString(DIALOG_SOURCE, mSource)
            mBundle.putInt(DIALOG_IMAGE, mDialogImage)
            mBundle.putString(DIALOG_TITLE, mTitle)
            mBundle.putString(DIALOG_MESSAGE, mMessage)
            mBundle.putString(DIALOG_POSITIVE_BUTTON, mPositiveButtonText)
            mBundle.putInt(DIALOG_POSITIVE_IMAGE, mPositiveImage)
            mBundle.putString(DIALOG_NEGATIVE_BUTTON, mNegativeButtonText)
            mBundle.putInt(DIALOG_NEGATIVE_IMAGE, mNegativeImage)
            val mCustomDialogFragment = CustomAlertDialogFragment()
            mCustomDialogFragment.arguments = mBundle
            return mCustomDialogFragment
        }
    }

    override fun onResume() {
        super.onResume()

        dialog!!.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_custom_dialog, container, false)
        val mView = mBinding.root

        if (arguments != null) {
            mDialogID = requireArguments().getInt(DIALOG_ID)
            mDialogImage = requireArguments().getInt(DIALOG_IMAGE)
            mDialogTitle = requireArguments().getString(DIALOG_TITLE)
            mDialogMessage = requireArguments().getString(DIALOG_MESSAGE)
            mDialogPositiveButton = requireArguments().getString(DIALOG_POSITIVE_BUTTON)
            mDialogNegativeButton = requireArguments().getString(DIALOG_NEGATIVE_BUTTON)
            mDialogPositiveImage = requireArguments().getInt(DIALOG_POSITIVE_IMAGE)
            mDialogNegativeImage = requireArguments().getInt(DIALOG_NEGATIVE_IMAGE)
            mSource = requireArguments().getString(DIALOG_SOURCE)
        }

        setViews()

        return mView
    }

    private fun setViews() {
        mBinding.imageViewTitleDialog.setImageResource(mDialogImage!!)
        mBinding.textViewTitleDialog.text = mDialogTitle
        mBinding.textViewMessageDialog.text = mDialogMessage

        mBinding.imageViewNegative.setImageResource(mDialogNegativeImage!!)
        mBinding.imageViewPositive.setImageResource(mDialogPositiveImage!!)
        mBinding.buttonNegativeDialog.text = mDialogNegativeButton
        mBinding.buttonPositiveDialog.text = mDialogPositiveButton

        if (mDialogPositiveButton == "") {
            mBinding.constraintAlertPositive.visibility = GONE
        }
        if (mDialogNegativeButton == "") {
            mBinding.constraintAlertNegative.visibility = GONE
        }

        mBinding.constraintAlertPositive.setOnClickListener {
            EventBus.getDefault().post(AlertDialogEvent(true, mDialogID!!, mSource!!))
            dismiss()
        }

        mBinding.constraintAlertNegative.setOnClickListener {
            dismiss()
        }

    }

}
