package com.rsl.foodnairesto.ui.main_screen.order_history.order_history_cart.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.data.database_download.models.ReportSubProductModel
import com.rsl.foodnairesto.databinding.RecyclerItemOrderHistorySubProductBinding

class OrderHistorySubProductAdapter(private val mSubProductList: ArrayList<ReportSubProductModel>):
    RecyclerView.Adapter<OrderHistorySubProductAdapter.OHSPViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OHSPViewHolder {
        val mBinding = RecyclerItemOrderHistorySubProductBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OHSPViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mSubProductList.size
    }

    override fun onBindViewHolder(holder: OHSPViewHolder, position: Int) {
        val mSubProduct = mSubProductList[holder.adapterPosition]
        holder.setSubProduct(mSubProduct)

        val mModifierAdapter = OrderHistoryCartModifierAdapter(mSubProduct.mIngredientList)
        holder.mBinding.recyclerViewProductTypes.adapter = mModifierAdapter
    }

    inner class OHSPViewHolder(val mBinding: RecyclerItemOrderHistorySubProductBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setSubProduct(mSubProduct: ReportSubProductModel) {
            mBinding.subProduct = mSubProduct
        }
    }
}