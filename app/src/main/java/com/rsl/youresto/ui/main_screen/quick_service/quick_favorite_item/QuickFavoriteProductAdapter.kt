package com.rsl.youresto.ui.main_screen.quick_service.quick_favorite_item

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.rsl.youresto.databinding.RecyclerItemQuickServiceProductBinding
import com.rsl.youresto.ui.main_screen.favorite_items.model.FavoriteProductModel
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.RESTAURANT_PASSWORD
import com.rsl.youresto.utils.AppConstants.RESTAURANT_USER_NAME
import com.rsl.youresto.utils.BasicAuthorization
import org.greenrobot.eventbus.EventBus
import java.util.*

class QuickFavoriteProductAdapter(private val mProductList: ArrayList<FavoriteProductModel>, private val mContext: Context) :
    RecyclerView.Adapter<QuickFavoriteProductAdapter.FavoriteProductHolder>() {

    private val mSharedPrefs = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
    private val mUserName = mSharedPrefs.getString(RESTAURANT_USER_NAME, "")
    private val mPassword = mSharedPrefs.getString(RESTAURANT_PASSWORD, "")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteProductHolder {
        val mBinding = RecyclerItemQuickServiceProductBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return FavoriteProductHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mProductList.size
    }

    @SuppressLint("LogNotTimber")
    override fun onBindViewHolder(holder: FavoriteProductHolder, position: Int) {
        val mProduct = mProductList[holder.adapterPosition]
        holder.setProductModel(mProduct)

        holder.mBinding.textViewProductName.ellipsize = TextUtils.TruncateAt.MARQUEE
        holder.mBinding.textViewProductName.isSingleLine = true
        holder.mBinding.textViewProductName.marqueeRepeatLimit = -1
        holder.mBinding.textViewProductName.isSelected = true

        holder.mBinding.textViewProductPrice.text =
            String.format(Locale.ENGLISH, "%.2f", mProduct.dineInPrice)


        if (mProduct.mProductImage.isNotEmpty())
            loadImage(mProduct.mProductImage, holder)

        holder.itemView.setOnClickListener {
            mSharedPrefs.edit().putInt(AppConstants.QUICK_SERVICE_FRAGMENT_TAB_SELECTED,0).apply()
            Log.e(javaClass.simpleName, "${mSharedPrefs.getInt(AppConstants.QUICK_SERVICE_FRAGMENT_TAB_SELECTED, 0)}")
            EventBus.getDefault().post(mProduct)
        }
    }

    class FavoriteProductHolder(val mBinding: RecyclerItemQuickServiceProductBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setProductModel(mModel: FavoriteProductModel) {
            mBinding.productModel = mModel
        }
    }

    private fun loadImage(mImageURL: String, holder: FavoriteProductHolder) {

        val mAuth = LazyHeaders.Builder() // can be cached in a field and reused
            .addHeader("Authorization", BasicAuthorization(mUserName, mPassword))
            .build()

        Glide
            .with(mContext)
            .load(GlideUrl(mImageURL, mAuth)) // GlideUrl is created anyway so there's no extra objects allocated
            .into(holder.mBinding.imageViewProduct)

    }
}