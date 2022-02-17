package com.rsl.foodnairesto.ui.main_screen.app_settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rsl.foodnairesto.repositories.AppSettingsRepository

@Suppress("UNCHECKED_CAST")
class AppSettingsViewModelFactory constructor(private val mRepository: AppSettingsRepository): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return AppSettingsViewModel(mRepository) as T
    }
}