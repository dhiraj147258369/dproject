package com.rsl.youresto.ui.main_screen.kitchen_print.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.database_download.models.IngredientsModel
import com.rsl.youresto.databinding.RecyclerItemKotHeaderBinding
import com.rsl.youresto.databinding.RecyclerItemKotProductBinding
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import java.math.BigDecimal

class KitchenPrintAdapter(var mContext: Context, var mCartProductList: ArrayList<CartProductModel>, private val mServiceType: Int): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object{
        private const val CART_TYPE = 1
        private const val CART_TYPE_3 = 3
        private const val HEADER_TYPE = 2
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            HEADER_TYPE -> {
                val mBinding = RecyclerItemKotHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return KOTHeaderViewHolder(mBinding)
            }

            CART_TYPE -> {
                val mCartBinding = RecyclerItemKotProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return KOTProductViewHolder(mCartBinding)
            }

            else -> {
                val mDefaultBinding =
                    RecyclerItemKotProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return KOTProductViewHolder(mDefaultBinding)
            }
        }
    }

    override fun getItemCount(): Int {
        return mCartProductList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mPosition = holder.adapterPosition

        when (val itemViewType = getItemViewType(mPosition)) {
            CART_TYPE, CART_TYPE_3 -> {
                val mProductHolder = holder as KOTProductViewHolder

                val mProduct = mCartProductList[mPosition]
                mProductHolder.setProductModel(mProduct)

                var mSeats: String

                when {
                    mProduct.mAssignedSeats!!.size > 0 && mServiceType == SERVICE_DINE_IN -> {
                        mSeats = "("
                        for(i in 0 until mProduct.mAssignedSeats!!.size) {
                            mSeats =
                                if(i == mProduct.mAssignedSeats!!.size - 1) {
                                mSeats + "S" + mProduct.mAssignedSeats!![i].mSeatNO + ")"
                            } else {
                                mSeats + "S" + mProduct.mAssignedSeats!![i].mSeatNO + ", "
                            }
                        }
                        mProductHolder.mBinding.textViewSeatsKot.text = mSeats
                    }
                    else -> mProductHolder.mBinding.textViewSeatsKot.visibility = GONE
                }

                when {
                    itemViewType != CART_TYPE_3 -> {
                        val mIngredientsList = mProduct.mShowModifierList
                        if (mProduct.mSpecialInstruction != "") {
                            val mIngredientsModel = IngredientsModel(
                                "0", mProduct.mSpecialInstruction, mProduct.mSpecialInstructionPrice,
                                BigDecimal(1), "", "",
                                isSelected = true,
                                isCompulsory = false,
                                mSelectionType = 1,
                                mCartIngredientID = "0"
                            )
                            mIngredientsList!!.add(mIngredientsModel)
                        }
                        val mIngredientAdapter = KitchenPrintIngredientAdapter(mIngredientsList!!, mProduct.mProductQuantity)
                        mProductHolder.mBinding.recyclerViewIngredientsKot.adapter = mIngredientAdapter
                    }
                    else -> {
                        // For Type 3 Products
                        val mIngredientAdapter = KitchenPrintSubProductAdapter(mProduct.mSubProductsList, mProduct.mProductQuantity.toInt())
                        mProductHolder.mBinding.recyclerViewIngredientsKot.adapter = mIngredientAdapter
                    }
                }

            }
            else -> {
                val mHeaderHolder = holder as KOTHeaderViewHolder
                val mProduct = mCartProductList[mPosition]
                mHeaderHolder.setProductHeader(mProduct)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            mCartProductList[position].mProductName == "" -> HEADER_TYPE
            mCartProductList[position].mProductType == 3 -> CART_TYPE_3
            else -> CART_TYPE
        }
    }

    inner class KOTProductViewHolder(val mBinding: RecyclerItemKotProductBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setProductModel(mProduct: CartProductModel) {
            mBinding.cartProduct = mProduct
        }
    }

    inner class KOTHeaderViewHolder(private val mHeaderBinding: RecyclerItemKotHeaderBinding) :
        RecyclerView.ViewHolder(mHeaderBinding.root) {

        fun setProductHeader(mProduct: CartProductModel) {
            mHeaderBinding.cartProduct = mProduct
        }
    }
}