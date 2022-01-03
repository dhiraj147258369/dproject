package com.rsl.youresto.repositories

import com.rsl.youresto.data.database_download.models.FavoriteItemsModel
import com.rsl.youresto.data.database_download.models.TablesModel
import com.rsl.youresto.data.main_login.MainLoginDao
import com.rsl.youresto.data.main_login.network.NetworkLogin
import com.rsl.youresto.network.LoginRemoteSource
import com.rsl.youresto.network.NetworkRestaurantData
import com.rsl.youresto.network.Resource
import com.rsl.youresto.network.models.PostLogin
import com.rsl.youresto.ui.main_screen.favorite_items.model.FavoriteProductModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class LoginRepository(private val loginDao: MainLoginDao, private val dataSource: LoginRemoteSource) {

    suspend fun authenticateUserWithEmail(login: PostLogin): Resource<NetworkLogin> {
        return saveLoginData(dataSource.authenticateUserWithEmail(login))
    }

    private suspend fun saveLoginData(resource: Resource<NetworkLogin>): Resource<NetworkLogin> {
        if (resource.status == Resource.Status.SUCCESS){
            resource.data?.let {
                withContext(Dispatchers.IO){
                    loginDao.insertLoginData(it.data)
                }
            }
        }
        return resource
    }

    suspend fun getData(): Resource<NetworkRestaurantData>  {
        val resource = withContext(Dispatchers.IO) {
            dataSource.getData()
        }

        if (resource.status == Resource.Status.SUCCESS && resource.data != null) {
            if (resource.data.status){
                val data = resource.data.data
                withContext(Dispatchers.IO){
                    loginDao.insertProductGroups(data.productGorups)
                }
                withContext(Dispatchers.IO){
                    loginDao.insertProductCategories(data.productCategories)
                }
                withContext(Dispatchers.IO){
                    loginDao.insertProducts(data.products)
                }

                withContext(Dispatchers.IO){
                    loginDao.insertServers(data.users)
                }
                withContext(Dispatchers.IO){
                    loginDao.insertIngredients(data.ingredients)
                }
                withContext(Dispatchers.IO){
                    loginDao.insertLocations(data.locations)
                }

                val tables = ArrayList<TablesModel>()
                for (location in data.locations){
                    tables.addAll(location.tables)
                }

                withContext(Dispatchers.IO){
                    loginDao.insertTables(tables)
                }

                withContext(Dispatchers.IO){
                    loginDao.insertTaxes(data.tax)
                }
                withContext(Dispatchers.IO){
                    loginDao.insertPaymentMethods(data.paymentMethods)
                }
                withContext(Dispatchers.IO){
                    loginDao.insertKitchens(data.kitchens)
                }

                //get favorites
                data.locations.map {
                    withContext(Dispatchers.IO){
                        async {
                            if (it.mLocationType == "2"){
                                val favoriteResource = withContext(Dispatchers.IO){
                                    dataSource.getFavorites(it.mLocationID)
                                }

                                if (favoriteResource.status == Resource.Status.SUCCESS){
                                    val categoryList = ArrayList<FavoriteItemsModel>()
                                    favoriteResource.data?.data?.map { favoriteProduct ->
                                        for (product in data.products){
                                            if (favoriteProduct.productId == product.mProductID){
                                                if (categoryList.isEmpty()){
                                                    val favoriteProductList = ArrayList<FavoriteProductModel>().apply {
                                                        add(FavoriteProductModel(
                                                            product.mProductID, product.mProductName,
                                                            product.mProductType, product.mGroupID ?: "",
                                                            product.mCategoryID ?: "", product.mGroupName,
                                                            product.mCategoryName, product.mProductImageUrl,
                                                            product.mProductSequence, product.mCategorySequence,
                                                            product.mDineInPrice.toDouble(),
                                                            product.mQuickServicePrice.toDouble(), true
                                                        ))
                                                    }
                                                    categoryList.add(
                                                        FavoriteItemsModel(
                                                            product.mGroupID ?: "",
                                                            product.mGroupName,
                                                            product.mCategoryID ?: "",
                                                            product.mCategoryName,
                                                            product.mCategorySequence,
                                                            it.mLocationID, favoriteProductList)
                                                    )
                                                } else {
                                                    for (category in categoryList){
                                                        if (product.mCategoryID == category.mCategoryID){
                                                            category.mProductArrayList.add(FavoriteProductModel(
                                                                product.mProductID, product.mProductName,
                                                                product.mProductType, product.mGroupID ?: "",
                                                                product.mCategoryID ?: "", product.mGroupName,
                                                                product.mCategoryName, product.mProductImageUrl,
                                                                product.mProductSequence, product.mCategorySequence,
                                                                product.mDineInPrice.toDouble(),
                                                                product.mQuickServicePrice.toDouble(), true
                                                            ))
                                                            break
                                                        }
                                                    }
                                                }
                                                break
                                            }
                                        }
                                    }

                                    //insert
                                    withContext(Dispatchers.IO){
                                        loginDao.insertFavoriteItems(categoryList)
                                    }
                                }
                            }
                        }
                    }
                }.awaitAll()
            }
        }

        return resource
    }

    suspend fun deleteAll() {
        withContext(Dispatchers.IO) {
            loginDao.deleteAllergen()
        }
        withContext(Dispatchers.IO) {
            loginDao.deleteFavoriteItems()
        }
        withContext(Dispatchers.IO) {
            loginDao.deleteIngredients()
        }
        withContext(Dispatchers.IO) {
            loginDao.deleteKitchen()
        }
        withContext(Dispatchers.IO) {
            loginDao.deleteLocation()
        }
        withContext(Dispatchers.IO) {
            loginDao.deletePaymentMethod()
        }
        withContext(Dispatchers.IO) {
            loginDao.deleteServers()
        }
        withContext(Dispatchers.IO) {
            loginDao.deleteTables()
        }
        withContext(Dispatchers.IO) {
            loginDao.deleteTax()
        }
        withContext(Dispatchers.IO) {
            loginDao.deleteProducts()
        }
        withContext(Dispatchers.IO) {
            loginDao.deleteCategories()
        }
        withContext(Dispatchers.IO) {
            loginDao.deleteGroups()
        }

    }
}