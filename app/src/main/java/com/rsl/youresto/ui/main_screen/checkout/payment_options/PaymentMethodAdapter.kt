package com.rsl.youresto.ui.main_screen.checkout.payment_options

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.youresto.data.database_download.models.PaymentMethodModel
import com.rsl.youresto.databinding.RecyclerItemPaymentMethodBinding
import org.greenrobot.eventbus.EventBus

class PaymentMethodAdapter(private var mMethodList: ArrayList<PaymentMethodModel>): RecyclerView.Adapter<PaymentMethodAdapter.MethodViewModel>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MethodViewModel {
        val mBinding = RecyclerItemPaymentMethodBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MethodViewModel(mBinding)
    }

    override fun getItemCount(): Int {
        return mMethodList.size
    }

    override fun onBindViewHolder(holder: MethodViewModel, position: Int) {
        val mMethod = mMethodList[holder.adapterPosition]
        holder.setMethod(mMethod)

        holder.mBinding.textViewPaymentMethodName.ellipsize = TextUtils.TruncateAt.MARQUEE
        holder.mBinding.textViewPaymentMethodName.marqueeRepeatLimit = -1
        holder.mBinding.textViewPaymentMethodName.isSelected = true
    }

    fun onMethodClick(mMethod: PaymentMethodModel) {
        EventBus.getDefault().post(mMethod)
    }

    inner class MethodViewModel(var mBinding: RecyclerItemPaymentMethodBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setMethod(mMethod: PaymentMethodModel) {
            mBinding.mMethod = mMethod
            mBinding.adapter = this@PaymentMethodAdapter
        }
    }
}