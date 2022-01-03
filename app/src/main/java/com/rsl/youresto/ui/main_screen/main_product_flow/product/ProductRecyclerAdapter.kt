package com.rsl.youresto.ui.main_screen.main_product_flow.product

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.database_download.models.ProductModel
import com.rsl.youresto.databinding.RecyclerItemProductBinding
import org.greenrobot.eventbus.EventBus
import java.util.*

class ProductRecyclerAdapter(mContext: Context, private val mProductList : ArrayList<ProductModel>) :
    RecyclerView.Adapter<ProductRecyclerAdapter.ProductViewHolder>() {

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

        holder.mBinding.textViewProductPrice.text = String.format(Locale.ENGLISH,"%.2f",mProduct.mDineInPrice)

//        when {
//            mSharedPrefs.getInt(LOCATION_SERVICE_TYPE,0) == SERVICE_DINE_IN ->
//                holder.mBinding.textViewProductPrice.text = String.format(Locale.ENGLISH,"%.2f",mProduct.mDineInPrice)
//            mSharedPrefs.getInt(LOCATION_SERVICE_TYPE,0) == SERVICE_QUICK_SERVICE ->
//                holder.mBinding.textViewProductPrice.text = String.format(Locale.ENGLISH,"%.2f",mProduct.mQuickServicePrice)
//            else ->
//                holder.mBinding.textViewProductPrice.text = String.format(Locale.ENGLISH,"%.2f",mProduct.mDeliveryPrice)
//        }

        marqueeProductName(holder)
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