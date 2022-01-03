package com.rsl.youresto.ui.main_screen.kitchen_print

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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.rsl.youresto.R
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.databinding.ActivityKitchenPrint80Binding
import com.rsl.youresto.ui.main_screen.cart.NewCartViewModel
import com.rsl.youresto.ui.main_screen.kitchen_print.adapter.KitchenPrintAdapter
import com.rsl.youresto.ui.main_screen.kitchen_print.event.KitchenActivityCreatedEvent
import com.rsl.youresto.ui.main_screen.kitchen_print.event.KitchenBundleEvent
import com.rsl.youresto.ui.main_screen.kitchen_print.event.KitchenPrintDoneEvent
import com.rsl.youresto.ui.main_screen.kitchen_print.event.PrintEvent
import com.rsl.youresto.ui.main_screen.kitchen_print.model.SingleKOTModel
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN_WITHOUT_SEATS
import com.rsl.youresto.utils.AppExecutors
import com.rsl.youresto.utils.Utils
import com.rsl.youresto.utils.custom_views.CustomToast
import com.rsl.youresto.utils.printer.PrinterUtil
import com.rsl.youresto.utils.printer_utils.AidlUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class KitchenPrint80Activity : AppCompatActivity() {

    private lateinit var mBinding: ActivityKitchenPrint80Binding
    private var mSerialNO: Int? = null
    private var mGroupName: String? = null
    private var mCartID: String? = null
    private var mSingleKOTData: SingleKOTModel? = null

    private lateinit var mSharedPrefs: SharedPreferences

    private lateinit var mTableID: String
    private var mTableNO: Int = 0
    private var mLocationType: Int = 0

    private var mExecutor: AppExecutors? = null

    private val cartViewModel: NewCartViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_kitchen_print80)
        this.setFinishOnTouchOutside(false)
        setParamForLayout(800, 600)

        EventBus.getDefault().post(KitchenActivityCreatedEvent(true))

        mExecutor = AppExecutors.getInstance()

        mSharedPrefs = getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

        mLocationType = mSharedPrefs.getInt(AppConstants.LOCATION_SERVICE_TYPE, 0)

        init()
    }

//    override fun onResume() {
//        super.onResume()
//
//        this.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        this.window!!.setBackgroundDrawableResource(android.R.color.transparent)
//    }

    private fun setParamForLayout(height: Int, width: Int) {
        val params = window.attributes
        params.height = height
        params.width = width

        this.window.attributes = params

        window.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun init() {
        if (mLocationType == AppConstants.SERVICE_DINE_IN) {
            mTableID = mSharedPrefs.getString(AppConstants.SELECTED_TABLE_ID, "")!!
            mTableNO = mSharedPrefs.getInt(AppConstants.SELECTED_TABLE_NO, 0)
        } else if (mLocationType == AppConstants.SERVICE_QUICK_SERVICE) {
            mTableID = mSharedPrefs.getString(AppConstants.QUICK_SERVICE_TABLE_ID, "")!!
            mTableNO = mSharedPrefs.getInt(AppConstants.QUICK_SERVICE_TABLE_NO, 0)
        }

        if (!mSharedPrefs.getBoolean(AppConstants.SEAT_SELECTION_ENABLED, false)) {
            mLocationType = SERVICE_DINE_IN_WITHOUT_SEATS
        }

        mSingleKOTData = SingleKOTModel()

        lifecycleScope.launch {
            val kitchens = withContext(Dispatchers.IO){
                cartViewModel.getKitchens()
            }

            mSingleKOTData?.mSerialNO = 1
            mSingleKOTData?.mKitchenModel = kitchens[0]

            setDineInPrintLayout()
        }
    }

    private fun setDineInPrintLayout() {
        mBinding.constraintQuickOptions.visibility = View.GONE

        val mTableNo = "Table: $mTableNO"
        mBinding.textViewTableNo.text = mTableNo

        val mTime = "Time: " + Utils.getStringFromDate("HH:mm", Date())
        mBinding.textViewTime.text = mTime

        val mLocation = "LOC: " + mSharedPrefs.getString(AppConstants.SELECTED_LOCATION_NAME, "")!!
        mBinding.textViewLocation.text = mLocation

        val mServer = "Server: " + mSharedPrefs.getString(AppConstants.LOGGED_IN_SERVER_NAME, "")!!
        mBinding.textViewServerName.text = mServer

//        if (mSingleKOTData!!.mProductList.size > 0) {
//            val mKitchenAdapter = KitchenPrintAdapter(this, sortCart(mSingleKOTData!!.mProductList), mLocationType)
//            mBinding.recyclerViewKitchenCart.adapter = mKitchenAdapter
//
//            val mCartNO = "CartNO: " + mSingleKOTData!!.mProductList[0].mCartNO
//            mBinding.textViewCartNo.text = mCartNO
//
//            Handler(Looper.getMainLooper()).postDelayed({ printKitchen() }, 1000)
//        }

        cartViewModel.getCarts(mTableID).observe(this){
            val mKitchenAdapter = KitchenPrintAdapter(this, sortCart(ArrayList(it)), mLocationType)
            mBinding.recyclerViewKitchenCart.adapter = mKitchenAdapter

            mSingleKOTData?.mProductList = ArrayList(it)

            val mCartNO = "CartNO: " + it[0].mCartNO
            mBinding.textViewCartNo.text = mCartNO

            Handler(Looper.getMainLooper()).postDelayed({ printKitchen() }, 1000)
        }

    }

    @SuppressLint("LogNotTimber")
    private fun printKitchen() {
        val width = mBinding.constraintLayoutKitchenPrint.width
        val height = mBinding.constraintLayoutKitchenPrint.height

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgDrawable = mBinding.constraintLayoutKitchenPrint.background
        if (bgDrawable != null)
            bgDrawable.draw(canvas)
        else
            canvas.drawColor(Color.WHITE)
        mBinding.constraintLayoutKitchenPrint.draw(canvas)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        val mBluetoothUtil = PrinterUtil(this)

        when (mSingleKOTData!!.mKitchenModel.mPrinterType) {
            AppConstants.BLUETOOTH_PRINTER -> {
                val mPrinterName = mSingleKOTData!!.mKitchenModel.mSelectedKitchenPrinterName

                mExecutor!!.diskIO().execute {

                    when {
                        mPrinterName.equals("InnerPrinter", ignoreCase = true) -> {
                            AidlUtil.getInstance().printBitmap(bitmap)
                            EventBus.getDefault().post(KitchenPrintDoneEvent(true))
                            finish()
                        }
                        else -> {
                            mBluetoothUtil.connect(mSingleKOTData!!.mKitchenModel, 1)

                            try {
                                mBluetoothUtil.splitImage2(
                                    bitmap,
                                    height / 150,
                                    1,
                                    AppConstants.BLUETOOTH_PRINTER,
                                    mSingleKOTData!!.mKitchenModel,
                                    1
                                )
                            } catch (e: IOException) {
                                e.printStackTrace()
                                CustomToast.makeText(
                                    this,
                                    "Printing Failed Exception: ${e.localizedMessage}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                        }
                    }
                }
            }
            AppConstants.NETWORK_PRINTER -> mExecutor!!.networkIO().execute {
                try {
                    Log.e(javaClass.simpleName, "printKitchen: height: $height")

                    var mRows = height.toDouble() / 200

                    var mHeight = height / Math.round(mRows).toInt()

                    when {
                        mHeight < 200 -> mBluetoothUtil.splitImage2(
                            bitmap, Math.round(mRows).toInt(), 1,
                            AppConstants.NETWORK_PRINTER, mSingleKOTData!!.mKitchenModel, 1
                        )
                        else -> {
                            mRows = height.toDouble() / 180

                            mHeight = height / Math.round(mRows).toInt()

                            when {
                                mHeight < 200 -> mBluetoothUtil.splitImage2(
                                    bitmap, Math.round(mRows).toInt(), 1,
                                    AppConstants.NETWORK_PRINTER, mSingleKOTData!!.mKitchenModel, 1
                                )
                                else -> {
                                    mRows = height.toDouble() / 160

                                    mHeight = height / Math.round(mRows).toInt()

                                    when {
                                        mHeight < 200 -> mBluetoothUtil.splitImage2(
                                            bitmap, Math.round(mRows).toInt(), 1,
                                            AppConstants.NETWORK_PRINTER, mSingleKOTData!!.mKitchenModel, 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    CustomToast.makeText(
                        this,
                        "Printing Failed Exception: ${e.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }

                finish()
            }
        }
    }

    private fun sortCart(mCartList: ArrayList<CartProductModel>): ArrayList<CartProductModel> {
        val mCourseList = ArrayList<String>()
        for (i in mCartList.indices) {
            if (!mCourseList.contains(mCartList[i].mCourseType)) {
                mCourseList.add(mCartList[i].mCourseType)
            }
        }

        val mFinalCartList = ArrayList<CartProductModel>()

        for (i in mCourseList.indices) {
            mFinalCartList.add(getCartModel(mCourseList[i]))
            for (j in mCartList.indices) {
                if (mCourseList[i] == mCartList[j].mCourseType) {
                    mFinalCartList.add(mCartList[j])
                }
            }
        }

        return mFinalCartList
    }

    private fun getCartModel(mCourseType: String): CartProductModel {
        return CartProductModel(
            "", "", mTableNO, "", 0, null, "", "", "", 0,
            "", "", null, "", "", "", "", mCourseType,
            0, "", BigDecimal(0), BigDecimal(0), BigDecimal(0), "", BigDecimal(0),
            ArrayList(), ArrayList(), ArrayList(), ArrayList(), Date(), "", 0, "", 0, true
        )
    }

    @Subscribe
    fun onPrintError(mEvent: PrintEvent) {
        CustomToast.makeText(this, "Please check if the printer is connected properly", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("LogNotTimber")
    @Subscribe
    fun onGetDataFromCart(mEvent: KitchenBundleEvent) {
        if (mEvent.mResult) {
            mSerialNO = mEvent.mSerialNO
            mGroupName = mEvent.mGroupName
            mCartID = mEvent.mCartID
            mSingleKOTData = mEvent.mSingleKOTData

            Log.e(
                javaClass.simpleName, "mSerialNO: $mSerialNO , mGroupName: $mGroupName , mCartID: $mCartID, " +
                        "mProductListSize: ${mSingleKOTData!!.mProductList.size}"
            )

            init()
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
