package com.rsl.youresto.ui.main_screen.estimate_bill_print.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.database_download.models.IngredientsModel
import com.rsl.youresto.databinding.RecyclerItemEstimateBillSubProductModifiersBinding

class EstimateBillPrint50SubProductModifierAdapter(var mIngredientList: ArrayList<IngredientsModel>) :
    RecyclerView.Adapter<EstimateBillPrint50SubProductModifierAdapter.SubProductModifierViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubProductModifierViewHolder {
        val mBinding = RecyclerItemEstimateBillSubProductModifiersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubProductModifierViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mIngredientList.size
    }

    override fun onBindViewHolder(holder: SubProductModifierViewHolder, position: Int) {
        val mIngredient = mIngredientList[holder.adapterPosition]
        holder.setIngredient(mIngredient)
    }

    class SubProductModifierViewHolder (val mBinding : RecyclerItemEstimateBillSubProductModifiersBinding)
        :RecyclerView.ViewHolder(mBinding.root){
        fun setIngredient(mIngredient: IngredientsModel) {
            mBinding.ingredientModel = mIngredient
        }
    }
}