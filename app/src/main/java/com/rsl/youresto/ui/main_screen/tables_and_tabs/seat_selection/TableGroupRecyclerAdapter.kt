package com.rsl.youresto.ui.main_screen.tables_and_tabs.seat_selection

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.tables.models.LocalTableGroupModel
import com.rsl.youresto.databinding.RecyclerItemTableGroupBinding
import org.greenrobot.eventbus.EventBus
@SuppressLint("LogNotTimber")
class TableGroupRecyclerAdapter(private val mTableGroupList: ArrayList<LocalTableGroupModel>) :
    RecyclerView.Adapter<TableGroupRecyclerAdapter.TableGroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TableGroupViewHolder {
        val mBinding = RecyclerItemTableGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TableGroupViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mTableGroupList.size
    }


    override fun onBindViewHolder(holder: TableGroupViewHolder, position: Int) {
        val mTableGroup = mTableGroupList[holder.adapterPosition]
        holder.bind(mTableGroup)
        Log.e(javaClass.simpleName, "onBindViewHolder: ${mTableGroup.mGroupName}")
    }

    fun onGroupClicked(mTableGroup: LocalTableGroupModel) {
        EventBus.getDefault().post(mTableGroup)
    }

    inner class TableGroupViewHolder(private val mBinding: RecyclerItemTableGroupBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mTableGroup: LocalTableGroupModel) {
            mBinding.tableGroup = mTableGroup
            mBinding.tableGroupAdapter = this@TableGroupRecyclerAdapter
        }

    }
}