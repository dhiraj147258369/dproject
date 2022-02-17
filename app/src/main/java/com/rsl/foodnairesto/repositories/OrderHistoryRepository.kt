package com.rsl.foodnairesto.repositories

import androidx.lifecycle.LiveData
import com.rsl.foodnairesto.data.database_download.models.ReportModel
import com.rsl.foodnairesto.data.order_history.OrderHistoryDao

class OrderHistoryRepository constructor(private val orderHistoryDao: OrderHistoryDao) {

    companion object {
        @Volatile
        private var sInstance: OrderHistoryRepository? = null

        fun getInstance(orderHistoryDao: OrderHistoryDao) =
            sInstance ?: synchronized(this){
                sInstance ?: OrderHistoryRepository(orderHistoryDao)
            }

    }

    fun getReportDataForTimeStamp(mFromDate: Long, mToDate: Long, mLocationTypeID: Int, mServerName: String): LiveData<List<ReportModel>> {
        return if(mLocationTypeID == 0 && mServerName == "ALL") orderHistoryDao.getReportDataForTimeStamp(mFromDate, mToDate)
        else if(mLocationTypeID != 0 && mServerName == "ALL") orderHistoryDao.getReportDataForTimeStampAndLocationType(mFromDate, mToDate, mLocationTypeID)
        else if(mLocationTypeID == 0 && mServerName != "ALL") orderHistoryDao.getReportDataForTimeStampAndServerName(mFromDate, mToDate, mServerName)
        else orderHistoryDao.getReportDataForTimeStampLocationTypeAndServerName(mFromDate, mToDate, mLocationTypeID, mServerName)
    }

    fun getReportDataForMID(mID: String): LiveData<ReportModel> {
        return orderHistoryDao.getReportDataForMID(mID)
    }

    fun getLastRowOfReport(): LiveData<ReportModel> {
        return orderHistoryDao.getLastRowOfReport()
    }
}