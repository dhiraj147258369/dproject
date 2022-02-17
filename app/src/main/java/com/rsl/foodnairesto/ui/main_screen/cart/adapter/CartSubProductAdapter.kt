package com.rsl.foodnairesto.ui.main_screen.cart.adapter

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.data.cart.models.CartSubProductModel
import com.rsl.foodnairesto.databinding.RecyclerItemCartSubProductsBinding

@SuppressLint("LogNotTimber")
class CartSubProductAdapter(private val mSubProductList: ArrayList<CartSubProductModel>): RecyclerView.Adapter<CartSubProductAdapter.SubProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubProductViewHolder {
        val mBinding = RecyclerItemCartSubProductsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubProductViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mSubProductList.size
    }

    override fun onBindViewHolder(holder: SubProductViewHolder, position: Int) {
        val mSubProduct = mSubProductList[holder.adapterPosition]
        holder.bind(mSubProduct)

        Log.e(javaClass.simpleName, "onBindViewHolder: ${mSubProduct.mProductName}}")

        val mModifierAdapter = CartModifierAdapter(mSubProduct.mIngredientsList)
        holder.mBinding.recyclerViewProductTypes.adapter = mModifierAdapter
    }

    class SubProductViewHolder(val mBinding: RecyclerItemCartSubProductsBinding):RecyclerView.ViewHolder(mBinding.root){

        fun bind(mSubProduct: CartSubProductModel){
            mBinding.subProduct = mSubProduct
        }
    }
}