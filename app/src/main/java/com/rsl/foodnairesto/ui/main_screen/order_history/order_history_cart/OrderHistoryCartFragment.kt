package com.rsl.foodnairesto.ui.main_screen.order_history.order_history_cart


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.Log.e
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.rsl.foodnairesto.App
import com.rsl.foodnairesto.R
import com.rsl.foodnairesto.data.database_download.models.ReportProductModel
import com.rsl.foodnairesto.databinding.FragmentOrderHistoryCartBinding
import com.rsl.foodnairesto.ui.main_screen.order_history.OrderHistoryViewModel
import com.rsl.foodnairesto.ui.main_screen.order_history.event.OrderHistoryListEvent
import com.rsl.foodnairesto.ui.main_screen.order_history.order_history_cart.adapter.OrderHistoryCartAdapter
import com.rsl.foodnairesto.utils.Animations
import com.rsl.foodnairesto.utils.InjectorUtils
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

        if (!App.isTablet)
            mID = OrderHistoryCartFragmentArgs.fromBundle(requireArguments()).mID

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
    var nCash=0.0
    var nUPI=0.0
    var nCard=0.0
    var nNetbanking=0.0


    private fun fetchReportData() {
        mOrderHistoryViewModel.getReportDataForMID(mID).observe(viewLifecycleOwner) {
            when {
                it != null -> {

                    mDiscountAmount = it.mDiscountAmount



                    mBinding.textViewOrderNoLabel.text = "Order NO: ${it.mCartNO}"
                    mBinding.textViewOrderTotal.text =
                        String.format(Locale.ENGLISH, "%.2f", it.mOrderTotal)
                    mBinding.textViewDiscount.text =
                        String.format(Locale.ENGLISH, "%.2f", mDiscountAmount)
                    mBinding.textViewTax.text = String.format(Locale.ENGLISH, "%.2f", it.mTaxAmount)
                    mBinding.textViewTaxLabel.text = "Tax (" + it.mTaxPercent + "%)"

                    when {
//                        it.mDiscountPercent > 0 -> mBinding.textViewDiscountLabel.text = "Discount (" + it.mDiscountPercent + "%)"
//                        else -> mBinding.textViewDiscountLabel.text = "Discount"
                        it.mDiscountPercent > 0 -> mBinding.textViewDiscountLabel.text =
                            "Discount (" + it.mDiscountPercent + "%)"
                        else -> mBinding.textViewDiscountLabel.text = "Discount"
                    }

                    for (i in 0 until it.paymentMethodsList.size) {
                        when (it.paymentMethodsList[i].paymentType) {
                            "CASH" -> {
                                nCash=it.paymentMethodsList[i].paymentAmount
                                mBinding.textNumberCash.text=String.format(Locale.ENGLISH, "%.2f", nCash)

                            }
                            "CARD" -> {
                                nCard=it.paymentMethodsList[i].paymentAmount
                                mBinding.textNumberCard.text=String.format(Locale.ENGLISH, "%.2f", nCard)
                            }
                            "UPI" -> {
                                nUPI=it.paymentMethodsList[i].paymentAmount
                                mBinding.textNumberUpi.text=String.format(Locale.ENGLISH, "%.2f", nUPI)
                            }
                            "NET BANKING" -> {
                                nNetbanking=it.paymentMethodsList[i].paymentAmount
                                mBinding.textNumberNetBanking.text=String.format(Locale.ENGLISH, "%.2f", nNetbanking)
                            }
                        }
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
                    mBinding.textViewSubTotal.text =
                        String.format(Locale.ENGLISH, "%.2f", mSubTotal)

                    if (it.deliverCharges > 0) {
                        mBinding.deliveryCharges.visibility = VISIBLE
                        mBinding.deliveryLabel.visibility = VISIBLE
                        mBinding.deliveryCharges.text =
                            String.format(Locale.ENGLISH, "%.2f", it.deliverCharges)
                    }
                    managePaymentViews()

                    val mProductList = ArrayList<ReportProductModel>()
                    mProductList.addAll(it.mProductList)
                    val mOrderProductAdapter = OrderHistoryCartAdapter(mProductList)
                    mBinding.recyclerViewOrderCart.adapter = mOrderProductAdapter
                    Animations.runLayoutAnimationFallDown(mBinding.recyclerViewOrderCart)
                }
            }
        }
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

        /////////////////////////////


        if (nCash == 0.0) {
            mBinding.textCash.visibility = View.GONE
            mBinding.textNumberCash.visibility = View.GONE
        } else {
            mBinding.textCash.visibility = View.VISIBLE
            mBinding.textNumberCash.visibility = View.VISIBLE
        }
        if (nCard == 0.0) {
            mBinding.textCard.visibility = View.GONE
            mBinding.textNumberCard.visibility = View.GONE
        } else {
            mBinding.textCard.visibility = View.VISIBLE
            mBinding.textNumberCard.visibility = View.VISIBLE
        }
        if (nUPI == 0.0) {
            mBinding.textUpi.visibility = View.GONE
            mBinding.textNumberUpi.visibility = View.GONE
        } else {
            mBinding.textUpi.visibility = View.VISIBLE
            mBinding.textNumberUpi.visibility = View.VISIBLE
        }
        if (nNetbanking == 0.0) {
            mBinding.textNetBanking.visibility = View.GONE
            mBinding.textNumberNetBanking.visibility = View.GONE
        } else {
            mBinding.textNetBanking.visibility = View.VISIBLE
            mBinding.textNumberNetBanking.visibility = View.VISIBLE
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
