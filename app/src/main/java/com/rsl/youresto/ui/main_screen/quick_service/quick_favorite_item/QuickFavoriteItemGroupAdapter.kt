package com.rsl.youresto.ui.main_screen.quick_service.quick_favorite_item

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.database_download.models.FavoriteItemsModel
import com.rsl.youresto.databinding.RecyclerItemQuickServiceFavoriteProductBinding
import com.rsl.youresto.utils.Utils
@SuppressLint("LogNotTimber")
class QuickFavoriteItemGroupAdapter(
    private val mContext: Context,
    private val mFavoriteList: ArrayList<FavoriteItemsModel>
) : RecyclerView.Adapter<QuickFavoriteItemGroupAdapter.GroupViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val mBinding = RecyclerItemQuickServiceFavoriteProductBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return GroupViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mFavoriteList.size
    }


    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val mFavoriteItemModel = mFavoriteList[holder.adapterPosition]
        holder.setModel(mFavoriteItemModel)

        when {
            mFavoriteItemModel.mProductArrayList.size <= 4 -> {
                val mLayoutManager = GridLayoutManager(mContext, 1, GridLayoutManager.HORIZONTAL, false)
                holder.mBinding.recyclerViewFavoriteProducts.layoutManager = mLayoutManager

//                val params = holder.itemView.layoutParams
//                params.height = Utils.dpToPx(140)
                //holder.itemView.layoutParams = params
            }
            mFavoriteItemModel.mProductArrayList.size <= 8 -> {
                val mLayoutManager = GridLayoutManager(mContext, 2, GridLayoutManager.HORIZONTAL, false)
                holder.mBinding.recyclerViewFavoriteProducts.layoutManager = mLayoutManager

//                val params = holder.itemView.layoutParams
//                params.height = Utils.dpToPx(230)
               // holder.itemView.layoutParams = params
            }
            mFavoriteItemModel.mProductArrayList.size > 8 -> {
                val mLayoutManager = GridLayoutManager(mContext, 3, GridLayoutManager.HORIZONTAL, false)
                holder.mBinding.recyclerViewFavoriteProducts.layoutManager = mLayoutManager

//                val params = holder.itemView.layoutParams
//                params.height = Utils.dpToPx(330)
              //  holder.itemView.layoutParams = params
            }
        }

        val mProductList = mFavoriteItemModel.mProductArrayList

        Log.e(javaClass.simpleName,"mProductList.size: " + mProductList.size)

        val mProductAdapter =
            QuickFavoriteProductAdapter(mProductList, mContext)
        holder.mBinding.recyclerViewFavoriteProducts.adapter = mProductAdapter
    }

    class GroupViewHolder(val mBinding: RecyclerItemQuickServiceFavoriteProductBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setModel(mModel: FavoriteItemsModel) {
            mBinding.favoriteGroupModel = mModel
        }
    }
}