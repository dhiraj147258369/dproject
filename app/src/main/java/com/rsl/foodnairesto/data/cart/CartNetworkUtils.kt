package com.rsl.foodnairesto.data.cart

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log.e
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.rsl.foodnairesto.data.cart.models.CartProductModel
import com.rsl.foodnairesto.data.cart.models.CartSubProductModel
import com.rsl.foodnairesto.data.database_download.models.*
import com.rsl.foodnairesto.data.tables.models.ServerTableSeatModel
import com.rsl.foodnairesto.utils.AppConstants.API_CART_ID
import com.rsl.foodnairesto.utils.AppConstants.API_CART_INGREDIENTS_ID
import com.rsl.foodnairesto.utils.AppConstants.API_CART_NO
import com.rsl.foodnairesto.utils.AppConstants.API_CART_PRODUCT_ID
import com.rsl.foodnairesto.utils.AppConstants.API_CART_SUB_PRODUCT_ID
import com.rsl.foodnairesto.utils.AppConstants.API_COURSE_TYPE
import com.rsl.foodnairesto.utils.AppConstants.API_INGREDIENT_ID
import com.rsl.foodnairesto.utils.AppConstants.API_INGREDIENT_NAME
import com.rsl.foodnairesto.utils.AppConstants.API_INGREDIENT_QUANTITY
import com.rsl.foodnairesto.utils.AppConstants.API_INGREDIENT_UNIT_PRICE
import com.rsl.foodnairesto.utils.AppConstants.API_LOCATION_ID
import com.rsl.foodnairesto.utils.AppConstants.API_ORDER_TYPE
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCTS
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_ASSIGNED_SEATS
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_CATEGORY_ID
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_GROUP_ID
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_ID
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_INGREDIENTS
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_NAME
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_PRINTER_ID
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_QTY
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_SEND_TO_KITCHEN_FLAG
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_SPECIAL_INSTRUCTION
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_SPECIAL_INSTRUCTION_PRICE
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_TOTAL_PRICE
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_TYPE
import com.rsl.foodnairesto.utils.AppConstants.API_PRODUCT_UNIT_PRICE
import com.rsl.foodnairesto.utils.AppConstants.API_RESTAURANT_ID
import com.rsl.foodnairesto.utils.AppConstants.API_SEQUENCE_NO
import com.rsl.foodnairesto.utils.AppConstants.API_SUB_PRODUCTS
import com.rsl.foodnairesto.utils.AppConstants.API_SUB_PRODUCT_CATEGORY_ID
import com.rsl.foodnairesto.utils.AppConstants.API_SUB_PRODUCT_GROUP_ID
import com.rsl.foodnairesto.utils.AppConstants.API_SUB_PRODUCT_ID
import com.rsl.foodnairesto.utils.AppConstants.API_SUB_PRODUCT_NAME
import com.rsl.foodnairesto.utils.AppConstants.API_SUB_PRODUCT_QUANTITY
import com.rsl.foodnairesto.utils.AppConstants.API_SUB_PRODUCT_UNIT_PRICE
import com.rsl.foodnairesto.utils.AppConstants.API_TIME
import com.rsl.foodnairesto.utils.AppConstants.AUTH_BASIC
import com.rsl.foodnairesto.utils.AppConstants.AUTH_CONTENT_TYPE
import com.rsl.foodnairesto.utils.AppConstants.AUTH_CONTENT_TYPE_VALUE
import com.rsl.foodnairesto.utils.AppConstants.DATE_FORMAT_DMY_HMS
import com.rsl.foodnairesto.utils.AppConstants.MY_PREFERENCES
import com.rsl.foodnairesto.utils.AppConstants.RESTAURANT_ID
import com.rsl.foodnairesto.utils.AppConstants.RESTAURANT_PASSWORD
import com.rsl.foodnairesto.utils.AppConstants.RESTAURANT_USER_NAME
import com.rsl.foodnairesto.utils.AppConstants.SELECTED_LOCATION_ID
import com.rsl.foodnairesto.utils.EndPoints.DELETE_CART_PRODUCT
import com.rsl.foodnairesto.utils.EndPoints.SUBMIT_CART
import com.rsl.foodnairesto.utils.Utils
import com.rsl.foodnairesto.utils.VolleySingleton
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("LogNotTimber")
object CartNetworkUtils {

    fun submitCart(
        mContext: Context, mProductCartModel: ArrayList<CartProductModel>,
        mInterface: CartNetworkInterface
    ) {
        val mSharedPrefs = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)

        val mJSONObject = getFormattedJsonObject(mContext, mProductCartModel)

        e(javaClass.simpleName, "submitCart: $mJSONObject")

        val mRequest = object : JsonObjectRequest(SUBMIT_CART, mJSONObject, { response ->
            mInterface.onSubmitCart(response.toString())
        }, { error -> mInterface.onSubmitCart(error.toString()) }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val credentials = mSharedPrefs.getString(RESTAURANT_USER_NAME, "")!! + ":" +
                        mSharedPrefs.getString(RESTAURANT_PASSWORD, "")
                val auth = AUTH_BASIC + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                headers[AUTH_CONTENT_TYPE] = AUTH_CONTENT_TYPE_VALUE
                headers["Authorization"] = auth
                return headers
            }
        }

        mRequest.retryPolicy = DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        VolleySingleton.instance!!.addToRequestQueue(mRequest)
    }

    fun parseSubmitCartResponse(response: JSONObject): ArrayList<CartProductModel> {

        val mProductList = ArrayList<CartProductModel>()
        val mDataArray = response.getJSONArray("data")
        if (mDataArray.length() > 0) {
            val mDataObj = mDataArray.getJSONObject(0)

            val mProductArray = mDataObj.getJSONArray("products")

            if (mProductArray.length() > 0) {
                for (i in 0 until mProductArray.length()) {

                    val mJsonObj = mProductArray.getJSONObject(i)

                    val mCartProductID = mJsonObj.getString("cart_product_id")
                    val mMainProductID = mJsonObj.getString("product_id")
                    val mCategoryID = mJsonObj.getString("category_id")
                    val mGroupID = mJsonObj.getString("group_id")
                    val mCourseType = mJsonObj.getString("course_type")
                    val mProductType = mJsonObj.getInt("product_type")
                    val mProductName = mJsonObj.getString("product_name")
                    val mProductUnitPrice = mJsonObj.getDouble("product_unit_price")
                    val mProductQty = mJsonObj.getInt("product_qty")
                    val mSpecialInstruction = mJsonObj.getString("special_instruction")
                    val mSpecialInstructionPrice = mJsonObj.getDouble("special_instruction_price")
                    val mKitchenFlag = mJsonObj.getInt("send_to_kitchen_flag")
                    val mPrinterID = mJsonObj.getString("printer_id")
                    val mSequenceNO = mJsonObj.getInt("seq_no")

//                    if (mProductType == 1) {

                    val mProductModel = CartProductModel(
                        "",
                        mDataObj.getString("table_id"),
                        mDataObj.getInt("tableno"),
                        mDataObj.getString("group_name"),
                        0,
                        ArrayList(),
                        mDataObj.getString("cart_id"),
                        mDataObj.getString("cart_no"),
                        mCartProductID,
                        0,
                        mDataObj.getString("user_id"),
                        mDataObj.getString("user_name"),
                        mMainProductID,
                        mCategoryID,
                        "",
                        mGroupID,
                        "",
                        mCourseType,
                        mProductType,
                        mProductName,
                        BigDecimal(mProductUnitPrice),
                        BigDecimal(mProductQty),
                        BigDecimal(0.0),
                        mSpecialInstruction,
                        BigDecimal(mSpecialInstructionPrice),
                        ArrayList(),
                        ArrayList(),
                        ArrayList(),
                        ArrayList(),
                        Date(),
                        "",
                        mKitchenFlag,
                        mPrinterID,
                        mSequenceNO,
                        true
                    )

                    mProductList.add(mProductModel)
                }
            }


        }
        return mProductList
    }

    @SuppressLint("LogNotTimber")
    private fun getFormattedJsonObject(
        mContext: Context,
        mProductCartList: ArrayList<CartProductModel>
    ): JSONObject {

        val mSharedPref = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        val mRestaurantID = mSharedPref.getString(RESTAURANT_ID, "")
        val mLocationID = mSharedPref.getString(SELECTED_LOCATION_ID, "")

        val mJSONObject = JSONObject()
        try {
            mJSONObject.put(API_RESTAURANT_ID, mRestaurantID)
            mJSONObject.put(API_LOCATION_ID, mLocationID)
            mJSONObject.put(API_CART_ID, mProductCartList[0].mCartID)
            mJSONObject.put(API_CART_NO, mProductCartList[0].mCartNO)
            mJSONObject.put(API_ORDER_TYPE, mProductCartList[0].mOrderType)
            val mProductArray = JSONArray()

            for (x in 0 until mProductCartList.size) {

                val mProductType = mProductCartList[x].mProductType

                val mSingleProductObject = JSONObject()
                mSingleProductObject.put(API_CART_PRODUCT_ID, mProductCartList[x].mCartProductID)
                mSingleProductObject.put(API_PRODUCT_CATEGORY_ID, mProductCartList[x].mProductCategoryID)
                mSingleProductObject.put(API_PRODUCT_GROUP_ID, mProductCartList[x].mProductGroupID)
                mSingleProductObject.put(API_COURSE_TYPE, mProductCartList[x].mCourseType)
                mSingleProductObject.put(API_PRODUCT_NAME, mProductCartList[x].mProductName)
                mSingleProductObject.put(API_PRODUCT_QTY, mProductCartList[x].mProductQuantity)
                mSingleProductObject.put(API_PRODUCT_TYPE, mProductType)
                mSingleProductObject.put(API_PRODUCT_UNIT_PRICE, mProductCartList[x].mProductUnitPrice)
                mSingleProductObject.put(API_PRODUCT_SPECIAL_INSTRUCTION, mProductCartList[x].mSpecialInstruction)
                mSingleProductObject.put(
                    API_PRODUCT_SPECIAL_INSTRUCTION_PRICE,
                    mProductCartList[x].mSpecialInstructionPrice
                )
                mSingleProductObject.put(API_SEQUENCE_NO, mProductCartList[x].mSequenceNO)


                when (mProductType) {
                    2 -> {

                        mSingleProductObject.put(API_PRODUCT_ID, mProductCartList[x].mProductID)

                        //ingredients
                        val mIngredientsArray = JSONArray()

                        val mIngredientsCategoryList = mProductCartList[x].mEditModifierList

                        for (i in 0 until mIngredientsCategoryList!!.size) {
                            val mIngredientCategory = mIngredientsCategoryList[i]

                            for (j in 0 until mIngredientCategory.mIngredientsList!!.size) {

                                val mIngredient = mIngredientCategory.mIngredientsList!![j]

                                when {
                                    mIngredient.isSelected -> {
                                        val mIngredientObject = JSONObject()
                                        mIngredientObject.put(API_CART_INGREDIENTS_ID, "")
                                        mIngredientObject.put(API_INGREDIENT_ID, mIngredient.mIngredientID)
                                        mIngredientObject.put(API_INGREDIENT_NAME, mIngredient.mIngredientName)
                                        mIngredientObject.put(API_INGREDIENT_QUANTITY, mIngredient.mIngredientQuantity)
                                        mIngredientObject.put(API_INGREDIENT_UNIT_PRICE, mIngredient.mIngredientPrice)

                                        mIngredientsArray.put(mIngredientObject)
                                    }
                                }
                            }
                        }

                        mSingleProductObject.put(API_PRODUCT_INGREDIENTS, mIngredientsArray)
                    }
                    else -> mSingleProductObject.put(API_PRODUCT_ID, mProductCartList[x].mProductID)
                }

                when (mProductType) {
                    3 -> {
                        //sub products
                        mSingleProductObject.put(API_PRODUCT_ID, mProductCartList[x].mProductID)
                        val mSubProductCategoryList = mProductCartList[x].mSubProductCategoryList

                        val mSubProductsList = ArrayList<ProductModel>()
                        for (i in 0 until mSubProductCategoryList!!.size) {
                            val mStoredProductList = mSubProductCategoryList[i].mProductList

                            for (j in 0 until mStoredProductList.size) {
                                when {
                                    mStoredProductList[j].isSelected -> mSubProductsList.add(mStoredProductList[j])
                                }
                            }
                        }

                        val mSubProductsArray = JSONArray()

                        for (i in mSubProductsList.indices) {

                            val mSubProduct = mSubProductsList[i]

                            val mSubProductObject = JSONObject()

                            val mSubProductIngredientsArray = JSONArray()

                            when (mSubProduct.mProductType) {
                                2 -> {
                                    val mGenericProductList = mSubProduct.mGenericProductList


                                    var mGenericProducts: GenericProducts? = null

                                    loop@ for (j in 0 until mGenericProductList.size) {
                                        when {
                                            mGenericProductList[j].isSelected -> {
                                                mGenericProducts = mGenericProductList[j]
                                                mSubProductObject.put(API_SUB_PRODUCT_ID, mGenericProducts.mGenericProductID)
                                                mSubProductObject.put(API_SUB_PRODUCT_CATEGORY_ID, mGenericProducts.mCategoryID)
                                                mSubProductObject.put(API_SUB_PRODUCT_GROUP_ID, mGenericProducts.mGroupID)
                                                mSubProductObject.put(API_SUB_PRODUCT_NAME, mGenericProducts.mGenericProductName)
                                                mSubProductObject.put(
                                                    API_SUB_PRODUCT_QUANTITY,
                                                    mProductCartList[x].mProductQuantity
                                                )
                                                mSubProductObject.put(API_SUB_PRODUCT_UNIT_PRICE, mGenericProducts.mDineInPrice)
                                                break@loop
                                            }
                                        }
                                    }

                                    mSubProductObject.put(API_CART_SUB_PRODUCT_ID, "")

                                    val mIngredientCategoryList = mGenericProducts!!.mIngredientCategoryList
                                    for (j in 0 until mIngredientCategoryList.size) {

                                        val mIngredientList = mIngredientCategoryList[j].mIngredientsList

                                        for (k in 0 until mIngredientList!!.size) {

                                            val mIngredient = mIngredientList[k]

                                            when {
                                                mIngredient.isSelected -> {
                                                    val mSubProductIngredientObject = JSONObject()

                                                    mSubProductIngredientObject.put(API_CART_INGREDIENTS_ID, "")
                                                    mSubProductIngredientObject.put(API_INGREDIENT_ID, mIngredient.mIngredientID)
                                                    mSubProductIngredientObject.put(
                                                        API_INGREDIENT_NAME,
                                                        mIngredient.mIngredientName
                                                    )
                                                    mSubProductIngredientObject.put(
                                                        API_INGREDIENT_QUANTITY,
                                                        mIngredient.mIngredientQuantity
                                                    )
                                                    mSubProductIngredientObject.put(
                                                        API_INGREDIENT_UNIT_PRICE,
                                                        mIngredient.mIngredientPrice
                                                    )

                                                    mSubProductIngredientsArray.put(mSubProductIngredientObject)
                                                }
                                            }
                                        }
                                    }

                                    mSubProductObject.put(API_PRODUCT_INGREDIENTS, mSubProductIngredientsArray)
                                    mSubProductsArray.put(mSubProductObject)
                                }
                                1 -> {
                                    mSubProductObject.put(API_CART_SUB_PRODUCT_ID, "")

                                    mSubProductObject.put(API_SUB_PRODUCT_ID, mSubProduct.mProductID)
                                    mSubProductObject.put(API_SUB_PRODUCT_CATEGORY_ID, mSubProduct.mCategoryID)
                                    mSubProductObject.put(API_SUB_PRODUCT_GROUP_ID, mSubProduct.mGroupID)
                                    mSubProductObject.put(API_SUB_PRODUCT_NAME, mSubProduct.mProductName)
                                    mSubProductObject.put(API_SUB_PRODUCT_QUANTITY, mProductCartList[x].mProductQuantity)
                                    mSubProductObject.put(API_SUB_PRODUCT_UNIT_PRICE, mSubProduct.mDineInPrice)
                                    mSubProductObject.put(API_PRODUCT_INGREDIENTS, mSubProductIngredientsArray)
                                    mSubProductsArray.put(mSubProductObject)
                                }
                            }

                        }

                        mSingleProductObject.put(API_SUB_PRODUCTS, mSubProductsArray)
                    }
                }


                val mSeatList = mProductCartList[x].mAssignedSeats

                val mSeats = StringBuilder()

                for (i in mSeatList!!.indices) {
                    when {
                        mSeatList[i].mSeatNO != 0 ->
                            when (i) {
                                mSeatList.size - 1 -> mSeats.append(mSeatList[i].mSeatNO)
                                else -> mSeats.append(mSeatList[i].mSeatNO).append(",")
                            }
                    }
                }
                mSingleProductObject.put(API_PRODUCT_ASSIGNED_SEATS, mSeats)
                mSingleProductObject.put(API_PRODUCT_TOTAL_PRICE, mProductCartList[x].mProductTotalPrice)
                mSingleProductObject.put(API_PRODUCT_SEND_TO_KITCHEN_FLAG, mProductCartList[x].mKitchenPrintFlag)
                mSingleProductObject.put(API_PRODUCT_PRINTER_ID, mProductCartList[x].mPrinterID)
                mSingleProductObject.put(
                    API_TIME,
                    Utils.getStringFromDate(DATE_FORMAT_DMY_HMS, mProductCartList[x].mDateInMillis)
                )
                mProductArray.put(mSingleProductObject)

            }

            mJSONObject.put(API_PRODUCTS, mProductArray)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return mJSONObject
    }


    internal fun getSyncCartUrl(mContext: Context): URL {
        return buildSyncCartUrl(mContext)
    }

    private fun buildSyncCartUrl(mContext: Context): URL {
        val sharedPreferences = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        val mRestaurantID = sharedPreferences.getString(RESTAURANT_ID, "")
        val loginQueryUri = Uri.parse(SUBMIT_CART).buildUpon()
            .appendQueryParameter(API_RESTAURANT_ID, mRestaurantID)
            .build()

        return URL(loginQueryUri.toString())
    }

    internal fun syncCart(mContext: Context, mSyncCartURL: URL, mInterface: CartNetworkInterface) {
        val mSharedPref = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)

        val mRequest = object : StringRequest(
            Method.GET, mSyncCartURL.toString(),
            { mInterface.onSyncCart(it) },
            { error -> mInterface.onSyncCart(error.toString()) }) {
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

        mRequest.retryPolicy = DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        VolleySingleton.instance!!.addToRequestQueue(mRequest)

    }

    @SuppressLint("LogNotTimber")
    fun getCartData(
        mProductList: ArrayList<ProductModel>,
        mLocalLocationID: String,
        mCartJSONObject: JSONObject
    ): List<CartProductModel> {
        val mSyncCartList = ArrayList<CartProductModel>()
        val mCartArray = mCartJSONObject.getJSONArray("data")

        for (i in 0 until mCartArray.length()) {
            val mJSONObject = mCartArray.getJSONObject(i)

            when {
                mJSONObject.has("products") -> {

                    val mProductArray = mJSONObject.getJSONArray("products")

                    val mTableID = mJSONObject.getString("table_id")
                    val mTableNO = mJSONObject.getInt("tableno")
                    val mGroupName = mJSONObject.getString("group_name")
                    val mServerID = mJSONObject.getString("user_id")
                    val mServerName = mJSONObject.getString("user_name")
                    val mLocationID = mJSONObject.getString("location_id")
                    val mCartID = mJSONObject.getString("cart_id")
                    val mCartNO = mJSONObject.getString("cart_no")
                    val mOrderType = mJSONObject.getInt("order_type")


                    when (mLocalLocationID) {
                        mLocationID -> for (j in 0 until mProductArray.length()) {
                            val mProductObject = mProductArray.getJSONObject(j)

                            val mProductName = mProductObject.getString("product_name")
                            val mProductID = mProductObject.getString("product_id")
                            val mCourseType = mProductObject.getString("course_type")
                            val mCategoryID = mProductObject.getString("category_id")
                            val mGroupID = mProductObject.getString("group_id")
                            val mPrinterID = mProductObject.getString("printer_id")
                            val mCartProductID = mProductObject.getString("cart_product_id")
                            val mGenericProductID = mProductObject.getString("product_id")
                            val mProductType = mProductObject.getInt("product_type")
                            val mProductUnitPrice =
                                BigDecimal(mProductObject.getDouble("product_unit_price")).setScale(2, RoundingMode.HALF_UP)
                            val mProductQty = BigDecimal(mProductObject.getInt("product_qty"))
                            val mSpecialInstruction = mProductObject.getString("special_instruction")
                            val mSpecialInstructionPrice =
                                BigDecimal(mProductObject.getDouble("special_instruction_price")).setScale(
                                    2,
                                    RoundingMode.HALF_UP
                                )

                            val mProductTotalPrice = (mProductUnitPrice + mSpecialInstructionPrice) * mProductQty
                            val mKitchenFlag = mProductObject.getInt("send_to_kitchen_flag")
                            val mDateInMillis =
                                Utils.getDateFromString("dd/MM/yyyy HH:mm:ss", mProductObject.getString("time"))
                            val mDate = Utils.getStringFromDate("dd/MM/yyyy HH:mm:ss", mDateInMillis)

                            val mAssignedSeats =
                                mProductObject.getString("assigned_seats").split(",".toRegex())
                                    .dropLastWhile { it.isEmpty() }
                                    .toTypedArray()

                            val mSeatList = ArrayList<ServerTableSeatModel>()

                            for (mAssignedSeat in mAssignedSeats)
                                mSeatList.add(ServerTableSeatModel(mAssignedSeat.toInt()))

                            val mIngredientCategoryList = ArrayList<IngredientCategoryModel>()
                            val mShowModifierList: ArrayList<IngredientsModel> = ArrayList()
                            val mSubProductList = ArrayList<CartSubProductModel>()
                            var mStoredSubProductCategoryList = ArrayList<SubProductCategoryModel>()


                            when (mProductType) {
                                2 -> {

                                    val mIngredientList = ArrayList<IngredientsModel>()
                                    val mIngredientsArray = mProductObject.getJSONArray("ingredients")

                                    for (k in 0 until mIngredientsArray.length()) {
                                        val mIngredientObject = mIngredientsArray.getJSONObject(k)

                                        val mIngredientPrice = mIngredientObject.getDouble("ingredent_unit_price")
                                        val mIngredientQty = mIngredientObject.getInt("ingredient_quantity")

                                        val mIngredientModel = IngredientsModel(
                                            mIngredientObject.getString("ingredient_id"),
                                            mIngredientObject.getString("ingredient_name"),
                                            BigDecimal(mIngredientPrice), BigDecimal(mIngredientQty), "", "",
                                            isSelected = true,
                                            isCompulsory = false,
                                            mSelectionType = 1,
                                            mCartIngredientID = mIngredientObject.getString("cart_ingredient_id")
                                        )

                                        mIngredientList.add(mIngredientModel)
                                    }

                                    e(javaClass.simpleName, "CartNetwork: ${mIngredientList.size}")

                                    var mGenericProduct: GenericProducts? = null

                                    product@
                                    for (k in 0 until mProductList.size) {
                                        when (mProductList[k].mProductType) {
                                            2 -> {
                                                val mGenericProductList = mProductList[k].mGenericProductList

                                                for (l in 0 until mGenericProductList.size) {
                                                    when (mGenericProductID) {
                                                        mGenericProductList[l].mGenericProductID -> {
                                                            mGenericProduct = mGenericProductList[l]
                                                            break@product
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    mIngredientCategoryList.addAll(mGenericProduct!!.mIngredientCategoryList)

                                    for (k in 0 until mIngredientCategoryList.size) {
                                        val mLocalIngredientList = mIngredientCategoryList[k].mIngredientsList

                                        for (l in 0 until mIngredientList.size) {
                                            loop@ for (m in 0 until mLocalIngredientList!!.size) {
                                                when (mIngredientList[l].mIngredientID) {
                                                    mLocalIngredientList[m].mIngredientID -> {
                                                        mLocalIngredientList[m].isSelected = true
                                                        mLocalIngredientList[m].mIngredientQuantity =
                                                            mIngredientList[l].mIngredientQuantity
                                                        mLocalIngredientList[m].mSelectionType = mIngredientList[l].mSelectionType
                                                        mShowModifierList.add(mIngredientList[l])

                                                        break@loop
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                3 -> {

                                    val mSubProductArray = mProductObject.getJSONArray("sub_products")

                                    //for sub products
                                    for (k in 0 until mSubProductArray.length()) {
                                        val mSubProductObject = mSubProductArray.getJSONObject(k)

                                        val mIngredientArray = mSubProductObject.getJSONArray("ingredients")
                                        val mIngredientList = ArrayList<IngredientsModel>()

                                        when {
                                            mIngredientArray.length() == 0 -> {
                                                //this is type 1 as it has no ingredients
                                                val mSubProduct =
                                                    CartSubProductModel(
                                                        mSubProductObject.getString("sub_product_id"),
                                                        mSubProductObject.getString("sub_product_id"),
                                                        mSubProductObject.getString("sub_product_id"),
                                                        mIngredientList, mSubProductObject.getString("sub_product_name"),
                                                        BigDecimal(mSubProductObject.getDouble("sub_product_unit_price")),
                                                        BigDecimal(mSubProductObject.getDouble("sub_product_unit_price")),
                                                        BigDecimal(mSubProductObject.getDouble("sub_product_unit_price")),
                                                        2, 0, 0
                                                    )

                                                mSubProductList.add(mSubProduct)
                                            }
                                            else -> {
                                                //this is type 2 as it has ingredients

                                                var mLocalProduct: GenericProducts? = null
                                                group@
                                                for (l in 0 until mProductList.size) {
                                                    when (mProductList[l].mProductType) {
                                                        2 -> {
                                                            val mGenericProductList = mProductList[l].mGenericProductList

                                                            for (m in 0 until mGenericProductList.size) {
                                                                when (mGenericProductList[m].mGenericProductID) {
                                                                    mSubProductObject.getString("sub_product_id")
                                                                    -> {
                                                                        mLocalProduct = mGenericProductList[m]
                                                                        break@group
                                                                    }
                                                                }
                                                            }


                                                        }
                                                    }
                                                }

                                                when {
                                                    mLocalProduct != null -> {

                                                        val mLocalIngredientCategoryList = mLocalProduct.mIngredientCategoryList

                                                        val mLocalIngredientList = ArrayList<IngredientsModel>()
                                                        for (l in 0 until mLocalIngredientCategoryList.size) {
                                                            val mStoredIngredientList = mLocalIngredientCategoryList[l].mIngredientsList
                                                            for (m in 0 until mStoredIngredientList!!.size) {
                                                                mLocalIngredientList.add(mStoredIngredientList[m])
                                                            }
                                                        }


                                                        for (l in 0 until mIngredientArray.length()) {
                                                            val mIngredientObject = mIngredientArray.getJSONObject(l)

                                                            loop@ for (m in 0 until mLocalIngredientList.size) {
                                                                when (mLocalIngredientList[m].mIngredientID) {
                                                                    mIngredientObject.getString("ingredient_id") -> {

                                                                        val mIngredient = IngredientsModel(
                                                                            mIngredientObject.getString("ingredient_id"),
                                                                            mIngredientObject.getString("ingredient_name"),
                                                                            BigDecimal(mIngredientObject.getDouble("ingredent_unit_price")),
                                                                            BigDecimal(mIngredientObject.getInt("ingredient_quantity")),
                                                                            mLocalIngredientList[m].mIngredientProductID,
                                                                            mLocalIngredientList[m].mIngredientCategoryID,
                                                                            true,
                                                                            mLocalIngredientList[m].isCompulsory,
                                                                            mLocalIngredientList[m].mSelectionType,
                                                                            mIngredientObject.getString("cart_ingredient_id")
                                                                        )
                                                                        mIngredientList.add(mIngredient)
                                                                        break@loop
                                                                    }
                                                                }
                                                            }
                                                        }

                                                        val mSubProduct =
                                                            CartSubProductModel(
                                                                mSubProductObject.getString("sub_product_id"),
                                                                mLocalProduct.mCategoryID,
                                                                mLocalProduct.mGroupID,
                                                                mIngredientList, mSubProductObject.getString("sub_product_name"),
                                                                BigDecimal(mSubProductObject.getDouble("sub_product_unit_price")),
                                                                mLocalProduct.mQuickServicePrice,
                                                                mLocalProduct.mDeliveryPrice,
                                                                2, 0, 0
                                                            )
                                                        mSubProductList.add(mSubProduct)

                                                    }
                                                }
                                            }
                                        }

                                        //for sub product category
                                        var mLocalType3Product: ProductModel? = null
                                        loop@ for (l in 0 until mProductList.size) {
                                            when (mProductID) {
                                                mProductList[l].mProductID -> {
                                                    mLocalType3Product = mProductList[l]
                                                    break@loop
                                                }
                                            }
                                        }

                                        mStoredSubProductCategoryList = mLocalType3Product!!.mSubProductCategoryList

                                        for (l in 0 until mStoredSubProductCategoryList.size) {
                                            val mStoredSubProductList = mStoredSubProductCategoryList[l].mProductList

                                            for (m in 0 until mStoredSubProductList.size) {
                                                when (mStoredSubProductList[m].mProductType) {
                                                    2 -> {

                                                        val mLocalGenericProductList = mStoredSubProductList[m].mGenericProductList

                                                        loop@ for (n in 0 until mLocalGenericProductList.size) {

                                                            when (mLocalGenericProductList[n].mGenericProductID) {
                                                                mSubProductObject.getString("sub_product_id")
                                                                -> {

                                                                    mStoredSubProductList[m].isSelected = true
                                                                    mLocalGenericProductList[n].isSelected = true
                                                                    val mLocalIngredientCategoryList =
                                                                        mLocalGenericProductList[n].mIngredientCategoryList


                                                                    for (o in 0 until mLocalIngredientCategoryList.size) {
                                                                        val mLocalIngredientList =
                                                                            mLocalIngredientCategoryList[o].mIngredientsList

                                                                        for (p in 0 until mLocalIngredientList!!.size) {


                                                                            for (q in 0 until mIngredientArray.length()) {
                                                                                val mIngredientObject =
                                                                                    mIngredientArray.getJSONObject(q)

                                                                                loop1@ for (r in 0 until mLocalIngredientList.size) {
                                                                                    when (mLocalIngredientList[r].mIngredientID) {
                                                                                        mIngredientObject.getString("ingredient_id") -> {
                                                                                            mLocalIngredientList[r].isSelected = true
                                                                                            mLocalIngredientList[r].mIngredientQuantity =
                                                                                                BigDecimal(mIngredientObject.getInt("ingredient_quantity"))
                                                                                            break@loop1
                                                                                        }
                                                                                    }
                                                                                }


                                                                            }
                                                                        }
                                                                    }
                                                                    break@loop

                                                                }
                                                            }
                                                        }

                                                    }
                                                    else -> {
                                                        when (mStoredSubProductList[m].mProductID) {
                                                            mSubProductObject.getString("sub_product_id") ->
                                                                mStoredSubProductList[m].isSelected = true
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    }

                                }
                            }

                            val mProductModel = CartProductModel(
                                mLocationID,
                                mTableID,
                                mTableNO,
                                mGroupName,
                                mSeatList.size,
                                mSeatList,
                                mCartID,
                                mCartNO,
                                mCartProductID,
                                mOrderType,
                                mServerID,
                                mServerName,
                                mProductID,
                                mCategoryID,
                                "",
                                mGroupID,
                                mCourseType,
                                mCourseType,
                                mProductType,
                                mProductName,
                                mProductUnitPrice,
                                mProductQty,
                                mProductTotalPrice,
                                mSpecialInstruction,
                                mSpecialInstructionPrice,
                                mIngredientCategoryList,
                                mShowModifierList,
                                mStoredSubProductCategoryList,
                                mSubProductList,
                                mDateInMillis,
                                mDate,
                                mKitchenFlag,
                                mPrinterID,
                                j,
                                true
                            )
                            mSyncCartList.add(mProductModel)


                        }
                    }

                }
            }
        }


        return mSyncCartList
    }

    fun deleteCartProduct(context: Context, mProductCartModel: CartProductModel, mInterface: CartNetworkInterface) {
        val mSharedPref = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)

        val mJSONObject = JSONObject()

        try {
            mJSONObject.put(API_RESTAURANT_ID, mSharedPref.getString(RESTAURANT_ID, ""))
            mJSONObject.put(API_CART_ID, mProductCartModel.mCartID)
            mJSONObject.put(API_CART_PRODUCT_ID, mProductCartModel.mCartProductID)
            mJSONObject.put(API_CART_SUB_PRODUCT_ID, "")
            mJSONObject.put(API_CART_INGREDIENTS_ID, "")


        } catch (e: JSONException) {
            e.printStackTrace()
        }

        e(javaClass.simpleName, "deleteCartProduct: JSON: $mJSONObject")

        val mJSONObjectRequest = object : JsonObjectRequest(
            Method.POST, DELETE_CART_PRODUCT, mJSONObject,
            { response -> mInterface.onDeleteCartProduct(response.toString()) },
            { error -> mInterface.onDeleteCartProduct(error.toString()) }) {
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

        mJSONObjectRequest.retryPolicy = DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        VolleySingleton.instance!!.addToRequestQueue(mJSONObjectRequest)
    }

    interface CartNetworkInterface {
        fun onSubmitCart(mResponse: String)
        fun onSyncCart(mResponse: String)
        fun onDeleteCartProduct(mResponse: String)
    }
}