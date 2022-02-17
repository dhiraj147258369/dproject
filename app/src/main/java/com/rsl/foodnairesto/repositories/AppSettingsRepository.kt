package com.rsl.foodnairesto.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rsl.foodnairesto.data.app_settings.AppSettingsDao
import com.rsl.foodnairesto.data.app_settings.model.SelectablePinPad
import com.rsl.foodnairesto.data.database_download.models.KitchenModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class AppSettingsRepository constructor(private val appSettingsDao: AppSettingsDao) {

    companion object {
        @Volatile
        private var sInstance: AppSettingsRepository? = null

        fun getInstance(appSettingsDao: AppSettingsDao) = sInstance ?: synchronized(this) {
            sInstance ?: AppSettingsRepository(appSettingsDao)
        }
    }

    private var parentJob = Job()
    private val coRoutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    fun getAllKitchenPrinters(): LiveData<List<KitchenModel>> {
        return appSettingsDao.getAllKitchenPrinters()
    }

    fun updateKitchenPrinterPaper(
        mKitchenID: String, mSelectedPrinterName: String,
        mSelectedPrinterPaperSize: Int, mPrinterType: Int
    ): LiveData<Int> {

        val mUpdateKitchenPrinterPaper = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mUpdateKitchenPrinterPaper.postValue(appSettingsDao.updateKitchenPrinterPaper(mKitchenID, mSelectedPrinterName, mSelectedPrinterPaperSize, mPrinterType))
        }

        return mUpdateKitchenPrinterPaper
    }

    fun getKitchenPrinter(mKitchenID: String): LiveData<KitchenModel> {
        return appSettingsDao.getKitchenPrinter(mKitchenID)
    }

    fun updateKitchenBluetoothPrinter(mKitchenID: String, mSelectedPrinterName: String, mPrinterType: Int): LiveData<Int> {
        val mUpdateBluetoothKitchenPrinter = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mUpdateBluetoothKitchenPrinter.postValue(appSettingsDao.updateKitchenBluetoothPrinter(mKitchenID,mSelectedPrinterName,mPrinterType))
        }

        return mUpdateBluetoothKitchenPrinter
    }

    fun updateKitchenNetworkPrinter(mKitchenID: String, mPrinterIPAddress: String, mPrinterPortNO: String): LiveData<Int> {
        val mUpdateNetworkKitchenPrinter = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mUpdateNetworkKitchenPrinter.postValue(appSettingsDao.updateKitchenNetworkPrinter(mKitchenID,mPrinterIPAddress,mPrinterPortNO))
        }

        return mUpdateNetworkKitchenPrinter
    }

    fun updateLogWoodIP(mLogWoodIP: String,mPortNO: String, mKitchenID: String): LiveData<Int> {
        val mUpdateLogWoodIP = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mUpdateLogWoodIP.postValue(appSettingsDao.updateLogWoodIP(mLogWoodIP, mPortNO, mKitchenID))
        }

        return mUpdateLogWoodIP
    }

    fun clearKitchenPrinterData(mKitchenID: String): LiveData<Int> {
        val mClearKitchenPrinter = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mClearKitchenPrinter.postValue(appSettingsDao.clearKitchenPrinterData(mKitchenID))
        }

        return mClearKitchenPrinter
    }

    fun getPaymentDevice(mLocationID: String): LiveData<SelectablePinPad> {
        val mPaymentDeviceData = MutableLiveData<SelectablePinPad>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mPaymentDeviceData.postValue(appSettingsDao.getPaymentDevice(mLocationID))
        }
        return mPaymentDeviceData
    }

    fun updatePaymentDevice(mPaymentDevice: SelectablePinPad, mInsertUpdate: Boolean): LiveData<Int> {
        val mUpdatePaymentDevice = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            if (mInsertUpdate)
                mUpdatePaymentDevice.postValue(appSettingsDao.insertPaymentDevice(mPaymentDevice).toInt())
            else
                mUpdatePaymentDevice.postValue(appSettingsDao.updatePaymentDevice(mPaymentDevice))
        }

        return mUpdatePaymentDevice
    }
}