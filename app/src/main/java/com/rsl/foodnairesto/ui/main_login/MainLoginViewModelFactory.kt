package com.rsl.foodnairesto.ui.main_login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rsl.foodnairesto.repositories.LoginRepository

class MainLoginViewModelFactory(private val mainLoginRepository: LoginRepository) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainLoginViewModel(mainLoginRepository) as T
    }
}