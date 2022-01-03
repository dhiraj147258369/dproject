package com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.tables.models.LocalTableGroupModel
import com.rsl.youresto.data.tables.models.LocalTableSeatModel
import com.rsl.youresto.databinding.RecyclerItemSeatAttMainBinding

class SeatsATTRecyclerAdapter(private val mTableGroupList: ArrayList<LocalTableGroupModel>) :
    RecyclerView.Adapter<SeatsATTRecyclerAdapter.SeatsViewHolder>(), ActualSeatATTRecyclerAdapter.ActualSeatInterface {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeatsViewHolder {
        val mBinding = RecyclerItemSeatAttMainBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SeatsViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mTableGroupList.size
    }

    override fun onBindViewHolder(holder: SeatsViewHolder, position: Int) {
        val mGroup = mTableGroupList[holder.adapterPosition]
        holder.bind(mGroup, holder.adapterPosition)

        var mSeatCount = 0
        val mPaidSeatList = ArrayList<LocalTableSeatModel>()
        val mUnPaidSeatList = ArrayList<LocalTableSeatModel>()
        for (i in 0 until mGroup.mSeatList!!.size) {
            if (mGroup.mSeatList!![i].isSelected)
                mSeatCount++

            if (mGroup.mSeatList!![i].isPaid)
                mPaidSeatList.add(mGroup.mSeatList!![i])
            else
                mUnPaidSeatList.add(mGroup.mSeatList!![i])
        }

        val mFinalSeatList = ArrayList<LocalTableSeatModel>()

        mFinalSeatList.addAll(mUnPaidSeatList)
        mFinalSeatList.addAll(mPaidSeatList)

        holder.mBinding.checkBoxAll.setOnCheckedChangeListener(null)
        holder.mBinding.checkBoxAll.isChecked = mSeatCount == mGroup.mSeatList!!.size

        val seatAdapter = ActualSeatATTRecyclerAdapter(holder.adapterPosition, mFinalSeatList, mGroup.isSelected, this)
        holder.mBinding.recyclerViewSeatsAtt.adapter = seatAdapter

        holder.mBinding.checkBoxAll.setOnCheckedChangeListener { _, isChecked -> onCheckAllSelected(mGroup, isChecked, holder) }
    }

    fun onGroupSelected(mPosition: Int) {
        for (i in 0 until mTableGroupList.size) {
            mTableGroupList[i].isSelected = i == mPosition
            for (j in 0 until mTableGroupList[i].mSeatList!!.size)
                mTableGroupList[i].mSeatList!![j].isSelected = false
        }

        notifyDataSetChanged()
    }

    private fun onCheckAllSelected(mGroup: LocalTableGroupModel, isChecked: Boolean, holder: SeatsViewHolder ) {
        for (i in 0 until mGroup.mSeatList!!.size)
            mGroup.mSeatList!![i].isSelected = isChecked


        var mSeatCount = 0
        val mPaidSeatList = ArrayList<LocalTableSeatModel>()
        val mUnPaidSeatList = ArrayList<LocalTableSeatModel>()
        for (i in 0 until mGroup.mSeatList!!.size) {
            if (mGroup.mSeatList!![i].isSelected)
                mSeatCount++

            if (mGroup.mSeatList!![i].isPaid)
                mPaidSeatList.add(mGroup.mSeatList!![i])
            else
                mUnPaidSeatList.add(mGroup.mSeatList!![i])
        }

        val mFinalSeatList = ArrayList<LocalTableSeatModel>()

        mFinalSeatList.addAll(mUnPaidSeatList)
        mFinalSeatList.addAll(mPaidSeatList)

        val seatAdapter = ActualSeatATTRecyclerAdapter(holder.adapterPosition, mFinalSeatList, mGroup.isSelected, this)
        holder.mBinding.recyclerViewSeatsAtt.adapter = seatAdapter
    }

    override fun onSelectAll(isSelected: Boolean, mGroupPosition: Int, mSeats : ArrayList<LocalTableSeatModel>) {

        val mGroup = mTableGroupList[mGroupPosition]

        if (isSelected){
            for (i in 0 until mGroup.mSeatList!!.size)
                mGroup.mSeatList!![i].isSelected = isSelected

            notifyItemChanged(mGroupPosition)
        } else {
            for (i in 0 until mSeats.size)
                for (j in 0 until mGroup.mSeatList!!.size)
                    if (mSeats[i] == mGroup.mSeatList!![j]){
                        mGroup.mSeatList!![j].isSelected = mSeats[i].isSelected
                    }

            notifyItemChanged(mGroupPosition)
        }


    }

    fun getGroupList(): ArrayList<LocalTableGroupModel> {
        return mTableGroupList
    }

    inner class SeatsViewHolder(val mBinding: RecyclerItemSeatAttMainBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun bind(mGroup: LocalTableGroupModel, mPosition: Int) {
            mBinding.group = mGroup
            mBinding.position = mPosition
            mBinding.adapter = this@SeatsATTRecyclerAdapter
        }
    }
}