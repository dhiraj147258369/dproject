package com.rsl.youresto.repositories

import com.rsl.youresto.data.server_login.ServerLoginDao

class ServerLoginRepository(private val serverLoginDao: ServerLoginDao) {

    fun getLocations() = serverLoginDao.getLocations()

    fun getServers() = serverLoginDao.getServers()

}