package com.rsl.youresto.ui.database_download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rsl.youresto.repositories.DatabaseDownloadRepository

@Suppress("UNCHECKED_CAST")
class DatabaseDownloadViewModelFactory(private val databaseDownloadRepository: DatabaseDownloadRepository) :
    ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return DatabaseDownloadViewModel(databaseDownloadRepository) as T
    }
}