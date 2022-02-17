package com.rsl.foodnairesto.repositories

import androidx.lifecycle.LiveData
import com.rsl.foodnairesto.data.database_download.models.TablesModel
import com.rsl.foodnairesto.data.tables.TablesDao
import com.rsl.foodnairesto.utils.AppPreferences

class NewTablesRepository(private val tablesDao: TablesDao, val prefs: AppPreferences) {

    fun getTablesData(mLocationID: String, mTableType: Int) = tablesDao.getLocalTables(mLocationID, mTableType)

    fun getTablesDataAll( mTableType: Int) = tablesDao.getLocalTablesAll( mTableType)

    suspend fun getCartWithTableId(tableId: String) = tablesDao.getCartWithTableId(tableId)

    fun getSearchedTablesData(mLocationID: String, mTableType: Int, searchText: String): LiveData<List<TablesModel>> {
        return tablesDao.getSearchedLocalTables(mLocationID, mTableType, searchText)
    }
    fun getSearchedTablesDataAll( mTableType: Int, searchText: String): LiveData<List<TablesModel>> {
        return tablesDao.getSearchedLocalTablesAll( mTableType, searchText)
    }

    suspend fun getTableWithLocation() = tablesDao.getTableWithLocation(prefs.getSelectedLocation())
}