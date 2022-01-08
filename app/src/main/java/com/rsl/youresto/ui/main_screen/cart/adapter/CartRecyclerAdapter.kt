package com.rsl.youresto.ui.main_screen.cart.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.databinding.RecyclerItemCartBinding
import com.rsl.youresto.databinding.RecyclerItemCartHeaderBinding
import com.rsl.youresto.ui.main_screen.cart.EditCartProductClickEvent
import com.rsl.youresto.ui.main_screen.cart.event.DeleteCartProductEvent
import com.rsl.youresto.ui.main_screen.cart.event.UpdateCartProductQuantityEvent
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.LOCATION_SERVICE_TYPE
import com.rsl.youresto.utils.AppConstants.SEAT_SELECTION_ENABLED
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import java.math.BigDecimal


@SuppressLint("LogNotTimber")
class CartRecyclerAdapter(var mContext: Context, private var mCartProductList: ArrayList<CartProductModel>)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object{
        private const val CART_TYPE = 1
        private const val HEADER_TYPE = 2
        private const val CART_TYPE_3 = 3
    }

    private val mSharedPrefs: SharedPreferences = mContext.getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {
            HEADER_TYPE -> {
                val mBinding = RecyclerItemCartHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return CartHeaderViewHolder(mBinding)
            }

            CART_TYPE -> {
                val mCartBinding = RecyclerItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return CartViewHolder(mCartBinding)
            }

            else -> {
                val mDefaultBinding =
                    RecyclerItemCartBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return CartViewHolder(mDefaultBinding)
            }
        }
    }

    override fun getItemCount(): Int {
        return mCartProductList.size
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            TextUtils.isEmpty(mCartProductList[position].mProductName) -> HEADER_TYPE
            mCartProductList[position].mProductType == 3 -> CART_TYPE_3
            else -> CART_TYPE
        }
    }

    fun onCartProductNameClick(mCartModel: CartProductModel){

//        if (mCartModel.mKitchenPrintFlag == 1){
//            CustomToast.makeText(mContext as Activity, "Cannot Edit Product, Sent to Kitchen Already", Toast.LENGTH_SHORT).show()
//            e(javaClass.simpleName, "QuantityChange")
//            return
//        }

        EventBus.getDefault().post(EditCartProductClickEvent(mCartModel))
    }

    fun onQuantityChanged(mPosition: Int, mChangeType: Int, mCartModel: CartProductModel) {
        e(javaClass.simpleName, "onQuantityChanged: ${mCartModel.mGroupName}")
//        if (mCartModel.mKitchenPrintFlag == 1){
//            CustomToast.makeText(mContext as Activity, "Cannot Edit Product, Sent to Kitchen Already", Toast.LENGTH_SHORT).show()
//            e(javaClass.simpleName, "QuantityChange")
//            return
//        } else EventBus.getDefault().post(CheckNetworkCartEvent(mPosition, mChangeType, mCartModel))

        var mProductQty = mCartModel.mProductQuantity

        var addOnPrice = BigDecimal(0)
        mCartModel.mShowModifierList?.map { addOn -> addOnPrice += addOn.mIngredientPrice  }

        if(mChangeType == 0) {      // 0 -> quantity decrease
            if(mProductQty > BigDecimal(1)) {
                mProductQty--
                val mProductTotalPrice = (mCartModel.mProductUnitPrice + mCartModel.mSpecialInstructionPrice + addOnPrice)* mProductQty
                EventBus.getDefault().post(UpdateCartProductQuantityEvent(mCartModel.mID,mProductQty,mProductTotalPrice, mCartModel.mGroupName))
                mCartModel.mProductQuantity = mProductQty
                mCartModel.mProductTotalPrice = mProductTotalPrice
                notifyItemChanged(mPosition)
            }
        } else {                    // 1 -> quantity increase
            mProductQty++
            val mProductTotalPrice = (mCartModel.mProductUnitPrice + mCartModel.mSpecialInstructionPrice + addOnPrice)* mProductQty
            EventBus.getDefault().post(UpdateCartProductQuantityEvent(mCartModel.mID,mProductQty,mProductTotalPrice, mCartModel.mGroupName))
            mCartModel.mProductQuantity = mProductQty
            mCartModel.mProductTotalPrice = mProductTotalPrice
            notifyItemChanged(mPosition)
        }
    }

    fun onItemDelete(mPosition: Int, mCartModel: CartProductModel) {

        if (mCartModel.mKitchenPrintFlag == 1){
            CustomToast.makeText(mContext as Activity, "Cannot Delete Product, Sent to Kitchen Already", Toast.LENGTH_SHORT).show()
            return
        }

        EventBus.getDefault().post(DeleteCartProductEvent(mCartModel, mPosition, mCartModel.mGroupName))

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mPosition = holder.adapterPosition

        when (val itemViewType = getItemViewType(mPosition)) {
            CART_TYPE, CART_TYPE_3 -> {
                val mCartHolder = holder as CartViewHolder

                val mCart = mCartProductList[mPosition]
                mCartHolder.setCartModel(mCart, mPosition, holder)

                when {
                    !mSharedPrefs.getBoolean(SEAT_SELECTION_ENABLED, true) -> mCartHolder.mBinding.recyclerViewAssignedSeats.visibility = GONE
                }


                when {
                    itemViewType != CART_TYPE_3 ->
                        when {
                            mCart.mShowModifierList!!.size > 0 -> {
                                e(javaClass.simpleName,"mCart.mShowModifierList!!.size: ${mCart.mShowModifierList!!.size}")
                                val mModifierAdapter = CartModifierAdapter(mCart.mShowModifierList!!)
                                mCartHolder.mBinding.recyclerViewProductTypes.adapter = mModifierAdapter
                            }
                            else -> {
                                val mModifierAdapter = CartModifierAdapter(ArrayList())
                                mCartHolder.mBinding.recyclerViewProductTypes.adapter = mModifierAdapter
                            }
                        }
                }

                when (itemViewType) {
                    CART_TYPE_3 -> {
                        val mSubProductAdapter = CartSubProductAdapter(mCart.mSubProductsList)
                        e(javaClass.simpleName, "onBindViewHolder: ${mCart.mSubProductsList.size}")
                        mCartHolder.mBinding.recyclerViewProductTypes.adapter = mSubProductAdapter
                    }
                }

                when (SERVICE_DINE_IN) {
                    mSharedPrefs.getInt(LOCATION_SERVICE_TYPE,0) -> {
                        val mSeatAdapter = CartSeatAdapter(mCart.mAssignedSeats!!)
                        mCartHolder.mBinding.recyclerViewAssignedSeats.adapter = mSeatAdapter
                    }
                }

                var mModifierPrice = BigDecimal(0)

                when {
                    mCart.mShowModifierList != null && mCart.mShowModifierList!!.size > 0 -> for (i in 0 until mCart.mShowModifierList!!.size){
                        mModifierPrice += mCart.mShowModifierList!![i].mIngredientPrice * mCart.mShowModifierList!![i].mIngredientQuantity
                    }
                }

                mCartHolder.mBinding.textViewCartProductPrice.text =
                    String.format("%.2f",mCart.mProductUnitPrice - mModifierPrice)

            }
            else -> {
                val mHeaderHolder = holder as CartHeaderViewHolder
                val mCart = mCartProductList[mPosition]
                mHeaderHolder.setCartHeader(mCart)
            }
        }
    }

    inner class CartViewHolder(val mBinding: RecyclerItemCartBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setCartModel(mCart: CartProductModel, position: Int, cartHolder: CartViewHolder) {
//            mCart.mProductTotalPrice = mCart.mProductUnitPrice * mCart.mProductQuantity
            mBinding.cartModel = mCart
            mBinding.cartAdapter = this@CartRecyclerAdapter
            mBinding.position = position
            mBinding.cartHolder = cartHolder

        }
    }

    inner class CartHeaderViewHolder(private val mHeaderBinding: RecyclerItemCartHeaderBinding) :
        RecyclerView.ViewHolder(mHeaderBinding.root) {

        fun setCartHeader(mCart: CartProductModel) {
            mHeaderBinding.cartModel = mCart
        }
    }
}