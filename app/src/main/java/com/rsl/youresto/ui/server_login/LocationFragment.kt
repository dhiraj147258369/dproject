package com.rsl.youresto.ui.server_login


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.rsl.youresto.App
import com.rsl.youresto.R
import com.rsl.youresto.data.database_download.models.LocationModel
import com.rsl.youresto.databinding.FragmentLocationBinding
import com.rsl.youresto.utils.AppPreferences
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class LocationFragment : Fragment() {

    private lateinit var binding: FragmentLocationBinding
    private val viewModel: ServerLoginViewModel by viewModel()
    private var locationId = ""
    private val prefs: AppPreferences by inject()

    private var isTablet: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isTablet = App.isTablet

        locationId = prefs.getSelectedLocation()

        setLocations()
        setRestaurantLOGO()
    }

    private fun setLocations() {
        viewModel.getLocations().observe(viewLifecycleOwner, {
            val locationList = ArrayList(it)
            if (locationId.isBlank()){
                if (locationList.isNotEmpty()) {
                    onLocationClicked(locationList[0])
                }
            }

            val mLocationAdapter = LocationRecyclerAdapter(locationList)
            binding.recyclerViewLocations.adapter = mLocationAdapter
        })

        if (!isTablet) {
            binding.textViewSelectLocation.setOnClickListener { findNavController().navigate(R.id.serverLoginFragment) }
        } else {
            binding.textViewSelectLocation.isVisible = false
        }
    }

    private fun setRestaurantLOGO() {
        Glide
            .with(this)
            .load(prefs.getRestaurantImage())
            .into(binding.circularImageViewTerminalLogo)

    }

    @Subscribe
    fun onLocationClicked(mLocation: LocationModel){
        prefs.setSelectedLocation(mLocation)

        if (!isTablet) findNavController().navigate(R.id.action_locationFragment_to_serverLoginFragment)
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
