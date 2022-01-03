package com.rsl.youresto.data.cart

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.database_download.models.KitchenModel
import com.rsl.youresto.data.database_download.models.PaymentMethodModel
import com.rsl.youresto.data.database_download.models.ProductGroupModel
import com.rsl.youresto.data.main_login.network.Login
import java.math.BigDecimal

@Dao
interface CartDao {

    //new cart methods
    @Delete
    fun deleteCartItem(cartProduct: CartProductModel): Int

    @Query("SELECT * FROM CartProductModel WHERE mTableID =:mTableNO")
    fun getCarts(mTableNO: String): List<CartProductModel>

    @Query("SELECT * FROM CartProductModel WHERE mTableID =:mTableNO")
    fun getCartData(mTableNO: String): LiveData<List<CartProductModel>>

    @Query("SELECT * FROM CartProductModel WHERE mCartID =:mCartId")
    fun getCartDataById(mCartId: String): LiveData<List<CartProductModel>>

    @Query("SELECT * FROM CartProductModel WHERE mCartID =:mCartId")
    fun getCartsById(mCartId: String): List<CartProductModel>



    @Query("SELECT * FROM KitchenModel")
    fun getKitchens(): List<KitchenModel>

    @Query("UPDATE CartProductModel SET mProductQuantity =:qty, mProductTotalPrice =:totalPrice WHERE mID =:rowId")
    fun updateQuantity(rowId: Int, qty: BigDecimal, totalPrice: BigDecimal): Int

    @Query("DELETE FROM CartProductModel WHERE mTableID =:mTableNO")
    fun deleteCart(mTableNO: String): Int

    @Query("SELECT * FROM PaymentMethodModel")
    fun getPaymentMethods(): List<PaymentMethodModel>


    @Insert
    fun insertCartProduct(mProductCartModel: CartProductModel): Long

    @Query("UPDATE CartProductModel SET mCartProductID =:mCartProductID WHERE mID =:mRowID")
    fun updateCartProductID(mCartProductID: String, mRowID: Int): Int

    @Query("SELECT mID FROM CartProductModel ORDER BY mID DESC LIMIT 1")
    fun getCartRowID(): Int

    @Query("SELECT * FROM CartProductModel WHERE mTableNO =:mTableNO AND mGroupName =:mGroupName")
    fun getCartData(mTableNO: Int, mGroupName: String): LiveData<List<CartProductModel>>

    @Query("SELECT * FROM CartProductModel WHERE mTableID =:mTableNO")
    fun getCartWithoutLiveData(mTableNO: String): List<CartProductModel>

    @Query("SELECT * FROM CartProductModel WHERE mTableNO =:mTableNO")
    fun getCartDataByTable(mTableNO: Int): LiveData<List<CartProductModel>>

    @Query("SELECT * FROM CartProductModel WHERE mCartID =:mCartID")
    fun getCartQuickServiceData(mCartID: String): List<CartProductModel>

    @Query("SELECT * FROM CartProductModel WHERE mTableNO =:mTableNO")
    fun getTableCartData(mTableNO: Int): LiveData<List<CartProductModel>>

    @Query("SELECT * FROM CartProductModel WHERE mTableNO =:mTableNO AND mCartID =:mCartID")
    fun getTableCartDataForQuickService(mTableNO: Int, mCartID: String): LiveData<List<CartProductModel>>

    @Query("SELECT * FROM ProductGroupModel")
    fun getGroups(): List<ProductGroupModel>

    @Query("DELETE FROM CartProductModel WHERE mLocationID =:mLocationID")
    fun deleteCartByLocation(mLocationID: String) : Int

    @Insert
    fun insertBulkCartProduct(mProductCartList: List<CartProductModel>) : LongArray

    @Query("UPDATE CartProductModel SET mProductQuantity =:mProductQuantity, mProductTotalPrice =:mProductTotalPrice WHERE mID =:mRowID")
    fun updateProductQuantity(mRowID: Int, mProductQuantity: BigDecimal, mProductTotalPrice: BigDecimal): Int

    @Query("SELECT * FROM CartProductModel WHERE mID =:mRowID")
    fun getCartProduct(mRowID: Int): CartProductModel

    @Query("DELETE FROM CartProductModel WHERE mID =:mID")
    fun deleteCartProduct(mID: Int): Int

    @Query("DELETE FROM CartProductModel WHERE mTableNO =:mTableNO AND mCartID =:mCartID")
    fun deleteCartForCartID(mTableNO: Int, mCartID: String): Int

    @Query("SELECT * FROM (SELECT * FROM CartProductModel ORDER BY mTotalGuestsCount) WHERE mLocationID =:mLocationID AND mServerID =:mServerID GROUP BY mCartID")
    fun getPendingOrderDataForLocationAndServer(mLocationID: String, mServerID: String): List<CartProductModel>

    @Query("SELECT * FROM (SELECT * FROM CartProductModel ORDER BY mTotalGuestsCount) WHERE mLocationID =:mLocationID GROUP BY mCartID")
    fun getPendingOrderDataForLocation(mLocationID: String): List<CartProductModel>

    @Query("SELECT * FROM (SELECT * FROM CartProductModel ORDER BY mTotalGuestsCount) WHERE mServerID =:mServerID GROUP BY mCartID")
    fun getPendingOrderDataForServer(mServerID: String): List<CartProductModel>

    @Query("SELECT * FROM (SELECT * FROM CartProductModel ORDER BY mTotalGuestsCount) GROUP BY mCartID")
    fun getAllPendingOrderData(): List<CartProductModel>

    @Query("SELECT * FROM Login")
    fun getRestaurantData(): Login

    @Query("SELECT * FROM CartProductModel WHERE mCartProductID=:mCartProductID")
    fun getCartProductByID(mCartProductID: String):CartProductModel

    @Query("SELECT * FROM CartProductModel WHERE mOrderType == 1")
    fun getAllCartDataIrrespectiveOfLocations(): LiveData<List<CartProductModel>>

    @Update
    fun updateCartProduct(mCartProduct: CartProductModel): Int

    @Query("UPDATE CartProductModel SET mKitchenPrintFlag = 1 WHERE mPrinterID =:mPrinterID AND mCartID =:mCartID")
    fun changeKOTFlag(mPrinterID: String, mCartID: String): Int

    @Query("SELECT * FROM CartProductModel WHERE mTableNO =:mTableNO AND mGroupName =:mGroupName")
    fun getTableCartData(mTableNO: Int, mGroupName: String): List<CartProductModel>

}