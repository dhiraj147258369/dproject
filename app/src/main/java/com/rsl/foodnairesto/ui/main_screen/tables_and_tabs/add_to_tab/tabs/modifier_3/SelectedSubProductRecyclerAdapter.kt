package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_3

import android.annotation.SuppressLint
import android.util.Log.e
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.data.database_download.models.ProductModel
import com.rsl.foodnairesto.data.database_download.models.SubProductCategoryModel
import com.rsl.foodnairesto.databinding.RecyclerItemSelectedSubProductsBinding
import org.greenrobot.eventbus.EventBus

@SuppressLint("LogNotTimber")
class SelectedSubProductRecyclerAdapter(private val mSubProductCategoryList: ArrayList<SubProductCategoryModel>) :
    RecyclerView.Adapter<SelectedSubProductRecyclerAdapter.SelectedProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedProductViewHolder {
//        val mBinding =
//            RecyclerItemSelectedSubProductsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val mBinding=RecyclerItemSelectedSubProductsBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return SelectedProductViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mSubProductCategoryList.size
    }

    override fun onBindViewHolder(holder: SelectedProductViewHolder, position: Int) {
        val mSubProduct = mSubProductCategoryList[holder.adapterPosition]


        var mSelectedSubProduct = ""
        var mSelectedSubProductID = ""
        for (i in 0 until mSubProduct.mProductList.size) {
            if (mSubProduct.mProductList[i].isSelected) {
                mSelectedSubProduct = mSubProduct.mProductList[i].mProductName
                mSelectedSubProductID = mSubProduct.mProductList[i].mProductID
                e(javaClass.simpleName, "onBindViewHolder: $mSelectedSubProduct" )
                break
            }
        }



        val mSelectedProduct: String

        if(mSelectedSubProduct.isEmpty()) {
            mSelectedProduct = "${mSubProduct.mCategoryName}: Not Added"
            val mAdd = "Add"
            holder.mBinding.textViewChangeProduct.text = mAdd
        }else{
            mSelectedProduct =
                "${mSubProduct.mCategoryName}: " + mSelectedSubProduct
            val mAdd = "Change"
            holder.mBinding.textViewChangeProduct.text = mAdd
        }

        holder.mBinding.textViewSelectedProduct.text = mSelectedProduct

        holder.bind(mSubProduct.mProductList, mSelectedSubProductID)
    }

    fun onChangedClicked(){
        EventBus.getDefault().post(SubProductChangeEvent())
    }

    fun onSelectedProductClicked(mProductList: ArrayList<ProductModel>, mSelectedProduct: String){
        EventBus.getDefault().post(SelectedProductClickEvent(mProductList, mSelectedProduct))
    }

    inner class SelectedProductViewHolder(val mBinding: RecyclerItemSelectedSubProductsBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mProductList: ArrayList<ProductModel>, mSelectedProduct: String){
            mBinding.adapter = this@SelectedSubProductRecyclerAdapter
            mBinding.productList = mProductList
            mBinding.selectedproductID = mSelectedProduct
        }
    }
}