package com.rsl.youresto.ui.main_screen.tables_and_tabs.tables

import androidx.lifecycle.*
import com.rsl.youresto.data.database_download.models.TablesModel
import com.rsl.youresto.repositories.NewTablesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewTablesViewModel(private val repo: NewTablesRepository): ViewModel() {

    val filterText = MutableLiveData<String>()

    fun getTablesData(mLocationID: String, mTableType: Int): LiveData<List<TablesModel>> {
        val data = Transformations.switchMap(filterText) {
            if (it.isBlank()) repo.getTablesData(mLocationID, mTableType) else repo.getSearchedTablesData(mLocationID, mTableType, "%$it%")
        }

        val mediatorData = MediatorLiveData<List<TablesModel>>()

        mediatorData.addSource(data){
            viewModelScope.launch {
                for (table in it){
                    val carts = withContext(Dispatchers.IO){
                        repo.getCartWithTableId(table.mTableID)
                    }
                    if (carts.isNotEmpty()) table.mTableNoOfOccupiedChairs = 2
                }
                mediatorData.postValue(it)
            }
        }

        return mediatorData
    }

    suspend fun getTableWithLocation() = repo.getTableWithLocation()
}