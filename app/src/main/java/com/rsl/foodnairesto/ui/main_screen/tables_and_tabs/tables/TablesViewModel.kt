package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.tables

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rsl.foodnairesto.data.database_download.models.TablesModel
import com.rsl.foodnairesto.data.tables.models.LocalTableGroupModel
import com.rsl.foodnairesto.repositories.TablesRepository

class TablesViewModel constructor(private val mRepository: TablesRepository) :ViewModel() {

    fun getTablesData(mLocationID: String, mTableType: Int): LiveData<List<TablesModel>> {
        return mRepository.getTablesData(mLocationID, mTableType)
    }

    fun getNoOfChairsForTable(mTableID: String?): LiveData<Int> {
        return mRepository.getNoOfChairsForTable(mTableID)
    }

    fun storeTableGroups(mGroup: LocalTableGroupModel, mTableID: String): LiveData<Long> {
        return mRepository.storeTableGroups(mGroup, mTableID)
    }

    fun deleteTableGroups(mTableID: String): LiveData<Int> {
        return mRepository.deleteTableGroups(mTableID)
    }

    fun getTableGroupsAndSeats(mTableID: String): LiveData<List<LocalTableGroupModel>> {
        return mRepository.getTableGroupsAndSeats(mTableID)
    }

    fun syncTables(mLocationID: String) : LiveData<Int>{
        return mRepository.syncTables(mLocationID)
    }

    fun getTable(mTableID: String): LiveData<TablesModel> {
        return mRepository.getTable(mTableID)
    }

    fun getLocalTableData(mTableID: String): LiveData<TablesModel> {
        return mRepository.getLocalTableData(mTableID)
    }

    fun occupyTable(mTableModel: TablesModel, mServiceType: Int): LiveData<TablesModel> {
        return mRepository.occupyTable(mTableModel, mServiceType)
    }

    fun clearTable(mTableID: String, mContext: Context): LiveData<Int> {
        return mRepository.clearTable(mTableID, mContext)
    }

    fun updateTableData(mTableModel: TablesModel, mGroupName: String): LiveData<Int> {
        return mRepository.updateTableData(mTableModel, mGroupName)
    }
}