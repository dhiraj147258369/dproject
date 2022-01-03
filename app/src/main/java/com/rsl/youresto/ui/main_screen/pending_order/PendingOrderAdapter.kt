package com.rsl.youresto.ui.main_screen.pending_order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.databinding.RecyclerItemPendingOrderBinding
import com.rsl.youresto.ui.main_screen.pending_order.event.PendingOrderDeleteEvent
import com.rsl.youresto.ui.main_screen.pending_order.event.PendingOrderEvent
import com.rsl.youresto.utils.DateUtils.getStringFromDate
import org.greenrobot.eventbus.EventBus
import com.rsl.youresto.utils.Utils.getSuperScriptForDate


class PendingOrderAdapter(var mCartList: ArrayList<CartProductModel>): RecyclerView.Adapter<PendingOrderAdapter.PendingViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingViewHolder {
        val mBinding = RecyclerItemPendingOrderBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PendingViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mCartList.size
    }

    override fun onBindViewHolder(holder: PendingViewHolder, position: Int) {
        val mCart = mCartList[holder.adapterPosition]
        holder.setOrder(mCart)

        val mDate = getStringFromDate("dd MMM hh:mm a", mCart.mDateInMillis)!!

        holder.mBinding.textViewTime.text = getSuperScriptForDate(mDate)

        holder.mBinding.linearLayoutPendingOrder.setOnLongClickListener {
            EventBus.getDefault().post(PendingOrderDeleteEvent(true,mCart))
            false
        }
    }

    fun onPendingItemClick(mCart: CartProductModel) {
        EventBus.getDefault().post(PendingOrderEvent(true,mCart))
    }

    inner class PendingViewHolder(var mBinding: RecyclerItemPendingOrderBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setOrder(mCart: CartProductModel) {
            mBinding.cart = mCart
            mBinding.adapter = this@PendingOrderAdapter
        }
    }
}