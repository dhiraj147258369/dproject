package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_2

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.data.database_download.models.IngredientCategoryModel
import com.rsl.foodnairesto.databinding.RecyclerItemVariantModifiersBinding

class VariantModifierCategoryATTRecyclerAdapter(
    private val mContext: Context,
    private val mModifierCategoryList: ArrayList<IngredientCategoryModel>,
    private val mMultipleSelectionCount: Int
) :
    RecyclerView.Adapter<VariantModifierCategoryATTRecyclerAdapter.VariantModifierCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariantModifierCategoryViewHolder {
        val mBinding = RecyclerItemVariantModifiersBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VariantModifierCategoryViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mModifierCategoryList.size
    }

    override fun onBindViewHolder(holder: VariantModifierCategoryViewHolder, position: Int) {
        val mCategory = mModifierCategoryList[holder.adapterPosition]
        holder.bind(mCategory)

        val mModifierAdapter =
            VariantModifierATTRecyclerAdapter(
                mContext, mCategory.mModifierSelection, mMultipleSelectionCount,
                mCategory.mIngredientsList!!
            )
        holder.mBinding.recyclerViewGeneric1.adapter = mModifierAdapter
    }

    inner class VariantModifierCategoryViewHolder(val mBinding: RecyclerItemVariantModifiersBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mCategory: IngredientCategoryModel) {
            mBinding.ingredientCategory = mCategory
        }
    }
}