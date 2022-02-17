package com.rsl.foodnairesto.data.server_login

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.rsl.foodnairesto.data.database_download.models.LocationModel
import com.rsl.foodnairesto.data.database_download.models.ServerModel

@Dao
interface ServerLoginDao {

    @Query("SELECT * FROM LocationModel")
    fun getLocations() : LiveData<List<LocationModel>>

    @Query("SELECT * FROM ServerModel")
    fun getServers() :LiveData<List<ServerModel>>
}