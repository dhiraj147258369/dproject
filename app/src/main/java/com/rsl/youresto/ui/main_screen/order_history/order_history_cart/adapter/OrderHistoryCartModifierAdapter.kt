package com.rsl.youresto.ui.main_screen.order_history.order_history_cart.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.database_download.models.ReportProductIngredientModel
import com.rsl.youresto.databinding.RecyclerItemOrderHistoryCartModifierBinding
import java.util.ArrayList

class OrderHistoryCartModifierAdapter(val mIngredientList: ArrayList<ReportProductIngredientModel>): RecyclerView.Adapter<OrderHistoryCartModifierAdapter.OHCMViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OHCMViewHolder {
        val mBinding = RecyclerItemOrderHistoryCartModifierBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OHCMViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mIngredientList.size
    }

    override fun onBindViewHolder(holder: OHCMViewHolder, position: Int) {
        val mIngredient = mIngredientList[holder.adapterPosition]
        holder.setIngredient(mIngredient)
    }

    inner class OHCMViewHolder(val mBinding: RecyclerItemOrderHistoryCartModifierBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setIngredient(mIngredient: ReportProductIngredientModel) {
            mBinding.mIngredient = mIngredient
        }
    }
}