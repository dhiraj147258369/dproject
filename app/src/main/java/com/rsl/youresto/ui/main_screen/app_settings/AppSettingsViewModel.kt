package com.rsl.youresto.ui.main_screen.app_settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rsl.youresto.data.app_settings.model.SelectablePinPad
import com.rsl.youresto.data.database_download.models.KitchenModel
import com.rsl.youresto.repositories.AppSettingsRepository

class AppSettingsViewModel constructor(private val mRepository: AppSettingsRepository) : ViewModel() {

    fun getAllKitchenPrinters(): LiveData<List<KitchenModel>> {
        return mRepository.getAllKitchenPrinters()
    }

    fun getKitchenPrinter(mKitchenID: String): LiveData<KitchenModel> {
        return mRepository.getKitchenPrinter(mKitchenID)
    }

    fun updateKitchenPrinterPaper(mKitchenID: String, mSelectedPrinterName: String, mSelectedPrinterPaperSize: Int, mPrinterType: Int): LiveData<Int> {
        return mRepository.updateKitchenPrinterPaper(mKitchenID, mSelectedPrinterName, mSelectedPrinterPaperSize, mPrinterType)
    }

    fun clearKitchenPrinterData(mKitchenID: String): LiveData<Int> {
        return mRepository.clearKitchenPrinterData(mKitchenID)
    }

    fun updateKitchenBluetoothPrinter(mKitchenID: String, mSelectedPrinterName: String, mPrinterType: Int): LiveData<Int> {
        return mRepository.updateKitchenBluetoothPrinter(mKitchenID, mSelectedPrinterName, mPrinterType)
    }

    fun updateKitchenNetworkPrinter(mKitchenID: String, mPrinterIPAddress: String, mPrinterPortNO: String): LiveData<Int> {
        return mRepository.updateKitchenNetworkPrinter(mKitchenID,mPrinterIPAddress,mPrinterPortNO)
    }

    fun updateLogWoodIP(mLogWoodIP: String, mPortNO: String, mKitchenID: String): LiveData<Int> {
        return mRepository.updateLogWoodIP(mLogWoodIP, mPortNO, mKitchenID)
    }

    fun getPaymentDevice(mLocationID: String): LiveData<SelectablePinPad> {
        return mRepository.getPaymentDevice(mLocationID)
    }

    fun updatePaymentDevice(mPaymentDevice: SelectablePinPad, mInsertUpdate: Boolean): LiveData<Int> {
        return mRepository.updatePaymentDevice(mPaymentDevice, mInsertUpdate)
    }
}