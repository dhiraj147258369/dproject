package com.rsl.foodnairesto.utils.new_print

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.rsl.foodnairesto.utils.AppConstants.BLUETOOTH_PRINTER
import com.rsl.foodnairesto.utils.AppConstants.NETWORK_PRINTER
import com.rsl.foodnairesto.utils.custom_views.CustomToast
import com.rsl.foodnairesto.utils.printer_utils.AidlUtil
import com.rsl.foodnairesto.utils.printer_utils.ESCUtil
import org.koin.core.component.KoinComponent
import java.io.IOException
import java.io.OutputStream
import java.util.*

class PrintUtils(val activity: Activity, private val printerName: String): KoinComponent {

    private var mAidlUtil: AidlUtil? = null

    init {
        mAidlUtil = AidlUtil(activity)
    }

    fun printText(print: String, printerType: Int) {

        val command = print.toByteArray()
        if (printerType == BLUETOOTH_PRINTER) {
            connect(command)
//            if (printerName.equals("InnerPrinter", ignoreCase = true)) {
//                mAidlUtil?.printTextReceiptContent(print, 24f, false, false)
//            } else {
//                connect(command)
//            }
        } else if (printerType == NETWORK_PRINTER) {
//            mExecutor.networkIO().execute {
//                try {
//                    networkTextPrint(command)
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
        }
    }

    private var mmOutputStream: OutputStream? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mmSocket: BluetoothSocket? = null
    private var mmDevice: BluetoothDevice? = null
    private var mBluetoothDeviceList = ArrayList<BluetoothDevice>()

    fun connect(command: ByteArray) {
        var printerDeviceCheck = false
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (mBluetoothAdapter == null) {
            CustomToast.makeText(activity, "No bluetooth device paired", Toast.LENGTH_SHORT).show()
        } else {
            val enableBluetooth = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(enableBluetooth, 0)
        }
        val pairedDevices = mBluetoothAdapter?.bondedDevices ?: setOf()
        if (pairedDevices.isNotEmpty()) {
            for (device in pairedDevices) {
                if (device.name.equals(printerName, ignoreCase = true)) {
                    printerDeviceCheck = true
                    mmDevice = device
                }
                mBluetoothDeviceList.add(device)
            }
        }
        if (printerDeviceCheck) {
            openBT()
            beginListenForData()
            try {
                Handler(Looper.getMainLooper()).postDelayed({
                    printText(
                        command
                    )
                }, 1000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            CustomToast.makeText(activity, "Printer not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openBT() {
        try {

            // Standard SerialPortService ID
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
            mmSocket = mmDevice!!.createRfcommSocketToServiceRecord(uuid)
            mmSocket?.connect()
            mmOutputStream = mmSocket?.outputStream
        } catch (e: Exception) {
            e.printStackTrace()
            CustomToast.makeText(activity, "Printer not found, try again!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun beginListenForData() {
        WakeUpPrinter()
        InitPrinter()
    }

    private fun printText(text: ByteArray): Int {
        try {
            if (mmOutputStream != null) {
                mmOutputStream?.write(ESCUtil.alignCenter())
                mmOutputStream?.write(text)
                mmOutputStream?.write("\n\n\n\n".toByteArray())
                mmOutputStream?.write(ESCUtil.cutter())
                if (mmSocket != null) mmSocket?.close()
            } else {
                CustomToast.makeText(activity, "Printer not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            CustomToast.makeText(activity, "Printer not found", Toast.LENGTH_SHORT).show()
        }
        return 1
    }

    private fun printImage(mBitmap: ByteArray, mCutPaper: Boolean): Int {
        try {
            mmOutputStream!!.write(mBitmap)
            if (mCutPaper) {
                mmOutputStream!!.write("\n\n\n".toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 1
    }

    private fun WakeUpPrinter() {
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

    private fun InitPrinter() {
        val combyte = byteArrayOf(27.toByte(), 64.toByte())
        try {
            if (mmOutputStream != null) {
                mmOutputStream!!.write(combyte)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}