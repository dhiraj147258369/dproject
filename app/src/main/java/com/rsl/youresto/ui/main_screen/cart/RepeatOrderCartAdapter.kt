package com.rsl.youresto.ui.main_screen.cart

import android.content.Context
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.databinding.RecyclerItemRepeatOrderBinding
import com.rsl.youresto.ui.main_screen.cart.adapter.CartModifierAdapter
import com.rsl.youresto.ui.main_screen.cart.adapter.CartSeatAdapter
import com.rsl.youresto.ui.main_screen.cart.adapter.CartSubProductAdapter
import com.rsl.youresto.ui.main_screen.cart.event.RepeatOrderEvent
import com.rsl.youresto.utils.AppConstants
import org.greenrobot.eventbus.EventBus
import java.math.BigDecimal

class RepeatOrderCartAdapter(
    var mContext: Context,
    private var mCartProductList: ArrayList<CartProductModel>
) :
    RecyclerView.Adapter<RepeatOrderCartAdapter.RepeatOrderViewHolder>() {

    private val mSharedPrefs: SharedPreferences =
        mContext.getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RepeatOrderViewHolder {
        val mBinding = RecyclerItemRepeatOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RepeatOrderViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mCartProductList.size
    }

    fun getCartUpdatedList(): ArrayList<CartProductModel> {
        return mCartProductList
    }

    override fun onBindViewHolder(mCartHolder: RepeatOrderViewHolder, mPosition: Int) {
        val mCart = mCartProductList[mPosition]
        mCartHolder.setCartModel(mCart, mPosition)

        when {
            !mSharedPrefs.getBoolean(
                AppConstants.SEAT_SELECTION_ENABLED,
                true
            ) -> mCartHolder.mBinding.recyclerViewAssignedSeats.visibility =
                View.GONE
        }


        if (mCart.mProductType != 3) {
            if (mCart.mShowModifierList!!.size > 0) {
                val mModifierAdapter = CartModifierAdapter(mCart.mShowModifierList!!)
                mCartHolder.mBinding.recyclerViewProductTypes.adapter = mModifierAdapter
            } else {
                val mModifierAdapter = CartModifierAdapter(ArrayList())
                mCartHolder.mBinding.recyclerViewProductTypes.adapter = mModifierAdapter
            }
        }

        if (mCart.mProductType == 3) {
            val mSubProductAdapter = CartSubProductAdapter(mCart.mSubProductsList)
            mCartHolder.mBinding.recyclerViewProductTypes.adapter = mSubProductAdapter
        }

        when (AppConstants.SERVICE_DINE_IN) {
            mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE, 0) -> {
                val mSeatAdapter = CartSeatAdapter(mCart.mAssignedSeats!!)
                mCartHolder.mBinding.recyclerViewAssignedSeats.adapter = mSeatAdapter
            }
        }

        var mModifierPrice = BigDecimal(0)

        when {
            mCart.mShowModifierList != null && mCart.mShowModifierList!!.size > 0 -> for (i in 0 until mCart.mShowModifierList!!.size) {
                mModifierPrice += mCart.mShowModifierList!![i].mIngredientPrice * mCart.mShowModifierList!![i].mIngredientQuantity
            }
        }

        mCartHolder.mBinding.textViewCartProductPrice.text =
            String.format("%.2f", mCart.mProductUnitPrice - mModifierPrice)

        mCartHolder.mBinding.constraintOverlap.setOnClickListener {
            mCart.isSelectedForRepeatOrder = !mCart.isSelectedForRepeatOrder
            notifyDataSetChanged()
            EventBus.getDefault().post(RepeatOrderEvent(mCartProductList))
        }
    }

    inner class RepeatOrderViewHolder(val mBinding: RecyclerItemRepeatOrderBinding) :
        RecyclerView.ViewHolder(mBinding.root) {
        fun setCartModel(mCart: CartProductModel, position: Int) {
            mBinding.cartModel = mCart
            mBinding.repeatOrderCartAdapter = this@RepeatOrderCartAdapter
            mBinding.position = position
        }
    }
}