package com.rsl.youresto.ui.main_screen.checkout.bill_print

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.database_download.models.IngredientsModel
import com.rsl.youresto.databinding.RecyclerItemShareBillPrint50Binding
import com.rsl.youresto.ui.main_screen.estimate_bill_print.adapter.EstimateBillPrint50CartSubProductAdapter
import com.rsl.youresto.ui.main_screen.estimate_bill_print.adapter.EstimateBillPrint50IngredientAdapter
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("LogNotTimber")
class ShareBillPrint50CartAdapter(
    var mCartList: ArrayList<CartProductModel>,
    var mSeatList: ArrayList<Int>,
    val mOrderType: Int
) :
    RecyclerView.Adapter<ShareBillPrint50CartAdapter.ShareBillPrint50CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareBillPrint50CartViewHolder {
        val mBinding = RecyclerItemShareBillPrint50Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShareBillPrint50CartViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mCartList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ShareBillPrint50CartViewHolder, position: Int) {
        val mCart = mCartList[holder.adapterPosition]
        holder.bind(mCart)

        var mCheckSeat = BigDecimal(0)
        when {
            mCart.mAssignedSeats!!.size > 1 && mOrderType == SERVICE_DINE_IN -> {

                for (i in 0 until mCart.mAssignedSeats!!.size) {
                    for (j in mSeatList.indices) {
                        when (mCart.mAssignedSeats!![i].mSeatNO) {
                            mSeatList[j] -> mCheckSeat++
                        }
                    }
                }

                when (mCheckSeat) {
                    BigDecimal(0) -> holder.mBinding.textViewTotalLabel.text = String.format(Locale.ENGLISH, "%.2f", 0.0)
                    else ->
                        when {
                            BigDecimal(mCart.mAssignedSeats!!.size) <= mCheckSeat -> holder.mBinding.textViewTotalLabel.text =
                                String.format(Locale.ENGLISH, "%.2f", mCart.mProductTotalPrice)
                            else -> {
                                val mTotalPrice =
                                    (mCart.mProductTotalPrice.divide(BigDecimal(mCart.mAssignedSeats!!.size), 2, RoundingMode.DOWN)) * mCheckSeat
                                holder.mBinding.textViewTotalLabel.text = String.format(Locale.ENGLISH, "%.2f", mTotalPrice)
                            }
                        }
                }


            }
            else -> holder.mBinding.textViewTotalLabel.text = String.format(Locale.ENGLISH, "%.2f", mCart.mProductTotalPrice)
        }

        var mSeats: String

        when {
            mCart.mAssignedSeats!!.size > 0 && mOrderType == SERVICE_DINE_IN -> {
                mSeats = "("
                for (i in 0 until mCart.mAssignedSeats!!.size) {
                    mSeats =
                        when (i) {
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

        if (mCart.mShowModifierList != null && mCart.mShowModifierList!!.size > 0) {
            for (i in 0 until mCart.mShowModifierList!!.size) {
                mModifierPrice += mCart.mShowModifierList!![i].mIngredientPrice * mCart.mShowModifierList!![i].mIngredientQuantity
            }
        }

        holder.mBinding.textViewPriceLabel.text =
            String.format(
                "%.2f",
                mCart.mProductUnitPrice - (mModifierPrice - (mCart.mSpecialInstructionPrice))
            )

    }

    class ShareBillPrint50CartViewHolder(val mBinding: RecyclerItemShareBillPrint50Binding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mCart: CartProductModel) {
            mBinding.cartProductModel = mCart
        }
    }
}