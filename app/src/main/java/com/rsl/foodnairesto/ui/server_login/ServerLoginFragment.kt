package com.rsl.foodnairesto.ui.server_login


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.rsl.foodnairesto.App
import com.rsl.foodnairesto.App.Companion.isTablet
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.data.database_download.models.LocationModel
import com.rsl.foodnairesto.data.database_download.models.ServerModel
import com.rsl.foodnairesto.databinding.FragmentServerLoginBinding
import com.rsl.foodnairesto.ui.main_screen.MainScreenActivity
import com.rsl.foodnairesto.ui.main_screen.checkout.payment_options.PaymentFragment
import com.rsl.foodnairesto.utils.AppPreferences
import com.rsl.foodnairesto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ServerLoginFragment : Fragment() {

    private lateinit var mBinding: FragmentServerLoginBinding
    private var serverList: ArrayList<ServerModel> = ArrayList()
    private val prefs: AppPreferences by inject()

    private val viewModel: ServerLoginViewModel by viewModel()


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

        initViews()
        setLocation()
        getServers()
        checkPassCode()
        if(isTablet) {
            clearLocationonFirst()
        }
    }

    /**
     * It will set the selected location to 1st location as a default location, when user/restaurant log in
     * for the first time
     */
    private fun setLocation() {
        mBinding.textViewSelectedLocation.text = prefs.getSelectedLocationName()
        mBinding.textViewSelectedLocation.visibility=View.GONE
        if (App.isTablet) mBinding.textViewChangeLocation.visibility = GONE
    }

    /**
     * This method will get the list of restaurantUserModel objects from the database using LiveData observer pattern,
     * which will contain all the necessary info related to users like ids and passwords
     */
    private fun getServers() {
        serverList = ArrayList()

        viewModel.getServers().observe(viewLifecycleOwner, {
            serverList.addAll(it)
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
        var loginintent=false
        mBinding.passCodeView.setOnTextChangeListener {

            when (it.length) {
                4 -> {
//                    mBinding.progressbarServerLogin.visibility = VISIBLE
                    if (serverList.size > 0) {

                        loop@ for (i in 0 until serverList.size) {
                            when (it) {
                                serverList[i].mServerPassword -> {
                                    mPassCodeFlag = true
//                                    var locationModel:LocationModel
//                                    viewModel.getLocations().observe(viewLifecycleOwner) {
////                                        Log.e("userLocation",serverList[i].mLocations[0].mLocation_Id)
//                                        var locationID = "9"
//                                        for (location in it) {
//                                            if (locationID.equals(location.mLocationID)) {
//                                                prefs.setSelectedLocation(location)
//                                            }
//                                        }
//
//                                    }

                                    if(serverList[i].mLocations.size>0) {

                                        Log.e("passserverLogin", mPassCodeFlag.toString())
                                        if (!loginintent) {
                                            loginIntent(serverList[i])
                                            loginintent = true
                                        }
                                    }else{
                                        CustomToast.makeText(
                                            requireActivity(),
                                            "Please use another pin!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
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
                        loginIntent(ServerModel("0", "Parag", "0000", "","", ArrayList()))
                    }
                }

            }
            if(isTablet){
            if(it.length<4){
                getCurrentFragment()?.let {

                    if (it is LocationFragment){
                        it.clearUI()
                        loginintent=false
                    }

                }
            }
            }
        }
    }

    private fun loginIntent(mServer: ServerModel) {
        mBinding.progressbarServerLogin.visibility = GONE
        val mMainScreenIntent = Intent(activity, MainScreenActivity::class.java)

        prefs.setServerDetails(mServer)
        prefs.clearOrderData()
        prefs.clearTableData()
//        var mfragment = LocationFragment()
//        val bundle = Bundle()
//        bundle.putSerializable("locationList", mServer.mLocations)
//        mfragment.arguments = bundle;
        EventBus.getDefault().post(MServerEvent(mServer))
        if(mServer.mUserType.equals("Restaurant manager")){
            prefs.setIsManager(true)
            prefs.setSelectedLocationType(1)
                    mMainScreenIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(mMainScreenIntent)
        requireActivity().finish()
        }else{
            prefs.setIsManager(false)
            if (!isTablet) {
                findNavController().navigate(R.id.locationFragment)
            } else {
//            mBinding.textViewSelectLocation.isVisible = false
                getCurrentFragment()?.let {

                       if (it is LocationFragment){
                           it.refresh()
                       }

                }
            }
        }

//       navLcationHost.let {
//            if (it is LocationFragment){
//                it.refresh()
//            }
//        }



//        mMainScreenIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//        startActivity(mMainScreenIntent)
//        requireActivity().finish()

    }

    private fun initViews() {
        mBinding.textViewChangeLocation.setOnClickListener { changeLocation() }
    }
    private fun getCurrentFragment(): Fragment? {

        return (requireActivity().supportFragmentManager.findFragmentById(R.id.server_passcode_host_fragment) as NavHostFragment).childFragmentManager.primaryNavigationFragment
    }

    fun clearLocationonFirst(){
        getCurrentFragment()?.let {

            if (it is LocationFragment){
                it.clearUI()

            }

        }
    }
    fun backPress(){
        findNavController().navigateUp()
    }

    private fun changeLocation() {
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
