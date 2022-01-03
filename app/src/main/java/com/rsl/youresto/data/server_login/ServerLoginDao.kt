package com.rsl.youresto.data.server_login

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rsl.youresto.data.database_download.models.LocationModel
import com.rsl.youresto.data.database_download.models.ServerModel
import com.rsl.youresto.data.server_login.models.ServerLoginModel
import com.rsl.youresto.data.server_login.models.ServerShiftModel
import java.util.*

@Dao
interface ServerLoginDao {

    @Query("SELECT * FROM LocationModel")
    fun getLocations() : LiveData<List<LocationModel>>

    @Query("SELECT * FROM ServerModel")
    fun getServers() :LiveData<List<ServerModel>>

    @Query("SELECT * FROM ServerShiftModel WHERE  mServerID =:mServerID AND mStartTimeStamp BETWEEN :mCurrentDate AND :mFutureDate")
    fun getLocalShiftDetails(
        mServerID: String,
        mCurrentDate: Date,
        mFutureDate: Date
    ): LiveData<List<ServerShiftModel>>

    @Insert
    fun insertShiftDetails(shiftModelList: List<ServerShiftModel>)

    @Query("SELECT * FROM ServerLoginModel WHERE mServerID =:mServerID  ORDER BY mDateTime DESC LIMIT 1")
    fun getServerLogin(mServerID: String): ServerLoginModel

    @Query("SELECT mDateTime FROM ServerLoginModel ORDER BY mDateTime DESC LIMIT 1")
    fun getLastRecord(): Date

    @Insert
    fun insertLoginDetails(loginDetailsList: List<ServerLoginModel>)

    @Insert
    fun insertSingleLoginDetails(loginDetail: ServerLoginModel) :Long

    @Query("UPDATE ServerShiftModel SET mEndTimeStamp =:mEndTime WHERE mServerID =:mServerID AND mEndTimeStamp IS NULL")
    fun updateShiftDetails(mEndTime: Date?, mServerID: String): Int
}