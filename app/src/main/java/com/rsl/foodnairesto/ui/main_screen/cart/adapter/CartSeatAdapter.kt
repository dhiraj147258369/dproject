package com.rsl.foodnairesto.ui.main_screen.cart.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.data.tables.models.ServerTableSeatModel
import com.rsl.foodnairesto.databinding.RecyclerItemCartSeatBinding

class CartSeatAdapter(var mSeatList: ArrayList<ServerTableSeatModel>): RecyclerView.Adapter<CartSeatAdapter.SeatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatViewHolder {
        val mBinding = RecyclerItemCartSeatBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SeatViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mSeatList.size
    }

    override fun onBindViewHolder(holder: SeatViewHolder, position: Int) {
        val mSeat = mSeatList[holder.adapterPosition]
        holder.setSeat(mSeat)
    }

    inner class SeatViewHolder(var mBinding: RecyclerItemCartSeatBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setSeat(mSeat: ServerTableSeatModel) {
            mBinding.seat = mSeat
        }
    }
}