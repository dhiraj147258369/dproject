package com.rsl.foodnairesto.data.app_settings

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rsl.foodnairesto.data.app_settings.model.SelectablePinPad
import com.rsl.foodnairesto.data.database_download.models.KitchenModel

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM KitchenModel")
    fun getAllKitchenPrinters(): LiveData<List<KitchenModel>>

    @Query("SELECT * FROM KitchenModel WHERE mKitchenID =:mKitchenID")
    fun getKitchenPrinter(mKitchenID: String): LiveData<KitchenModel>

    @Query("UPDATE KitchenModel SET mSelectedKitchenPrinterName =:mSelectedPrinterName, mSelectedKitchenPrinterSize =:mSelectedPrinterPaperSize, mPrinterType =:mPrinterType WHERE mKitchenID =:mKitchenID")
    fun updateKitchenPrinterPaper(mKitchenID: String, mSelectedPrinterName: String, mSelectedPrinterPaperSize: Int, mPrinterType: Int): Int

    @Query("UPDATE KitchenModel SET mSelectedKitchenPrinterName =:mSelectedPrinterName, mPrinterType =:mPrinterType, mNetworkPrinterIP = '', mNetworkPrinterPort = ''  WHERE mKitchenID =:mKitchenID")
    fun updateKitchenBluetoothPrinter(mKitchenID: String, mSelectedPrinterName: String, mPrinterType: Int): Int

    @Query("UPDATE KitchenModel SET mNetworkPrinterIP =:mPrinterIPAddress, mNetworkPrinterPort =:mPrinterPortNO, mPrinterType = 2, mSelectedKitchenPrinterName = 'Network Printer' WHERE mKitchenID =:mKitchenID")
    fun updateKitchenNetworkPrinter(mKitchenID: String, mPrinterIPAddress: String, mPrinterPortNO: String): Int

    @Query("UPDATE KitchenModel SET mLogWoodServerIP =:mLogWoodIP, mLogwoodServerPort =:mPortNO WHERE mKitchenID =:mKitchenID")
    fun updateLogWoodIP(mLogWoodIP: String, mPortNO: String, mKitchenID: String): Int

    @Query("UPDATE KitchenModel SET mSelectedKitchenPrinterName = 'NO_TYPE', mLogwoodServerIP = '', mLogWoodServerPort = '', mNetworkPrinterIP = '', mNetworkPrinterPort = '', mPrinterType = 0 WHERE mKitchenID =:mKitchenID")
    fun clearKitchenPrinterData(mKitchenID: String): Int

    @Query("SELECT * FROM SelectablePinPad WHERE mLocationID =:mLocationID")
    fun getPaymentDevice(mLocationID: String): SelectablePinPad

    @Insert
    fun insertPaymentDevice(mPaymentDevice: SelectablePinPad): Long

    @Update
    fun updatePaymentDevice(mPaymentDevice: SelectablePinPad): Int
}