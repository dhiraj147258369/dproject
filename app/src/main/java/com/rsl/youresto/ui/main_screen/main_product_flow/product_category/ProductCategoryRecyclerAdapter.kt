package com.rsl.youresto.ui.main_screen.main_product_flow.product_category

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.database_download.models.ProductCategoryModel
import com.rsl.youresto.databinding.RecyclerItemProductCategoryBinding
import org.greenrobot.eventbus.EventBus

class ProductCategoryRecyclerAdapter(private val mCategoryList: ArrayList<ProductCategoryModel>) :
    RecyclerView.Adapter<ProductCategoryRecyclerAdapter.ProductCategoryViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductCategoryViewHolder {
        val mBinding = RecyclerItemProductCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductCategoryViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mCategoryList.size
    }

    override fun onBindViewHolder(holder: ProductCategoryViewHolder, position: Int) {
        val mCategory = mCategoryList[holder.adapterPosition]
        holder.bind(mCategory)

        marqueeProductName(holder)
    }

    fun onCategoryClicked(mCategory: ProductCategoryModel) {
        EventBus.getDefault().post(mCategory)
    }

    private fun marqueeProductName(holder: ProductCategoryViewHolder){
        holder.mBinding.textViewProductCategoryName.ellipsize = TextUtils.TruncateAt.MARQUEE
        holder.mBinding.textViewProductCategoryName.setSingleLine(true)
        holder.mBinding.textViewProductCategoryName.marqueeRepeatLimit = -1
        holder.mBinding.textViewProductCategoryName.isSelected = true
    }

    inner class ProductCategoryViewHolder(val mBinding: RecyclerItemProductCategoryBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mCategory: ProductCategoryModel) {
            mBinding.category = mCategory
            mBinding.adapter = this@ProductCategoryRecyclerAdapter
        }
    }
}