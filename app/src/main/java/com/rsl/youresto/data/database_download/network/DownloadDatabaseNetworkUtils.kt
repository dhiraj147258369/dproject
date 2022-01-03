package com.rsl.youresto.data.database_download.network

import android.annotation.SuppressLint
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.util.Log.e
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.rsl.youresto.R
import com.rsl.youresto.data.database_download.models.*
import com.rsl.youresto.data.tables.models.ServerTableGroupModel
import com.rsl.youresto.utils.AppConstants.API_DATE
import com.rsl.youresto.utils.AppConstants.API_LOCATION_ID
import com.rsl.youresto.utils.AppConstants.API_RESTAURANT_ID
import com.rsl.youresto.utils.AppConstants.AUTH_BASIC
import com.rsl.youresto.utils.AppConstants.AUTH_CONTENT_TYPE
import com.rsl.youresto.utils.AppConstants.AUTH_CONTENT_TYPE_VALUE
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.NO_TYPE
import com.rsl.youresto.utils.AppConstants.PAPER_SIZE_80
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_TABLE_ID
import com.rsl.youresto.utils.AppConstants.QUICK_SERVICE_TABLE_NO
import com.rsl.youresto.utils.AppConstants.RESTAURANT_PASSWORD
import com.rsl.youresto.utils.AppConstants.RESTAURANT_USER_NAME
import com.rsl.youresto.utils.AppConstants.SERVICE_QUICK_SERVICE
import com.rsl.youresto.utils.EndPoints.API_IP
import com.rsl.youresto.utils.EndPoints.CART_REPORT
import com.rsl.youresto.utils.EndPoints.FAVORITE_ITEMS
import com.rsl.youresto.utils.EndPoints.GET_ALL_DATA
import com.rsl.youresto.utils.VolleySingleton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.net.MalformedURLException
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("LogNotTimber")
object DownloadDatabaseNetworkUtils {

    fun getDataURL(mRestaurantID: String?): URL {
        return buildWithTerminalIDQuery(mRestaurantID)
    }

    private fun buildWithTerminalIDQuery(mRestaurantID: String?): URL {
        val getAllDataUri = Uri.parse(GET_ALL_DATA).buildUpon()
            .appendQueryParameter(API_RESTAURANT_ID, mRestaurantID)
            .build()

        return URL(getAllDataUri.toString())
    }

    fun getResponseFromDatabaseDownload(
        context: Context,
        mGetAllDataURL: String,
        mInterface: DownloadDatabaseNetworkInterface
    ) {
        val mSharedPrefs: SharedPreferences = context.getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)

        e("DatabaseDownload", "getResponseFromDatabaseDownload: ")

        val mUsername: String? = mSharedPrefs.getString(RESTAURANT_USER_NAME, "")
        val mPassword: String? = mSharedPrefs.getString(RESTAURANT_PASSWORD, "")

        val stringRequest = object : StringRequest(
            Method.GET, mGetAllDataURL,
            Response.Listener { response -> mInterface.onGetALLResponse(1, response) },
            Response.ErrorListener { mInterface.onGetALLResponse(0, it.toString())}) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                val credentials = "$mUsername:$mPassword"
                val auth = AUTH_BASIC + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                headers[AUTH_CONTENT_TYPE] = AUTH_CONTENT_TYPE_VALUE
                headers["Authorization"] = auth
                return headers
            }
        }

        VolleySingleton.instance?.addToRequestQueue(stringRequest)
    }

    @Throws(JSONException::class)
    fun getAllergenData(mAllergenArray: JSONArray): ArrayList<AllergenModel> {

        val allergenEntries = ArrayList<AllergenModel>()

        for (i in 0 until mAllergenArray.length()) {
            val mSingleAllergen = mAllergenArray.getJSONObject(i)

            val mAllergenModel = AllergenModel(
                mSingleAllergen.getString(" allergen_id"),
                mSingleAllergen.getString("allergen_name"),
                mSingleAllergen.getString("allergen_description")
            )

            allergenEntries.add(mAllergenModel)
        }

        return allergenEntries
    }


    @Throws(JSONException::class)
    fun getServerData(mServerArray: JSONArray): ArrayList<ServerModel> {

        val restaurantUserEntries = ArrayList<ServerModel>()

        for (i in 0 until mServerArray.length()) {

            val mSingleUser = mServerArray.getJSONObject(i)

            //            JSONArray mUserPermissionArray = mSingleUser.getJSONArray("permissions");
            val mUserPermissionList = ArrayList<Int>()

            val mUserModel = ServerModel(
                mSingleUser.getString("id"),
                mSingleUser.getString("name"),
                mSingleUser.getString("pin"),
                mSingleUser.getString("password"),
                mUserPermissionList
            )

            restaurantUserEntries.add(mUserModel)
        }

        return restaurantUserEntries
    }

    fun getGroupData(
        mGroupArray: JSONArray,
        mServerIngredientList: ArrayList<IngredientsModel>
    ): ArrayList<ProductGroupModel> {

        val restaurantGroupEntries = ArrayList<ProductGroupModel>()

        var mCategorySequence = 0

        var productCategoryEntries: ArrayList<ProductCategoryModel>
        var productEntries: ArrayList<ProductModel>

        for (i in 0 until mGroupArray.length()) {
            val mSingleGroup = mGroupArray.getJSONObject(i)

            val mCategoryArray = mSingleGroup.getJSONArray("categories")

            productCategoryEntries = ArrayList()
            for (j in 0 until mCategoryArray.length()) {
                val mSingleCategory = mCategoryArray.getJSONObject(j)

                val mProductArray = mSingleCategory.getJSONArray("product")

                productEntries = ArrayList()

                for ((mProductSequence, k) in (0 until mProductArray.length()).withIndex()) {

                    val mSingleProduct = mProductArray.getJSONObject(k)

                    val mIngredientsArray = mSingleProduct.getJSONArray("ingredients")

                    val mIngredientsList = java.util.ArrayList<String>()
                    for (l in 0 until mIngredientsArray.length()) {
                        val mSingleIngredient = mIngredientsArray.getJSONObject(l)
                        mIngredientsList.add(mSingleIngredient.getString("ingredient_id"))
                    }

                    var mProductImageURL = ""

                    when {
                        mSingleProduct.getString("product_image").isNotEmpty() -> mProductImageURL = mSingleProduct.getString("product_image").replace("localhost", API_IP)
                    }


                    //allergen related
                    val mAllergenArray = mSingleProduct.getJSONArray("allergens")

                    val mAllergenList = java.util.ArrayList<String>()
                    for (l in 0 until mAllergenArray.length()) {
                        val mSingleAllergen = mAllergenArray.getJSONObject(l)
                        mAllergenList.add(mSingleAllergen.getString("allergen_id"))
                    }

                    val mGenericProductsList = java.util.ArrayList<GenericProducts>()

                    when {
                        mSingleProduct.getBoolean("product_generic") -> {
                            val mGenericProductsArray = mSingleProduct.getJSONArray("generic_products")

                            for (l in 0 until mGenericProductsArray.length()) {
                                val mGenericObject = mGenericProductsArray.getJSONObject(l)

                                val mGenericIngredientsArray = mGenericObject.getJSONArray("ingredients")

                                val mGenericIngredientsList = ArrayList<IngredientsModel>()

                                val mIngredientCategoryIDList = ArrayList<String>()

                                for (m in 0 until mGenericIngredientsArray.length()) {
                                    val mIngredientObject = mGenericIngredientsArray.getJSONObject(m)

                                    val mIngredientsModel = IngredientsModel(
                                        mIngredientObject.getString("ingredient_id"),
                                        mIngredientObject.getString("ingredient_name"),
                                        BigDecimal(mIngredientObject.getDouble("ingredient_price")),
                                        BigDecimal(0),
                                        mIngredientObject.getString("product_id"),
                                        mIngredientObject.getString("product_category_id"),
                                        false, isCompulsory = false, mSelectionType = 0, mCartIngredientID = "0"
                                    )

                                    when {
                                        !mIngredientCategoryIDList.contains(mIngredientObject.getString("product_category_id")) -> mIngredientCategoryIDList.add(mIngredientObject.getString("product_category_id"))
                                    }

                                    mGenericIngredientsList.add(mIngredientsModel)
                                }

                                val mIngredientCategoryList: ArrayList<IngredientCategoryModel> = ArrayList()

                                for (m in 0 until mIngredientCategoryIDList.size) {

                                    val mIngredientList = ArrayList<IngredientsModel>()

                                    for (n in 0 until mGenericIngredientsList.size) {
                                        when (mGenericIngredientsList[n].mIngredientCategoryID) {
                                            mIngredientCategoryIDList[m] -> mIngredientList.add(mGenericIngredientsList[n])
                                        }
                                    }

                                    mIngredientCategoryList.add(
                                        IngredientCategoryModel(
                                            mIngredientCategoryIDList[m], "",
                                            mIngredientList, false, 0, 0
                                        )
                                    )

                                }

                                val mGenericProduct = GenericProducts(
                                    l,
                                    mGenericObject.getString("generic_product_id"),
                                    mSingleProduct.getString("product_id"),
                                    mSingleProduct.getString("category_id"),
                                    mSingleProduct.getString("group_id"),
                                    mGenericObject.getString("generic_product_name"),
                                    BigDecimal(mGenericObject.getDouble("dine_in_price")),
                                    BigDecimal(mGenericObject.getDouble("quick_service_price")),
                                    BigDecimal(mGenericObject.getDouble("delivery_price")),
                                    mGenericObject.getInt("topping_limit"),
                                    mIngredientCategoryList,
                                    false
                                )

                                mGenericProductsList.add(mGenericProduct)
                            }
                        }
                    }

                    var mProductType = mSingleProduct.getInt("type") ?: 0

//                    when {
//                        mSingleProduct.getInt("type") -> mProductType = mSingleProduct.getInt("type")
//                    }

                    e("TAG", "getGroupData: type: $mProductType", )

                    val mProductModel = ProductModel(
                        k,
                        mSingleProduct.getString("product_id"),
                        mSingleProduct.getBoolean("active"),
                        mSingleProduct.getString("category_id"),
                        mSingleCategory.getString("category_name"),
                        mSingleProduct.getString("group_id"),
                        mSingleGroup.getString("group_name"),
                        mIngredientsList,
                        mAllergenList,
                        mSingleProduct.getString("product_name"),
                        mSingleProduct.getString("product_description"),
                        BigDecimal(mSingleProduct.getDouble("dine_in_price")),
                        BigDecimal(mSingleProduct.getDouble("quick_service_price")),
                        BigDecimal(mSingleProduct.getDouble("delivery_price")),
                        mProductImageURL,
                        mProductType,
                        mProductSequence,
                        mSingleCategory.getInt("seq_no"),
                        mSingleProduct.getString("kitchen_id"),
                        mGenericProductsList,
                        ArrayList()
                    )

                    productEntries.add(mProductModel)
                }

                var mCategoryImageURL = ""

                var mModifierSelection = 0

                when {
                    mSingleCategory.getString("modifier_selection").isNotEmpty() ->
                        mModifierSelection = mSingleCategory.getInt("modifier_selection")
                }

                when {
                    mSingleCategory.getString("image_url").isNotEmpty() ->
                        mCategoryImageURL = mSingleCategory.getString("image_url").replace("localhost", API_IP)
                }

                val mCategoryModel = ProductCategoryModel(
                    mSingleCategory.getString("category_name"),
                    mSingleCategory.getString("category_id"),
                    mSingleCategory.getString("group_id"),
                    mSingleCategory.getBoolean("active"),
                    mSingleCategory.getBoolean("display_pos"),
                    mCategoryImageURL,
                    mSingleCategory.getInt("seq_no"),
                    mSingleCategory.getBoolean("isCompulsory"),
                    mModifierSelection,
                    productEntries
                )

                productCategoryEntries.add(mCategoryModel)
                mCategorySequence++
            }

            var mGroupImageURL = ""

            when {
                mSingleGroup.getString("image_url").isNotEmpty() ->
                    mGroupImageURL = mSingleGroup.getString("image_url").replace("localhost", API_IP)
            }

            val mGroupModel = ProductGroupModel(
                mSingleGroup.getString("group_name"),
                mSingleGroup.getString("group_id"),
                mSingleGroup.getBoolean("active"),
                mGroupImageURL,
                mSingleGroup.getBoolean("display_pos"),
                productCategoryEntries
            )

            restaurantGroupEntries.add(mGroupModel)
        }

        return setIngredientsCategories(restaurantGroupEntries, mServerIngredientList)
    }

    private fun setIngredientsCategories(
        mGroupList: ArrayList<ProductGroupModel>,
        mServerIngredientList: ArrayList<IngredientsModel>
    ): ArrayList<ProductGroupModel> {

        val mAllCategoryList: ArrayList<ProductCategoryModel> = ArrayList()

        for (i in 0 until mGroupList.size)
            mAllCategoryList.addAll(mGroupList[i].mProductCategoryList)

        for (i in 0 until mGroupList.size) {
            val categoryList = mGroupList[i].mProductCategoryList
            for (j in 0 until categoryList.size) {
                val productList = categoryList[j].mProductList
                for (k in 0 until productList!!.size) {
                    val genericProductList = productList[k].mGenericProductList
                    for (l in 0 until genericProductList.size) {
                        val ingredientCategoryList =
                            genericProductList[l].mIngredientCategoryList

                        for (m in 0 until ingredientCategoryList.size) {
                            loop@ for (n in 0 until mAllCategoryList.size) {
                                when (ingredientCategoryList[m].mCategoryID) {
                                    mAllCategoryList[n].mCategoryID -> {
                                        ingredientCategoryList[m].mCategoryName = mAllCategoryList[n].mCategoryName
                                        ingredientCategoryList[m].isCompulsory = mAllCategoryList[n].isCompulsory
                                        ingredientCategoryList[m].mModifierSelection =
                                            mAllCategoryList[n].mModifierSelection
                                        ingredientCategoryList[m].mCategorySequence = mAllCategoryList[n].mCategorySequence
                                        break@loop
                                    }
                                }
                            }
                        }

                        genericProductList[l].mIngredientCategoryList = ingredientCategoryList
                    }
                }
            }
        }

        for (i in 0 until mGroupList.size) {
            val categoryList = mGroupList[i].mProductCategoryList
            for (j in 0 until categoryList.size) {
                val productList = categoryList[j].mProductList
                for (k in 0 until productList!!.size) {
                    when (productList[k].mProductType) {
                        3 -> {
                            val mSubProductIDList = productList[k].mIngredientsList
                            productList[k].mSubProductCategoryList =
                                getSubProducts(mGroupList, mServerIngredientList, mSubProductIDList)
                        }
                    }
                }
            }
        }

        return mGroupList
    }


    private fun getSubProducts(
        mGroupList: ArrayList<ProductGroupModel>,
        mServerIngredientList: ArrayList<IngredientsModel>,
        mSubProductIDList: ArrayList<String>
    ): ArrayList<SubProductCategoryModel> {

        val mSubProductCategoryList = ArrayList<SubProductCategoryModel>()

        for (l in 0 until mSubProductIDList.size) {

            var mProductID = ""
            var mCategoryID = ""
            var mCategoryName = ""
            loop@ for (i in 0 until mServerIngredientList.size) {
                when (mServerIngredientList[i].mIngredientID) {
                    mSubProductIDList[l] -> {
                        mProductID = mServerIngredientList[i].mIngredientProductID
                        mCategoryID = mServerIngredientList[i].mIngredientCategoryID
                        break@loop
                    }
                }
            }


            e(javaClass.simpleName, "getSubProducts: $mCategoryID And $mProductID")
            var mProductModel: ProductModel? = null

            group@
            for (i in 0 until mGroupList.size) {
                val categoryList = mGroupList[i].mProductCategoryList
                for (j in 0 until categoryList.size) {
                    when (mCategoryID) {
                        categoryList[j].mCategoryID -> {
                            mCategoryName = categoryList[j].mCategoryName
                            val productList = categoryList[j].mProductList
                            for (k in 0 until productList!!.size) {
                                when (mProductID) {
                                    productList[k].mProductID -> {
                                        mProductModel = productList[k]
                                        break@group
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val mProductList = ArrayList<ProductModel>()

            when {
                mProductModel != null -> mProductList.add(mProductModel)
            }

            val mSubProductCategoryModel = SubProductCategoryModel(mCategoryID, mCategoryName, mProductList)
            mSubProductCategoryList.add(mSubProductCategoryModel)
        }

        val mFinalSubProductCategoryList = ArrayList<SubProductCategoryModel>()
        for (i in 0 until mSubProductCategoryList.size) {
            when {
                mFinalSubProductCategoryList.size > 0 -> {

                    var mHasCategoryBoolean = false
                    loop@ for (j in 0 until mFinalSubProductCategoryList.size) {
                        when (mFinalSubProductCategoryList[j].mCategoryID) {
                            mSubProductCategoryList[i].mCategoryID -> {
                                val mProductList = mFinalSubProductCategoryList[j].mProductList
                                mProductList.addAll(mSubProductCategoryList[i].mProductList)
                                mHasCategoryBoolean = true
                                break@loop
                            }
                            else -> mHasCategoryBoolean = false
                        }
                    }

                    when {
                        !mHasCategoryBoolean -> mFinalSubProductCategoryList.add(mSubProductCategoryList[i])
                    }

                }
                else -> mFinalSubProductCategoryList.add(mSubProductCategoryList[i])
            }
        }

        return mFinalSubProductCategoryList
    }


    @Throws(JSONException::class)
    fun getIngredientsData(mTypeArray: JSONArray): ArrayList<IngredientsModel> {

        val ingredientsEntries = java.util.ArrayList<IngredientsModel>()

        for (i in 0 until mTypeArray.length()) {
            val mSingleType = mTypeArray.getJSONObject(i)

            val mIngredientsModel = IngredientsModel(
                mSingleType.getString("ingredient_id"),
                mSingleType.getString("ingredient_name"),
                BigDecimal(mSingleType.getDouble("ingredient_price")),
                BigDecimal(0),
                mSingleType.getString("product_id"),
                mSingleType.getString("product_category_id"),
                false, isCompulsory = false, mSelectionType = 3, mCartIngredientID = "0"
            )

            ingredientsEntries.add(mIngredientsModel)
        }

        return ingredientsEntries
    }

    @Throws(JSONException::class)
    fun getLocationData(mLocationArray: JSONArray): java.util.ArrayList<LocationModel> {

        val locationEntries = java.util.ArrayList<LocationModel>()

        for (i in 0 until mLocationArray.length()) {
            val mSingleLocation = mLocationArray.getJSONObject(i)

            val mLocationTypeList = java.util.ArrayList<LocationTypeModel>()

            val mLocationTypeArray = mSingleLocation.getJSONArray("location_type")

            if (mLocationTypeArray.length() > 0) {
                for (j in 0 until mLocationTypeArray.length()) {
                    val mLocationTypeObject = mLocationTypeArray.getJSONObject(j)
                    mLocationTypeList.add(
                        LocationTypeModel(
                            mLocationTypeObject.getInt("location_type"),
                            mLocationTypeObject.getBoolean("default_location")
                        )
                    )
                }
            } else {
                mLocationTypeList.add(LocationTypeModel(1, true))
            }

            val mLocationModel = LocationModel(
                mSingleLocation.getString("location_id"),
                mSingleLocation.getString("location_name"),
                "1"
            )

            locationEntries.add(mLocationModel)
        }

        return locationEntries
    }

    @Throws(JSONException::class)
    fun getTaxData(mTaxArray: JSONArray): ArrayList<TaxModel> {

        val taxEntries = ArrayList<TaxModel>()

        for (i in 0 until mTaxArray.length()) {
            val mSingleTax = mTaxArray.getJSONObject(i)


            val mTaxModel = TaxModel(
                mSingleTax.getString("tax_id"),
                mSingleTax.getString("tin_no"),
                mSingleTax.getString("tax_name"),
                BigDecimal(mSingleTax.getDouble("tax_percentage"))
            )

            taxEntries.add(mTaxModel)
        }

        return taxEntries
    }

    @Throws(JSONException::class)
    fun getRestaurantTablesData(mLocationArray: JSONArray, mContext: Context): java.util.ArrayList<TablesModel> {

        val mSharedPrefs = mContext.getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)

        val tableEntries = java.util.ArrayList<TablesModel>()

        for (i in 0 until mLocationArray.length()) {
            val mSingleLocation = mLocationArray.getJSONObject(i)

            val mTableArray = mSingleLocation.getJSONArray("tables")

            for (j in 0 until mTableArray.length()) {

                val mTableObject = mTableArray.getJSONObject(j)

                val mGroupList = ArrayList<ServerTableGroupModel>()

                val mTableModel = TablesModel(
                    mTableObject.getString("location_id"),
                    mTableObject.getString("table_id"),
                    mTableObject.getInt("table_no"),
                    mTableObject.getInt("no_of_chairs"),
                    0,
                    mTableObject.getInt("table_type"),
                    "",
                    "0",
                    mGroupList
                )

                if (mTableObject.getInt("table_type") == SERVICE_QUICK_SERVICE) {
                    //ADD SERVICE_DELIVERY

                    val mEditor = mSharedPrefs.edit()
                    mEditor.putString(QUICK_SERVICE_TABLE_ID, mTableObject.getString("table_id"))
                    mEditor.putInt(QUICK_SERVICE_TABLE_NO, mTableObject.getInt("table_no"))
                    mEditor.apply()
                }

                tableEntries.add(mTableModel)

            }
        }

        return tableEntries
    }

    @Throws(JSONException::class)
    fun getKitchenData(mKitchenArray: JSONArray): java.util.ArrayList<KitchenModel> {

        e(javaClass.simpleName,"getKitchenData")

        val kitchenEntries = java.util.ArrayList<KitchenModel>()

        for (i in 0 until mKitchenArray.length()) {

            val mSingleKitchen = mKitchenArray.getJSONObject(i)

            val mKitchenModel = KitchenModel(
                mSingleKitchen.getString("kitchen_id"),
                mSingleKitchen.getString("kitchen_name"),
                NO_TYPE,
                PAPER_SIZE_80, "","", "", "", 0
            )


            kitchenEntries.add(mKitchenModel)
        }

        return kitchenEntries
    }

    @Throws(JSONException::class)
    fun getPaymentData(mPaymentArray: JSONArray): java.util.ArrayList<PaymentMethodModel> {
        val paymentMethodEntries = java.util.ArrayList<PaymentMethodModel>()

        for (i in 0 until mPaymentArray.length()) {
            val mPaymentMethod = mPaymentArray.getJSONObject(i)

            var mImageResource = 0
            val mPaymentMethodType = 0
            val mPayment: PaymentMethodModel
            if (mPaymentMethod.get("payment_method_type") != "") {
                mImageResource = when {
                    mPaymentMethod.getInt("payment_method_type") == 1 -> R.drawable.ic_cash_white
                    mPaymentMethod.getInt("payment_method_type") == 2 -> R.drawable.ic_credit_card_white
                    mPaymentMethod.getInt("payment_method_type") == 3 -> R.drawable.ic_wallet_white
                    else -> 0
                }

                mPayment = PaymentMethodModel(
                    mPaymentMethod.getString("payment_method_id"),
                    mPaymentMethod.getString("payment_method_name"),
                    mPaymentMethod.getInt("payment_method_type"),
                    mImageResource
                )
            } else {
                mPayment = PaymentMethodModel(
                    mPaymentMethod.getString("payment_method_id"),
                    mPaymentMethod.getString("payment_method_name"),
                    mPaymentMethodType,
                    mImageResource
                )
            }



            paymentMethodEntries.add(mPayment)
        }

        return paymentMethodEntries
    }

    fun getFavoriteItemURL(mRestaurantID: String?, mLocation: String?): URL {
        return buildWithFavoriteItemQuery(mRestaurantID, mLocation)
    }

    private fun buildWithFavoriteItemQuery(mRestaurantID: String?, mLocation: String?): URL {
        val getAllDataUri = Uri.parse(FAVORITE_ITEMS).buildUpon()
            .appendQueryParameter(API_RESTAURANT_ID, mRestaurantID)
            .appendQueryParameter(API_LOCATION_ID, mLocation)
            .build()

        return URL(getAllDataUri.toString())
    }

    fun getResponseFromFavoriteItemsAPI(
        mContext: Context,
        mFavoriteItemURL: String,
        mLocationID: String?,
        mInterface: DatabaseDownloadDataSource
    ) {
        val mSharedPref = mContext.getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)

        val mRequest = object : StringRequest(Method.GET, mFavoriteItemURL, { response ->
            if (response != null) {
                e(javaClass.simpleName, "getResponseFromFavoriteItemsAPI: $response")
                val mProductList = ArrayList<DownloadedFavoriteModel>()
                try {
                    val mJsonObj = JSONObject(response)
                    if (mJsonObj.getString("status") == "OK") {
                        val mDataArray = mJsonObj.getJSONArray("data")
                        for (i in 0 until mDataArray.length()) {
                            val mProductObj = mDataArray.getJSONObject(i)
                            mProductList.add(
                                DownloadedFavoriteModel(
                                    mProductObj.getString("product_id"), mProductObj.getInt("seq_no")
                                )
                            )
                        }
                        mInterface.favoriteItemsResponse(mProductList, mLocationID!!)
                    } else {
                        mInterface.favoriteItemsResponse(mProductList, "")
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        }, { }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val credentials = mSharedPref.getString(RESTAURANT_USER_NAME, "")!! + ":" +
                        mSharedPref.getString(RESTAURANT_PASSWORD, "")
                val auth = AUTH_BASIC + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                headers[AUTH_CONTENT_TYPE] = AUTH_CONTENT_TYPE_VALUE
                headers["Authorization"] = auth
                return headers
            }
        }
        VolleySingleton.instance!!.addToRequestQueue(mRequest)
    }

    fun getReportsDataURL(mRestaurantID: String, mDate: String): URL {
        return buildWithReportsDataQuery(mRestaurantID, mDate)!!
    }

    private fun buildWithReportsDataQuery(mRestaurantID: String, mDate: String): URL? {
        val getAllDataUri = Uri.parse(CART_REPORT).buildUpon()
            .appendQueryParameter(API_RESTAURANT_ID, mRestaurantID)
            .appendQueryParameter(API_DATE, mDate)
            .appendQueryParameter("userId", "")
            .build()

        return try {
            val getReportsURL = URL(getAllDataUri.toString())
            Log.v(javaClass.simpleName, "URL: $getReportsURL")
            getReportsURL
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            null
        }

    }

    fun getResponseFromReportsDataAPI(mContext: Context, mReportURL: String, mInterface: DownloadDatabaseNetworkInterface) {
        val mSharedPref = mContext.getSharedPreferences(MY_PREFERENCES, MODE_PRIVATE)

        val mQueue = Volley.newRequestQueue(mContext)
        val mRequest = object : StringRequest(Method.GET, mReportURL, { response ->
            when {
                response != null -> {
                    e(javaClass.simpleName, "getResponseFromReportsDataAPI: $response")
                    val mReportList = java.util.ArrayList<ReportModel>()
                    try {
                        val mJsonObj = JSONObject(response)
                        when {
                            mJsonObj.getString("status") == "OK" -> {

                                val mDataArray = mJsonObj.getJSONArray("data")

                                when {
                                    mDataArray.length() > 0 -> {
                                        for (i in 0 until mDataArray.length()) {
                                            val mPaymentList = java.util.ArrayList<ReportPaymentModel>()
                                            val mProductList = java.util.ArrayList<ReportProductModel>()

                                            val mDataJsonObj = mDataArray.getJSONObject(i)

                                            val mOrderType = mDataJsonObj.getInt("order_type")
                                            val mTableNO = mDataJsonObj.getInt("tableno")
                                            val mTaxAmount = mDataJsonObj.getDouble("tax_amount")
                                            val mAmountPaid = mDataJsonObj.getDouble("amount_paid")
                                            val mCartTotal = mDataJsonObj.getDouble("cart_total")
                                            val mDiscountAmount = mDataJsonObj.getDouble("discount_amount")
                                            val mDiscountPercent = mDataJsonObj.getDouble("discount_percent")
                                            val mTaxPercent = mDataJsonObj.getDouble("tax_percent")
                                            val mOrderTotal = mDataJsonObj.getDouble("order_total")
                                            val mRestaurantID = mDataJsonObj.getString("restaurant_id")
                                            val mPaymentSelectionType = mDataJsonObj.getString("payment_selection_type")
                                            val mCartNO = mDataJsonObj.getString("cart_no")
                                            val mTableID = mDataJsonObj.getString("table_id")
                                            val mDiscountType = mDataJsonObj.getString("discount_type")
                                            val mCartID = mDataJsonObj.getString("cart_id")
                                            val mDateTime = mDataJsonObj.getString("time").replace(".0","")
                                            val mServerName = mDataJsonObj.getString("username")
                                            val mDateTimeInTimeStamp = changeDateTimeInMillis(mDateTime.replace(".0", ""))

                                            e(javaClass.simpleName, "getResponseFromReportsDataAPI: Date: $mDateTime")

                                            val mPaymentArray = mDataJsonObj.getJSONArray("payments")

                                            for (j in 0 until mPaymentArray.length()) {
                                                val mPaymentJsonObj = mPaymentArray.getJSONObject(j)

                                                val mTransactionID = mPaymentJsonObj.getString("transaction_id")
                                                val mWalletName = mPaymentJsonObj.getString("wallet_name")
                                                val mEntryMode = mPaymentJsonObj.getString("entry_mode")
                                                val mPaymentMethodName = mPaymentJsonObj.getString("payment_method_name")
                                                val mCardType = mPaymentJsonObj.getString("card_type")
                                                val mCardProvider = mPaymentJsonObj.getString("card_provider")
                                                val mPaidBySeat = mPaymentJsonObj.getString("seat")
                                                val mPaymentMethodID = mPaymentJsonObj.getString("payment_method_id")
                                                val mCardNO = mPaymentJsonObj.getString("card_no")
                                                val mReferenceNO = mPaymentJsonObj.getString("reference_no")
                                                val mPaymentMethodType = mPaymentJsonObj.getInt("payment_method_type")
                                                val mAmount = mPaymentJsonObj.getDouble("amount")
                                                val mCashAmount = mPaymentJsonObj.getDouble("cash_given")
                                                val mChangeAmount = mPaymentJsonObj.getDouble("change_given")

                                                val mReportPaymentModel = ReportPaymentModel(
                                                    mTransactionID,
                                                    mPaymentMethodType,
                                                    mAmount,
                                                    mWalletName,
                                                    mEntryMode,
                                                    mPaymentMethodName,
                                                    mCashAmount,
                                                    mCardType,
                                                    mCardProvider,
                                                    mPaidBySeat,
                                                    mChangeAmount,
                                                    mPaymentMethodID,
                                                    mCardNO,
                                                    mReferenceNO
                                                )
                                                mPaymentList.add(mReportPaymentModel)
                                            }

                                            val mProductArray = mDataJsonObj.getJSONArray("products")

                                            for (k in 0 until mProductArray.length()) {
                                                val mProductJsonObj = mProductArray.getJSONObject(k)

                                                val mAssignedSeats = mProductJsonObj.getString("assigned_seats")
                                                val mProductName = mProductJsonObj.getString("product_name")
                                                val mCourseType = mProductJsonObj.getString("course_type")
                                                val mCategoryID = mProductJsonObj.getString("category_id")
                                                val mGroupID = mProductJsonObj.getString("group_id")
                                                val mSpecialInstructions = mProductJsonObj.getString("special_instruction")
                                                val mProductID = mProductJsonObj.getString("product_id")
                                                val mPrinterID = mProductJsonObj.getString("printer_id")
                                                val mDate = mProductJsonObj.getString("time")
                                                val mCartProductID = mProductJsonObj.getString("cart_product_id")
                                                val mProductUnitPrice = mProductJsonObj.getDouble("product_unit_price")
                                                val mSpecialInstructionPrice = mProductJsonObj.getDouble("special_instruction_price")
                                                val mProductType = mProductJsonObj.getInt("product_type")
                                                val mProductQty = mProductJsonObj.getInt("product_qty")

                                                when (mProductType) {
                                                    1 -> {
                                                        val mProductTotal = (mProductUnitPrice + mSpecialInstructionPrice) * mProductQty

                                                        val mReportProductModel = ReportProductModel(
                                                            mGroupID,
                                                            mCategoryID,
                                                            mProductID,
                                                            mProductName,
                                                            mProductQty,
                                                            mCourseType,
                                                            mProductUnitPrice,
                                                            mProductTotal,
                                                            mProductType,
                                                            mSpecialInstructions,
                                                            mSpecialInstructionPrice,
                                                            mDate,
                                                            mPrinterID,
                                                            mCartProductID,
                                                            mAssignedSeats,
                                                            ArrayList(),
                                                            ArrayList()
                                                        )
                                                        mProductList.add(mReportProductModel)
                                                    }
                                                    2 -> {
                                                        val mIngredientArray = mProductJsonObj.getJSONArray("ingredients")

                                                        var mIngredientTotal = 0.0
                                                        var mProductTotal: Double

                                                        val mIngredientList = java.util.ArrayList<ReportProductIngredientModel>()
                                                        for (l in 0 until mIngredientArray.length()) {
                                                            val mIngredientObj = mIngredientArray.getJSONObject(l)
                                                            val mIngredientID = mIngredientObj.getString("ingredient_id")
                                                            val mIngredientName = mIngredientObj.getString("ingredient_name")
                                                            val mIngredientQty = mIngredientObj.getInt("ingredient_quantity")
                                                            val mCartIngredientID = mIngredientObj.getString("cart_ingredient_id")
                                                            val mIngredientUnitPrice = mIngredientObj.getDouble("ingredent_unit_price")

                                                            mIngredientTotal += mIngredientUnitPrice * mIngredientQty

                                                            val mIngredientModel = ReportProductIngredientModel(
                                                                mIngredientID,
                                                                mIngredientName,
                                                                mIngredientQty,
                                                                mCartIngredientID,
                                                                mIngredientUnitPrice
                                                            )

                                                            mIngredientList.add(mIngredientModel)
                                                        }

                                                        mProductTotal =
                                                            (mIngredientTotal + mProductUnitPrice + mSpecialInstructionPrice) * mProductQty

                                                        val mReportProductModel = ReportProductModel(
                                                            mGroupID,
                                                            mCategoryID,
                                                            mProductID,
                                                            mProductName,
                                                            mProductQty,
                                                            mCourseType,
                                                            mProductUnitPrice,
                                                            mProductTotal,
                                                            mProductType,
                                                            mSpecialInstructions,
                                                            mSpecialInstructionPrice,
                                                            mDate,
                                                            mPrinterID,
                                                            mCartProductID,
                                                            mAssignedSeats,
                                                            mIngredientList,
                                                            ArrayList()
                                                        )
                                                        mProductList.add(mReportProductModel)

                                                    }
                                                    else -> {
                                                        val mSubProductArray = mProductJsonObj.getJSONArray("sub_products")
                                                        val mProductTotal = (mProductUnitPrice + mSpecialInstructionPrice) * mProductQty

                                                        val mSubProductList = java.util.ArrayList<ReportSubProductModel>()
                                                        for (m in 0 until mSubProductArray.length()) {
                                                            val mSubProductObj = mSubProductArray.getJSONObject(m)
                                                            val mSubProductID = mSubProductObj.getString("sub_product_id")
                                                            val mSubProductName = mSubProductObj.getString("sub_product_name")
                                                            val mCartSubProductID = mSubProductObj.getString("cart_sub_product_id")
                                                            val mSubProductUnitPrice = mSubProductObj.getDouble("sub_product_unit_price")
                                                            val mSubProductQty = mSubProductObj.getInt("sub_product_qty")

                                                            val mIngredientArray = mSubProductObj.getJSONArray("ingredients")
                                                            val mIngredientList = java.util.ArrayList<ReportProductIngredientModel>()

                                                            when {
                                                                mIngredientArray.length() > 0 -> {
                                                                    for (l in 0 until mIngredientArray.length()) {
                                                                        val mIngredientObj = mIngredientArray.getJSONObject(l)
                                                                        val mIngredientID = mIngredientObj.getString("ingredient_id")
                                                                        val mIngredientName = mIngredientObj.getString("ingredient_name")
                                                                        val mIngredientQty = mIngredientObj.getInt("ingredient_quantity")
                                                                        val mCartIngredientID = mIngredientObj.getString("cart_ingredient_id")
                                                                        val mIngredientUnitPrice =
                                                                            mIngredientObj.getDouble("ingredent_unit_price")

                                                                        val mIngredientModel = ReportProductIngredientModel(
                                                                            mIngredientID,
                                                                            mIngredientName,
                                                                            mIngredientQty,
                                                                            mCartIngredientID,
                                                                            mIngredientUnitPrice
                                                                        )

                                                                        mIngredientList.add(mIngredientModel)
                                                                    }

                                                                    val mSubProductModel = ReportSubProductModel(
                                                                        mSubProductID,
                                                                        mSubProductName,
                                                                        mSubProductUnitPrice,
                                                                        mSubProductQty,
                                                                        mCartSubProductID,
                                                                        mIngredientList
                                                                    )
                                                                    mSubProductList.add(mSubProductModel)
                                                                }
                                                                else -> {
                                                                    val mIngredientNullList =
                                                                        java.util.ArrayList<ReportProductIngredientModel>()

                                                                    val mSubProductModel = ReportSubProductModel(
                                                                        mSubProductID,
                                                                        mSubProductName,
                                                                        mSubProductUnitPrice,
                                                                        mSubProductQty,
                                                                        mCartSubProductID,
                                                                        mIngredientNullList
                                                                    )
                                                                    mSubProductList.add(mSubProductModel)
                                                                }
                                                            }
                                                        }

                                                        val mReportProductModel = ReportProductModel(
                                                            mGroupID,
                                                            mCategoryID,
                                                            mProductID,
                                                            mProductName,
                                                            mProductQty,
                                                            mCourseType,
                                                            mProductUnitPrice,
                                                            mProductTotal,
                                                            mProductType,
                                                            mSpecialInstructions,
                                                            mSpecialInstructionPrice,
                                                            mDate,
                                                            mPrinterID,
                                                            mCartProductID,
                                                            mAssignedSeats,
                                                            ArrayList(),
                                                            mSubProductList
                                                        )
                                                        mProductList.add(mReportProductModel)
                                                    }
                                                }
                                            }

                                            e(javaClass.simpleName, "getResponseFromReportsDataAPI: TimeStamp: $mDateTimeInTimeStamp")

                                            val mReportModel = ReportModel(
                                                mRestaurantID,
                                                mPaymentSelectionType,
                                                mTableNO,
                                                mCartID,
                                                mCartNO,
                                                mCartTotal,
                                                mTableID,
                                                mDiscountType,
                                                mDiscountPercent,
                                                mDiscountAmount,
                                                mTaxPercent,
                                                mTaxAmount,
                                                mAmountPaid,
                                                mOrderTotal,
                                                mDateTime,
                                                mDateTimeInTimeStamp,
                                                mOrderType,
                                                mServerName,
                                                mPaymentList,
                                                mProductList
                                            )

                                            mReportList.add(mReportModel)
                                        }
                                        mInterface.onReportsResponse(mReportList)
                                    }
                                    else -> {
                                        val mReportModel = ReportModel(
                                            "0", "0", 0, "0", "0", 0.0, "0",
                                            "0", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "0", Date(), 0,
                                            "", ArrayList(), ArrayList()
                                        )
                                        mReportList.add(mReportModel)
                                        mInterface.onReportsResponse(mReportList)
                                    }
                                }
                            }
                            else -> {
                                val mReportModel = ReportModel(
                                    "0", "0", 0, "0", "0", 0.0, "0",
                                    "0", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, "0", Date(), 0,
                                    "", ArrayList(), ArrayList()
                                )
                                mReportList.add(mReportModel)
                                mInterface.onReportsResponse(mReportList)
                            }
                        }

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }
        }, { }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val credentials = mSharedPref.getString(RESTAURANT_USER_NAME, "")!! + ":" +
                        mSharedPref.getString(RESTAURANT_PASSWORD, "")
                val auth = AUTH_BASIC + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                headers[AUTH_CONTENT_TYPE] = AUTH_CONTENT_TYPE_VALUE
                headers["Authorization"] = auth
                return headers
            }
        }
        mQueue.add(mRequest)
    }

    private fun changeDateTimeInMillis(date: String): Date {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        var mDate: Date? = null
        try {
            mDate = sdf.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        sdf.applyPattern("dd-MM-yyyy HH:mm")
        return Objects.requireNonNull<Date>(mDate)
    }

    interface DownloadDatabaseNetworkInterface {
        fun onGetALLResponse(mFlag: Int, response: String)
        fun favoriteItemsResponse(mProductList: ArrayList<DownloadedFavoriteModel>, mLocationID: String)
        fun onReportsResponse(mReportList: ArrayList<ReportModel>)
    }
}