package com.rsl.youresto.ui.main_screen.tables_and_tabs.seat_selection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.tables.models.LocalTableSeatModel
import com.rsl.youresto.databinding.RecyclerItemSeatSelectionSeatsBinding
import org.greenrobot.eventbus.EventBus

class TableSeatRecyclerAdapter(private val mSeatList: ArrayList<LocalTableSeatModel>) :
    RecyclerView.Adapter<TableSeatRecyclerAdapter.TableSeatViewHolder>() {

    private var mSelectedGroup = "A"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableSeatViewHolder {
        val mBinding = RecyclerItemSeatSelectionSeatsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TableSeatViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mSeatList.size
    }

    override fun onBindViewHolder(holder: TableSeatViewHolder, position: Int) {
        val mSeat = mSeatList[holder.adapterPosition]
        holder.bind(mSeat)
    }

    fun onSeatClicked(mSeat : LocalTableSeatModel){
        EventBus.getDefault().post(mSeat)
    }

    internal fun setSeatGroup(mSelectedGroup: String) {
        this.mSelectedGroup = mSelectedGroup
        notifyDataSetChanged()
    }

    inner class TableSeatViewHolder(val mBinding: RecyclerItemSeatSelectionSeatsBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mSeat: LocalTableSeatModel) {
            mBinding.seat = mSeat
            mBinding.seatAdapter = this@TableSeatRecyclerAdapter
        }
    }
}