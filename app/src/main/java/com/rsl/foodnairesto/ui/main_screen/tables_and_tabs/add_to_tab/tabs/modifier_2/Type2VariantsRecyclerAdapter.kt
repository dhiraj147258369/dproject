package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_2

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.data.database_download.models.IngredientsModel
import com.rsl.foodnairesto.databinding.RecyclerItemVariantsBinding
import org.greenrobot.eventbus.EventBus

@SuppressLint("LogNotTimber")
class Type2VariantsRecyclerAdapter (private val mVariantList: ArrayList<IngredientsModel>) :
    RecyclerView.Adapter<Type2VariantsRecyclerAdapter.VariantsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VariantsViewHolder {
        val mBinding =
            RecyclerItemVariantsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VariantsViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mVariantList.size
    }


    override fun onBindViewHolder(holder: VariantsViewHolder, position: Int) {
        val mVariant = mVariantList[holder.adapterPosition]
        holder.bind(mVariant)

        holder.mBinding.checkBoxVariant.setOnCheckedChangeListener(null)
        holder.mBinding.checkBoxVariant.isChecked = mVariant.isSelected

        holder.mBinding.checkBoxVariant.setOnCheckedChangeListener { _, _ ->
            onVariantChecked(holder.adapterPosition)
        }
    }

    private fun onVariantChecked(position: Int) {
        mVariantList[position].isSelected = !mVariantList[position].isSelected
        notifyItemChanged(position)
        EventBus.getDefault().post(mVariantList[position])
    }

    inner class VariantsViewHolder(val mBinding: RecyclerItemVariantsBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mGenericProducts: IngredientsModel) {
            mBinding.genericProduct = mGenericProducts
        }

    }

}