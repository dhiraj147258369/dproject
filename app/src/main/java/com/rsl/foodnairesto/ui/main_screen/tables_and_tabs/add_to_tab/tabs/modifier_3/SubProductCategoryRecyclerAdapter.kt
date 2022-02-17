package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_3

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.data.database_download.models.SubProductCategoryModel
import com.rsl.foodnairesto.databinding.RecyclerItemType3ProductCategoryBinding
@SuppressLint("LogNotTimber")
class SubProductCategoryRecyclerAdapter(private val mSubProductCategoryList: ArrayList<SubProductCategoryModel>):
    RecyclerView.Adapter<SubProductCategoryRecyclerAdapter.SubProductViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubProductViewHolder {
        val mBinding = RecyclerItemType3ProductCategoryBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return SubProductViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mSubProductCategoryList.size
    }

    override fun onBindViewHolder(holder: SubProductViewHolder, position: Int) {
        val mCategory = mSubProductCategoryList[holder.adapterPosition]
        holder.bind(mCategory)

        val mSubProductAdapter = SubProductRecyclerAdapter(mCategory.mProductList)
        holder.mBinding.recyclerViewGeneric1.adapter = mSubProductAdapter
    }

    inner class SubProductViewHolder (val mBinding : RecyclerItemType3ProductCategoryBinding):
        RecyclerView.ViewHolder(mBinding.root){

        fun bind(mCategory: SubProductCategoryModel){
            mBinding.subCategory = mCategory
        }
    }
}