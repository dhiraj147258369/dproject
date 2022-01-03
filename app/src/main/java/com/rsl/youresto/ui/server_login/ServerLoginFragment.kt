package com.rsl.youresto.ui.server_login


import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.R
import com.rsl.youresto.data.database_download.models.LocationModel
import com.rsl.youresto.data.database_download.models.ServerModel
import com.rsl.youresto.data.server_login.models.ServerShiftModel
import com.rsl.youresto.databinding.FragmentServerLoginBinding
import com.rsl.youresto.ui.main_screen.MainScreenActivity
import com.rsl.youresto.utils.AppConstants.API_LOG_IN
import com.rsl.youresto.utils.AppConstants.API_LOG_OUT
import com.rsl.youresto.utils.AppConstants.LOGGED_IN_SERVER_ID
import com.rsl.youresto.utils.AppConstants.LOGGED_IN_SERVER_NAME
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_NAME
import com.rsl.youresto.utils.DateUtils
import com.rsl.youresto.utils.InjectorUtils
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class ServerLoginFragment : Fragment() {

    private lateinit var mBinding: FragmentServerLoginBinding
    private var mServerList: ArrayList<ServerModel>? = null
    private lateinit var mSharedPrefs: SharedPreferences
    private var mViewModel: ServerLoginViewModel? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = FragmentServerLoginBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mSharedPrefs = requireActivity().getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)

        initViews()
        setLocation()
        getServers()
        checkPassCode()
    }

    /**
     * It will set the selected location to 1st location as a default location, when user/restaurant log in
     * for the first time
     */
    private fun setLocation() {
        mBinding.textViewSelectedLocation.text = mSharedPrefs.getString(SELECTED_LOCATION_NAME, "")
    }

    /**
     * This method will get the list of restaurantUserModel objects from the database using LiveData observer pattern,
     * which will contain all the necessary info related to users like ids and passwords
     */
    private fun getServers() {
        mServerList = ArrayList()
        val mFactory = InjectorUtils.provideServerLoginViewModelFactory(requireActivity().applicationContext)
        mViewModel = ViewModelProviders.of(this, mFactory).get(ServerLoginViewModel::class.java)

        mViewModel!!.getServers().observe(viewLifecycleOwner, {
            mServerList?.addAll(it)
        })
    }


    private var mPassCodeFlag = false
    private var mOverrideLogin = false

    /**
     * This method will check if entered pin by the user matches with the one from list of restaurantUserModel objects
     * and if it does matches it will fire up an intent for the home screen, and if it doesn't, it will show the appropriate
     * error message. Also it will store the shared-preferences related to logged in user.
     */
    private fun checkPassCode() {
        mBinding.passCodeView.setOnTextChangeListener {
            when (it.length) {
                4 -> {
                    mBinding.progressbarServerLogin.visibility = VISIBLE
                    if (mServerList!!.size > 0) {

                        loop@ for (i in 0 until mServerList!!.size) {
                            when (it) {
                                mServerList!![i].mServerPassword -> {
                                    mPassCodeFlag = true
                                    loginIntent(mServerList!![i])
//                                    Network.isNetworkAvailableWithInternetAccess(requireActivity()).observe(viewLifecycleOwner, { hasNetwork ->
//                                        when {
//                                            hasNetwork -> checkServerShiftDetails(mServerList!![i])
//                                            else -> {
//                                                CustomToast.makeText(requireActivity(), getString(R.string.network_error), Toast.LENGTH_SHORT).show()
//
//                                                mBinding.progressbarServerLogin.visibility = GONE
//                                                mBinding.passCodeView.setError(true)
//                                            }
//                                        }
//                                    })
                                    break@loop
                                }
                                "0000" -> {
                                    CustomToast.makeText(
                                        requireActivity(),
                                        "Overruled Login, please enter your pin again",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    mOverrideLogin = true
                                    mPassCodeFlag = true
                                    mBinding.progressbarServerLogin.visibility = GONE
                                    mBinding.passCodeView.setError(true)
                                    Handler(Looper.getMainLooper()).postDelayed({ mOverrideLogin = false }, 4000)
                                    break@loop


                                }
                                else -> mPassCodeFlag = false
                            }
                        }

                        when {
                            !mPassCodeFlag -> {
                                CustomToast.makeText(
                                    requireActivity(),
                                    "Please enter correct pin.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                mBinding.progressbarServerLogin.visibility = GONE
                                mBinding.passCodeView.setError(true)
                            }
                        }

                    } else {
                        loginIntent(ServerModel("0", "Parag", "0000", "", ArrayList()))
                    }
                }
            }
        }
    }

    private var mEndTime = false

    private fun checkServerShiftDetails(mServer: ServerModel) {
        val mServerData: LiveData<List<ServerShiftModel>> =
            mViewModel!!.getServerShiftDetails(
                mServer.mServerID, DateUtils.getDateForShift(false),
                DateUtils.getDateForShift(true)
            )

        mServerData.observe(viewLifecycleOwner, {
            when {
                mOverrideLogin -> {
                    loginIntent(mServer)
                    mPassCodeFlag = true
                }
                else -> {

                    for (element in it) {
                        if (element.mEndTimeStamp == null) {
                            mEndTime = true
                            break
                        } else {
                            mEndTime = false
                        }
                    }

                    if (mEndTime) {
                        //completed: check login
                        val mLoginData = checkLogin(mServer.mServerID)

                        mLoginData.observe(viewLifecycleOwner, { mLoginFlag ->
                            if (mLoginFlag) {
                                submitLoginDetails(mServer)

                                loginIntent(mServer)
                            } else {
                                CustomToast.makeText(
                                    requireActivity(),
                                    "You're logged in from another device \n" + "Please log out from that device first",
                                    Toast.LENGTH_SHORT
                                ).show()
                                mBinding.progressbarServerLogin.visibility = GONE
                                mBinding.passCodeView.setError(true)
                            }

                            mLoginData.removeObservers(this)
                        })
                    } else {
                        mBinding.progressbarServerLogin.visibility = GONE
                        moreShiftDialog(mServer)
                    }

                }
            }
            mServerData.removeObservers(this)
        })

    }

    private fun submitLoginDetails(mServer: ServerModel) {
        mViewModel!!.submitLoginDetails(mServer.mServerID, mServer.mServerName, API_LOG_IN)
    }

    private val mServerLoginData = MutableLiveData<Boolean>()

    private fun checkLogin(mServerID: String): LiveData<Boolean> {

        mViewModel!!.getServerLoginDetails(mServerID).observe(viewLifecycleOwner, {
            when {
                it != null && it.mLogInFlag == API_LOG_OUT -> mServerLoginData.postValue(true)
                it != null && it.mLogInFlag == API_LOG_IN -> mServerLoginData.postValue(false)
                it == null -> mServerLoginData.postValue(false)
            }
        })

        return mServerLoginData
    }

    private fun loginIntent(mServer: ServerModel) {
        mBinding.progressbarServerLogin.visibility = GONE
        val mMainScreenIntent = Intent(activity, MainScreenActivity::class.java)

        val mEditor = mSharedPrefs.edit()
        mEditor.putString(LOGGED_IN_SERVER_ID, mServer.mServerID)
        mEditor.putString(LOGGED_IN_SERVER_NAME, mServer.mServerName)
        mEditor.apply()

        mMainScreenIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(mMainScreenIntent)
        requireActivity().finish()
    }

    private fun moreShiftDialog(mServer: ServerModel) {

        //completed: moreShiftDialog

        val shiftCountData = mViewModel!!.getShiftCount(
            mServer.mServerID,
            DateUtils.getDateForShift(false),
            DateUtils.getDateForShift(true)
        )

        shiftCountData.observe(viewLifecycleOwner, { mShiftCount ->
            if (mShiftCount != null) {
                val builder = AlertDialog.Builder(requireActivity())
                if (mShiftCount > 1)
                    builder.setMessage(
                        "You have already completed " + mShiftCount
                                + " Shifts, do you really want to start another one?"
                    )
                else
                    builder.setMessage(
                        ("You have already completed " + mShiftCount
                                + " Shift, do you really want to start another one?")
                    )

                builder.setTitle("Another Shift?")
                builder.setPositiveButton("Yes, I wanna work!") { dialog, _ ->
                    startShiftAgain(mServer)
                    dialog.dismiss()
                }
                builder.setNegativeButton("No, I wanna rest!") { dialog, _ ->
                    dialog.dismiss()
                    mBinding.passCodeView.setError(true)
                }

                builder.create().show()

                shiftCountData.removeObservers(this)
            }
        })
    }

    private fun startShiftAgain(mServer: ServerModel) {

        checkLogin(mServer.mServerID).observe(viewLifecycleOwner, { serverLogin ->

            if (serverLogin != null && serverLogin) {

                mViewModel!!.startShift(mServer.mServerID).observe(viewLifecycleOwner, { integer ->
                    if (integer != null && integer == 1) {
                        submitLoginDetails(mServer)

                        loginIntent(mServer)
                    }
                })

            } else {
                CustomToast.makeText(
                    requireActivity(),
                    "You're logged in from another device \n" + "Please log out from that device first",
                    Toast.LENGTH_SHORT
                ).show()
                mBinding.passCodeView.setError(true)
            }
        })
    }

    private fun initViews() {
        mBinding.textViewChangeLocation.setOnClickListener { changeLocation() }
    }

    fun backPress(){
        findNavController().navigateUp()
    }

    private fun changeLocation() {
        val mEditor: SharedPreferences.Editor = mSharedPrefs.edit()
        mEditor.putString(SELECTED_LOCATION_NAME, "")
        mEditor.putString(SELECTED_LOCATION_ID, "")
        mEditor.apply()

        findNavController().navigate(R.id.action_serverLoginFragment_to_locationFragment)
    }

    @Subscribe
    fun locationChanged(locationModel: LocationModel){
        mBinding.textViewSelectedLocation.text = locationModel.mLocationName
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
