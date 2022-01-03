package com.rsl.youresto.ui.main_screen.checkout.payment_options.card

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.databinding.RecyclerItemPinpadBinding
import org.greenrobot.eventbus.EventBus

class PinPadRecyclerAdapter(private val mPinPadList: ArrayList<PinPadModel>) : RecyclerView.Adapter<PinPadRecyclerAdapter.PinPadViewModel>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PinPadViewModel {
        val mBinding = RecyclerItemPinpadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PinPadViewModel(mBinding)
    }

    override fun getItemCount(): Int {
        return mPinPadList.size
    }

    override fun onBindViewHolder(holder: PinPadViewModel, position: Int) {
        val mPinPad = mPinPadList[holder.adapterPosition]
        holder.bind(mPinPad)
    }

    fun onPinPadSelected(mPinPad: PinPadModel){
        EventBus.getDefault().post(mPinPad)
    }

    inner class PinPadViewModel(val mBinding : RecyclerItemPinpadBinding): RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mPinPad: PinPadModel){
            mBinding.pinPad = mPinPad
            mBinding.adapter = this@PinPadRecyclerAdapter
        }
    }
}