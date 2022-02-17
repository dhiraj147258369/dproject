package com.rsl.foodnairesto.ui.main_login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rsl.foodnairesto.data.main_login.network.NetworkLogin
import com.rsl.foodnairesto.network.NetworkRestaurantData
import com.rsl.foodnairesto.network.Resource
import com.rsl.foodnairesto.network.models.PostLogin
import com.rsl.foodnairesto.repositories.LoginRepository
import com.rsl.foodnairesto.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainLoginViewModel(private val repository: LoginRepository): ViewModel(){

    private val _authData = MutableLiveData<Event<Resource<NetworkLogin>>>()
    val authData: LiveData<Event<Resource<NetworkLogin>>> get() = _authData

    fun authenticateUserWithEmail(login: PostLogin) {
        viewModelScope.launch {
            val resource = withContext(Dispatchers.IO) {
                repository.authenticateUserWithEmail(login)
            }

            if (resource.status == Resource.Status.SUCCESS){
                resource.data?.let {
                    _authData.value = Event(resource)
                }
            }
        }
    }

    private val _data = MutableLiveData<Event<NetworkRestaurantData>>()
    val data: LiveData<Event<NetworkRestaurantData>> get() = _data

    fun downloadData() {
        viewModelScope.launch {
            val resource = withContext(Dispatchers.IO){
                repository.getData()
            }

            if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
                _data.value = Event(resource.data)
            } else {
                _data.value = Event(NetworkRestaurantData(false))
            }
        }
    }

    suspend fun deleteAll() = repository.deleteAll()
}