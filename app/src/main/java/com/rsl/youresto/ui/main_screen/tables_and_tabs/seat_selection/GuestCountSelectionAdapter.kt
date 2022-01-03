package com.rsl.youresto.ui.main_screen.tables_and_tabs.seat_selection

import android.annotation.SuppressLint
import android.util.Log.e
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.R
import com.rsl.youresto.databinding.RecyclerItemGuestSelectionBinding
import com.rsl.youresto.ui.main_screen.tables_and_tabs.seat_selection.event.GuestCountSelectionEvent
import org.greenrobot.eventbus.EventBus

@SuppressLint("LogNotTimber")
class GuestCountSelectionAdapter(var mGuestCountList: ArrayList<Int>):
    RecyclerView.Adapter<GuestCountSelectionAdapter.GuestViewHolder>() {

    private var mSelectedGuestCount = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
        val mBinding = RecyclerItemGuestSelectionBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return GuestViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mGuestCountList.size
    }

    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {
        val mGuest = mGuestCountList[holder.adapterPosition]
        holder.setGuest(mGuest)

        if(mSelectedGuestCount == holder.adapterPosition) {
            holder.mBinding.textViewOccupiedChairs.setBackgroundResource(R.drawable.background_green_border_black)
        } else {
            holder.mBinding.textViewOccupiedChairs.setBackgroundResource(R.drawable.background_white_border_black)
        }
    }

    fun onGuestCountClick(mGuestCount: Int) {
        e(javaClass.simpleName,"onGuestCountClick: $mGuestCount")
        mSelectedGuestCount = mGuestCount - 1
        notifyDataSetChanged()
        EventBus.getDefault().post(GuestCountSelectionEvent(true,mGuestCount))
    }

    inner class GuestViewHolder(var mBinding: RecyclerItemGuestSelectionBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setGuest(mGuest: Int) {
            mBinding.guest = mGuest
            mBinding.adapter = this@GuestCountSelectionAdapter
        }
    }
}