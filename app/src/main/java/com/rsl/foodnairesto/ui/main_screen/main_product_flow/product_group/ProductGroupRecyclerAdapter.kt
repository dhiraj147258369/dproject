package com.rsl.foodnairesto.ui.main_screen.main_product_flow.product_group

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.rsl.foodnairesto.data.database_download.models.ProductGroupModel
import com.rsl.foodnairesto.databinding.RecyclerItemProductGroupBinding
import com.rsl.foodnairesto.utils.AppConstants
import com.rsl.foodnairesto.utils.AppConstants.LOCATION_SERVICE_TYPE
import com.rsl.foodnairesto.utils.AppConstants.MY_PREFERENCES
import com.rsl.foodnairesto.utils.AppConstants.QUICK_SERVICE_FRAGMENT_TAB_SELECTED
import com.rsl.foodnairesto.utils.AppConstants.RESTAURANT_PASSWORD
import com.rsl.foodnairesto.utils.AppConstants.RESTAURANT_USER_NAME
import com.rsl.foodnairesto.utils.BasicAuthorization
import org.greenrobot.eventbus.EventBus

class ProductGroupRecyclerAdapter(private val mGroupList: ArrayList<ProductGroupModel>, private val mContext : Context) :
    RecyclerView.Adapter<ProductGroupRecyclerAdapter.ProductGroupViewHolder>() {

    private var mUserName: String? = null
    private var mPassword: String? = null
    private var mSharedPrefs: SharedPreferences? = null

    init {
        mSharedPrefs = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        mUserName = mSharedPrefs!!.getString(RESTAURANT_USER_NAME, "")
        mPassword = mSharedPrefs!!.getString(RESTAURANT_PASSWORD, "")
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductGroupViewHolder {
        val mBinding = RecyclerItemProductGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProductGroupViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mGroupList.size
    }

    override fun onBindViewHolder(holder: ProductGroupViewHolder, position: Int) {
        val mGroup = mGroupList[holder.adapterPosition]
        holder.bind(mGroup)

        loadImage(mGroup.mGroupImageURL, holder)
        marqueeProductName(holder)
    }

    fun onGroupClicked(mGroup: ProductGroupModel){
        if(mSharedPrefs!!.getInt(LOCATION_SERVICE_TYPE, 0) == AppConstants.SERVICE_QUICK_SERVICE) {
            mSharedPrefs!!.edit().putInt(QUICK_SERVICE_FRAGMENT_TAB_SELECTED, 1).apply()
        }
        EventBus.getDefault().post(mGroup)
    }

    private fun marqueeProductName(holder: ProductGroupViewHolder){
        holder.mBinding.textViewProductGroupName.ellipsize = TextUtils.TruncateAt.MARQUEE
        holder.mBinding.textViewProductGroupName.setSingleLine(true)
        holder.mBinding.textViewProductGroupName.marqueeRepeatLimit = -1
        holder.mBinding.textViewProductGroupName.isSelected = true
    }

    inner class ProductGroupViewHolder(val mBinding : RecyclerItemProductGroupBinding) : RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mGroup:ProductGroupModel){
            mBinding.group = mGroup
            mBinding.adapter = this@ProductGroupRecyclerAdapter
        }
    }

    private fun loadImage(mImageURL: String, holder: ProductGroupViewHolder) {

        if(mImageURL.isNotEmpty()){
            val mAuth = LazyHeaders.Builder() // can be cached in a field and reused
                .addHeader("Authorization", BasicAuthorization(mUserName, mPassword))
                .build()

            Glide
                .with(mContext)
                .load(GlideUrl(mImageURL, mAuth)) // GlideUrl is created anyway so there's no extra objects allocated
                .into(holder.mBinding.imageViewProductGroup)
        }


    }
}