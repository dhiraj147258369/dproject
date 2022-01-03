package com.rsl.youresto.ui.main_screen.kitchen_print.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.database_download.models.IngredientsModel
import com.rsl.youresto.databinding.RecyclerItemKotIngredientBinding
import java.math.BigDecimal

class KitchenPrintIngredientAdapter(var mIngredientList: ArrayList<IngredientsModel>, val mProductQuantity: BigDecimal):
    RecyclerView.Adapter<KitchenPrintIngredientAdapter.IngredientViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val mBinding = RecyclerItemKotIngredientBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return IngredientViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mIngredientList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val mIngredient = mIngredientList[holder.adapterPosition]
        holder.setIngredient(mIngredient, mProductQuantity)
        holder.mBinding.textViewIngredientQuantity.text = (mIngredient.mIngredientQuantity * mProductQuantity).toString()
    }

    class IngredientViewHolder(var mBinding: RecyclerItemKotIngredientBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setIngredient(mIngredient: IngredientsModel, mProductQuantity: BigDecimal) {
            mBinding.mIngredient = mIngredient
            mBinding.productQuantity = mProductQuantity
        }
    }
}