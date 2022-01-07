package com.rsl.youresto.data.tables

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.database_download.models.TablesModel
import com.rsl.youresto.data.tables.models.LocalTableGroupModel
import com.rsl.youresto.data.tables.models.ServerTableGroupModel
import java.util.ArrayList

@Dao
interface TablesDao {

    @Query("SELECT * FROM TablesModel WHERE mLocationID = :mLocationID AND mTableType =:mTableType")
    fun getLocalTables(mLocationID: String, mTableType: Int): LiveData<List<TablesModel>>

    @Query("SELECT * FROM TablesModel WHERE mLocationID = :mLocationID AND mTableType =:mTableType AND mTableID LIKE :searchText")
    fun getSearchedLocalTables(mLocationID: String, mTableType: Int, searchText: String): LiveData<List<TablesModel>>



    @Query("SELECT * FROM CartProductModel WHERE mTableID =:tableId")
    suspend fun getCartWithTableId(tableId: String): List<CartProductModel>

    @Query("SELECT * FROM TablesModel WHERE mLocationID =:locationId")
    suspend fun getTableWithLocation(locationId: String): List<TablesModel>



    @Query("SELECT * FROM TablesModel WHERE mTableID =:mTableID")
    fun getTable(mTableID: String): LiveData<TablesModel>

    @Query("SELECT * FROM TablesModel WHERE mTableID =:mTableID")
    fun getLocalTableData(mTableID: String): TablesModel

    @Query("SELECT * FROM TablesModel WHERE mTableID =:mTableID")
    fun getLocalTable(mTableID: String): TablesModel

//    @Query("SELECT * FROM LocalTableGroupModel WHERE mTableID =:mTableID")
//    fun getLocalTableData(mTableID: String): LiveData<List<LocalTableGroupModel>>

    @Query("SELECT mTableTotalNoOfChairs FROM TablesModel WHERE mTableID =:mTableID")
    fun getNoOfChairsForTable(mTableID: String?): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun storeTableGroups(mGroup: LocalTableGroupModel): Long

    @Query("DELETE FROM LocalTableGroupModel WHERE mTableID =:mTableID")
    fun deleteTableGroups(mTableID: String): Int

    @Query("UPDATE TablesModel SET mTableNoOfOccupiedChairs =:mChairs WHERE mTableID =:mTableID")
    fun updateTableOccupiedChairs(mTableID: String, mChairs: Int)

    @Query("SELECT * FROM LocalTableGroupModel WHERE mTableID =:mTableID")
    fun getTableGroupsAndSeats(mTableID: String): List<LocalTableGroupModel>

    @Query("SELECT * FROM LocalTableGroupModel WHERE mLocationID =:mLocationID")
    fun getAllTableGroupsAndSeatsByLocation(mLocationID: String): List<LocalTableGroupModel>

    @Query("DELETE FROM TablesModel WHERE mLocationID =:mLocationID")
    fun deleteLocationTables(mLocationID: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTables(mTableList: List<TablesModel>)

    @Query("DELETE FROM LocalTableGroupModel WHERE mLocationID =:mLocationID")
    fun deleteTableGroupsByLocation(mLocationID: String): Int

    @Query("DELETE FROM LocalTableGroupModel WHERE mTableID IN (:mTableIDs)")
    fun deleteTableGroupsByTableNOs(mTableIDs: List<String>) : Int

    @Insert/*(onConflict = OnConflictStrategy.REPLACE)*/
    fun insertLocalGroups(mGroupList: List<LocalTableGroupModel>)

    @Query("UPDATE TablesModel SET mTableNoOfOccupiedChairs =:mOccupiedChairs, mOccupiedByUser =:mServer, mOccupiedByUserID =:mServerID, mGroupList =:mGroupList WHERE mTableID =:mTableID")
    fun occupyTable(
        mOccupiedChairs: Int, mServer: String, mServerID: String, mGroupList: ArrayList<ServerTableGroupModel>, mTableID: String
    ): Int

    @Query("DELETE FROM CartProductModel WHERE mTableID =:mTableID")
    fun clearCartForTable(mTableID: String): Int

    @Query("DELETE FROM CheckoutModel WHERE mTableID =:mTableID")
    fun clearCheckoutForTable(mTableID: String): Int

    @Query("UPDATE TablesModel SET mTableNoOfOccupiedChairs = 0, mOccupiedByUser = '', mOccupiedByUserID = '', mGroupList =:mGroupList WHERE mTableID =:mTableID")
    fun updateTable(mGroupList: ArrayList<ServerTableGroupModel>, mTableID: String): Int

}