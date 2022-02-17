package com.rsl.foodnairesto.ui.main_screen.tables_and_tabs.tables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rsl.foodnairesto.repositories.TablesRepository

@Suppress("UNCHECKED_CAST")
class TablesViewModelFactory constructor(private val mRepository: TablesRepository): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TablesViewModel(mRepository) as T
    }
}