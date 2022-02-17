package com.rsl.foodnairesto.ui.main_screen.main_product_flow

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rsl.foodnairesto.data.main_product.models.CartBarModel
import com.rsl.foodnairesto.databinding.RecyclerItemCartBarItemBinding
import com.rsl.foodnairesto.ui.main_screen.main_product_flow.event.CartBarGroupEvent
import org.greenrobot.eventbus.EventBus

class CartBarGroupAdapter(private var mCartBarList: ArrayList<CartBarModel>): RecyclerView.Adapter<CartBarGroupAdapter.CartBarViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartBarViewHolder {
        val mBinding: RecyclerItemCartBarItemBinding = RecyclerItemCartBarItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return CartBarViewHolder(mBinding)
    }

    override fun getItemCount(): Int {
        return mCartBarList.size
    }

    override fun onBindViewHolder(holder: CartBarViewHolder, position: Int) {
        val mCartBarModel = mCartBarList[holder.adapterPosition]
        holder.setCartBarData(mCartBarModel)
    }

    inner class CartBarViewHolder(var mBinding: RecyclerItemCartBarItemBinding) : RecyclerView.ViewHolder(mBinding.root) {
        fun setCartBarData(mCartBarModel: CartBarModel) {
            mBinding.cartBarModel = mCartBarModel
            mBinding.adapter = this@CartBarGroupAdapter
        }
    }

    fun onGroupClicked(cartBarModel: CartBarModel) {
        EventBus.getDefault().post(CartBarGroupEvent(true,cartBarModel))
    }
}