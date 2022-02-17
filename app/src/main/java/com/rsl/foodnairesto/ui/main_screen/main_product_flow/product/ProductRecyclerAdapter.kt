package com.rsl.foodnairesto.ui.main_screen.main_product_flow.product

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.rsl.foodnairesto.data.database_download.models.ProductModel
import com.rsl.foodnairesto.databinding.RecyclerItemProductBinding
import org.greenrobot.eventbus.EventBus
import java.util.*

class ProductRecyclerAdapter(
    val mContext: Context,
    private val mProductList: ArrayList<ProductModel>,
    TableID: String?
) :
    RecyclerView.Adapter<ProductRecyclerAdapter.ProductViewHolder>() {
    var TableID=TableID

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val mBinding = RecyclerItemProductBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return ProductViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mProductList.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val mProduct = mProductList[holder.adapterPosition]
        holder.bind(mProduct)

        if(mProduct.mTablewisePrice.size>0) {
            for (price in mProduct.mTablewisePrice) {
                if (TableID == price.mTableid) {
                    holder.mBinding.textViewProductPrice.text = String.format(Locale.ENGLISH,"%.2f",price.mPricew)
                }else{
                    holder.mBinding.textViewProductPrice.text = String.format(Locale.ENGLISH,"%.2f",mProduct.mDineInPrice)
                }
            }
        }else{
            holder.mBinding.textViewProductPrice.text = String.format(Locale.ENGLISH,"%.2f",mProduct.mDineInPrice)
        }




        marqueeProductName(holder)
        loadImage(mProduct.mProductImageUrl, holder)
    }

    private fun loadImage(mImageURL: String, holder: ProductViewHolder) {
        Glide
            .with(mContext)
            .load(mImageURL) // GlideUrl is created anyway so there's no extra objects allocated
            .into(holder.mBinding.imageViewProduct)
    }

    private fun marqueeProductName(holder: ProductViewHolder){
        holder.mBinding.textViewProductName.ellipsize = TextUtils.TruncateAt.MARQUEE
        holder.mBinding.textViewProductName.isSingleLine = true
        holder.mBinding.textViewProductName.marqueeRepeatLimit = -1
        holder.mBinding.textViewProductName.isSelected = true
    }

    inner class ProductViewHolder(val mBinding : RecyclerItemProductBinding) : RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mProduct: ProductModel){
            mBinding.product = mProduct
            mBinding.adapter = this@ProductRecyclerAdapter
        }
    }

    fun onProductClick(mProduct: ProductModel){
        EventBus.getDefault().post(mProduct)
    }
}