package com.rsl.youresto.ui.main_login


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast.LENGTH_SHORT
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.R
import com.rsl.youresto.databinding.FragmentMainLoginBinding
import com.rsl.youresto.ui.server_login.ServerLoginActivity
import com.rsl.youresto.utils.AppConstants.AUTO_LOGOUT_ENABLED
import com.rsl.youresto.utils.AppConstants.FROM_RESTAURANT_LOGIN
import com.rsl.youresto.utils.AppConstants.INTENT_FROM
import com.rsl.youresto.utils.AppConstants.IS_LOGGED_IN
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.RESTAURANT_ID
import com.rsl.youresto.utils.AppConstants.RESTAURANT_LOGO
import com.rsl.youresto.utils.AppConstants.RESTAURANT_NAME
import com.rsl.youresto.utils.AppConstants.RESTAURANT_PASSWORD
import com.rsl.youresto.utils.AppConstants.RESTAURANT_USER_NAME
import com.rsl.youresto.utils.AppConstants.SEAT_SELECTION_ENABLED
import com.rsl.youresto.utils.custom_views.CustomToast
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.content.DialogInterface
import android.widget.Toast
import android.app.AlertDialog
import com.rsl.youresto.data.main_login.network.Login
import com.rsl.youresto.network.Resource
import com.rsl.youresto.network.models.PostLogin
import com.rsl.youresto.ui.database_download.DatabaseDownloadActivity
import com.rsl.youresto.utils.Network
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("LogNotTimber")
class MainLoginFragment : Fragment() {

    private lateinit var mBinding: FragmentMainLoginBinding
    private var mValidateUserName: Boolean = false
    private var mValidatePassword: Boolean = false
    private var mSharedPref: SharedPreferences? = null
    private val perms = arrayOf(
        "android.permission.READ_EXTERNAL_STORAGE",
        "android.permission.WRITE_EXTERNAL_STORAGE",
        "android.permission.CAMERA"
    )

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }

    private val loginViewModel: MainLoginViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_login, container, false)
        val view: View = mBinding.root

        validateInputs()

        mBinding.textViewForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.forgotPasswordFragment)
        }

        mBinding.buttonProceed.setOnClickListener {
            enableSignInViews()
            Network.isNetworkAvailableWithInternetAccess(requireActivity()).observe(viewLifecycleOwner,
                {
                    if (it) {
                        terminalLogin(mBinding.editTextUserName.text.toString(), mBinding.editTextPassword.text.toString())
                    } else {
                        disableSignInViews()
                        CustomToast.makeText(requireActivity(), getString(R.string.network_error), LENGTH_SHORT).show()
                    }
                })
        }

        if (!checkPermission()) {
            requestPermission()
        }

        return view
    }

    private fun terminalLogin(username: String, password: String) {

        val postLogin = PostLogin(username, password)
        loginViewModel.authenticateUserWithEmail(postLogin)

        loginViewModel.authData.observe(viewLifecycleOwner) {event ->
            event?.getContentIfNotHandled()?.let {
                when (it.status) {
                    Resource.Status.LOADING -> {
//                        showProgress(true)
                    }
                    Resource.Status.SUCCESS -> {
                        it.data?.let { networkLogin ->
                            if (networkLogin.status) {
                                setPreferences(networkLogin.data)
                                CustomToast.createToast(requireActivity(), "Login Successful", LENGTH_SHORT).show()
                                val loggedInIntent = Intent(activity, DatabaseDownloadActivity::class.java)
                                loggedInIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                                loggedInIntent.putExtra(INTENT_FROM, FROM_RESTAURANT_LOGIN)
                                startActivity(loggedInIntent)
                                requireActivity().finish()
                            } else {
                                CustomToast.createToast(requireActivity(), networkLogin.msg, LENGTH_SHORT).show()
                                disableSignInViews()
                            }
                        }

                    }
                    Resource.Status.ERROR -> {
                        //show error
                    }
                }
            }

        }
    }

    private fun setPreferences(restaurantLoginModel: Login) {
        //if response is successful, keep the info of the logged in user in SharedPreferences to faster access
        val mEditor = mSharedPref!!.edit()
        mEditor.putBoolean(IS_LOGGED_IN, true)
        mEditor.putString(RESTAURANT_ID, restaurantLoginModel.id)
        mEditor.putString(RESTAURANT_NAME, restaurantLoginModel.name)
//        mEditor.putString(MERCHANT_TID, restaurantLoginModel.mMerchantTID)
//        mEditor.putString(MERCHANT_TK, restaurantLoginModel.mMerchantTK)
        mEditor.putString(RESTAURANT_LOGO, restaurantLoginModel.profilePhoto)
        mEditor.putString(RESTAURANT_USER_NAME, mBinding.editTextUserName.text.toString())
        mEditor.putString(RESTAURANT_PASSWORD, mBinding.editTextPassword.text.toString())

        mEditor.putBoolean(AUTO_LOGOUT_ENABLED, true)
        mEditor.putBoolean(SEAT_SELECTION_ENABLED, true)
        mEditor.apply()
    }

    private fun validateInputs() {
        mBinding.buttonProceed.isEnabled = false

        mBinding.editTextUserName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {/* not required */
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {/* not required */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mValidateUserName = s!!.isNotEmpty()
                if (mValidateUserName && mValidatePassword) {
                    mBinding.buttonProceed.isEnabled = true
                    mBinding.buttonProceed.setBackgroundResource(R.drawable.background_proceed_button)
                } else {
                    mBinding.buttonProceed.isEnabled = true
                    mBinding.buttonProceed.setBackgroundResource(R.drawable.background_proceed_button_disable)
                }
            }

        })

        mBinding.editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {/* not required */
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {/* not required */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mValidatePassword = s!!.isNotEmpty()
                if (mValidateUserName && mValidatePassword) {
                    mBinding.buttonProceed.isEnabled = true
                    mBinding.buttonProceed.setBackgroundResource(R.drawable.background_proceed_button)
                } else {
                    mBinding.buttonProceed.isEnabled = true
                    mBinding.buttonProceed.setBackgroundResource(R.drawable.background_proceed_button_disable)
                }
            }
        })
    }


    /**
     * This method will start upon starting the login process and upon starting this the fields will be set disables
     * so that user can't enter anything while login process is still on
     */
    private fun enableSignInViews() {
        mBinding.loginProgressbar.visibility = View.VISIBLE

        mBinding.editTextUserName.isEnabled = false
        mBinding.editTextPassword.isEnabled = false
        mBinding.buttonProceed.isEnabled = false
    }

    /**
     * This method will enable the views if the login process returns the error, so that user can enter valid data
     */
    private fun disableSignInViews() {
        mBinding.loginProgressbar.visibility = View.GONE

        mBinding.editTextUserName.isEnabled = true
        mBinding.editTextPassword.isEnabled = true
        mBinding.buttonProceed.isEnabled = true
    }


    override fun onStart() {
        super.onStart()
        if (activity != null)
            mSharedPref = requireActivity().getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)

        //check if the user is already logged in, if he is then go to the next activity directly
        if (mSharedPref!!.getBoolean(IS_LOGGED_IN, false)) {
            val mLoggedInIntent = Intent(activity, ServerLoginActivity::class.java)
            mLoggedInIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            mLoggedInIntent.putExtra(INTENT_FROM, FROM_RESTAURANT_LOGIN)
            startActivity(mLoggedInIntent)
            requireActivity().finish()
        }
    }

    private fun checkPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CustomToast.makeText(requireActivity(), "Permission Granted", LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireActivity(), "Permission Denied", LENGTH_SHORT).show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ContextCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    showMessageOKCancel("You need to allow access permissions",
                        { _, _ ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermission()
                            }
                        })
                }
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permsRequestCode = 200
            requestPermissions(perms, permsRequestCode)
        }
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(requireActivity())
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

}
