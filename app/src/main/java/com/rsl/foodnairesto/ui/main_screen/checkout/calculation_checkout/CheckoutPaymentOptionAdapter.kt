package com.rsl.foodnairesto.ui.main_screen.checkout.calculation_checkout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.databinding.RecyclerItemCheckoutPaymentOptionBinding
import com.rsl.foodnairesto.ui.main_screen.checkout.calculation_checkout.event.CheckoutPaymentMethodEvent
import org.greenrobot.eventbus.EventBus

class CheckoutPaymentOptionAdapter(var mPaymentOption: ArrayList<CheckoutPaymentModel>)
    : RecyclerView.Adapter<CheckoutPaymentOptionAdapter.OptionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val mBinding = RecyclerItemCheckoutPaymentOptionBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OptionViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mPaymentOption.size
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        val mOption = mPaymentOption[holder.adapterPosition]
        holder.setOption(mOption)
    }

    fun onPaymentMethodClick(mPaymentOption: CheckoutPaymentModel) {
        EventBus.getDefault().post(CheckoutPaymentMethodEvent(mPaymentOption))
    }

    inner class OptionViewHolder(var mBinding: RecyclerItemCheckoutPaymentOptionBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setOption(mPaymentOption: CheckoutPaymentModel) {
            mBinding.option = mPaymentOption
            mBinding.adapter = this@CheckoutPaymentOptionAdapter
        }
    }
}