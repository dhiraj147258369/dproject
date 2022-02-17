package com.rsl.foodnairesto.ui.server_login


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.rsl.foodnairesto.App
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.data.database_download.models.LocationModel
import com.rsl.foodnairesto.data.database_download.models.LocationUserList
import com.rsl.foodnairesto.data.database_download.models.ServerModel
import com.rsl.foodnairesto.databinding.FragmentLocationBinding
import com.rsl.foodnairesto.ui.main_screen.MainScreenActivity
import com.rsl.foodnairesto.utils.AppPreferences
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class LocationFragment : Fragment() {

    private lateinit var binding: FragmentLocationBinding
    private val viewModel: ServerLoginViewModel by viewModel()
    private val viewModel2: ServerLoginViewModel by viewModel()
    private var locationId = ""
    private var serverId = ""
    private var serverModel: ServerModel = ServerModel()
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
        serverId=prefs.getServerId()

        getServers()
//        setLocations()
        setRestaurantLOGO()
    }


     fun getServers() {
        serverModel = ServerModel()



        viewModel2.getServers().observe(viewLifecycleOwner) {

            for (i in 0 until it.size) {
                if (it[i].mServerID.equals(serverId)) {
                    serverModel = it[i]
                }
            }
            setLocations()
        }
    }

    private fun setLocations() {



        viewModel.getLocations().observe(viewLifecycleOwner) {
            var locationUser=LocationModel()
            val locationList = ArrayList<LocationModel>()
            for(i in 0 until it.size){
                var L_id=it[i].mLocationID
                Log.e("L_Id",L_id)
                for(j in 0 until serverModel.mLocations.size){
                    var addBoolean=true
                    Log.e("mLocationsId",L_id)
                    Log.e("mLocationsId",L_id+"-"+serverModel.mLocations[j].mLocation_Id)
                    for(k in 0 until locationList.size){
                        if(locationList[k].mLocationID.equals(L_id)){
                            addBoolean=false
                        }
                    }

                    if(L_id.equals(serverModel.mLocations[j].mLocation_Id)){

                        if(addBoolean) {
                            locationList.add(it[i])
                        }
                    }
                }
            }
//            var locationList= ArrayList(it)
            if (locationId.isBlank()) {
                if (locationList.isNotEmpty()) {
                    onLocationClicked(locationList[0])
                }
            }

            val mLocationAdapter = LocationRecyclerAdapter(locationList)
            binding.recyclerViewLocations.layoutManager=LinearLayoutManager(requireContext())
            binding.recyclerViewLocations.adapter = mLocationAdapter
        }

        if (!isTablet) {
            binding.textViewSelectLocation.setOnClickListener { findNavController().navigate(R.id.serverLoginFragment) }
        } else {
//            binding.textViewSelectLocation.isVisible = false
        }
        binding.textViewSelectLocation.text=serverModel.mServerName
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
        val mMainScreenIntent = Intent(activity, MainScreenActivity::class.java)
        mMainScreenIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(mMainScreenIntent)
        requireActivity().finish()
//        if (!isTablet) findNavController().navigate(R.id.action_locationFragment_to_serverLoginFragment)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }
    fun refresh(){
        locationId = prefs.getSelectedLocation()
        serverId=prefs.getServerId()
        getServers()
    }
    fun clearUI(){
locationId= ""
        serverId= ""
        serverModel=ServerModel()
        getServers()


    }
    fun backPress(){
        findNavController().navigateUp()
    }
}