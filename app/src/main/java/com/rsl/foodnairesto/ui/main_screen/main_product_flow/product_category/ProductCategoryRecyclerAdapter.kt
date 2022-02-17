package com.rsl.foodnairesto.ui.main_screen.main_product_flow.product_category

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rsl.foodnairesto.data.database_download.models.ProductCategoryModel
import com.rsl.foodnairesto.databinding.RecyclerItemProductCategoryBinding
import org.greenrobot.eventbus.EventBus

class ProductCategoryRecyclerAdapter(val context: Context, private val mCategoryList: ArrayList<ProductCategoryModel>) :
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

            loadImage(mCategory.mCategoryImageUrl, this)
        }

        private fun loadImage(mImageURL: String, holder: ProductCategoryViewHolder) {
            Glide
                .with(context)
                .load(mImageURL) // GlideUrl is created anyway so there's no extra objects allocated
                .into(holder.mBinding.imageViewProductCategory)
        }
    }
}