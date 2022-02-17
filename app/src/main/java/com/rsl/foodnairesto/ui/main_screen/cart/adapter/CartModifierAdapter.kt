package com.rsl.foodnairesto.ui.main_screen.cart.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.data.database_download.models.IngredientsModel
import com.rsl.foodnairesto.databinding.RecyclerItemCartModifierBinding

class CartModifierAdapter(var mIngredientsList: ArrayList<IngredientsModel>) :
    RecyclerView.Adapter<CartModifierAdapter.ModifierViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModifierViewHolder {
        val mBinding = RecyclerItemCartModifierBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ModifierViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mIngredientsList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ModifierViewHolder, position: Int) {
        val mIngredient = mIngredientsList[holder.adapterPosition]

        holder.mBinding.textViewIngredientName.text = mIngredient.mIngredientName

        holder.mBinding.textViewIngredientPrice.text = String.format("%.2f", mIngredient.mIngredientPrice)
    }

    inner class ModifierViewHolder(var mBinding: RecyclerItemCartModifierBinding) : RecyclerView.ViewHolder(mBinding.root)
}