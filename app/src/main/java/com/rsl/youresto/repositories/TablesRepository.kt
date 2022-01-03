package com.rsl.youresto.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log.e
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rsl.youresto.data.database_download.models.TablesModel
import com.rsl.youresto.data.tables.TableDataSource
import com.rsl.youresto.data.tables.TablesDao
import com.rsl.youresto.data.tables.models.LocalTableGroupModel
import com.rsl.youresto.data.tables.models.LocalTableSeatModel
import com.rsl.youresto.utils.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

@SuppressLint("LogNotTimber")
class TablesRepository constructor(private val tablesDao: TablesDao, private val tableDataSource: TableDataSource?) {

    companion object {
        @Volatile
        private var sInstance: TablesRepository? = null

        fun getInstance(tablesDao: TablesDao, tableDataSource: TableDataSource?) = sInstance ?: synchronized(this) {
            sInstance ?: TablesRepository(tablesDao, tableDataSource)
        }
    }

    private var parentJob = Job()
    private val coRoutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    fun getTablesData(mLocationID: String, mTableType: Int): LiveData<List<TablesModel>> {
        return tablesDao.getLocalTables(mLocationID, mTableType)
    }

    fun getTable(mTableID: String): LiveData<TablesModel> {
        return tablesDao.getTable(mTableID)
    }

    fun getLocalTableData(mTableID: String): LiveData<TablesModel> {
        val mTableData = MutableLiveData<TablesModel>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) { mTableData.postValue(tablesDao.getLocalTableData(mTableID)) }
        return mTableData
    }

    fun getNoOfChairsForTable(mTableID: String?): LiveData<Int> {
        val mSeatData = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) { mSeatData.postValue(tablesDao.getNoOfChairsForTable(mTableID)) }
        return mSeatData
    }

    fun storeTableGroups(mGroup: LocalTableGroupModel, mTableID: String): LiveData<Long> {
        val mStoreGroupData = MutableLiveData<Long>()

        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            tablesDao.updateTableOccupiedChairs(mTableID, mGroup.mSeatList!!.size)
            mStoreGroupData.postValue(tablesDao.storeTableGroups(mGroup))
        }

        return mStoreGroupData
    }

    fun deleteTableGroups(mTableID: String): LiveData<Int> {
        val mDeleteGroupData = MutableLiveData<Int>()

        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) { mDeleteGroupData.postValue(tablesDao.deleteTableGroups(mTableID)) }

        return mDeleteGroupData
    }

    fun getTableGroupsAndSeats(mTableID: String): LiveData<List<LocalTableGroupModel>> {
        val mTableData = MutableLiveData<List<LocalTableGroupModel>>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) { mTableData.postValue(tablesDao.getTableGroupsAndSeats(mTableID)) }
        return mTableData
    }

    fun syncTables(mLocationID: String): LiveData<Int> {
        val syncData = MutableLiveData<Int>()
        tableDataSource!!.syncTables(mLocationID)!!.observeForever {
            when {
                it != null -> when {
                    it.isNotEmpty() -> {
                        val scope = CoroutineScope(coRoutineContext)
                        scope.launch(Dispatchers.IO) {
                            val mDelete = tablesDao.deleteLocationTables(mLocationID)

                            when {
                                mDelete > -1 -> {
                                    tablesDao.insertTables(it)
                                    storeLocalGroups(
                                        ArrayList(it),
                                        tablesDao.getAllTableGroupsAndSeatsByLocation(mLocationID),
                                        mLocationID
                                    )

                                    syncData.postValue(1)
                                }
                            }
                        }
                    }
                    else -> syncData.postValue(2)
                }
            }
        }
        return syncData
    }

    private fun storeLocalGroups(
        mServerTableList: ArrayList<TablesModel>,
        mLocalTableGroupList: List<LocalTableGroupModel>,
        mLocationID: String
    ) {
        val mGroupList: ArrayList<LocalTableGroupModel> = ArrayList()

        when {
            mLocalTableGroupList.isNotEmpty() -> for (i in 0 until mServerTableList.size) {

                val mServerGroupList = mServerTableList[i].mGroupList

                when {
                    mServerGroupList!!.isNotEmpty() -> for (j in 0 until mServerGroupList.size) {

                        val mServerGroup = mServerGroupList[j]
                        val mServerSeatList = mServerGroup.mSeatList!!

                        var mHasLocalGroup = true
                        loop1@ for (k in 0 until mLocalTableGroupList.size) {

                            val mTableLocalGroup = mLocalTableGroupList[k]
                            val mStoredLocalSeatList = mTableLocalGroup.mSeatList

                            when {
                                mServerGroup.mTableID == mTableLocalGroup.mTableID && mServerGroup.mGroupName == mTableLocalGroup.mGroupName -> {

                                    val mLocalSeatList: ArrayList<LocalTableSeatModel> = ArrayList()

                                    for (l in 0 until mServerSeatList.size) {
                                        val mServerSeat = mServerSeatList[l]

                                        var mHasLocalSeat = true
                                        loop2@ for (m in 0 until mStoredLocalSeatList!!.size) {
                                            when (mServerSeat.mSeatNO) {
                                                mStoredLocalSeatList[m].mSeatNO -> {

                                                    val mLocalSeat = LocalTableSeatModel(
                                                        mServerSeat.mSeatNO,
                                                        mServerGroup.mGroupName,
                                                        mServerTableList[i].mTableNo,
                                                        mServerTableList[i].mTableID,
                                                        true, mStoredLocalSeatList[m].isPaid
                                                    )
                                                    mLocalSeatList.add(mLocalSeat)
                                                    mHasLocalSeat = true
                                                    break@loop2
                                                }
                                                else -> mHasLocalSeat = false
                                            }
                                        }

                                        when {
                                            !mHasLocalSeat -> {
                                                val mLocalSeat = LocalTableSeatModel(
                                                    mServerSeat.mSeatNO,
                                                    mServerGroup.mGroupName,
                                                    mServerTableList[i].mTableNo,
                                                    mServerTableList[i].mTableID,
                                                    true, isPaid = false
                                                )
                                                mLocalSeatList.add(mLocalSeat)
                                            }
                                        }
                                    }

                                    val mLocalGroup = LocalTableGroupModel(
                                        mServerGroup.mGroupName,
                                        true,
                                        mServerTableList[i].mTableNo,
                                        mServerTableList[i].mTableID,
                                        mServerTableList[i].mLocationID,
                                        mLocalSeatList
                                    )

                                    mGroupList.add(mLocalGroup)


                                    mHasLocalGroup = true
                                    break@loop1
                                }
                                else -> mHasLocalGroup = false
                            }

                        }

                        when {
                            !mHasLocalGroup -> {
                                val mLocalSeatList: ArrayList<LocalTableSeatModel> = ArrayList()
                                for (l in 0 until mServerSeatList.size) {

                                    val mServerSeat = mServerSeatList[l]

                                    val mLocalSeat = LocalTableSeatModel(
                                        mServerSeat.mSeatNO,
                                        mServerGroup.mGroupName,
                                        mServerTableList[i].mTableNo,
                                        mServerTableList[i].mTableID,
                                        true, isPaid = false
                                    )
                                    mLocalSeatList.add(mLocalSeat)
                                }

                                val mLocalGroup = LocalTableGroupModel(
                                    mServerGroup.mGroupName,
                                    true,
                                    mServerTableList[i].mTableNo,
                                    mServerTableList[i].mTableID,
                                    mServerTableList[i].mLocationID,
                                    mLocalSeatList
                                )

                                mGroupList.add(mLocalGroup)

                            }
                        }

                    }
                }

            }
            else -> {
                for (i in 0 until mServerTableList.size) {
                    val mServerGroupList = mServerTableList[i].mGroupList

                    when {
                        mServerGroupList!!.isNotEmpty() ->
                            for (j in 0 until mServerGroupList.size) {

                                val mServerGroup = mServerGroupList[j]
                                val mServerSeatList = mServerGroup.mSeatList!!

                                val mLocalSeatList: ArrayList<LocalTableSeatModel> = ArrayList()
                                for (l in 0 until mServerSeatList.size) {

                                    val mServerSeat = mServerSeatList[l]

                                    val mLocalSeat = LocalTableSeatModel(
                                        mServerSeat.mSeatNO,
                                        mServerGroup.mGroupName,
                                        mServerTableList[i].mTableNo,
                                        mServerTableList[i].mTableID,
                                        true, isPaid = false
                                    )
                                    mLocalSeatList.add(mLocalSeat)
                                }

                                val mLocalGroup = LocalTableGroupModel(
                                    mServerGroup.mGroupName,
                                    true,
                                    mServerTableList[i].mTableNo,
                                    mServerTableList[i].mTableID,
                                    mServerTableList[i].mLocationID,
                                    mLocalSeatList
                                )

                                mGroupList.add(mLocalGroup)
                            }
                    }
                }
            }
        }


        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {

            val mSuccess = tablesDao.deleteTableGroupsByLocation(mLocationID)

            when {
                mSuccess > -1 -> tablesDao.insertLocalGroups(mGroupList)
            }
        }
    }

    fun occupyTable(mTableModel: TablesModel, mServiceType: Int): LiveData<TablesModel> {
        val mOccupyTableData = MutableLiveData<TablesModel>()
        tableDataSource!!.occupyTable(mTableModel)!!.observeForever {
            val scope = CoroutineScope(coRoutineContext)
            scope.launch(Dispatchers.IO) {
                var mUpdate = 0
                if (it != null) {

                    mUpdate = tablesDao.occupyTable(
                        it.mTableNoOfOccupiedChairs,
                        it.mOccupiedByUser,
                        it.mOccupiedByUserID,
                        it.mGroupList!!,
                        it.mTableID
                    )

                    if (mUpdate > -1) {
                        e(javaClass.simpleName, "occupyTable: GroupList: ${it.mGroupList!!.size}")
                        mOccupyTableData.postValue(it)
                    }
                } else {
//                    mOccupyTableData.postValue(null)
                }

                e(javaClass.simpleName, "occupyTable: update: $mUpdate")
            }
        }
        return mOccupyTableData
    }

    fun clearTable(mTableID: String, mContext: Context): LiveData<Int> {
        val mClearTableData = MutableLiveData<Int>()

        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {

            Handler(Looper.getMainLooper()).post {
                Network.isNetworkAvailableWithInternetAccess(mContext).observeForever {
                    when {
                        it -> tableDataSource!!.clearTable(mTableID).observeForever { integer ->
                            when {
                                integer != null ->
                                    when (integer) {
                                        1 -> {
                                            val mScope = CoroutineScope(coRoutineContext)
                                            mScope.launch(Dispatchers.IO) {
                                                val mClearCart = tablesDao.clearCartForTable(mTableID)
                                                when {
                                                    mClearCart > -1 -> {
                                                        val mUpdateTable = tablesDao.updateTable(ArrayList(), mTableID)
                                                        when {
                                                            mUpdateTable > 0 -> tablesDao.clearCheckoutForTable(mTableID)
                                                        }
                                                    }
                                                }
                                                tablesDao.deleteTableGroups(mTableID)
                                            }
                                            mClearTableData.postValue(integer)
                                        }
                                        else -> mClearTableData.postValue(0)
                                    }
                            }
                        }
                        else -> mClearTableData.postValue(-1)
                    }
                }
            }
        }

        return mClearTableData
    }

    fun updateTableData(mTableModel: TablesModel, mGroupName: String): LiveData<Int> {
        val mUpdateTableData = MutableLiveData<Int>()
        tableDataSource!!.updateTableData(mTableModel, mGroupName)
            .observeForever { integer -> mUpdateTableData.postValue(integer) }
        return mUpdateTableData
    }
}