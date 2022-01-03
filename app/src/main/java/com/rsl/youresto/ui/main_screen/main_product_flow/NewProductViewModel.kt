package com.rsl.youresto.ui.main_screen.main_product_flow

import androidx.lifecycle.*
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.database_download.models.ProductGroupModel
import com.rsl.youresto.network.Resource
import com.rsl.youresto.network.models.NetworkCartResponse
import com.rsl.youresto.repositories.NewProductRepository
import com.rsl.youresto.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewProductViewModel(private val repo: NewProductRepository): ViewModel() {

    fun getProductGroups() : LiveData<List<ProductGroupModel>> = repo.getProductGroups()

    fun getProductCategories(mGroupID : String?) = repo.getProductCategories(mGroupID)

    val filterText = MutableLiveData<String>()

    fun getProducts(categoryId : String) = Transformations.switchMap(filterText) {
            if (it.isNotBlank()) repo.getSearchedProducts(categoryId, "%$it%")
            else repo.getProducts(categoryId)
        }


    fun getProduct(productId: String) =
        repo.getProduct(productId)


    fun getProductIngredients(ingredientIds: List<String>) = repo.getProductIngredients(ingredientIds)


    private val _cartData = MutableLiveData<Event<NetworkCartResponse>>()
    val cartData: LiveData<Event<NetworkCartResponse>> get() = _cartData

    fun submitCartProduct(cartProduct: CartProductModel) {
        viewModelScope.launch {
            val resource = withContext(Dispatchers.IO) {
                repo.submitCartProduct(cartProduct)
            }

            if (resource.status == Resource.Status.SUCCESS){
                _cartData.value = Event(resource.data!!)
            } else {
                _cartData.value = Event(NetworkCartResponse(status = false, msg = "Something went wrong please try again"))
            }
        }
    }

    suspend fun getCartProductByID(cartProductId: String) = repo.getCartProductByID(cartProductId)

    suspend fun getQuickServiceTable() = repo.getQuickServiceTable()
}