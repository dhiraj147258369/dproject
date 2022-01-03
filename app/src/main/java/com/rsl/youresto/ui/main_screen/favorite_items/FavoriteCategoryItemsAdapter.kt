package com.rsl.youresto.ui.main_screen.favorite_items

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.rsl.youresto.databinding.RecyclerItemFavoriteProductBinding
import com.rsl.youresto.ui.main_screen.favorite_items.event.FavoriteItemSelectOrDeSelectEvent
import com.rsl.youresto.ui.main_screen.favorite_items.model.FavoriteProductModel
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.RESTAURANT_PASSWORD
import com.rsl.youresto.utils.AppConstants.RESTAURANT_USER_NAME
import com.rsl.youresto.utils.BasicAuthorization
import org.greenrobot.eventbus.EventBus

class FavoriteCategoryItemsAdapter(private val mContext: Context, private val mFavoriteItemsList: ArrayList<FavoriteProductModel>):
    RecyclerView.Adapter<FavoriteCategoryItemsAdapter.ViewHolder>() {

    var mSharedPrefs: SharedPreferences = mContext.getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)
    private var mUserName: String = mSharedPrefs.getString(RESTAURANT_USER_NAME, "")!!
    private var mPassword: String = mSharedPrefs.getString(RESTAURANT_PASSWORD, "")!!

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mBinding: RecyclerItemFavoriteProductBinding = RecyclerItemFavoriteProductBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mFavoriteItemsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mProductModel = mFavoriteItemsList[holder.adapterPosition]
        holder.setProduct(mProductModel)

        marqueeProductName(holder)

        if (mProductModel.isSelected) {
            holder.mBinding.constraintLayoutAlpha.visibility = View.VISIBLE
            holder.mBinding.imageViewCheck.visibility = View.VISIBLE
        } else {
            holder.mBinding.constraintLayoutAlpha.visibility = GONE
            holder.mBinding.imageViewCheck.visibility = GONE
        }

        if (mProductModel.mProductImage.trim().isNotEmpty())
            loadImage(mProductModel.mProductImage, holder)
    }

    private fun marqueeProductName(holder: ViewHolder){
        holder.mBinding.textViewGroupName.ellipsize = TextUtils.TruncateAt.MARQUEE
        holder.mBinding.textViewGroupName.setSingleLine(true)
        holder.mBinding.textViewGroupName.marqueeRepeatLimit = -1
        holder.mBinding.textViewGroupName.isSelected = true
    }

    private fun loadImage(mImageURL: String, holder: ViewHolder) {
        val mAuth = LazyHeaders.Builder() // can be cached in a field and reused
            .addHeader("Authorization", BasicAuthorization(mUserName, mPassword))
            .build()

        Glide
            .with(mContext)
            .load(GlideUrl(mImageURL, mAuth)) // GlideUrl is created anyway so there's no extra objects allocated
            .into(holder.mBinding.imageViewGroupImage)
    }

    @SuppressLint("LogNotTimber")
    fun onProductSelected(favoriteModel: FavoriteProductModel) {
        Log.e(javaClass.simpleName,"onProductSelected")
        if(favoriteModel.isSelected) {
            favoriteModel.isSelected = false
            EventBus.getDefault().post(FavoriteItemSelectOrDeSelectEvent(false,favoriteModel))
        } else {
            favoriteModel.isSelected = true
            EventBus.getDefault().post(FavoriteItemSelectOrDeSelectEvent(true,favoriteModel))
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(val mBinding: RecyclerItemFavoriteProductBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setProduct(mProductModel: FavoriteProductModel ) {
            mBinding.favoriteModel = mProductModel
            mBinding.favoriteAdapter = this@FavoriteCategoryItemsAdapter
        }
    }
}