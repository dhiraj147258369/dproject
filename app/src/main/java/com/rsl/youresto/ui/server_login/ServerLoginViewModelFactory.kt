package com.rsl.youresto.ui.server_login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rsl.youresto.repositories.ServerLoginRepository

@Suppress("UNCHECKED_CAST")
class ServerLoginViewModelFactory(private val mRepository: ServerLoginRepository) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return ServerLoginViewModel(mRepository) as T
    }
}