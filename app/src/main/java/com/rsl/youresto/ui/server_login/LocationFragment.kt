package com.rsl.youresto.ui.server_login


import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.rsl.youresto.R
import com.rsl.youresto.data.database_download.models.LocationModel
import com.rsl.youresto.databinding.FragmentLocationBinding
import com.rsl.youresto.utils.AppConstants.LOCATION_SERVICE_TYPE
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.RESTAURANT_LOGO
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_NAME
import com.rsl.youresto.utils.ImageStorage
import com.rsl.youresto.utils.InjectorUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe


class LocationFragment : Fragment() {

    private lateinit var mBinding: FragmentLocationBinding
    private lateinit var mViewModel: ServerLoginViewModel
    private var mLocationID = ""
    private lateinit var mSharedPrefs: SharedPreferences

    private var isTablet: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        mBinding = FragmentLocationBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isTablet = requireContext().resources.getBoolean(R.bool.isTablet)

        mSharedPrefs = requireActivity().getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)

        mLocationID = mSharedPrefs.getString(SELECTED_LOCATION_ID, "")!!

        val factory = InjectorUtils.provideServerLoginViewModelFactory(requireActivity().applicationContext)
        mViewModel = ViewModelProviders.of(this, factory).get(ServerLoginViewModel::class.java)

        setLocations()
        setRestaurantLOGO()
    }

    private fun setLocations() {
        mViewModel.getLocations().observe(viewLifecycleOwner, {
            val locationList = ArrayList(it)
            if ((mSharedPrefs.getString(SELECTED_LOCATION_ID, "")?: "").isNotBlank()){

            } else {
                if (locationList.isNotEmpty()) {
                    onLocationClicked(locationList[0])
                }
            }

            val mLocationAdapter = LocationRecyclerAdapter(locationList)
            mBinding.recyclerViewLocations.adapter = mLocationAdapter
        })

        if (!isTablet) {
            mBinding.textViewSelectLocation.setOnClickListener { findNavController().navigate(R.id.serverLoginFragment) }
        } else {
            mBinding.textViewSelectLocation.isVisible = false
        }
    }

    private fun setRestaurantLOGO() {
        val mImageURL: String? = mSharedPrefs.getString(RESTAURANT_LOGO, "")

        try {
            mBinding.circularImageViewTerminalLogo.setImageBitmap(
                ImageStorage.getImage(mImageURL)
            )
        }catch (e: Exception){
            e.printStackTrace()
        }


    }

    @Subscribe
    fun onLocationClicked(mLocation: LocationModel){
        val mEditor: SharedPreferences.Editor = mSharedPrefs.edit()
        mEditor.putString(SELECTED_LOCATION_NAME, mLocation.mLocationName)
        mEditor.putString(SELECTED_LOCATION_ID, mLocation.mLocationID)
        mEditor.putInt(LOCATION_SERVICE_TYPE, mLocation.mLocationType.toInt())
        mEditor.apply()

        if (!isTablet) findNavController().navigate(R.id.action_locationFragment_to_serverLoginFragment)
    }

    override fun onStart() {
        super.onStart()
       if (!isTablet) {
           val mLocationName = mSharedPrefs.getString(SELECTED_LOCATION_NAME, "") ?: ""
           if (mLocationName.isNotEmpty()) findNavController().navigate(R.id.serverLoginFragment)
       }

        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }
}
