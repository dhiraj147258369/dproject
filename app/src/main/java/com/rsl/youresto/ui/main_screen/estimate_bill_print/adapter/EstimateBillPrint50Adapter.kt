package com.rsl.youresto.ui.main_screen.estimate_bill_print.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.database_download.models.IngredientsModel
import com.rsl.youresto.databinding.RecyclerItemProductEstimateBillBinding
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import java.math.BigDecimal

class EstimateBillPrint50Adapter(var mCartList: ArrayList<CartProductModel>, val mOrderType: Int) :
    RecyclerView.Adapter<EstimateBillPrint50Adapter.Estimate50ViewHolder>() {

    companion object{
        private const val CART_TYPE_1_OR_2 = 1
        private const val CART_TYPE_3 = 3
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Estimate50ViewHolder {
        val mBinding =
            RecyclerItemProductEstimateBillBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Estimate50ViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mCartList.size
    }

    override fun onBindViewHolder(holder: Estimate50ViewHolder, position: Int) {
        val mCart = mCartList[holder.adapterPosition]
        holder.setCart(mCart)

        var mSeats: String

        when {
            mCart.mAssignedSeats!!.size > 0 && mOrderType == SERVICE_DINE_IN -> {
                mSeats = "("
                for (i in 0 until mCart.mAssignedSeats!!.size) {
                    mSeats = when (i) {
                        mCart.mAssignedSeats!!.size - 1 -> mSeats + "S" + mCart.mAssignedSeats!![i].mSeatNO + ")"
                        else -> mSeats + "S" + mCart.mAssignedSeats!![i].mSeatNO + ", "
                    }
                }
                holder.mBinding.textViewSeatsMainBill.text = mSeats
            }
            else -> holder.mBinding.textViewSeatsMainBill.visibility = View.GONE
        }

        when (mCart.mProductType) {
            3 -> {
                val mSubProductAdapter = EstimateBillPrint50CartSubProductAdapter(mCart.mSubProductsList)
                holder.mBinding.recyclerViewIngredientsMainBill.adapter = mSubProductAdapter

            }
            else -> {
                val mIngredientsList = mCart.mShowModifierList
                when {
                    mCart.mSpecialInstruction != "" -> {
                        val mIngredientsModel = IngredientsModel(
                            "0", mCart.mSpecialInstruction, mCart.mSpecialInstructionPrice,
                            BigDecimal(1), "", "",
                            isSelected = true,
                            isCompulsory = false,
                            mSelectionType = 1,
                            mCartIngredientID = "0"
                        )
                        mIngredientsList!!.add(mIngredientsModel)
                    }
                }

                val mIngredientAdapter =
                    EstimateBillPrint50IngredientAdapter(
                        mIngredientsList!!,
                        mCart.mProductQuantity
                    )
                holder.mBinding.recyclerViewIngredientsMainBill.adapter = mIngredientAdapter
            }
        }

        var mModifierPrice = BigDecimal(0)

        when {
            mCart.mShowModifierList != null && mCart.mShowModifierList!!.size > 0 -> for (i in 0 until mCart.mShowModifierList!!.size) {
                mModifierPrice += mCart.mShowModifierList!![i].mIngredientPrice * mCart.mShowModifierList!![i].mIngredientQuantity
            }
        }

        holder.mBinding.textViewPriceLabel.text =
            String.format(
                "%.2f",
                mCart.mProductUnitPrice - (mModifierPrice - (mCart.mSpecialInstructionPrice))
            )
    }

    override fun getItemViewType(position: Int): Int {
        return if (mCartList[position].mProductType == 3) {
            CART_TYPE_3
        } else {
            CART_TYPE_1_OR_2
        }
    }

    inner class Estimate50ViewHolder(var mBinding: RecyclerItemProductEstimateBillBinding) :
        RecyclerView.ViewHolder(mBinding.root) {
        fun setCart(mCart: CartProductModel) {
            mBinding.cartProductModel = mCart
        }
    }
}