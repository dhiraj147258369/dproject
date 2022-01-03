package com.rsl.youresto.repositories

import androidx.lifecycle.LiveData
import com.rsl.youresto.data.database_download.models.TablesModel
import com.rsl.youresto.data.tables.TablesDao

class NewTablesRepository(val tablesDao: TablesDao) {

    fun getTablesData(mLocationID: String, mTableType: Int) = tablesDao.getLocalTables(mLocationID, mTableType)

    suspend fun getCartWithTableId(tableId: String) = tablesDao.getCartWithTableId(tableId)

    fun getSearchedTablesData(mLocationID: String, mTableType: Int, searchText: String): LiveData<List<TablesModel>> {
        return tablesDao.getSearchedLocalTables(mLocationID, mTableType, searchText)
    }

}