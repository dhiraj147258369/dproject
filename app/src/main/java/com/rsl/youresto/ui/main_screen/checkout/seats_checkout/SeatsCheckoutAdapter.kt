package com.rsl.youresto.ui.main_screen.checkout.seats_checkout

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log.e
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.checkout.model.SeatPriceModel
import com.rsl.youresto.databinding.RecyclerItemSeatCheckoutBinding
import com.rsl.youresto.ui.main_screen.checkout.seats_checkout.event.SeatsCheckoutEvent
import org.greenrobot.eventbus.EventBus

@SuppressLint("LogNotTimber")
class SeatsCheckoutAdapter(var mContext: Context,
                           var mSeatList: ArrayList<SeatPriceModel>,
                           private var mPaidSeatCount: Int) :
    RecyclerView.Adapter<SeatsCheckoutAdapter.SeatsViewHolder>() {

    private var mSelectedSeatsCount = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatsViewHolder {
        val mBinding = RecyclerItemSeatCheckoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SeatsViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mSeatList.size
    }

    override fun onBindViewHolder(holder: SeatsViewHolder, position: Int) {
        val mSeat = mSeatList[holder.adapterPosition]

        if (mSelectedSeatsCount + mPaidSeatCount == mSeatList.size)
            EventBus.getDefault().post(SeatsCheckoutEvent(true))
        else
            EventBus.getDefault().post(SeatsCheckoutEvent(false))

        holder.setGroups(mSeat, holder.adapterPosition)
    }

    fun setSelectedSeat(mCount: Int) {
        e(javaClass.simpleName, "setSelectedSeat : $mCount")
        this.mSelectedSeatsCount = mCount
        notifyDataSetChanged()
    }

    fun onSeatClicked(mSeat: SeatPriceModel, mPosition: Int) {
        if (!mSeat.mPaid) mSeat.isSelected = !mSeat.isSelected
        if (mSeat.isSelected) mSelectedSeatsCount++ else mSelectedSeatsCount--
        notifyItemChanged(mPosition)
    }

    inner class SeatsViewHolder(val mBinding: RecyclerItemSeatCheckoutBinding) :
        RecyclerView.ViewHolder(mBinding.root) {
        fun setGroups(mSeat: SeatPriceModel, mPosition: Int) {
            mBinding.seat = mSeat
            mBinding.position = mPosition
            mBinding.seatsAdapter = this@SeatsCheckoutAdapter
        }
    }
}