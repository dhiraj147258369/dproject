package com.rsl.youresto.ui.main_screen.estimate_bill_print.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.database_download.models.IngredientsModel
import com.rsl.youresto.databinding.RecyclerItemIngredientEstimateBillBinding
import java.math.BigDecimal

class EstimateBillPrint50IngredientAdapter(var mIngredientList: ArrayList<IngredientsModel>, val mProductQuantity: BigDecimal):
    RecyclerView.Adapter<EstimateBillPrint50IngredientAdapter.IngredientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val mBinding = RecyclerItemIngredientEstimateBillBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return IngredientViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mIngredientList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val mIngredient = mIngredientList[holder.adapterPosition]
        holder.setIngredient(mIngredient)

        holder.mBinding.textViewQuantityLabel.text = (mIngredient.mIngredientQuantity * mProductQuantity).toString()
    }

    inner class IngredientViewHolder(var mBinding: RecyclerItemIngredientEstimateBillBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setIngredient(mIngredient: IngredientsModel) {
            mBinding.ingredientModel = mIngredient
        }
    }
}