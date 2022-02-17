package com.rsl.foodnairesto.repositories

import android.util.Log
import androidx.core.text.isDigitsOnly
import com.rsl.foodnairesto.data.cart.CartDao
import com.rsl.foodnairesto.data.cart.CartRemoteSource
import com.rsl.foodnairesto.data.cart.models.CartProductModel
import com.rsl.foodnairesto.data.checkout.model.NetworkCheckoutResponse
import com.rsl.foodnairesto.data.checkout.model.PostCheckout
import com.rsl.foodnairesto.data.database_download.models.IngredientsModel
import com.rsl.foodnairesto.network.Resource
import com.rsl.foodnairesto.network.models.AppliedTaxDetail
import com.rsl.foodnairesto.network.models.CartDetail
import com.rsl.foodnairesto.network.models.NetworkCartResponse
import com.rsl.foodnairesto.network.models.PostCart
import com.rsl.foodnairesto.utils.AppConstants.SERVICE_QUICK_SERVICE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class NewCartRepository(private val remoteSource: CartRemoteSource, private val cartDao: CartDao) {

    fun getCarts(tableNo: String) =
        if (remoteSource.prefs.getLocationServiceType() == SERVICE_QUICK_SERVICE){
            cartDao.getCartDataById(remoteSource.prefs.selectedQuickServiceCartId())
        } else {
            cartDao.getCartData(tableNo)
        }
    fun getCartDataById(cartId: String) = cartDao.getCartDataById(cartId)

    suspend fun getCartByTable(tableId: String) = withContext(Dispatchers.IO) {
        if (remoteSource.prefs.getLocationServiceType() == SERVICE_QUICK_SERVICE){
            cartDao.getCartsById(remoteSource.prefs.selectedQuickServiceCartId())
        } else {
            cartDao.getCarts(tableId)
        }
    }

    fun getCartsById(cartId: String) = cartDao.getCartsById(cartId)

    suspend fun getPaymentMethods() = withContext(Dispatchers.IO) {cartDao.getPaymentMethods()}

    suspend fun updateQuantity(rowId: Int, qty: BigDecimal, totalPrice: BigDecimal, tableId: String) {
       withContext(Dispatchers.IO) {
           cartDao.updateQuantity(rowId, qty, totalPrice)
       }

        val carts = if (remoteSource.prefs.getLocationServiceType() == SERVICE_QUICK_SERVICE){
            withContext(Dispatchers.IO) {
                cartDao.getCartsById(remoteSource.prefs.selectedQuickServiceCartId())
            }
        } else {
            withContext(Dispatchers.IO) {
                cartDao.getCarts(tableId)
            }
        }

        var taxTotal = 0.0
        var subTotal = BigDecimal(0.0)

        val allCarts = ArrayList<CartDetail>()

        for (cart in carts) {

            val cAddons = ArrayList<CartDetail.Addon>()
            cart.mShowModifierList?.map { cAddons.add(CartDetail.Addon(it.mIngredientID.toInt())) }
            val cDetails = CartDetail().apply {
                addon = cAddons
                price = cart.mProductUnitPrice.toDouble()
                this.qty = cart.mProductQuantity.toInt()
                subtotal = cart.mProductTotalPrice.toDouble()
                recipeId = cart.mProductID?.toInt() ?: 0
                specialNotes = cart.mSpecialInstruction
            }

            if (cart.taxPercentage > 0) taxTotal += (cart.mProductTotalPrice.toDouble() * cart.taxPercentage) / 100

            subTotal += cart.mProductTotalPrice

            allCarts.add(cDetails)
        }

        val gTotal: BigDecimal = subTotal + BigDecimal(taxTotal)

        val taxDetails = AppliedTaxDetail(carts[0].taxName, carts[0].taxPercentage.toString(), taxTotal.toString())

        val networkCart = PostCart().apply {
            cartDetails.addAll(allCarts)
            appliedTaxDetails.add(taxDetails)
            restaurantId = remoteSource.prefs.getRestaurantId().toInt()
            netTotal = gTotal.toDouble()
            orderBy = carts[0].mServerID.toInt()
            this.subTotal = subTotal.toDouble()
            try {
                this.tableId = carts[0].mTableID.toInt()
            } catch (e: Exception){
                e.printStackTrace()
            }
            noOfPerson = 2 /*cartProduct.mTotalGuestsCount*/
            try {
                if (carts[0].mCartID.isNotBlank()) orderId = carts[0].mCartID
            } catch (e: Exception){
                e.printStackTrace()
            }
        }


        val resource = withContext(Dispatchers.IO) {
            remoteSource.submitCart(networkCart)
        }

        if (resource.status == Resource.Status.SUCCESS){
            resource.data?.let {

            }
        } else {
            //revert changes
        }
    }

    suspend fun deleteCartItem(cartProduct: CartProductModel): Resource<NetworkCartResponse> {

        val carts = if (cartProduct.mOrderType == SERVICE_QUICK_SERVICE){
            withContext(Dispatchers.IO) {
                cartDao.getCartsById(cartProduct.mCartID)
            }
        } else {
            withContext(Dispatchers.IO) {
                cartDao.getCarts(cartProduct.mTableID)
            }
        }

        withContext(Dispatchers.IO){
            cartDao.deleteCartItem(cartProduct)
        }

        var taxTotal = 0.0
        var subTotal = BigDecimal(0.0)

        val allCarts = ArrayList<CartDetail>()

        for (cart in carts) {

            if (cart.mID == cartProduct.mID){
                continue
            }

            val cAddons = ArrayList<CartDetail.Addon>()
            cart.mShowModifierList?.map { cAddons.add(CartDetail.Addon(it.mIngredientID.toInt())) }
            val cDetails = CartDetail().apply {
                addon = cAddons
                price = cart.mProductUnitPrice.toDouble()
                qty = cart.mProductQuantity.toInt()
                subtotal = cart.mProductTotalPrice.toDouble()
                recipeId = cart.mProductID?.toInt() ?: 0
                specialNotes = cart.mSpecialInstruction
            }

            if (cart.taxPercentage > 0) taxTotal += (cart.mProductTotalPrice.toDouble() * cart.taxPercentage) / 100

            subTotal += cart.mProductTotalPrice

            allCarts.add(cDetails)
        }


        val gTotal: BigDecimal = subTotal + BigDecimal(taxTotal)

        val taxDetails = AppliedTaxDetail(cartProduct.taxName, cartProduct.taxPercentage.toString(), taxTotal.toString())

        val networkCart = PostCart().apply {
            cartDetails.addAll(allCarts)
            appliedTaxDetails.add(taxDetails)
            restaurantId = remoteSource.prefs.getRestaurantId().toInt()
            netTotal = gTotal.toDouble()
            orderBy = cartProduct.mServerID.toInt()
            this.subTotal = subTotal.toDouble()
            try {
                tableId = cartProduct.mTableID.toInt()
            } catch (e: Exception){
                e.printStackTrace()
            }
            noOfPerson = 2 /*cartProduct.mTotalGuestsCount*/
            try {
                if (cartProduct.mCartID.isDigitsOnly()) orderId = cartProduct.mCartID
            } catch (e: Exception){
                e.printStackTrace()
            }
        }


        val resource = withContext(Dispatchers.IO) {
            remoteSource.submitCart(networkCart)
        }

        if (resource.status == Resource.Status.SUCCESS){
            resource.data?.let {
                withContext(Dispatchers.IO){
                    cartDao.deleteCartItem(cartProduct)
                }
            }
        } else {
            //todo: reinsert Cart item
        }

        return resource
    }

    suspend fun deleteCart(tableId: String?, cartId: String?){
        val carts = if (cartId != null){
            withContext(Dispatchers.IO) {
                cartDao.getCartsById(cartId)
            }
        } else {
            withContext(Dispatchers.IO) {
                cartDao.getCarts(tableId ?: "")
            }
        }

        withContext(Dispatchers.IO){
            cartDao.deleteEntireCart(carts)
        }

        val networkCart = PostCart().apply {
            cartDetails.addAll(ArrayList())
            appliedTaxDetails.add(AppliedTaxDetail())
            restaurantId = remoteSource.prefs.getRestaurantId().toInt()
            netTotal = 0.0
            orderBy = 0
            this.subTotal = 0.0
            try {
                this.tableId = carts[0].mTableID.toInt()
            } catch (e: Exception){
                e.printStackTrace()
            }
            noOfPerson = 0 /*cartProduct.mTotalGuestsCount*/
            try {
                if (carts[0].mCartID.isDigitsOnly()) orderId = carts[0].mCartID
            } catch (e: Exception){
                e.printStackTrace()
            }
        }

        withContext(Dispatchers.IO) {
            remoteSource.submitCart(networkCart)
        }

    }

    fun getPendingOrderCartData() = cartDao.getLocationPendingOrders(remoteSource.prefs.getSelectedLocation())
    fun getPendingOrderCartDatafilter(mServerID: String) = cartDao.getLocationPendingOrdersfilter(remoteSource.prefs.getSelectedLocation(),mServerID)

    suspend fun checkoutOrder(checkout: PostCheckout, orderId: String): Resource<NetworkCheckoutResponse>  {
         val resource = withContext(Dispatchers.IO) {
             remoteSource.checkoutOrder(checkout)
         }

        if (resource.status == Resource.Status.SUCCESS){
            resource.data?.let {
//                withContext(Dispatchers.IO){
//                    cartDao.deleteCart(orderId)
//                }
            }
        }

        return resource
    }

    suspend fun deleteCart(orderId: String) = withContext(Dispatchers.IO) {cartDao.deleteCart(orderId)}

    suspend fun syncCarts(){
        val resource = withContext(Dispatchers.IO) {
            remoteSource.syncCarts()
        }

        if (resource.status == Resource.Status.SUCCESS){
            withContext(Dispatchers.IO) {cartDao.deleteCartByLocation(remoteSource.prefs.getSelectedLocation())}
            resource.data?.map { receiveCart ->
                withContext(Dispatchers.IO) {
                    async {
                        val cartProducts = ArrayList<CartProductModel>()
                        for (cart in receiveCart.cartDetails) {

                            val product = cartDao.getProduct(cart.recipeId.toString())
                            if(product!=null){
                            val table = cartDao.getTable(receiveCart.tableId.toString())

                            val mShowModifierList = ArrayList<IngredientsModel>()
                            if (cart.addon.isNotEmpty()) {
                                val addOnIdList = ArrayList<String>()
                                cart.addon.map { addOn -> addOnIdList.add(addOn.addonId.toString()) }
                                val localAddOns = cartDao.getAddOnsByIds(addOnIdList)
                                mShowModifierList.addAll(localAddOns)
                            }

                            var addOnTotal = BigDecimal(0.0)
                            mShowModifierList.map { modifier ->
                                addOnTotal += (modifier.mIngredientPrice * BigDecimal(
                                    cart.qty
                                ))
                            }

                            val cartProductTotalPrice =
                                BigDecimal(cart.price * cart.qty) + addOnTotal
                            Log.e("mCartProduct_", cart.recipeId.toString())
                            if (product != null) {
                                Log.e("mCartProduct", product.mCategoryID.toString())
                            }
                            val mCartProduct = CartProductModel(
                                remoteSource.prefs.getSelectedLocation(),
                                receiveCart.tableId.toString(),
                                table.mTableNo,
                                "z",
                                0,
                                ArrayList(),
                                receiveCart.orderId,
                                receiveCart.orderId,
                                "",
                                remoteSource.prefs.getLocationServiceType(),
                                receiveCart.orderBy.toString(),
                                "",
                                cart.recipeId.toString(),
                                product.mCategoryID ?: "",
                                "",
                                product.mGroupID ?: "",
                                "",
                                "",
                                product.mProductType,
                                product.mProductName,
                                product.mDineInPrice,
                                BigDecimal(cart.qty),
                                mProductTotalPrice = cartProductTotalPrice,
                                "",
                                BigDecimal(0.0),
                                ArrayList(),
                                mShowModifierList,
                                ArrayList(),
                                ArrayList(),
                                Date(),
                                "",
                                0,
                                "",
                                0,
                                true,
                                taxName = "",
                                taxPercentage = 0.0,
                                tableOrderId = receiveCart.tableOrderId
                            )

                            cartProducts.add(mCartProduct)
                        }
                        }

                        val ids = cartDao.insertBulkCartProduct(cartProducts)
                    }
                }
            }?.awaitAll()
        }
    }

}