package com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_3

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.database_download.models.GenericProducts
import com.rsl.youresto.databinding.RecyclerItemVariantsBinding
import org.greenrobot.eventbus.EventBus

@SuppressLint("LogNotTimber")
class Type3VariantsRecyclerAdapter(private val mVariantList: ArrayList<GenericProducts>) :
    RecyclerView.Adapter<Type3VariantsRecyclerAdapter.VariantsViewHolder>() {

    private var mViewsPopulated = false

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

        holder.mBinding.checkBoxVariant.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                onVariantChecked(mVariant, holder.adapterPosition)
        }

        holder.mBinding.checkBoxVariant.isChecked = mVariant.isSelected

        if (mVariantList.size == position + 1)
            mViewsPopulated = true
    }

    private fun onVariantChecked(mVariant: GenericProducts, position: Int) {
        for (i in 0 until mVariantList.size)
            mVariantList[i].isSelected = i == position

        EventBus.getDefault().post(mVariant)
        if (mViewsPopulated) {
            notifyDataSetChanged()
            mViewsPopulated = false
        }
    }

    inner class VariantsViewHolder(val mBinding: RecyclerItemVariantsBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mGenericProducts: GenericProducts) {
//            mBinding.genericProduct = mGenericProducts
        }

    }
}