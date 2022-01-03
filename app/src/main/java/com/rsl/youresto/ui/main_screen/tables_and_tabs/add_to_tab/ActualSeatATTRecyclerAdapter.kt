package com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.tables.models.LocalTableSeatModel
import com.rsl.youresto.databinding.RecyclerItemSeatAttBinding

class ActualSeatATTRecyclerAdapter(private val mGroupPosition : Int, val mSeatList :ArrayList<LocalTableSeatModel>,
                                   val mGroupSelected: Boolean, private val mInterface :ActualSeatInterface)
    :RecyclerView.Adapter<ActualSeatATTRecyclerAdapter.SeatViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        val mBinding = RecyclerItemSeatAttBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SeatViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mSeatList.size
    }

    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        val mSeat = mSeatList[holder.adapterPosition]
        holder.bind(mSeat, holder.adapterPosition)
    }

    fun onSeatSelected(mSeat:LocalTableSeatModel, mPosition: Int){
        mSeat.isSelected = !mSeat.isSelected && mGroupSelected
        notifyItemChanged(mPosition)

        var mSelectedSeats = 0
        for (i in 0 until mSeatList.size) if (mSeatList[i].isSelected) mSelectedSeats++

        if (mSelectedSeats == mSeatList.size)
            mInterface.onSelectAll(true, mGroupPosition, mSeatList)
        else
            mInterface.onSelectAll(false, mGroupPosition, mSeatList)
    }

    inner class SeatViewHolder(val mBinding : RecyclerItemSeatAttBinding):RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mSeat:LocalTableSeatModel, mPosition: Int){
            mBinding.seat = mSeat
            mBinding.position = mPosition
            mBinding.adapter = this@ActualSeatATTRecyclerAdapter
            mBinding.groupSelected = mGroupSelected
        }
    }

    interface ActualSeatInterface{
        fun onSelectAll(isSelected: Boolean, mGroupPosition: Int, mSeats : ArrayList<LocalTableSeatModel>)
    }
}