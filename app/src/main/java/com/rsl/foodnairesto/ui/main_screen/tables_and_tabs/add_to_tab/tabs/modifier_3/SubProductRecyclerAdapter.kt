package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_3

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.data.database_download.models.ProductModel
import com.rsl.foodnairesto.databinding.RecyclerItemSubProductBinding
import org.greenrobot.eventbus.EventBus

class SubProductRecyclerAdapter(private val mSubProductList: ArrayList<ProductModel>):
    RecyclerView.Adapter<SubProductRecyclerAdapter.SubProductViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubProductViewHolder {
        val mBinding = RecyclerItemSubProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubProductViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mSubProductList.size
    }

    override fun onBindViewHolder(holder: SubProductViewHolder, position: Int) {
        val mSubProduct = mSubProductList[holder.adapterPosition]
        holder.bind(mSubProduct)

        holder.mBinding.checkBoxVariant.setOnCheckedChangeListener(null)
        holder.mBinding.checkBoxVariant.isChecked = mSubProduct.isSelected

        if(mSubProduct.isSelected){
            EventBus.getDefault().post(mSubProduct)
        }

        holder.mBinding.checkBoxVariant.setOnCheckedChangeListener { _, _ ->
            onSubProductChecked(holder.adapterPosition)
        }
    }

    private fun onSubProductChecked(mPosition: Int){
        for (i in 0 until mSubProductList.size)
            mSubProductList[i].isSelected = i == mPosition

        notifyDataSetChanged()
    }

    class SubProductViewHolder(val mBinding: RecyclerItemSubProductBinding):RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mSubProduct: ProductModel){
            mBinding.productModel = mSubProduct
        }
    }
}