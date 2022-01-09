package com.rsl.youresto.ui.main_screen.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.checkout.model.NetworkCheckoutResponse
import com.rsl.youresto.data.checkout.model.PostCheckout
import com.rsl.youresto.network.Resource
import com.rsl.youresto.network.models.NetworkCartResponse
import com.rsl.youresto.repositories.NewCartRepository
import com.rsl.youresto.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class NewCartViewModel(private val repo: NewCartRepository): ViewModel() {

    fun getCarts(tableNo: String) = repo.getCarts(tableNo)

    fun getCartDataById(cartId: String) = repo.getCartDataById(cartId)

    suspend fun getCartByTable(tableId: String) = withContext(Dispatchers.IO) { repo.getCartByTable(tableId) }

    fun updateQuantity(rowId: Int, qty: BigDecimal, totalPrice: BigDecimal, tableId: String){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                repo.updateQuantity(rowId, qty, totalPrice, tableId)
            }
        }
    }

    private val _deleteCartData = MutableLiveData<Event<NetworkCartResponse>>()
    val deleteCartData: LiveData<Event<NetworkCartResponse>> get() = _deleteCartData

    fun deleteCartItem(cartProduct: CartProductModel){
        viewModelScope.launch {
            val resource = withContext(Dispatchers.IO) {
                repo.deleteCartItem(cartProduct)
            }

            if (resource.status == Resource.Status.SUCCESS){
                _deleteCartData.value = Event(resource.data!!)
            } else {
                _deleteCartData.value = Event(NetworkCartResponse())
            }
        }
    }

    private val _checkoutData = MutableLiveData<Event<NetworkCheckoutResponse>>()
    val checkoutData: LiveData<Event<NetworkCheckoutResponse>> get() = _checkoutData

    fun checkoutOrder(checkout: PostCheckout, orderId: String) {
        viewModelScope.launch {
            val resource = withContext(Dispatchers.IO) {
                repo.checkoutOrder(checkout, orderId)
            }

            if (resource.status == Resource.Status.SUCCESS){
                _checkoutData.value = Event(resource.data!!)
            } else {
                _checkoutData.value = Event(NetworkCheckoutResponse())
            }
        }
    }

    fun deleteCart(orderId: String){
        viewModelScope.launch {
            repo.deleteCart(orderId)
        }
    }

    suspend fun getPaymentMethods() = repo.getPaymentMethods()


    fun syncCarts() {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                repo.syncCarts()
            }
        }
    }

    fun deleteCart(tableId: String?, cartId: String?){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                repo.deleteCart(tableId, cartId)
            }
        }
    }

    fun getPendingOrderCartData() = repo.getPendingOrderCartData()
}