package com.rsl.youresto.ui.main_screen.order_history.order_history_cart.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.database_download.models.ReportProductIngredientModel
import com.rsl.youresto.data.database_download.models.ReportProductModel
import com.rsl.youresto.databinding.RecyclerItemOrderHistoryCartBinding

class OrderHistoryCartAdapter(val mProductList: ArrayList<ReportProductModel>): RecyclerView.Adapter<OrderHistoryCartAdapter.OHCViewHolder>() {

    companion object{
        private const val CART_TYPE = 1
        private const val CART_TYPE_2 = 2
        private const val CART_TYPE_3 = 3
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OHCViewHolder {
        val mBinding = RecyclerItemOrderHistoryCartBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OHCViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mProductList.size
    }

    override fun onBindViewHolder(holder: OHCViewHolder, position: Int) {
        val mProduct = mProductList[holder.adapterPosition]
        holder.setProduct(mProduct)

        val itemViewType = getItemViewType(position)

        if (itemViewType == CART_TYPE_2) {
            if (mProduct.mIngredientList.size > 0) {
                val mIngredientList = ArrayList<ReportProductIngredientModel>()
                mIngredientList.addAll(mProduct.mIngredientList)

                val mIngredientAdapter = OrderHistoryCartModifierAdapter(mIngredientList)
                holder.mBinding.recyclerViewProductTypes.adapter = mIngredientAdapter
            } else {
                holder.mBinding.recyclerViewProductTypes.adapter = null
            }
        } else if(itemViewType == CART_TYPE_3) {
            if (mProduct.mSubProductList.size > 0) {
                val mSubProductAdapter = OrderHistorySubProductAdapter(mProduct.mSubProductList)
                holder.mBinding.recyclerViewProductTypes.adapter = mSubProductAdapter
            } else {
                holder.mBinding.recyclerViewProductTypes.adapter = null
            }
        }


    }

    override fun getItemViewType(position: Int): Int {
        return when (mProductList[position].mProductType) {
            3 -> CART_TYPE_3
            2 -> CART_TYPE_2
            else -> CART_TYPE
        }
    }

    inner class OHCViewHolder(val mBinding: RecyclerItemOrderHistoryCartBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setProduct(mProduct: ReportProductModel) {
            mBinding.mProduct = mProduct
        }
    }
}