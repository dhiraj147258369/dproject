package com.rsl.youresto.ui.main_screen.order_history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rsl.youresto.network.Resource
import com.rsl.youresto.network.models.NetworkReportModel
import com.rsl.youresto.repositories.NewOrdersRepository
import com.rsl.youresto.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewOrdersViewModel(private val repo: NewOrdersRepository): ViewModel() {

    private val _reportData = MutableLiveData<Event<NetworkReportModel>>()
    val reportData: LiveData<Event<NetworkReportModel>> get() = _reportData

    fun getCartReportsFromNetwork(){
        viewModelScope.launch {
            val resource = withContext(Dispatchers.IO){
                repo.getCartReports()
            }

            if (resource.status == Resource.Status.SUCCESS){
                _reportData.value = Event(resource.data!!)
            } else {
                _reportData.value = Event(NetworkReportModel())
            }
        }
    }
}