package com.rsl.youresto.ui.server_login

import androidx.lifecycle.ViewModel
import com.rsl.youresto.repositories.ServerLoginRepository

class ServerLoginViewModel(private val repo: ServerLoginRepository): ViewModel(){

    fun getLocations() = repo.getLocations()

    fun getServers() = repo.getServers()
}