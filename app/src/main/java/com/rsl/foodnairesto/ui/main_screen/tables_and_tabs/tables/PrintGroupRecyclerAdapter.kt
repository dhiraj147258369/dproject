package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.tables

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.data.tables.models.ServerTableGroupModel
import com.rsl.foodnairesto.databinding.RecyclerItemPrintGroupBinding
import com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.tables.events.PrintGroupEvent
import org.greenrobot.eventbus.EventBus

class PrintGroupRecyclerAdapter(val mGroupList: ArrayList<ServerTableGroupModel>): RecyclerView.Adapter<PrintGroupRecyclerAdapter.PrintGroupViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrintGroupViewHolder {
        val mBinding =
            RecyclerItemPrintGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PrintGroupViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mGroupList.size
    }

    override fun onBindViewHolder(holder: PrintGroupViewHolder, position: Int) {
        val mGroup = mGroupList[holder.adapterPosition]
        holder.bind(mGroup)
    }

    fun onGroupClick(mGroup: ServerTableGroupModel){
        EventBus.getDefault().post(PrintGroupEvent(mGroup))
    }

    inner class PrintGroupViewHolder( val mBinding :RecyclerItemPrintGroupBinding):RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mGroup: ServerTableGroupModel){
            mBinding.tableGroup = mGroup
            mBinding.adapter = this@PrintGroupRecyclerAdapter
        }
    }
}