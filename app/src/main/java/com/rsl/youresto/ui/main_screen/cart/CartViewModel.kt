package com.rsl.youresto.ui.main_screen.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.main_login.network.Login
import com.rsl.youresto.repositories.CartRepository
import java.math.BigDecimal

class CartViewModel(private val mRepository: CartRepository) : ViewModel(){

    fun insertCartProduct(mProductCartModel: CartProductModel): LiveData<Long> {
        return mRepository.insertCartProduct(mProductCartModel)
    }

    fun insertBulkCartProduct(mProductList: List<CartProductModel>): LiveData<LongArray> {
        return mRepository.insertBulkCartProduct(mProductList)
    }

    fun submitCartItemToTheServer(mProductCartModel: CartProductModel): LiveData<Int> {
        return mRepository.submitCartItemToTheServer(mProductCartModel)
    }

    fun getCartData(mTableNO: Int, mGroupName: String): LiveData<List<CartProductModel>> {
        return mRepository.getCartData(mTableNO, mGroupName)
    }

    fun getCartWithoutLiveData(mTableNO: String, mGroupName: String): LiveData<List<CartProductModel>> {
        return mRepository.getCartWithoutLiveData(mTableNO, mGroupName)
    }

    fun getCartDataByTable(mTableNO: Int): LiveData<List<CartProductModel>> {
        return mRepository.getCartDataByTable(mTableNO)
    }

    fun getCartDataWithCartID(mCartID: String): LiveData<List<CartProductModel>> {
        return mRepository.getCartQuickServiceData(mCartID)
    }

    fun getTableCartData(mTableNO: Int): LiveData<List<CartProductModel>> {
        return mRepository.getTableCartData(mTableNO)
    }

    fun getTableCartDataForCartID(mTableNO: Int, mCartID: String): LiveData<List<CartProductModel>> {
        return mRepository.getTableCartDataForQuickService(mTableNO, mCartID)
    }

    fun syncCart(mLocationID: String) :LiveData<Int>{
        return mRepository.syncCart(mLocationID)
    }

    fun updateProductQuantity(mRowID: Int, mProductQuantity: BigDecimal, mProductTotalPrice: BigDecimal): LiveData<Int> {
        return mRepository.updateProductQuantity(mRowID,mProductQuantity,mProductTotalPrice)
    }

    fun updateProductToServer(mCart: CartProductModel): LiveData<Int> {
        return mRepository.updateProductToServer(mCart)
    }

    fun getCartProduct(mRowID: Int): LiveData<CartProductModel> {
        return mRepository.getCartProduct(mRowID)
    }

    fun deleteCartOnServer(mCart: CartProductModel): LiveData<Int> {
        return mRepository.deleteCartOnServer(mCart)
    }

    fun getPendingOrderCartData(mLocationID: String, mServerID: String): LiveData<List<CartProductModel>> {
        return mRepository.getPendingOrderCartData(mLocationID, mServerID)
    }

    fun getRestaurantDetails(): LiveData<Login> {
        return mRepository.getRestaurantDetails()
    }

    fun getCartProductByID(mCartProductID: String): LiveData<CartProductModel>{
        return mRepository.getCartProductByID(mCartProductID)
    }

    fun getAllCartDataIrrespectiveOfLocations(): LiveData<List<CartProductModel>> {
        return mRepository.getAllCartDataIrrespectiveOfLocations()
    }

    fun updateCartProduct(mCartProduct: CartProductModel): LiveData<Int>{
        return mRepository.updateCartProduct(mCartProduct)
    }

    fun changeKOTFlag(mPrinterID: String, mCartID: String): LiveData<Int> {
        return mRepository.changeKOTFlag(mPrinterID, mCartID)
    }

    fun submitRepeatOrderItemsToServer(mFinalProductList: ArrayList<CartProductModel>): LiveData<Int> {
        return mRepository.submitRepeatOrderItemsToServer(mFinalProductList)
    }
}