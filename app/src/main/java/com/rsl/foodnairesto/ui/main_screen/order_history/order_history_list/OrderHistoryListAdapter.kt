package com.rsl.foodnairesto.ui.main_screen.order_history.order_history_list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.data.database_download.models.ReportModel
import com.rsl.foodnairesto.databinding.RecyclerItemOrderHistoryBinding
import com.rsl.foodnairesto.ui.main_screen.order_history.event.OrderHistoryListEvent
import com.rsl.foodnairesto.utils.DateUtils
import com.rsl.foodnairesto.utils.Utils
import org.greenrobot.eventbus.EventBus

class OrderHistoryListAdapter(private val mOrderList: ArrayList<ReportModel>): RecyclerView.Adapter<OrderHistoryListAdapter.OrderListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderListViewHolder {
        val mBinding = RecyclerItemOrderHistoryBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OrderListViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mOrderList.size
    }

    override fun onBindViewHolder(holder: OrderListViewHolder, position: Int) {
        val mOrder = mOrderList[holder.adapterPosition]
        holder.setOrder(mOrder)

        val mDate = DateUtils.getStringFromDate("dd MMM hh:mm a", mOrder.mDateTimeInTimeStamp)!!
        holder.mBinding.textViewTime.text = Utils.getSuperScriptForDate(mDate)
    }

    fun onOrderClick(mOrder: ReportModel) {
        EventBus.getDefault().post(OrderHistoryListEvent(true, mOrder))
    }

    inner class OrderListViewHolder(val mBinding: RecyclerItemOrderHistoryBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setOrder(mOrder: ReportModel) {
            mBinding.report = mOrder
            mBinding.adapter = this@OrderHistoryListAdapter
        }
    }
}