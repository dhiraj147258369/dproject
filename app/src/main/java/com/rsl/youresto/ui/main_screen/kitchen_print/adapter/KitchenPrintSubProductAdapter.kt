package com.rsl.youresto.ui.main_screen.kitchen_print.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.cart.models.CartSubProductModel
import com.rsl.youresto.databinding.RecyclerItemKpSubProductBinding

class KitchenPrintSubProductAdapter(val mSubProductsList: ArrayList<CartSubProductModel>, val mProductQuantity: Int): RecyclerView.Adapter<KitchenPrintSubProductAdapter.SubProductViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubProductViewHolder {
        val mBinding = RecyclerItemKpSubProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubProductViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mSubProductsList.size
    }

    override fun onBindViewHolder(holder: SubProductViewHolder, position: Int) {
        val mSubProduct = mSubProductsList[holder.adapterPosition]
        holder.bind(mSubProduct, mProductQuantity)

        val mIngredientAdapter = KitchenPrintIngredientAdapter(mSubProduct.mIngredientsList, mProductQuantity.toBigDecimal())
        holder.mBinding.recyclerViewProductTypes.adapter = mIngredientAdapter
    }

    class SubProductViewHolder(val mBinding: RecyclerItemKpSubProductBinding):RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mSubProduct: CartSubProductModel, quantity: Int){
            mBinding.subProduct = mSubProduct
            mBinding.quantity = quantity
        }
    }
}