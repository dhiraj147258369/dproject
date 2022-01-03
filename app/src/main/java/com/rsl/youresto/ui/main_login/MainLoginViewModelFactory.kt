package com.rsl.youresto.ui.main_login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rsl.youresto.repositories.LoginRepository

class MainLoginViewModelFactory(private val mainLoginRepository: LoginRepository) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainLoginViewModel(mainLoginRepository) as T
    }
}