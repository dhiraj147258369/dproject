package com.rsl.youresto.ui.main_screen.order_history.order_history_cart


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.rsl.youresto.R
import com.rsl.youresto.data.database_download.models.ReportProductModel
import com.rsl.youresto.databinding.FragmentOrderHistoryCartBinding
import com.rsl.youresto.ui.main_screen.order_history.OrderHistoryViewModel
import com.rsl.youresto.ui.main_screen.order_history.event.OrderHistoryListEvent
import com.rsl.youresto.ui.main_screen.order_history.order_history_cart.adapter.OrderHistoryCartAdapter
import com.rsl.youresto.utils.Animations
import com.rsl.youresto.utils.InjectorUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("SetTextI18n")
class OrderHistoryCartFragment : Fragment() {

    private lateinit var mBinding: FragmentOrderHistoryCartBinding
    private lateinit var mOrderHistoryViewModel: OrderHistoryViewModel
    private var mID = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(inflater,R.layout.fragment_order_history_cart, container, false)
        val mView = mBinding.root

        val factory = InjectorUtils.provideOrderHistoryViewModelFactory(requireActivity())
        mOrderHistoryViewModel = ViewModelProviders.of(this, factory).get(OrderHistoryViewModel::class.java)

//        mID = OrderHistoryCartFragmentArgs.fromBundle(requireArguments()).mID

        fetchReportData()
        expandAndCollapsePaymentDetails()

        return mView
    }

    private var mSubTotal: Double = 0.toDouble()
    private var mTip = 0.0
    private var mCash = 0.0
    private var mChange = 0.0
    private var mCard = 0.0
    private var mWallet = 0.0
    var mDiscountAmount = 0.0

    private fun fetchReportData() {
        mOrderHistoryViewModel.getReportDataForMID(mID).observe(viewLifecycleOwner, {
            when {
                it != null -> {

                    mDiscountAmount = it.mDiscountAmount

                    mBinding.textViewOrderNoLabel.text = "Order NO: ${it.mCartNO}"
                    mBinding.textViewOrderTotal.text = String.format(Locale.ENGLISH,"%.2f",it.mOrderTotal)
                    mBinding.textViewDiscount.text = String.format(Locale.ENGLISH, "%.2f", mDiscountAmount)
                    mBinding.textViewTax.text = String.format(Locale.ENGLISH, "%.2f", it.mTaxAmount)
                    mBinding.textViewTaxLabel.text = "Tax (" + it.mTaxPercent + "%)"

                    when {
                        it.mDiscountPercent > 0 -> mBinding.textViewDiscountLabel.text = "Discount (" + it.mDiscountPercent + "%)"
                        else -> mBinding.textViewDiscountLabel.text = "Discount"
                    }

                    for (i in 0 until it.mPaymentList.size) {
                        when (it.mPaymentList[i].mPaymentMethodType) {
                            1 -> {
                                mCash += it.mPaymentList[i].mCashAmount
                                mChange += it.mPaymentList[i].mChangeAmount
                            }
                            2 -> mCard += it.mPaymentList[i].mAmount
                            3 -> mWallet += it.mPaymentList[i].mAmount
                            4 -> mTip += it.mPaymentList[i].mAmount
                        }
                    }

                    mSubTotal = it.mOrderTotal - it.mTaxAmount - mTip + it.mDiscountAmount

                    mBinding.textViewTip.text = String.format(Locale.ENGLISH, "%.2f", mTip)
                    mBinding.textViewCash.text = String.format(Locale.ENGLISH, "%.2f", mCash)
                    mBinding.textViewChange.text = String.format(Locale.ENGLISH, "%.2f", mChange)
                    mBinding.textViewCard.text = String.format(Locale.ENGLISH, "%.2f", mCard)
                    mBinding.textViewWallet.text = String.format(Locale.ENGLISH, "%.2f", mWallet)
                    mBinding.textViewSubTotal.text = String.format(Locale.ENGLISH, "%.2f", mSubTotal)

                    managePaymentViews()

                    val mProductList = ArrayList<ReportProductModel>()
                    mProductList.addAll(it.mProductList)
                    val mOrderProductAdapter = OrderHistoryCartAdapter(mProductList)
                    mBinding.recyclerViewOrderCart.adapter = mOrderProductAdapter
                    Animations.runLayoutAnimationFallDown(mBinding.recyclerViewOrderCart)
                }
            }
        })
    }

    private fun expandAndCollapsePaymentDetails() {
        when (mBinding.constraintLayout2.visibility) {
            View.VISIBLE -> {
                mBinding.imageViewArrowDragUp.visibility = View.INVISIBLE
                mBinding.imageViewArrowDragDown.visibility = View.VISIBLE
            }
            else -> {
                mBinding.imageViewArrowDragUp.visibility = View.VISIBLE
                mBinding.imageViewArrowDragDown.visibility = View.INVISIBLE
            }
        }

        mBinding.imageViewArrowDragUp.setOnClickListener {
            mBinding.constraintLayout2.visibility = View.VISIBLE
            mBinding.imageViewArrowDragUp.visibility = View.INVISIBLE
            mBinding.imageViewArrowDragDown.visibility = View.VISIBLE
        }
        mBinding.imageViewArrowDragDown.setOnClickListener {
            mBinding.constraintLayout2.visibility = View.GONE
            mBinding.imageViewArrowDragUp.visibility = View.VISIBLE
            mBinding.imageViewArrowDragDown.visibility = View.INVISIBLE
        }
    }

    @SuppressLint("LogNotTimber")
    private fun managePaymentViews() {
        e(javaClass.simpleName,"cash $mCash")

        if (mCash == 0.0) {
            mBinding.textViewCash.visibility = View.GONE
            mBinding.textViewCashLabel.visibility = View.GONE
            mBinding.textViewChange.visibility = View.GONE
            mBinding.textViewChangeLabel.visibility = View.GONE
        } else {
            mBinding.textViewCash.visibility = View.VISIBLE
            mBinding.textViewCashLabel.visibility = View.VISIBLE
            mBinding.textViewChange.visibility = View.VISIBLE
            mBinding.textViewChangeLabel.visibility = View.VISIBLE
        }
        if (mCard == 0.0) {
            mBinding.textViewCard.visibility = View.GONE
            mBinding.textViewCardLabel.visibility = View.GONE
        } else {
            mBinding.textViewCard.visibility = View.VISIBLE
            mBinding.textViewCardLabel.visibility = View.VISIBLE
        }
        if (mWallet == 0.0) {
            mBinding.textViewWallet.visibility = View.GONE
            mBinding.textViewWalletLabel.visibility = View.GONE
        } else {
            mBinding.textViewWallet.visibility = View.VISIBLE
            mBinding.textViewWalletLabel.visibility = View.VISIBLE
        }
        if (mTip == 0.0) {
            mBinding.textViewTip.visibility = View.GONE
            mBinding.textViewTipLabel.visibility = View.GONE
        } else {
            mBinding.textViewTip.visibility = View.VISIBLE
            mBinding.textViewTipLabel.visibility = View.VISIBLE
        }
        if (mDiscountAmount == 0.0) {
            mBinding.textViewDiscount.visibility = View.GONE
            mBinding.textViewDiscountLabel.visibility = View.GONE
        } else {
            mBinding.textViewDiscount.visibility = View.VISIBLE
            mBinding.textViewDiscountLabel.visibility = View.VISIBLE
        }
    }

    @Subscribe
    fun onOrderClick(mEvent: OrderHistoryListEvent) {
        if (mEvent.mResult) {
            mID = mEvent.mOrder.id
            fetchReportData()
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
    }
}
