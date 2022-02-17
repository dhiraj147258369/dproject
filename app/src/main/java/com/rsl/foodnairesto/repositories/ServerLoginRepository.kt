package com.rsl.foodnairesto.repositories

import com.rsl.foodnairesto.data.server_login.ServerLoginDao

class ServerLoginRepository(private val serverLoginDao: ServerLoginDao) {

    fun getLocations() = serverLoginDao.getLocations()

    fun getServers() = serverLoginDao.getServers()

}