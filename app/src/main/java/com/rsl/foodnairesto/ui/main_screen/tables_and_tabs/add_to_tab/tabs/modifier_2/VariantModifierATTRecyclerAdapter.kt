package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.add_to_tab.tabs.modifier_2

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Log.e
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.data.database_download.models.IngredientsModel
import com.rsl.foodnairesto.databinding.RecyclerItemModifierMultipleBinding
import com.rsl.foodnairesto.databinding.RecyclerItemModifierSingleBinding
import com.rsl.foodnairesto.utils.AppConstants.MULTIPLE_SELECTION_COUNT
import com.rsl.foodnairesto.utils.AppConstants.MY_PREFERENCES
import com.rsl.foodnairesto.utils.custom_views.CustomToast
import org.greenrobot.eventbus.EventBus
import java.math.BigDecimal

@SuppressLint("LogNotTimber")
class VariantModifierATTRecyclerAdapter(
    private val mContext: Context,
    private val mViewType: Int,
    private val mMultipleSelectionCount: Int,
    private val mModifierList: ArrayList<IngredientsModel>,
    private val mSharedPreferences: SharedPreferences = mContext.getSharedPreferences(
        MY_PREFERENCES,
        MODE_PRIVATE
    )
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val SINGLE_SELECTION = 1
        private const val MULTIPLE_SELECTION = 2
    }

    private var mLocalSingleSelection = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (mViewType) {
            SINGLE_SELECTION -> singleBinding(parent)
            MULTIPLE_SELECTION -> {
                val mBinding =
                    RecyclerItemModifierMultipleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return MultipleModifierViewHolder(mBinding)
            }
            else -> singleBinding(parent)
        }
    }

    private fun singleBinding(parent: ViewGroup): RecyclerView.ViewHolder{
        val mBinding =
            RecyclerItemModifierSingleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
       return SingleModifierViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mModifierList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val mIngredient = mModifierList[holder.adapterPosition]

        if (mIngredient.isSelected){
            EventBus.getDefault().post(ModifierClickedEvent())
        }

        val itemViewType = getItemViewType(holder.adapterPosition)

        if (itemViewType == SINGLE_SELECTION) {
            val singleHolder = holder as SingleModifierViewHolder
            singleHolder.bind(mIngredient)

            singleHolder.mBinding.checkBoxIngredient.setOnCheckedChangeListener(null)
            singleHolder.mBinding.checkBoxIngredient.isChecked = mIngredient.isSelected
            singleHolder.mBinding.checkBoxIngredient.setOnCheckedChangeListener{_, _ ->
                onModifierChecked(singleHolder.adapterPosition)
            }
        } else {
            val multipleHolder = holder as MultipleModifierViewHolder
            multipleHolder.bind(mIngredient, holder.adapterPosition)


            holder.mBinding.textViewTotalPrice.text =
            mContext.getString(R.string.string_currency_sign) + String.format("%.2f",(mIngredient.mIngredientPrice * mIngredient.mIngredientQuantity))

            holder.mBinding.textViewUnitPrice.text =
                mContext.getString(R.string.string_currency_sign) + String.format("%.2f",(mIngredient.mIngredientPrice))
        }
    }

    private fun onModifierChecked(position: Int){

        if (mLocalSingleSelection < 1)
            mLocalSingleSelection++

        for (i in 0 until mModifierList.size) {
            mModifierList[i].isSelected = i == position
            if(i == position) mModifierList[i].mIngredientQuantity = BigDecimal(1)
        }

        notifyDataSetChanged()

    }


    fun onPlusClicked(mIngredient: IngredientsModel, mPosition: Int) {

        var mModifierQuantity = mSharedPreferences.getInt(MULTIPLE_SELECTION_COUNT, 0)

        e(javaClass.simpleName, "onPlusClicked mModifierQuantity: $mModifierQuantity")

        if (mModifierQuantity < mMultipleSelectionCount) {
            mIngredient.isSelected = true
            mIngredient.mIngredientQuantity = mIngredient.mIngredientQuantity + BigDecimal(1)
            mModifierQuantity++

            val mEditor = mSharedPreferences.edit()
            mEditor.putInt(MULTIPLE_SELECTION_COUNT, mModifierQuantity)
            mEditor.apply()

            notifyItemChanged(mPosition)
        }else
            CustomToast.makeText(
                mContext as Activity,
                "Cannot add more than $mMultipleSelectionCount toppings",
                Toast.LENGTH_SHORT
            ).show()

    }

    fun onMinusClicked(mIngredient: IngredientsModel, mPosition: Int) {

        var mModifierQuantity = mSharedPreferences.getInt(MULTIPLE_SELECTION_COUNT, 0)

        if (mIngredient.mIngredientQuantity > BigDecimal(0)) {
            mIngredient.mIngredientQuantity = mIngredient.mIngredientQuantity - BigDecimal(1)
            mModifierQuantity--

            val mEditor = mSharedPreferences.edit()
            mEditor.putInt(MULTIPLE_SELECTION_COUNT, mModifierQuantity)
            mEditor.apply()

            if (mIngredient.mIngredientQuantity == BigDecimal(0))
                mIngredient.isSelected = false
        }
        notifyItemChanged(mPosition)

        EventBus.getDefault().post(ModifierClickedEvent())

    }

    override fun getItemViewType(position: Int): Int {
        return if (mViewType == SINGLE_SELECTION) {
            SINGLE_SELECTION
        } else {
            MULTIPLE_SELECTION
        }
    }

    class SingleModifierViewHolder(val mBinding: RecyclerItemModifierSingleBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mIngredient: IngredientsModel) {
            mBinding.ingredient = mIngredient
        }

    }

    inner class MultipleModifierViewHolder(val mBinding: RecyclerItemModifierMultipleBinding) :
        RecyclerView.ViewHolder(mBinding.root) {

        fun bind(mIngredient: IngredientsModel, mPosition: Int) {
            mBinding.ingredient = mIngredient
            mBinding.adapter = this@VariantModifierATTRecyclerAdapter
            mBinding.position = mPosition
        }
    }
}