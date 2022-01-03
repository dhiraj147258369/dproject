package com.rsl.youresto.ui.main_screen.tables_and_tabs.add_to_tab.tabs

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.database_download.models.AllergenModel
import com.rsl.youresto.databinding.RecyclerItemProductAllergenBinding

class AllergenRecyclerAdapter(private val mAllergenList: ArrayList<AllergenModel>) :
    RecyclerView.Adapter<AllergenRecyclerAdapter.AllergenViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllergenViewHolder {
        val mBinding =
            RecyclerItemProductAllergenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AllergenViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mAllergenList.size
    }

    override fun onBindViewHolder(holder: AllergenViewHolder, position: Int) {
        val mAllergen = mAllergenList[holder.adapterPosition]
        holder.bind(mAllergen)
    }

    class AllergenViewHolder(val mBinding: RecyclerItemProductAllergenBinding) :RecyclerView.ViewHolder(mBinding.root) {
        fun bind(mAllergen: AllergenModel){
            mBinding.allergenModel = mAllergen
        }
    }
}