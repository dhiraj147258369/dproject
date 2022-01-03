package com.rsl.youresto.ui.main_screen.estimate_bill_print.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.cart.models.CartSubProductModel
import com.rsl.youresto.databinding.RecyclerItemEstimateBillCartSubProductBinding

class EstimateBillPrint50CartSubProductAdapter(private val mSubProductList: ArrayList<CartSubProductModel>) :
    RecyclerView.Adapter<EstimateBillPrint50CartSubProductAdapter.CartSubProductViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartSubProductViewHolder {
        val mBinding = RecyclerItemEstimateBillCartSubProductBinding.inflate(LayoutInflater.from(parent.context), parent,false)
        return CartSubProductViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mSubProductList.size
    }

    override fun onBindViewHolder(holder: CartSubProductViewHolder, position: Int) {
        val mSubProduct = mSubProductList[holder.adapterPosition]
        holder.bind(mSubProduct)

        val mIngredientAdapter =
            EstimateBillPrint50SubProductModifierAdapter(
                mSubProduct.mIngredientsList
            )
        holder.mBinding.recyclerViewProductTypes.adapter = mIngredientAdapter
    }

    class CartSubProductViewHolder (val mBinding: RecyclerItemEstimateBillCartSubProductBinding): RecyclerView.ViewHolder(mBinding.root){

        fun bind(mSubProduct: CartSubProductModel){
            mBinding.subProduct = mSubProduct
        }
    }
}