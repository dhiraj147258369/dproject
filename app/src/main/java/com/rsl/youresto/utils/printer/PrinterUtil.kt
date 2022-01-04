package com.rsl.youresto.utils.printer

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log.e
import android.widget.Toast
import com.rsl.youresto.data.database_download.models.KitchenModel
import com.rsl.youresto.ui.main_screen.checkout.bill_print.BillPrintEvent
import com.rsl.youresto.ui.main_screen.checkout.events.SeatPaymentCompleteEvent
import com.rsl.youresto.ui.main_screen.kitchen_print.event.KitchenPrintDoneEvent
import com.rsl.youresto.ui.main_screen.kitchen_print.event.PrintEvent
import com.rsl.youresto.utils.AppConstants
import com.rsl.youresto.utils.AppConstants.ENABLE_KITCHEN_PRINT
import com.rsl.youresto.utils.custom_views.CustomToast
import com.rsl.youresto.utils.printer_utils.BytesUtilBluetooth
import com.rsl.youresto.utils.printer_utils.ESCUtil
import com.rsl.youresto.utils.printer_utils.ESCUtil.clearBuffer
import org.greenrobot.eventbus.EventBus
import java.io.ByteArrayInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.Socket
import java.util.*

@SuppressLint("LogNotTimber")
class PrinterUtil(var mContext: Activity) {

    private var mmOutputStream: OutputStream? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mmSocket: BluetoothSocket? = null
    private var mmDevice: BluetoothDevice? = null
    private var mBluetoothDeviceList = ArrayList<BluetoothDevice>()
    private var mSharedPrefs: SharedPreferences =
        mContext.getSharedPreferences(AppConstants.MY_PREFERENCES, Context.MODE_PRIVATE)

    private var mPrintType = 0


    fun connect(mPrinter: KitchenModel, mPrintType: Int): Int {

        this.mPrintType = mPrintType

        e(javaClass.simpleName, "connect: $mPrintType")

        var printerDeviceCheck = false
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (mBluetoothAdapter == null) {
            mContext.runOnUiThread {
                CustomToast.makeText(mContext, "No bluetooth device paired", Toast.LENGTH_SHORT).show()
                mContext.finish()
            }
            return 0
        }

        if (!mBluetoothAdapter!!.isEnabled) {
            val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            mContext.startActivityForResult(enableBluetooth, 0)
        }

        val pairedDevices = mBluetoothAdapter!!.bondedDevices

        if (pairedDevices.size > 0) {
            for (device in pairedDevices) {
                if (device.name.equals(mPrinter.mSelectedKitchenPrinterName, ignoreCase = true)) {
                    printerDeviceCheck = true
                    mmDevice = device
                }
                mBluetoothDeviceList.add(device)

            }
        }

        if (printerDeviceCheck) {
            openBT()
            beginListenForData()

        } else {
            mContext.runOnUiThread {
                CustomToast.makeText(mContext, "Printer not found", Toast.LENGTH_SHORT).show()
            }
            mContext.finish()
        }

        return 1
    }

    private fun openBT() {
        try {

            // Standard SerialPortService ID
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            mmSocket = mmDevice!!.createRfcommSocketToServiceRecord(uuid)
            mmSocket!!.connect()
            mmOutputStream = mmSocket!!.outputStream


        } catch (e: Exception) {
            e.printStackTrace()

            mContext.runOnUiThread {
                EventBus.getDefault().post(BillPrintEvent(false))
                EventBus.getDefault().post(KitchenPrintDoneEvent(false))
                Handler().postDelayed({ mContext.finish()}, 500)

                mContext.finish()
            }
        }

    }

    private fun beginListenForData() {
        wakeUpPrinter()
        initPrinter()
    }

    private fun printImage(mBitmap: ByteArray, mCutPaper: Boolean): Int {
        e(javaClass.simpleName, "in printImage $mCutPaper")

        mmOutputStream!!.write(clearBuffer())

        when (mPrintType) {
            1 -> when {
                mSharedPrefs.getBoolean(ENABLE_KITCHEN_PRINT, false) -> try {
                    mmOutputStream!!.write(mBitmap)
                    when {
                        mCutPaper -> {
                            mmOutputStream!!.write("\n\n\n\n".toByteArray())
                            mmOutputStream!!.write(ESCUtil.cutter())
                            mContext.runOnUiThread{
                                Handler().postDelayed({
                                    e(javaClass.simpleName, "in handler")
                                    EventBus.getDefault().post(KitchenPrintDoneEvent(true))

                                    mContext.finish()
                                },2000)
                            }


                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                else -> EventBus.getDefault().post(KitchenPrintDoneEvent(true))
            }
            2 -> try {
                mmOutputStream!!.write(mBitmap)
                if (mCutPaper) {
                    mmOutputStream!!.write("\n\n\n\n".toByteArray())
                    mmOutputStream!!.write(ESCUtil.cutter())
                    mContext.runOnUiThread {
                        Handler().postDelayed({
                            EventBus.getDefault().post(BillPrintEvent(true))
                            mContext.finish()
                        }, 2000)
                    }

                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (mCutPaper) {
                    mContext.runOnUiThread {
                        EventBus.getDefault().post(BillPrintEvent(false))
                        Handler().postDelayed({ mContext.finish()}, 500)

                    }

                }
            }
        }



        return 1

    }

    private fun wakeUpPrinter() {
        val b = ByteArray(3)

        try {
            if (mmOutputStream != null) {
                mmOutputStream!!.write(b)
            }

            Thread.sleep(100L)
        } catch (var2: Exception) {
            var2.printStackTrace()
        }

    }

    private fun initPrinter() {
        val combyte = byteArrayOf(27.toByte(), 64.toByte())

        try {
            if (mmOutputStream != null) {
                mmOutputStream!!.write(combyte)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Throws(IOException::class)
    fun splitImage2(mBitmap: Bitmap, rows: Int, cols: Int, mPrintType: Int, mPrinter: KitchenModel, mBillKitchenPrint: Int) {

        this.mPrintType = mBillKitchenPrint

        try {
            e("TAG", "splitImage2: rows: $rows" )
            // For height and width of the small image chunks.
            val chunkHeight = mBitmap.height / rows
            val chunkWidth = mBitmap.width / cols

            // Getting the scaled bitmap of the source image.
            val scaledBitmap = Bitmap.createScaledBitmap(mBitmap, mBitmap.width, mBitmap.height, true)

            // xCoord and yCoord are the pixel positions of the image chunks.
            var mPrintFlag = 0
            var yCoord = 0
            for (x in 0 until rows) {
                var xCoord = 0
                for (y in 0 until cols) {
                    val bmp = Bitmap.createBitmap(scaledBitmap, xCoord, yCoord, chunkWidth, chunkHeight)
                    e(javaClass.simpleName, "splitImage2: chunkHeight: $chunkHeight")
                    e(javaClass.simpleName, "splitImage2: chunkWidth: $chunkWidth")
                    if (bmp != null) {
                        val command = BytesUtilBluetooth.decodeBitmap(bmp)
                        mPrintFlag = if (mPrintType == AppConstants.NETWORK_PRINTER)
                            networkPrint(command, x == rows - 1, mPrinter)
                        else
                            printImage(command, x == rows - 1)
                    }
                    xCoord += chunkWidth
                }
                yCoord += chunkHeight

                e(javaClass.simpleName, "splitImage2: $x")
            }

            if (mPrintFlag != 1) {
                EventBus.getDefault().post(PrintEvent(mPrintFlag))
            }

            EventBus.getDefault().post(SeatPaymentCompleteEvent(true, 0, javaClass.simpleName))

            if (mmSocket != null)
                mmSocket!!.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    private fun networkPrint(mBitmapData: ByteArray, mCutPaper: Boolean, mPrinter: KitchenModel): Int {

        when {
            mSharedPrefs.getBoolean(ENABLE_KITCHEN_PRINT, false) || mPrintType == 2 -> {

                val clientSocket: Socket

                try {
                    e("TAG", "networkPrint: ${mPrinter.mNetworkPrinterIP}" )
                    val inputStream = ByteArrayInputStream(mBitmapData)
                    clientSocket = Socket(mPrinter.mNetworkPrinterIP, Integer.parseInt(mPrinter.mNetworkPrinterPort))

                    val dOut = DataOutputStream(clientSocket.getOutputStream())
                    val buffer = ByteArray(mBitmapData.size)
                    while (inputStream.read(buffer) != -1) {
                        dOut.write(clearBuffer())
                        dOut.write(ESCUtil.alignCenter())
                        dOut.write(buffer)
                        when {
                            mCutPaper -> {
                                dOut.write("\n\n".toByteArray())
                                dOut.write(ESCUtil.cutter())
                                mContext.runOnUiThread {
                                    when (mPrintType) {
                                        1 -> Handler().postDelayed({
                                            EventBus.getDefault().post(KitchenPrintDoneEvent(true))
                                        }, 500)
                                        2 -> Handler().postDelayed({
                                            EventBus.getDefault().post(BillPrintEvent(true))
                                        }, 500)
                                    }

                                    mContext.finish()

                                }

                            }
                        }
                    }
                    dOut.close()

                    clientSocket.close()
                    return 1
                } catch (e: Exception) {
                    e(javaClass.simpleName, e.toString(), e)
                    mContext.runOnUiThread {
                        when (mPrintType) {
                            1 -> Handler(Looper.getMainLooper()).postDelayed({
                                EventBus.getDefault().post(KitchenPrintDoneEvent(true))
                            }, 500)
                            2 -> Handler(Looper.getMainLooper()).postDelayed({
                                EventBus.getDefault().post(BillPrintEvent(false))
                            }, 500)
                        }
                    }

                    return -1
                }
            }
            else -> {
                Handler().postDelayed({
                    EventBus.getDefault().post(KitchenPrintDoneEvent(true))
                }, 500)
                return 1
            }
        }

    }
}