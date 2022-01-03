package com.rsl.youresto.ui.server_login

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.database_download.models.LocationModel
import com.rsl.youresto.databinding.RecyclerItemLocationBinding
import com.rsl.youresto.utils.AppConstants.SERVICE_QUICK_SERVICE
import org.greenrobot.eventbus.EventBus

class LocationRecyclerAdapter constructor(private val mLocationList: ArrayList<LocationModel>) :
    RecyclerView.Adapter<LocationRecyclerAdapter.LocationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val mBinding = RecyclerItemLocationBinding.inflate(inflater, parent, false)

        return LocationViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mLocationList.size
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(mLocationList[position])
    }

    fun onLocationClicked(mLocation: LocationModel){
        EventBus.getDefault().post(mLocation)
    }

    inner class LocationViewHolder(private val mBinding: RecyclerItemLocationBinding) :
        RecyclerView.ViewHolder(mBinding.root) {
        fun bind(location: LocationModel) {
            mBinding.locationModel = location

            if (location.mLocationType.toInt() == SERVICE_QUICK_SERVICE){
                mBinding.textViewLocationName.text = "${location.mLocationName} (Take Away)"
            } else {

                mBinding.textViewLocationName.text = location.mLocationName
            }

            mBinding.locationAdapter = this@LocationRecyclerAdapter
        }
    }
}