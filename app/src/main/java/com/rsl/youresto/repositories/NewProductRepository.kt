package com.rsl.youresto.repositories

import androidx.core.text.isDigitsOnly
import androidx.lifecycle.LiveData
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.database_download.models.ProductGroupModel
import com.rsl.youresto.data.database_download.models.TablesModel
import com.rsl.youresto.data.main_product.MainProductDao
import com.rsl.youresto.network.ProductRemoteSource
import com.rsl.youresto.network.Resource
import com.rsl.youresto.network.models.AppliedTaxDetail
import com.rsl.youresto.network.models.CartDetail
import com.rsl.youresto.network.models.NetworkCartResponse
import com.rsl.youresto.network.models.PostCart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class NewProductRepository(private val mainProductDao: MainProductDao, private val remoteSource: ProductRemoteSource) {

    fun getProductGroups() : LiveData<List<ProductGroupModel>> = mainProductDao.getProductGroups(0)

    fun getProductCategories(mGroupID : String?) = mainProductDao.getProductCategories(mGroupID)

    fun getProducts(categoryId : String) = if (categoryId.isNotBlank()) mainProductDao.getProducts(categoryId) else mainProductDao.getAllProducts()

    fun getSearchedProducts(categoryId : String, searchText: String) =
        if (categoryId.isNotBlank()) mainProductDao.getSearchedProducts(categoryId, searchText) else mainProductDao.getSearchedAllProducts(searchText)


    fun getProduct(productId: String) = mainProductDao.getProduct(productId)
    fun getProductIngredients(ingredientIds: List<String>) = mainProductDao.getProductIngredients(ingredientIds)


    suspend fun submitCartProduct(cartProduct: CartProductModel): Resource<NetworkCartResponse> {

        val carts = withContext(Dispatchers.IO) {
            mainProductDao.getCartData(cartProduct.mTableID)
        }

        val isTableOccupied = carts.isNotEmpty()

        if (isTableOccupied) {
            cartProduct.mCartID = carts[0].mCartID
            cartProduct.mCartNO = carts[0].mCartID
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
                qty = cart.mProductQuantity.toInt()
                subtotal = cart.mProductTotalPrice.toDouble()
                recipeId = cart.mProductID?.toInt() ?: 0
                specialNotes = cart.mSpecialInstruction
            }

            if (cart.taxPercentage > 0) taxTotal += (cart.mProductTotalPrice.toDouble() * cart.taxPercentage) / 100

            subTotal += cart.mProductTotalPrice

            allCarts.add(cDetails)
        }

        val count = withContext(Dispatchers.IO){
            mainProductDao.getCartCount()
        }

        cartProduct.mCartProductID = (count + 1).toString()

        val cartId = withContext(Dispatchers.IO){
            mainProductDao.saveCartProduct(cartProduct)
        }

        val addons = ArrayList<CartDetail.Addon>()
        cartProduct.mShowModifierList?.map { addons.add(CartDetail.Addon(it.mIngredientID.toInt())) }

        val cartDetail = CartDetail().apply {
            addon = addons
            price = cartProduct.mProductUnitPrice.toDouble()
            qty = cartProduct.mProductQuantity.toInt()
            subtotal = cartProduct.mProductUnitPrice.toDouble() * cartProduct.mProductQuantity.toInt()
            recipeId = cartProduct.mProductID?.toInt() ?: 0
            specialNotes = cartProduct.mSpecialInstruction
        }

        if (cartProduct.taxPercentage > 0) taxTotal += (cartProduct.mProductUnitPrice.toDouble() * cartProduct.mProductQuantity.toInt() * cartProduct.taxPercentage) / 100

        subTotal += BigDecimal(cartProduct.mProductUnitPrice.toDouble() * cartProduct.mProductQuantity.toInt())

        val gTotal: BigDecimal = subTotal + BigDecimal(taxTotal)

        allCarts.add(cartDetail)

        val taxDetails = AppliedTaxDetail(cartProduct.taxName, cartProduct.taxPercentage.toString(), taxTotal.toString())

        val networkCart = PostCart().apply {
            cartDetails.addAll(allCarts)
            appliedTaxDetails.add(taxDetails)
            restaurantId = remoteSource.prefs.getRestaurantId().toInt()
            netTotal = gTotal.toDouble()
            orderBy = cartProduct.mServerID.toInt()
            this.subTotal = subTotal.toDouble()
            try{
                tableId = if (cartProduct.mTableID.isDigitsOnly()) cartProduct.mTableID.toInt() else 0
            }catch (e: Exception){
                e.printStackTrace()
            }
            noOfPerson = 2 /*cartProduct.mTotalGuestsCount*/
            if (isTableOccupied) orderId = cartProduct.mCartID
            locationId = cartProduct.mLocationID
            orderType = cartProduct.mOrderType.toString()
        }



        val resource = withContext(Dispatchers.IO) {
            remoteSource.submitCart(networkCart)
        }

        if (resource.status == Resource.Status.SUCCESS){
            resource.data?.let {
//                cartProduct.mID = it.itemIds[it.itemIds.size - 1].toInt()
//                cartProduct.mID =
                cartProduct.mCartID = it.orderId.toString()
                cartProduct.mCartNO = it.orderId.toString()
                cartProduct.tableOrderId = it.tableOrdersId.toString()

                withContext(Dispatchers.IO){
                    mainProductDao.updateCartProduct(cartProduct)
                }
            }
        } else {
            //todo: delete product if something went wrong
            //not empty body
        }

        return resource
    }

    suspend fun getCartProductByID(cartProductId: String)
        = withContext(Dispatchers.IO) { mainProductDao.getCartProductByID(cartProductId)}

    suspend fun getQuickServiceTable(): TablesModel?{
        val tables = withContext(Dispatchers.IO){
            mainProductDao.getTables(remoteSource.prefs.getSelectedLocation())
        }

        var quickServiceTable: TablesModel? = null
        tables.map { table ->
            withContext(Dispatchers.IO){
                async {
                    val carts = withContext(Dispatchers.IO) {
                        mainProductDao.getCartData(table.mTableID)
                    }

                    if (carts.isEmpty()){
                        quickServiceTable = table
                    }
                }
            }
        }.awaitAll()

        return quickServiceTable
    }


}