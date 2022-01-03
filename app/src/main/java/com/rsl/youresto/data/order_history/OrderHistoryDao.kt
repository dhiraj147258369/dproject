package com.rsl.youresto.data.order_history

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rsl.youresto.data.database_download.models.ProductModel
import com.rsl.youresto.data.database_download.models.ReportModel
import com.rsl.youresto.data.database_download.models.ServerModel

@Dao
interface OrderHistoryDao {

    @Query("SELECT * FROM ProductModel WHERE mProductID = :productId")
    fun getProduct(productId: String): ProductModel?


    @Query("SELECT * FROM ServerModel WHERE mServerID =:id")
    fun getServerById(id: String): ServerModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReports(reports: List<ReportModel>)


    @Query("SELECT * FROM ReportModel WHERE mDateTimeInTimeStamp BETWEEN :mFromDate AND :mToDate")
    fun getReportDataForTimeStamp(mFromDate: Long, mToDate: Long): LiveData<List<ReportModel>>

    @Query("SELECT * FROM ReportModel WHERE mOrderType =:mLocationTypeID AND mDateTimeInTimeStamp BETWEEN :mFromDate AND :mToDate")
    fun getReportDataForTimeStampAndLocationType(mFromDate: Long, mToDate: Long, mLocationTypeID: Int): LiveData<List<ReportModel>>

    @Query("SELECT * FROM ReportModel WHERE mServerName =:mServerName AND mDateTimeInTimeStamp BETWEEN :mFromDate AND :mToDate")
    fun getReportDataForTimeStampAndServerName(mFromDate: Long, mToDate: Long, mServerName: String): LiveData<List<ReportModel>>

    @Query("SELECT * FROM ReportModel WHERE mServerName =:mServerName AND mOrderType =:mLocationTypeID AND mDateTimeInTimeStamp BETWEEN :mFromDate AND :mToDate")
    fun getReportDataForTimeStampLocationTypeAndServerName(mFromDate: Long, mToDate: Long, mLocationTypeID: Int, mServerName: String): LiveData<List<ReportModel>>

    @Query("SELECT * FROM ReportModel WHERE id =:mID")
    fun getReportDataForMID(mID: String): LiveData<ReportModel>

    @Query("SELECT * FROM ReportModel ORDER BY mDateTimeInTimeStamp DESC LIMIT 1")
    fun getLastRowOfReport(): LiveData<ReportModel>
}