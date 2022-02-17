package com.rsl.foodnairesto.ui.main_screen.order_history

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rsl.foodnairesto.data.database_download.models.ReportModel
import com.rsl.foodnairesto.repositories.OrderHistoryRepository

class OrderHistoryViewModel(val mRepository: OrderHistoryRepository): ViewModel() {

    fun getReportDataForTimeStamp(mFromDate: Long, mToDate: Long, mLocationTypeID: Int, mServerName: String): LiveData<List<ReportModel>> {
        return mRepository.getReportDataForTimeStamp(mFromDate, mToDate, mLocationTypeID, mServerName)
    }

    fun getReportDataForMID(mID: String): LiveData<ReportModel> {
        return mRepository.getReportDataForMID(mID)
    }

    fun getLastRowOfReport(): LiveData<ReportModel> {
        return mRepository.getLastRowOfReport()
    }
}