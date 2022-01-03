package com.rsl.youresto.ui.main_screen.estimate_bill_print

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.rsl.youresto.R
import com.rsl.youresto.data.database_download.models.KitchenModel
import com.rsl.youresto.databinding.ActivityEstimateBillPrint80Binding
import com.rsl.youresto.ui.main_screen.cart.CartViewModel
import com.rsl.youresto.ui.main_screen.cart.CartViewModelFactory
import com.rsl.youresto.ui.main_screen.cart.NewCartViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModel
import com.rsl.youresto.ui.main_screen.checkout.CheckoutViewModelFactory
import com.rsl.youresto.ui.main_screen.estimate_bill_print.adapter.EstimateBillPrint50Adapter
import com.rsl.youresto.utils.*
import com.rsl.youresto.utils.AppConstants.BLUETOOTH_PRINTER
import com.rsl.youresto.utils.AppConstants.NETWORK_PRINTER
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.printer.PrinterUtil
import com.rsl.youresto.utils.printer_utils.AidlUtil
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.util.*
import kotlin.math.roundToLong

@SuppressLint("LogNotTimber")
class EstimateBillPrint80Activity : AppCompatActivity() {

    private lateinit var mBinding: ActivityEstimateBillPrint80Binding
    private lateinit var mCartViewModel: CartViewModel
    private lateinit var mCheckoutViewModel: CheckoutViewModel

    private lateinit var mSharedPrefs: SharedPreferences

    private var mOrderType: Int? = null
    private var mTableNO: Int? = null
    private var mOrderNO: String? = null
    private var mGroupName: String? = null
    private var mCartTotal = BigDecimal(0)
    private var mTableID: String = ""
    private var mTaxPercentage = BigDecimal(0)
    private var mCartID: String? = null

    private var mExecutor: AppExecutors? = null

    private val cartViewModel: NewCartViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setFinishOnTouchOutside(false)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_estimate_bill_print80)

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
            mOrderNO = intent.getStringExtra(AppConstants.ORDER_NO)
            mCartID = intent.getStringExtra(AppConstants.API_CART_ID)
            mGroupName = intent.getStringExtra(AppConstants.GROUP_NAME)
        }

        mTableID = mSharedPrefs.getString(AppConstants.SELECTED_TABLE_ID, "") ?: ""

        if (mOrderType == SERVICE_DINE_IN) {
            setDineInPrintLayout()
        } else if (mOrderType == AppConstants.SERVICE_QUICK_SERVICE) {

        }
    }

    override fun onResume() {
        super.onResume()

        this.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        this.window!!.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun calculateTaxValue(mAmount: BigDecimal): BigDecimal {
        return mAmount * mTaxPercentage / BigDecimal(100)
    }

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
                        else -> mBinding.textViewRestaurantAddress1.visibility = View.GONE
                    }

//                    when {
//                        it.city.isNotBlank() -> mBinding.textViewRestaurantAddress2.text = it.city
//                        else -> mBinding.textViewRestaurantAddress2.visibility = GONE
//                    }

//                    when {
//                        it.mAddress3.isNotBlank() -> mBinding.textViewRestaurantAddress3.text = it.mAddress3
//                        else -> mBinding.textViewRestaurantAddress3.visibility = GONE
//                    }
                    mBinding.textViewRestaurantAddress3.visibility = View.GONE

                    when {
                        it.city.isNotBlank() -> mBinding.textViewRestaurantCity.text = it.city
                        else -> mBinding.textViewRestaurantCity.visibility = View.GONE
                    }

                    when {
                        it.postcode.isNotBlank() -> mBinding.textViewRestaurantPincode.text = it.postcode
                        else -> mBinding.textViewRestaurantPincode.visibility = View.GONE
                    }

                    when {
                        it.contactNumber.isNotBlank() -> mBinding.textViewRestaurantPhone.text = it.contactNumber
                        else -> mBinding.textViewRestaurantPhone.visibility = View.GONE
                    }

                    when {
                        it.email.isNotBlank() -> mBinding.textViewRestaurantEmail.text = it.email
                        else -> mBinding.textViewRestaurantEmail.visibility = View.GONE
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

                    mBinding.textViewMessage1.visibility = View.GONE
                    mBinding.textViewMessage2.visibility = View.GONE

                    val mServer = "Server: " + mSharedPrefs.getString(AppConstants.LOGGED_IN_SERVER_NAME, "")!!
                    mBinding.textViewServerName.text = mServer

                    val mLocation = "LOC: " + mSharedPrefs.getString(AppConstants.SELECTED_LOCATION_NAME, "")!!
                    mBinding.textViewLocation.text = mLocation

                    val mTime = "Time: " + Utils.getStringFromDate("dd/MM/yyyy HH:mm", Date())
                    mBinding.textViewTime.text = mTime

                    when (mOrderType) {
                        SERVICE_DINE_IN -> mBinding.textViewOrderType.text = "DINE IN"
                        AppConstants.SERVICE_QUICK_SERVICE -> mBinding.textViewOrderType.text = "QUICK SERVICE"
                    }


                    mBinding.textViewOrderNo.text = "Order NO: $mOrderNO"
                    mBinding.textViewTableNo.text = "Table: $mTableNO($mGroupName)"


                    cartData()

                }
            }
        })
    }

    private fun cartData() {
        when (mOrderType) {
            SERVICE_DINE_IN -> {

                cartViewModel.getCarts(mTableID).observe(this){
                    val mFinalCartList = ArrayList(it)
                    for (i in 0 until mFinalCartList.size) {
                        mCartTotal += mFinalCartList[i].mProductTotalPrice
                    }

                    mBinding.textViewSubTotalAmount.text =
                        String.format(Locale.ENGLISH, "%.2f", mCartTotal)

                    mBinding.textViewTaxAmount.text = String.format(
                        Locale.ENGLISH,
                        "%.2f",
                        calculateTaxValue(mCartTotal)
                    )

                    val mTotalAmount = mCartTotal + calculateTaxValue(mCartTotal)
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
            }
            else -> mCartViewModel.getCartDataWithCartID(mCartID!!).observe(this,
                { cartList ->
                    when {
                        cartList.isNotEmpty() -> {
                            mBinding.textViewGuestsNo.visibility = View.GONE

                            val mFinalCartList = ArrayList(cartList)

                            for(i in 0 until cartList.size) {
                                mCartTotal += cartList[i].mProductTotalPrice
                            }

                            mBinding.textViewSubTotalAmount.text = String.format(Locale.ENGLISH,"%.2f",mCartTotal)

                            mBinding.textViewTaxAmount.text = String.format(Locale.ENGLISH,"%.2f",calculateTaxValue(mCartTotal))

                            val mTotalAmount = mCartTotal + calculateTaxValue(mCartTotal)
                            mBinding.textViewTotalAmount.text = String.format(Locale.ENGLISH,"%.2f",mTotalAmount)

                            val mEstimate50Adapter =
                                EstimateBillPrint50Adapter(
                                    mFinalCartList, AppConstants.SERVICE_QUICK_SERVICE
                                )
                            mBinding.recyclerViewProducts.adapter = mEstimate50Adapter

                            Handler().postDelayed({
                                printBill()
                            },100)
                        }
                    }
                })
        }
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

        when {
            mSharedPrefs.getInt(AppConstants.SELECTED_BILL_PRINTER_TYPE, 0) == BLUETOOTH_PRINTER -> {
                val mPrinterName = mSharedPrefs.getString(AppConstants.SELECTED_BILL_PRINTER_NAME, "")

                val mPrintModel = KitchenModel(
                    "0", "", mPrinterName!!,
                    AppConstants.PAPER_SIZE_50, "", "", "", "", 1
                )

                mExecutor!!.diskIO().execute {
                    when {
                        mPrinterName.equals("InnerPrinter", ignoreCase = true) -> {
                            AidlUtil.getInstance().printBitmap(bitmap)
                            finish()
                        }
                        else -> {
                            mBluetoothUtil.connect(mPrintModel, 2)

                            try {
                                mBluetoothUtil.splitImage2(bitmap, height / 150, 1, BLUETOOTH_PRINTER, mPrintModel, 2)
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    }
                }

            }
            mSharedPrefs.getInt(AppConstants.SELECTED_BILL_PRINTER_TYPE, 0) == NETWORK_PRINTER -> {

                val mPrinterName = mSharedPrefs.getString(AppConstants.SELECTED_BILL_PRINTER_NAME, "")
                val mNetworkPrinterIP = mSharedPrefs.getString(AppConstants.SELECTED_BILL_PRINTER_NETWORK_IP, "")
                val mNetworkPrinterPort = mSharedPrefs.getString(AppConstants.SELECTED_BILL_PRINTER_NETWORK_PORT, "")

                val mPrintModel = KitchenModel(
                    "0", "", mPrinterName!!,
                    AppConstants.PAPER_SIZE_50, "", "", mNetworkPrinterIP!!, mNetworkPrinterPort!!, 2
                )

                mExecutor!!.networkIO().execute {
                    try {
                        Log.e(javaClass.simpleName, "printKitchen: height: $height")

                        var mRows = height.toDouble() / 200

                        var mHeight = height / mRows.roundToLong().toInt()

                        when {
                            mHeight < 200 -> mBluetoothUtil.splitImage2(
                                bitmap, mRows.roundToLong().toInt(), 1,
                                NETWORK_PRINTER, mPrintModel, 2
                            )
                            else -> {
                                mRows = height.toDouble() / 180

                                mHeight = height / mRows.roundToLong().toInt()

                                when {
                                    mHeight < 200 -> mBluetoothUtil.splitImage2(
                                        bitmap, mRows.roundToLong().toInt(), 1,
                                        NETWORK_PRINTER, mPrintModel, 2
                                    )
                                    else -> {
                                        mRows = height.toDouble() / 160

                                        mHeight = height / mRows.roundToLong().toInt()

                                        when {
                                            mHeight < 200 -> mBluetoothUtil.splitImage2(
                                                bitmap, mRows.roundToLong().toInt(), 1,
                                                NETWORK_PRINTER, mPrintModel, 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    finish()
                }
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
