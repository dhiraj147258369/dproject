package com.rsl.youresto.ui.main_screen.checkout.bill_print

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log.e
import android.view.View.*
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.rsl.youresto.R
import com.rsl.youresto.data.database_download.models.KitchenModel
import com.rsl.youresto.databinding.ActivityShareBillPrint50Binding
import com.rsl.youresto.ui.main_screen.cart.CartViewModel
import com.rsl.youresto.ui.main_screen.cart.CartViewModelFactory
import com.rsl.youresto.ui.main_screen.cart.NewCartViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModelFactory
import com.rsl.youresto.ui.main_screen.checkout.events.SeatPaymentCompleteEvent
import com.rsl.youresto.ui.main_screen.estimate_bill_print.adapter.EstimateBillPrint50Adapter
import com.rsl.youresto.utils.*
import com.rsl.youresto.utils.AppConstants.LOGGED_IN_SERVER_NAME
import com.rsl.youresto.utils.AppConstants.PAPER_SIZE_50
import com.rsl.youresto.utils.AppConstants.SELECTED_BILL_PRINTER_NAME
import com.rsl.youresto.utils.AppConstants.SELECTED_BILL_PRINTER_NETWORK_IP
import com.rsl.youresto.utils.AppConstants.SELECTED_BILL_PRINTER_NETWORK_PORT
import com.rsl.youresto.utils.AppConstants.SELECTED_LOCATION_NAME
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN_WITHOUT_SEATS
import com.rsl.youresto.utils.printer.PrinterUtil
import com.rsl.youresto.utils.printer_utils.AidlUtil
import org.greenrobot.eventbus.EventBus
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.util.*
import kotlin.math.roundToLong

@SuppressLint("LogNotTimber")
class ShareBillPrint50Activity : AppCompatActivity() {

    private lateinit var mBinding: ActivityShareBillPrint50Binding
    private lateinit var mCartViewModel: CartViewModel
    private lateinit var mCheckoutViewModel: CheckoutViewModel

    private lateinit var mSharedPrefs: SharedPreferences

    private var mOrderType: Int? = null
    private var mTableNO: Int? = null
    private var mTableID: String? = null
    private var mOrderNO: String? = null
    private var mCartID: String? = null
    private var mGroupName: String? = null
    private var mTaxPercentage = BigDecimal(0.0)

    private var mExecutor: AppExecutors? = null

    private val cartViewModel: NewCartViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setFinishOnTouchOutside(false)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_share_bill_print50)

        val cartFactory: CartViewModelFactory = InjectorUtils.provideCartViewModelFactory(applicationContext)
        mCartViewModel = ViewModelProviders.of(this, cartFactory).get(CartViewModel::class.java)

        val checkoutFactory: CheckoutViewModelFactory =
            InjectorUtils.provideCheckoutViewModelFactory(applicationContext)
        mCheckoutViewModel = ViewModelProviders.of(this, checkoutFactory).get(CheckoutViewModel::class.java)

        mExecutor = AppExecutors.getInstance()

        mSharedPrefs = getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        if (intent != null) {
            mOrderType = intent.getIntExtra(AppConstants.ORDER_TYPE, 0)
            mTableNO = intent.getIntExtra(AppConstants.TABLE_NO, 0)
            mTableID = intent.getStringExtra(AppConstants.TABLE_ID)
            mOrderNO = intent.getStringExtra(AppConstants.ORDER_NO)
            mCartID = intent.getStringExtra(AppConstants.API_CART_ID)
            mGroupName = intent.getStringExtra(AppConstants.GROUP_NAME)

//            if (!mSharedPrefs.getBoolean(AppConstants.SEAT_SELECTION_ENABLED, false)) {
//                mOrderType = SERVICE_DINE_IN_WITHOUT_SEATS
//                mGroupName = "Z"
//            }
//
//            if (mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE, 0) == SERVICE_QUICK_SERVICE){
//                mOrderType = SERVICE_QUICK_SERVICE
//            }
        }
        mOrderType = mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE, 0)

        mTableID = mSharedPrefs.getString(AppConstants.SELECTED_TABLE_ID, "") ?: ""

        setDineInPrintLayout()
    }

    override fun onResume() {
        super.onResume()

        this.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        this.window!!.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private var mCheckoutRowID = 0

    @SuppressLint("SetTextI18n")
    private fun setDineInPrintLayout() {
        setTaxLabel()

        mCartViewModel.getRestaurantDetails().observe(this, {
            when {
                it != null -> {
                    mBinding.textViewRestaurantName.text = it.restaurantName


                    Glide
                        .with(this)
                        .load(GlideUrl(it.imgUrl)) // GlideUrl is created anyway so there's no extra objects allocated
                        .into(mBinding.imageViewRestaurantLogo)

//                    mBinding.imageViewRestaurantLogo.setImageBitmap(
//                        ImageStorage.getImage(it.imgUrl)
//                    )

                    when {
                        it.address.isNotBlank() -> mBinding.textViewRestaurantAddress1.text = it.address
                        else -> mBinding.textViewRestaurantAddress1.visibility = GONE
                    }

//                    when {
//                        it.city.isNotBlank() -> mBinding.textViewRestaurantAddress2.text = it.city
//                        else -> mBinding.textViewRestaurantAddress2.visibility = GONE
//                    }

//                    when {
//                        it.mAddress3.isNotBlank() -> mBinding.textViewRestaurantAddress3.text = it.mAddress3
//                        else -> mBinding.textViewRestaurantAddress3.visibility = GONE
//                    }
                    mBinding.textViewRestaurantAddress3.visibility = GONE

                    when {
                        it.city.isNotBlank() -> mBinding.textViewRestaurantCity.text = it.city
                        else -> mBinding.textViewRestaurantCity.visibility = GONE
                    }

                    when {
                        it.postcode.isNotBlank() -> mBinding.textViewRestaurantPincode.text = it.postcode
                        else -> mBinding.textViewRestaurantPincode.visibility = GONE
                    }

                    when {
                        it.contactNumber.isNotBlank() -> mBinding.textViewRestaurantPhone.text = it.contactNumber
                        else -> mBinding.textViewRestaurantPhone.visibility = GONE
                    }

                    when {
                        it.email.isNotBlank() -> mBinding.textViewRestaurantEmail.text = it.email
                        else -> mBinding.textViewRestaurantEmail.visibility = GONE
                    }

//                    when {
//                        it.mReceiptMessage1.isNotBlank() -> mBinding.textViewMessage1.text = it.mReceiptMessage1
//                        else -> mBinding.textViewMessage1.visibility = GONE
//                    }
//
//                    when {
//                        it.mReceiptMessage2.isNotBlank() -> mBinding.textViewMessage2.text = it.mReceiptMessage2
//                        else -> mBinding.textViewMessage2.visibility = GONE
//                    }

                    mBinding.textViewMessage1.visibility = GONE
                    mBinding.textViewMessage2.visibility = GONE

                    val mServer = "Server: " + mSharedPrefs.getString(LOGGED_IN_SERVER_NAME, "")!!
                    mBinding.textViewServerName.text = mServer

                    val mLocation = "LOC: " + mSharedPrefs.getString(SELECTED_LOCATION_NAME, "")!!
                    mBinding.textViewLocation.text = mLocation

                    val mTime = "Time: " + Utils.getStringFromDate("dd/MM/yyyy HH:mm", Date())
                    mBinding.textViewTime.text = mTime

                    when (mOrderType) {
                        SERVICE_DINE_IN -> {
                            mBinding.textViewOrderType.text = "DINE IN"
                            mBinding.textViewTableNo.text = "Table: $mTableNO($mGroupName)"
                            mBinding.textViewTotalLabel.text = "Share"
                        }
                        SERVICE_DINE_IN_WITHOUT_SEATS -> {
                            mBinding.textViewOrderType.text = "DINE IN"
                            mBinding.textViewTableNo.text = "Table: $mTableNO($mGroupName)"
                            mBinding.textViewTotalLabel.text = "Total"
                        }
                        else -> {
                            mBinding.textViewOrderType.text = "Quick Service"
                            mBinding.textViewTableNo.visibility = GONE
                            mBinding.textViewTotalLabel.text = "Total"
                        }
                    }

                    mBinding.textViewOrderNo.text = "Order NO: $mOrderNO"


//                    when (mOrderType) {

                    e(javaClass.simpleName, "mOrderType: $mOrderType")
                    e(javaClass.simpleName, "mTableNO: $mTableNO!!")
                    e(javaClass.simpleName, "mGroupName: $mGroupName!!")

                    cartData()

//                    if(mOrderType == SERVICE_DINE_IN || mOrderType == SERVICE_DINE_IN_WITHOUT_SEATS){
//                            mCartViewModel.getCartData(mTableNO!!, mGroupName!!).observe(this,
//                                { cartList ->
//
//                                e(javaClass.simpleName, "cartList: ${cartList.size}")
//                        when {
//                            cartList.isNotEmpty() -> {
//                                mBinding.textViewGuestsNo.text = "Guest: ${cartList[0].mTotalGuestsCount}"
//
//                                mCheckoutViewModel.getCheckoutDataByTableAndGroup(mTableID!!, mGroupName!!)
//                                    .observe(this, { checkoutModel ->
//
//                                        when {
//                                            checkoutModel != null -> {
//
//                                                mCheckoutRowID = checkoutModel.mID
//
//                                                val mCheckoutTransactionList = checkoutModel.mCheckoutTransaction
//
//                                                when {
//                                                    mCheckoutTransactionList.size > 0 -> {
//
//                                                        val mCheckoutTransaction =
//                                                            mCheckoutTransactionList[mCheckoutTransactionList.size - 1]
//
//                                                        val mSeatList = mCheckoutTransaction.mSeatList
//
//                                                        val mFinalCartList = ArrayList<CartProductModel>()
//
//                                                        when {
//                                                            mSeatList.size > 0 -> for (i in 0 until cartList.size) {
//
//                                                                val mCartSeatList = cartList[i].mAssignedSeats
//
//                                                                for (j in 0 until mCartSeatList!!.size) {
//                                                                    for (k in 0 until mSeatList.size) {
//                                                                        when (mCartSeatList[j].mSeatNO) {
//                                                                            mSeatList[k] -> {
//                                                                                e(javaClass.simpleName, "Seats: ${mSeatList[k]}")
//                                                                                when {
//                                                                                    !mFinalCartList.contains(cartList[i]) -> mFinalCartList.add(cartList[i])
//                                                                                }
//                                                                            }
//                                                                        }
//                                                                    }
//                                                                }
//
//                                                            }
//                                                        }
//
//                                                        val mShareBillPrint50CartAdapter =
//                                                            ShareBillPrint50CartAdapter(
//                                                                mFinalCartList, mSeatList, mOrderType!!
//                                                            )
//                                                        mBinding.recyclerViewProducts.adapter = mShareBillPrint50CartAdapter
//
//                                                        mBinding.textViewSubTotalAmount.text =
//                                                            String.format(
//                                                                Locale.ENGLISH, "%.2f",
//                                                                mCheckoutTransaction.mSeatCartTotal
//                                                            )
//
//                                                        mBinding.textViewTaxAmount.text =
//                                                            String.format(Locale.ENGLISH, "%.2f", mCheckoutTransaction.mTaxAmount)
//
//                                                        when {
//                                                            mCheckoutTransaction.mDiscountAmount > BigDecimal(0) -> {
//                                                                mBinding.textViewDiscountLabel.visibility = VISIBLE
//                                                                mBinding.textViewDiscountAmount.visibility = VISIBLE
//
//                                                                mBinding.textViewDiscountAmount.text =
//                                                                    String.format(
//                                                                        Locale.ENGLISH,
//                                                                        "%.2f",
//                                                                        mCheckoutTransaction.mDiscountAmount
//                                                                    )
//
//                                                                mBinding.textViewSubTotalAfterDiscountAmount.visibility = VISIBLE
//                                                                mBinding.textViewSubTotalAfterDiscountLabel.visibility = VISIBLE
//
//                                                                mBinding.textViewSubTotalAfterDiscountAmount.text =
//                                                                    String.format(
//                                                                        Locale.ENGLISH, "%.2f",
//                                                                        mCheckoutTransaction.mSeatCartTotal - mCheckoutTransaction.mDiscountAmount
//                                                                    )
//                                                            }
//                                                        }
//
//                                                        when {
//                                                            mCheckoutTransaction.mTipAmount > BigDecimal(0) -> {
//                                                                mBinding.textViewTipAmount.visibility = VISIBLE
//                                                                mBinding.textViewTipLabel.visibility = VISIBLE
//
//                                                                mBinding.textViewTipAmount.text =
//                                                                    String.format(
//                                                                        Locale.ENGLISH,
//                                                                        "%.2f",
//                                                                        mCheckoutTransaction.mTipAmount
//                                                                    )
//                                                            }
//                                                        }
//
//                                                        val mTotalAmount = mCheckoutTransaction.mOrderTotal
//                                                        mBinding.textViewTotalAmount.text =
//                                                            String.format(Locale.ENGLISH, "%.2f", mTotalAmount)
//
//                                                        val mPaymentTransactionList = mCheckoutTransaction.mPaymentTransaction
//
//                                                        mCheckoutViewModel.getPaymentMethods()
//                                                            .observe(this, { paymentMethods ->
//
//                                                                for (i in 0 until paymentMethods.size) {
//
//                                                                    var mPaymentMethodTotal = BigDecimal(0.0)
//                                                                    var mChangeForCash = BigDecimal(0.0)
//
//                                                                    for (j in 0 until mPaymentTransactionList.size) {
//
//                                                                        when (paymentMethods[i].mPaymentMethodID) {
//                                                                            mPaymentTransactionList[j].mPaymentMethodID ->
//                                                                                when (TYPE_CASH) {
//                                                                                    paymentMethods[i].mPaymentMethodType -> {
//                                                                                        mPaymentMethodTotal += mPaymentTransactionList[j].mCash
//                                                                                        mChangeForCash += mPaymentTransactionList[j].mChange
//                                                                                    }
//                                                                                    else -> mPaymentMethodTotal += mPaymentTransactionList[j].mTransactionAmount
//                                                                                }
//                                                                        }
//
//                                                                    }
//
//                                                                    when {
//                                                                        paymentMethods[i].mPaymentMethodType == TYPE_CASH
//                                                                                && mPaymentMethodTotal > BigDecimal(0)
//                                                                        -> {
//                                                                            mBinding.textViewCashPaidLabel.visibility = VISIBLE
//                                                                            mBinding.textViewCashPaidAmount.visibility = VISIBLE
//
//                                                                            mBinding.textViewCashPaidAmount.text =
//                                                                                String.format("%.2f", mPaymentMethodTotal)
//
//                                                                            when {
//                                                                                mChangeForCash > BigDecimal(0) -> {
//                                                                                    mBinding.textViewChangeLabel.visibility = VISIBLE
//                                                                                    mBinding.textViewChangeAmount.visibility = VISIBLE
//                                                                                    mBinding.textViewChangeAmount.text =
//                                                                                        String.format("%.2f", mChangeForCash)
//                                                                                }
//                                                                            }
//
//                                                                        }
//                                                                        paymentMethods[i].mPaymentMethodType == TYPE_CARD
//                                                                                && mPaymentMethodTotal > BigDecimal(0)
//                                                                        -> {
//                                                                            mBinding.textViewCardPaidLabel.visibility = VISIBLE
//                                                                            mBinding.textViewCardPaidAmount.visibility = VISIBLE
//
//                                                                            mBinding.textViewCardPaidAmount.text =
//                                                                                String.format("%.2f", mPaymentMethodTotal)
//                                                                        }
//                                                                        paymentMethods[i].mPaymentMethodType == TYPE_WALLET
//                                                                                && mPaymentMethodTotal > BigDecimal(0)
//                                                                        -> {
//                                                                            mBinding.textViewWalletPaidLabel.visibility = VISIBLE
//                                                                            mBinding.textViewWalletPaidAmount.visibility = VISIBLE
//
//                                                                            mBinding.textViewWalletPaidAmount.text =
//                                                                                String.format("%.2f", mPaymentMethodTotal)
//                                                                        }
//                                                                        paymentMethods[i].mPaymentMethodType == TYPE_TIP &&
//                                                                                mPaymentMethodTotal > BigDecimal(0)
//                                                                        -> {
//                                                                            mBinding.textViewTipLabel.visibility = VISIBLE
//                                                                            mBinding.textViewTipAmount.visibility = VISIBLE
//
//                                                                            mBinding.textViewTipAmount.text =
//                                                                                String.format("%.2f", mPaymentMethodTotal)
//                                                                        }
//                                                                    }
//                                                                }
//
//                                                            })
//
//                                                        val mRemainingAmount = checkoutModel.mOrderTotal - checkoutModel.mAmountPaid
//
//                                                        when {
//                                                            mRemainingAmount > BigDecimal(0.05) -> {
//                                                                mBinding.textViewRemainingLabel.visibility = VISIBLE
//                                                                mBinding.textViewRemainingAmount.visibility = VISIBLE
//
//                                                                mBinding.textViewRemainingAmount.text =
//                                                                    String.format("%.2f", mRemainingAmount)
//                                                            }
//                                                        }
//
//                                                        val mSeatBuilder = StringBuilder()
//
//                                                        for (i in 0 until mSeatList.size)
//                                                            when (i) {
//                                                                mSeatList.size - 1 -> mSeatBuilder.append(mSeatList[i])
//                                                                else -> mSeatBuilder.append(mSeatList[i]).append(",")
//                                                            }
//
//                                                        mBinding.textViewPaidBy.visibility = VISIBLE
//                                                        mBinding.textViewPaidBy.text = "Paid By: $mSeatList"
//
//                                                        Handler().postDelayed({
//                                                            printBill()
//                                                        }, 100)
//
//                                                    }
//                                                }
//                                            }
//                                        }
//
//                                    })
//
//
//                            }
//                        }
//                    })
//                    }else{
//                        mCartViewModel.getCartDataWithCartID(mCartID!!).observe(this, { cartList ->
//                            when {
//                                cartList.isNotEmpty() -> {
//                                    mBinding.textViewGuestsNo.visibility = GONE
//
//                                    val mFinalCartList = ArrayList(cartList)
//
//                                    mCheckoutViewModel.getCheckoutDataByCartID(mCartID!!)
//                                        .observe(this, { checkoutModel ->
//
//                                            when {
//                                                checkoutModel != null -> {
//
//                                                    mCheckoutRowID = checkoutModel.mID
//
//                                                    val mCheckoutTransactionList = checkoutModel.mCheckoutTransaction
//
//                                                    when {
//                                                        mCheckoutTransactionList.size > 0 -> {
//
//                                                            val mCheckoutTransaction =
//                                                                mCheckoutTransactionList[mCheckoutTransactionList.size - 1]
//
//                                                            val mSeatList = mCheckoutTransaction.mSeatList
//
//                                                            val mShareBillPrint50CartAdapter =
//                                                                ShareBillPrint50CartAdapter(
//                                                                    mFinalCartList, mSeatList, mOrderType!!
//                                                                )
//                                                            mBinding.recyclerViewProducts.adapter = mShareBillPrint50CartAdapter
//
//                                                            mBinding.textViewSubTotalAmount.text =
//                                                                String.format(
//                                                                    Locale.ENGLISH, "%.2f",
//                                                                    mCheckoutTransaction.mSeatCartTotal
//                                                                )
//
//                                                            mBinding.textViewTaxAmount.text =
//                                                                String.format(Locale.ENGLISH, "%.2f", mCheckoutTransaction.mTaxAmount)
//
//                                                            when {
//                                                                mCheckoutTransaction.mDiscountAmount > BigDecimal(0) -> {
//                                                                    mBinding.textViewDiscountLabel.visibility = VISIBLE
//                                                                    mBinding.textViewDiscountAmount.visibility = VISIBLE
//
//                                                                    mBinding.textViewDiscountAmount.text =
//                                                                        String.format(
//                                                                            Locale.ENGLISH,
//                                                                            "%.2f",
//                                                                            mCheckoutTransaction.mDiscountAmount
//                                                                        )
//
//                                                                    mBinding.textViewSubTotalAfterDiscountAmount.visibility = VISIBLE
//                                                                    mBinding.textViewSubTotalAfterDiscountLabel.visibility = VISIBLE
//
//                                                                    mBinding.textViewSubTotalAfterDiscountAmount.text =
//                                                                        String.format(
//                                                                            Locale.ENGLISH, "%.2f",
//                                                                            mCheckoutTransaction.mSeatCartTotal - mCheckoutTransaction.mDiscountAmount
//                                                                        )
//                                                                }
//                                                            }
//
//                                                            when {
//                                                                mCheckoutTransaction.mTipAmount > BigDecimal(0) -> {
//                                                                    mBinding.textViewTipAmount.visibility = VISIBLE
//                                                                    mBinding.textViewTipLabel.visibility = VISIBLE
//
//                                                                    mBinding.textViewTipAmount.text =
//                                                                        String.format(
//                                                                            Locale.ENGLISH,
//                                                                            "%.2f",
//                                                                            mCheckoutTransaction.mTipAmount
//                                                                        )
//                                                                }
//                                                            }
//
//                                                            val mTotalAmount = mCheckoutTransaction.mOrderTotal
//                                                            mBinding.textViewTotalAmount.text =
//                                                                String.format(Locale.ENGLISH, "%.2f", mTotalAmount)
//
//                                                            val mPaymentTransactionList = mCheckoutTransaction.mPaymentTransaction
//
//                                                            mCheckoutViewModel.getPaymentMethods()
//                                                                .observe(this, { paymentMethods ->
//
//                                                                    for (i in 0 until paymentMethods.size) {
//
//                                                                        var mPaymentMethodTotal = BigDecimal(0.0)
//                                                                        var mChangeForCash = BigDecimal(0.0)
//
//                                                                        for (j in 0 until mPaymentTransactionList.size) {
//
//                                                                            when (paymentMethods[i].mPaymentMethodID) {
//                                                                                mPaymentTransactionList[j].mPaymentMethodID
//                                                                                -> when (TYPE_CASH) {
//                                                                                    paymentMethods[i].mPaymentMethodType -> {
//                                                                                        mPaymentMethodTotal += mPaymentTransactionList[j].mCash
//                                                                                        mChangeForCash += mPaymentTransactionList[j].mChange
//                                                                                    }
//                                                                                    else -> mPaymentMethodTotal += mPaymentTransactionList[j].mTransactionAmount
//                                                                                }
//                                                                            }
//
//                                                                        }
//
//
//                                                                        when {
//                                                                            paymentMethods[i].mPaymentMethodType == TYPE_CASH &&
//                                                                                    mPaymentMethodTotal > BigDecimal(0)
//                                                                            -> {
//                                                                                mBinding.textViewCashPaidLabel.visibility = VISIBLE
//                                                                                mBinding.textViewCashPaidAmount.visibility = VISIBLE
//
//                                                                                mBinding.textViewCashPaidAmount.text =
//                                                                                    String.format("%.2f", mPaymentMethodTotal)
//
//                                                                                when {
//                                                                                    mChangeForCash > BigDecimal(0) -> {
//                                                                                        mBinding.textViewChangeLabel.visibility = VISIBLE
//                                                                                        mBinding.textViewChangeAmount.visibility = VISIBLE
//                                                                                        mBinding.textViewChangeAmount.text =
//                                                                                            String.format("%.2f", mChangeForCash)
//                                                                                    }
//                                                                                }
//
//                                                                            }
//                                                                            paymentMethods[i].mPaymentMethodType == TYPE_CARD &&
//                                                                                    mPaymentMethodTotal > BigDecimal(0)
//                                                                            -> {
//                                                                                mBinding.textViewCardPaidLabel.visibility = VISIBLE
//                                                                                mBinding.textViewCardPaidAmount.visibility = VISIBLE
//
//                                                                                mBinding.textViewCardPaidAmount.text =
//                                                                                    String.format("%.2f", mPaymentMethodTotal)
//                                                                            }
//                                                                            paymentMethods[i].mPaymentMethodType == TYPE_WALLET &&
//                                                                                    mPaymentMethodTotal > BigDecimal(0)
//                                                                            -> {
//                                                                                mBinding.textViewWalletPaidLabel.visibility = VISIBLE
//                                                                                mBinding.textViewWalletPaidAmount.visibility = VISIBLE
//
//                                                                                mBinding.textViewWalletPaidAmount.text =
//                                                                                    String.format("%.2f", mPaymentMethodTotal)
//                                                                            }
//                                                                            paymentMethods[i].mPaymentMethodType == TYPE_TIP &&
//                                                                                    mPaymentMethodTotal > BigDecimal(0)
//                                                                            -> {
//                                                                                mBinding.textViewTipLabel.visibility = VISIBLE
//                                                                                mBinding.textViewTipAmount.visibility = VISIBLE
//
//                                                                                mBinding.textViewTipAmount.text =
//                                                                                    String.format("%.2f", mPaymentMethodTotal)
//                                                                            }
//                                                                        }
//                                                                    }
//
//                                                                })
//
//                                                            val mRemainingAmount = checkoutModel.mOrderTotal - checkoutModel.mAmountPaid
//
//                                                            when {
//                                                                mRemainingAmount > BigDecimal(0.05) -> {
//                                                                    mBinding.textViewRemainingLabel.visibility = VISIBLE
//                                                                    mBinding.textViewRemainingAmount.visibility = VISIBLE
//
//                                                                    mBinding.textViewRemainingAmount.text =
//                                                                        String.format("%.2f", mRemainingAmount)
//                                                                }
//                                                            }
//
//                                                            Handler().postDelayed({
//                                                                printBill()
//                                                            }, 100)
//
//                                                        }
//                                                    }
//                                                }
//                                            }
//
//                                        })
//
//                                }
//                            }
//                        })
//                    }
//                    }
                }
            }
        })
    }

    private fun cartData() {

        var mCartTotal = 0.0

        cartViewModel.getCarts(mTableID ?: "").observe(this){
            val mFinalCartList = ArrayList(it)
            for (i in 0 until mFinalCartList.size) {
                mCartTotal += mFinalCartList[i].mProductTotalPrice.toDouble()
            }

            mBinding.textViewSubTotalAmount.text =
                String.format(Locale.ENGLISH, "%.2f", mCartTotal)

//                    mBinding.textViewTaxAmount.text = String.format(
//                        Locale.ENGLISH,
//                        "%.2f",
//                        calculateTaxValue(mCartTotal)
//                    )

            val mTotalAmount = mCartTotal /*+ calculateTaxValue(mCartTotal)*/
            mBinding.textViewTotalAmount.text =
                String.format(Locale.ENGLISH, "%.2f", mTotalAmount)

            val mEstimate50Adapter =
                EstimateBillPrint50Adapter(
                    mFinalCartList, SERVICE_DINE_IN
                )
            mBinding.recyclerViewProducts.adapter = mEstimate50Adapter

            Handler(Looper.getMainLooper()).postDelayed({
                printBill()
            }, 100)
        }

//        when (mOrderType) {
//            SERVICE_DINE_IN -> {
//
//                var mCartTotal = 0.0
//
//                cartViewModel.getCarts(mTableID ?: "").observe(this){
//                    val mFinalCartList = java.util.ArrayList(it)
//                    for (i in 0 until mFinalCartList.size) {
//                        mCartTotal += mFinalCartList[i].mProductTotalPrice.toDouble()
//                    }
//
//                    mBinding.textViewSubTotalAmount.text =
//                        String.format(Locale.ENGLISH, "%.2f", mCartTotal)
//
////                    mBinding.textViewTaxAmount.text = String.format(
////                        Locale.ENGLISH,
////                        "%.2f",
////                        calculateTaxValue(mCartTotal)
////                    )
//
//                    val mTotalAmount = mCartTotal /*+ calculateTaxValue(mCartTotal)*/
//                    mBinding.textViewTotalAmount.text =
//                        String.format(Locale.ENGLISH, "%.2f", mTotalAmount)
//
//                    val mEstimate50Adapter =
//                        EstimateBillPrint50Adapter(
//                            mFinalCartList, SERVICE_DINE_IN
//                        )
//                    mBinding.recyclerViewProducts.adapter = mEstimate50Adapter
//
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        printBill()
//                    }, 100)
//                }
//            }
//            else -> cartViewModel.getCarts(mCartID ?: "").observe(this,
//                { cartList ->
////                    when {
////                        cartList.isNotEmpty() -> {
////                            mBinding.textViewGuestsNo.visibility = View.GONE
////
////                            val mFinalCartList = java.util.ArrayList(cartList)
////
////                            for(i in 0 until cartList.size) {
////                                mCartTotal += cartList[i].mProductTotalPrice
////                            }
////
////                            mBinding.textViewSubTotalAmount.text = String.format(Locale.ENGLISH,"%.2f",mCartTotal)
////
////                            mBinding.textViewTaxAmount.text = String.format(Locale.ENGLISH,"%.2f",calculateTaxValue(mCartTotal))
////
////                            val mTotalAmount = mCartTotal + calculateTaxValue(mCartTotal)
////                            mBinding.textViewTotalAmount.text = String.format(Locale.ENGLISH,"%.2f",mTotalAmount)
////
////                            val mEstimate50Adapter =
////                                EstimateBillPrint50Adapter(
////                                    mFinalCartList, SERVICE_QUICK_SERVICE
////                                )
////                            mBinding.recyclerViewProducts.adapter = mEstimate50Adapter
////
////                            Handler().postDelayed({
////                                printBill()
////                            },100)
////                        }
////                    }
//                })
//        }
    }

    private fun printBill() {
        val width = mBinding.constraintMainBillPrint.width
        val height = mBinding.constraintMainBillPrint.height

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = mBinding.constraintMainBillPrint.background
        if (bgDrawable != null)
            bgDrawable.draw(canvas)
        else
            canvas.drawColor(Color.WHITE)
        mBinding.constraintMainBillPrint.draw(canvas)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        val mBluetoothUtil = PrinterUtil(this)

        if (mSharedPrefs.getString(SELECTED_BILL_PRINTER_NETWORK_IP, "")!!.isBlank()) {
            val mPrinterName = mSharedPrefs.getString(SELECTED_BILL_PRINTER_NAME, "")

            val mPrintModel = KitchenModel(
                "0", "", mPrinterName!!,
                PAPER_SIZE_50, "", "", "", "", 1
            )

            mExecutor!!.diskIO().execute {
                if (mPrinterName.equals("InnerPrinter", ignoreCase = true)) {
                    AidlUtil.getInstance().printBitmap(bitmap)
                    runOnUiThread {
                        EventBus.getDefault().post(BillPrintEvent(true))
                        Handler().postDelayed({
                            finish()
                        }, 2000)
                    }


                } else {
                    mBluetoothUtil.connect(mPrintModel, 2)

                    try {
                        mBluetoothUtil.splitImage2(
                            bitmap,
                            height / 150,
                            1,
                            AppConstants.BLUETOOTH_PRINTER,
                            mPrintModel,
                            2
                        )
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

            }

        } else {

            val mPrinterName = mSharedPrefs.getString(SELECTED_BILL_PRINTER_NAME, "")
            val mNetworkPrinterIP = mSharedPrefs.getString(SELECTED_BILL_PRINTER_NETWORK_IP, "")
            val mNetworkPrinterPort = mSharedPrefs.getString(SELECTED_BILL_PRINTER_NETWORK_PORT, "")

            val mPrintModel = KitchenModel(
                "0", "", mPrinterName!!,
                PAPER_SIZE_50, "", "", mNetworkPrinterIP!!, mNetworkPrinterPort!!, 2
            )

            mExecutor!!.networkIO().execute {
                try {
                    e(javaClass.simpleName, "printKitchen: height: $height")

                    var mRows = height.toDouble() / 200

                    var mHeight = height / mRows.roundToLong().toInt()

                    when {
                        mHeight < 200 -> mBluetoothUtil.splitImage2(
                            bitmap, mRows.roundToLong().toInt(), 1,
                            AppConstants.NETWORK_PRINTER, mPrintModel, 2
                        )
                        else -> {
                            mRows = height.toDouble() / 180

                            mHeight = height / mRows.roundToLong().toInt()

                            when {
                                mHeight < 200 -> mBluetoothUtil.splitImage2(
                                    bitmap, mRows.roundToLong().toInt(), 1,
                                    AppConstants.NETWORK_PRINTER, mPrintModel, 2
                                )
                                else -> {
                                    mRows = height.toDouble() / 160

                                    mHeight = height / mRows.roundToLong().toInt()

                                    when {
                                        mHeight < 200 -> mBluetoothUtil.splitImage2(
                                            bitmap, mRows.roundToLong().toInt(), 1,
                                            AppConstants.NETWORK_PRINTER, mPrintModel, 2
                                        )
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                EventBus.getDefault().post(SeatPaymentCompleteEvent(true, mCheckoutRowID, javaClass.simpleName))
                finish()
            }
        }
    }

    private fun setTaxLabel() {
        val mTaxBuilder = StringBuilder()
        mTaxBuilder.append("Tax(")
        mCheckoutViewModel.getTaxData().observe(this, { taxModels ->
            if (taxModels != null) {
                for (i in 0 until taxModels.size) {
                    val mTax = taxModels[i]
                    mTaxPercentage += taxModels[i].mTaxPercentage
                    if (i == taxModels.size - 1)
                        mTaxBuilder.append(mTax.mTaxName).append(" ").append(mTax.mTaxPercentage).append("%")
                    else
                        mTaxBuilder.append(mTax.mTaxName).append(" ").append(mTax.mTaxPercentage).append("%").append("\n")
                }
                mTaxBuilder.append(")")

                mBinding.textViewTaxLabel.text = mTaxBuilder.toString()
            }
        })
    }
}
