package com.rsl.youresto.data.checkout

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.util.Base64
import android.util.Log.e
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.rsl.youresto.data.checkout.model.CheckoutModel
import com.rsl.youresto.data.checkout.model.CheckoutTransaction
import com.rsl.youresto.data.checkout.model.PaymentTransaction
import com.rsl.youresto.data.database_download.models.PaymentMethodModel
import com.rsl.youresto.data.database_download.models.TaxModel
import com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.model.YoyoModel
import com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.model.YoyoPaymentAuthModel
import com.rsl.youresto.utils.AppConstants.API_AMOUNT
import com.rsl.youresto.utils.AppConstants.API_AMOUNT_PAID
import com.rsl.youresto.utils.AppConstants.API_CARD_NO
import com.rsl.youresto.utils.AppConstants.API_CARD_PROVIDER
import com.rsl.youresto.utils.AppConstants.API_CARD_TYPE
import com.rsl.youresto.utils.AppConstants.API_CART_ID
import com.rsl.youresto.utils.AppConstants.API_CART_PAYMENT_ID
import com.rsl.youresto.utils.AppConstants.API_CART_TOTAL
import com.rsl.youresto.utils.AppConstants.API_CASH_GIVEN
import com.rsl.youresto.utils.AppConstants.API_CHANGE_GIVEN
import com.rsl.youresto.utils.AppConstants.API_DISCOUNT_AMOUNT
import com.rsl.youresto.utils.AppConstants.API_DISCOUNT_PERCENT
import com.rsl.youresto.utils.AppConstants.API_DISCOUNT_TYPE
import com.rsl.youresto.utils.AppConstants.API_ENTRY_MODE
import com.rsl.youresto.utils.AppConstants.API_FULL_PAID
import com.rsl.youresto.utils.AppConstants.API_GROUP_NAME
import com.rsl.youresto.utils.AppConstants.API_ORDER_TOTAL
import com.rsl.youresto.utils.AppConstants.API_ORDER_TYPE
import com.rsl.youresto.utils.AppConstants.API_PAYMENT_ARRAY
import com.rsl.youresto.utils.AppConstants.API_PAYMENT_METHOD_ID
import com.rsl.youresto.utils.AppConstants.API_PAYMENT_METHOD_NAME
import com.rsl.youresto.utils.AppConstants.API_PAYMENT_METHOD_TYPE
import com.rsl.youresto.utils.AppConstants.API_PAYMENT_SELECTION_TYPE
import com.rsl.youresto.utils.AppConstants.API_REFERENCE_NO
import com.rsl.youresto.utils.AppConstants.API_RESTAURANT_ID
import com.rsl.youresto.utils.AppConstants.API_SEAT
import com.rsl.youresto.utils.AppConstants.API_SERVICE_CHARGE
import com.rsl.youresto.utils.AppConstants.API_SERVICE_CHARGE_PERCENT
import com.rsl.youresto.utils.AppConstants.API_TABLE_ID
import com.rsl.youresto.utils.AppConstants.API_TAX_AMOUNT
import com.rsl.youresto.utils.AppConstants.API_TAX_PERCENT
import com.rsl.youresto.utils.AppConstants.API_TIME
import com.rsl.youresto.utils.AppConstants.API_TRANSACTION_ID
import com.rsl.youresto.utils.AppConstants.API_WALLET_NAME
import com.rsl.youresto.utils.AppConstants.AUTH_BASIC
import com.rsl.youresto.utils.AppConstants.AUTH_CONTENT_TYPE
import com.rsl.youresto.utils.AppConstants.AUTH_CONTENT_TYPE_VALUE
import com.rsl.youresto.utils.AppConstants.DATE_FORMAT_DMY_HMS
import com.rsl.youresto.utils.AppConstants.LOCATION_SERVICE_TYPE
import com.rsl.youresto.utils.AppConstants.MY_PREFERENCES
import com.rsl.youresto.utils.AppConstants.RESTAURANT_ID
import com.rsl.youresto.utils.AppConstants.RESTAURANT_PASSWORD
import com.rsl.youresto.utils.AppConstants.RESTAURANT_USER_NAME
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppConstants.TYPE_TIP
import com.rsl.youresto.utils.AppConstants.TYPE_WALLET
import com.rsl.youresto.utils.EndPoints
import com.rsl.youresto.utils.EndPoints.CART_PAYMENT
import com.rsl.youresto.utils.EndPoints.YOYO_WALLET_URL
import com.rsl.youresto.utils.Utils
import com.rsl.youresto.utils.VolleySingleton
import org.json.JSONArray
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


@SuppressLint("LogNotTimber")
object CheckoutNetworkUtils {

    fun updatePaymentInfo(
        mContext: Context,
        mCheckoutModel: CheckoutModel,
        mTipPaymentMethod: PaymentMethodModel,
        mInterface: CheckoutNetworkInterface
    ) {

        val mSharedPrefs = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)

        val mJSONObject = getJSONObject(mSharedPrefs, mCheckoutModel, mTipPaymentMethod)

        e(javaClass.simpleName, "updatePaymentInfo: $mJSONObject")

        val mRequest = object : JsonObjectRequest(Method.POST, CART_PAYMENT, mJSONObject,
            { response -> mInterface.onPaymentResponse(response.toString(), true) },
            { error -> mInterface.onPaymentResponse(error.toString(), false) }) {
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

    private fun getJSONObject(
        mSharedPrefs: SharedPreferences,
        mCheckout: CheckoutModel,
        mTipPaymentMethod: PaymentMethodModel
    ): JSONObject {

        val mRestaurantID = mSharedPrefs.getString(RESTAURANT_ID, "")

        val mJSONObject = JSONObject()

        mJSONObject.put(API_RESTAURANT_ID, mRestaurantID)
        mJSONObject.put(API_CART_ID, mCheckout.mCartID)
        mJSONObject.put(API_TABLE_ID, mCheckout.mTableID)
        mJSONObject.put(API_GROUP_NAME, mCheckout.mGroupName)
        mJSONObject.put(API_CART_TOTAL, mCheckout.mCartTotal)
        mJSONObject.put(API_TAX_AMOUNT, mCheckout.mTaxAmount)
        mJSONObject.put(API_TAX_PERCENT, mCheckout.mTaxPercent)
        mJSONObject.put(API_SERVICE_CHARGE, mCheckout.mServiceChargeAmount)
        mJSONObject.put(API_SERVICE_CHARGE_PERCENT, mCheckout.mServiceChargePercent)

        mJSONObject.put(API_CART_PAYMENT_ID, mCheckout.mCartPaymentID)
        mJSONObject.put(API_DISCOUNT_AMOUNT, mCheckout.mDiscountAmount)
        mJSONObject.put(API_DISCOUNT_PERCENT, mCheckout.mDiscountPercent)
        mJSONObject.put(API_DISCOUNT_TYPE, "")

        mJSONObject.put(API_ORDER_TOTAL, mCheckout.mOrderTotal)
        mJSONObject.put(API_ORDER_TYPE, mCheckout.mOrderType)
        mJSONObject.put(API_PAYMENT_SELECTION_TYPE, "SEAT SELECTION")

        mJSONObject.put(API_FULL_PAID, true)

        mJSONObject.put(API_AMOUNT_PAID, mCheckout.mAmountPaid)
        mJSONObject.put(API_TIME, Utils.getStringFromDate(DATE_FORMAT_DMY_HMS, Date()))


        val mCheckoutTransactionList = mCheckout.mCheckoutTransaction
        var mCheckoutTransaction: CheckoutTransaction? = null

        val mPaymentTransactionList = ArrayList<PaymentTransaction>()
        val mSeatList = ArrayList<Int>()

        for (i in 0 until mCheckoutTransactionList.size) {
            if (!mCheckoutTransactionList[i].isSentToServer) {
                mCheckoutTransaction = mCheckoutTransactionList[i]
                mPaymentTransactionList.addAll(mCheckoutTransactionList[i].mPaymentTransaction)
                mSeatList.addAll(mCheckoutTransactionList[i].mSeatList)
                break
            }
        }

        val mSeats = StringBuilder()

        for (i in 0 until mSeatList.size) {
            if (i == mSeatList.size - 1)
                mSeats.append(mSeatList[i])
            else
                mSeats.append(mSeatList[i]).append(",")
        }

        val mPaymentArray = JSONArray()

        for (i in 0 until mPaymentTransactionList.size) {
            val mPayment = mPaymentTransactionList[i]
            val mPaymentObject = JSONObject()

            mPaymentObject.put(API_AMOUNT, mPayment.mTransactionAmount)
            mPaymentObject.put(API_REFERENCE_NO, mPayment.mReferenceNO)
            mPaymentObject.put(API_TRANSACTION_ID, mPayment.mTransactionID)
            mPaymentObject.put(API_PAYMENT_METHOD_NAME, mPayment.mPaymentMethodName)
            mPaymentObject.put(API_PAYMENT_METHOD_TYPE, mPayment.mPaymentMethodType)
            mPaymentObject.put(API_PAYMENT_METHOD_ID, mPayment.mPaymentMethodID)
            mPaymentObject.put(API_SEAT, mSeats)

            mPaymentObject.put(API_CASH_GIVEN, mPayment.mCash)
            mPaymentObject.put(API_CHANGE_GIVEN, mPayment.mChange)
            mPaymentObject.put(API_WALLET_NAME, mPayment.mWalletName)
            mPaymentObject.put(API_CARD_NO, mPayment.mCardNo)
            mPaymentObject.put(API_CARD_TYPE, mPayment.mCardType)
            mPaymentObject.put(API_CARD_PROVIDER, mPayment.mCardProvider)
            mPaymentObject.put(API_ENTRY_MODE, mPayment.mCardEntryType)

            mPaymentArray.put(mPaymentObject)
        }

        //add tip
        if (mCheckoutTransaction!!.mTipAmount > BigDecimal(0)) {

            val mPaymentObject = JSONObject()

            mPaymentObject.put(API_AMOUNT, mCheckoutTransaction.mTipAmount)
            mPaymentObject.put(API_REFERENCE_NO, "")
            mPaymentObject.put(API_TRANSACTION_ID, "")
            mPaymentObject.put(API_PAYMENT_METHOD_NAME, mTipPaymentMethod.mPaymentMethodName)
            mPaymentObject.put(API_PAYMENT_METHOD_TYPE, mTipPaymentMethod.mPaymentMethodType)
            mPaymentObject.put(API_PAYMENT_METHOD_ID, mTipPaymentMethod.mPaymentMethodID)
            mPaymentObject.put(API_SEAT, mSeats)

            mPaymentObject.put(API_CASH_GIVEN, 0.0)
            mPaymentObject.put(API_CHANGE_GIVEN, 0.0)
            mPaymentObject.put(API_WALLET_NAME, "")
            mPaymentObject.put(API_CARD_NO, "")
            mPaymentObject.put(API_CARD_TYPE, "")
            mPaymentObject.put(API_CARD_PROVIDER, "")
            mPaymentObject.put(API_ENTRY_MODE, "")

            mPaymentArray.put(mPaymentObject)

        }

        mJSONObject.put(API_PAYMENT_ARRAY, mPaymentArray)

        return mJSONObject
    }

    fun submitOrder(mContext: Context, mCheckoutModel: CheckoutModel, mInterface: CheckoutNetworkInterface) {
        val mSharedPrefs = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        val mRestaurantID = mSharedPrefs.getString(RESTAURANT_ID, "")

        val mJSONObject = JSONObject()
        mJSONObject.put(API_RESTAURANT_ID, mRestaurantID)
        mJSONObject.put(API_CART_ID, mCheckoutModel.mCartID)

        e(javaClass.simpleName, "submitOrder: $mJSONObject")

        val mRequest = object : JsonObjectRequest(Method.POST, EndPoints.SUBMIT_ORDER, mJSONObject,
            { response -> mInterface.onOrderSubmitResponse(response.toString()) },
            { error -> mInterface.onOrderSubmitResponse(error.toString()) }) {
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

    fun yoyoPaymentAuth(
        context: Context,
        mPaymentAuthModel: YoyoPaymentAuthModel,
        mYoYoList: ArrayList<YoyoModel>,
        mInterface: CheckoutNetworkInterface
    ) {
        val mSharedPrefs = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)

        val mXMLData = getPaymentAuthXML(mPaymentAuthModel, mYoYoList)
        e(javaClass.simpleName, "Yoyo Payment Auth XML: $mXMLData")

        val mRequest = object : StringRequest(
            Method.POST, YOYO_WALLET_URL,
            Response.Listener {
                mInterface.onYoyoPaymentAuthResponse(it.toString())
            }, Response.ErrorListener {
                mInterface.onYoyoPaymentAuthResponse(it.toString())
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val credentials = mSharedPrefs.getString(RESTAURANT_USER_NAME, "")!! + ":" +
                        mSharedPrefs.getString(RESTAURANT_PASSWORD, "")
                val auth = AUTH_BASIC + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                headers[AUTH_CONTENT_TYPE] = AUTH_CONTENT_TYPE_VALUE
                headers["Authorization"] = auth
                return headers
            }

            override fun getBodyContentType(): String {
                return "application/xml"
            }

            override fun getBody(): ByteArray {
                return try {
                    mXMLData.toByteArray()
                } catch (uee: UnsupportedEncodingException) {
                    VolleyLog.wtf(
                        "Unsupported Encoding while trying to get the bytes of %s using %s",
                        mXMLData,
                        "utf-8"
                    )
                    byteArrayOf()
                }
            }
        }

        mRequest.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        VolleySingleton.instance?.addToRequestQueue(mRequest)
    }

    private fun getPaymentAuthXML(mPaymentAuthModel: YoyoPaymentAuthModel, mYoYoList: ArrayList<YoyoModel>): String {

        var mTotalToPayAmount = BigDecimal(0)

        val mEnteredAmount: BigDecimal
        for (i in 0 until mPaymentAuthModel.mCheckoutTransaction.mPaymentTransaction.size) {
            if (mPaymentAuthModel.mCheckoutTransaction.mPaymentTransaction[i].mWalletName == "") {
                mEnteredAmount = mPaymentAuthModel.mCheckoutTransaction.mPaymentTransaction[i].mTransactionAmount
                e(javaClass.simpleName, "mEnteredAmount $mEnteredAmount")
                break
            }
        }

        for (i in 0 until mYoYoList.size) {
            val mUnitInstructionPrice = mYoYoList[i].mSpecialInstructionPrice.divide(mYoYoList[i].mProductQuantity, 2, RoundingMode.DOWN)
            val mProductAmount : BigDecimal = (mYoYoList[i].mProductPrice + mUnitInstructionPrice).divide(mYoYoList[i].mProductQuantity, 2, RoundingMode.DOWN)
            val mTotalAmount =
                (mProductAmount * mYoYoList[i].mProductQuantity) + mYoYoList[i].mSpecialInstructionPrice
            val mTaxAmount = (mProductAmount * mPaymentAuthModel.mTaxPercent).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)
            val mTotalToPay = (mTaxAmount * mYoYoList[i].mProductQuantity) + mTotalAmount
            mTotalToPayAmount += mTotalToPay
        }

        val mStringBuilder = StringBuilder()
        val mXMLString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<service>\n" +
                "<action>payment_authorisation</action>" +
                "<apiKey>3642949908613951</apiKey>\n" +
                //"<apiKey>15642659</apiKey>\n" +       // Invalid API Testing
                "<clientId>114</clientId>\n" +
                "<transactionReferences>\n" +
                "<tillId></tillId>\n" +
                "<operatorID></operatorID>\n" +
                "<tillTransactionId>1234567890</tillTransactionId>\n" +
                "<tillTimestamp>" + mPaymentAuthModel.mDateTime + "</tillTimestamp>\n" +
                "</transactionReferences>\n" + "<detail>\n" +
                "<barcode>" + mPaymentAuthModel.mQRCode + "</barcode>\n" +
//                "<basket_total_to_pay>" + mPaymentAuthModel.mCheckoutTransaction.mOrderTotal +
                "<basket_total_to_pay>" + mTotalToPayAmount +
                "</basket_total_to_pay>\n" +
                "<currency>GBP</currency>\n" +
                "<basket>\n"
        mStringBuilder.append(mXMLString)


        for (i in 0 until mYoYoList.size) {

            val mUnitInstructionPrice = mYoYoList[i].mSpecialInstructionPrice.divide(mYoYoList[i].mProductQuantity, 2, RoundingMode.DOWN)

            val mProductAmount : BigDecimal = (mYoYoList[i].mProductPrice + mUnitInstructionPrice).divide(mYoYoList[i].mProductQuantity, 2, RoundingMode.DOWN)


            val mTotalAmount =
                (mProductAmount * mYoYoList[i].mProductQuantity) + mYoYoList[i].mSpecialInstructionPrice

            val mTaxAmount = (mProductAmount * mPaymentAuthModel.mTaxPercent).divide(BigDecimal(100), 2, RoundingMode.HALF_UP)

            val mTotalToPay = (mTaxAmount * mYoYoList[i].mProductQuantity) + mTotalAmount

            val mItemGross = mProductAmount + mTaxAmount

            val mItemListString = "<item><description>" + mYoYoList[i].mProductName + "</description>\n" +
                    "<lineNo>1</lineNo>\n" +
                    "<productCode>PROD123</productCode>\n" +
                    "<quantity>" + mYoYoList[i].mProductQuantity + "</quantity>\n" +
                    "<categories>\n" +
                    "<category>" + mYoYoList[i].mProductCategory + "</category>\n" +
                    "</categories>\n" +
                    "<itemNet>" +mProductAmount + "</itemNet>\n" +
                    "<itemTax>" + mTaxAmount + "</itemTax>\n" +
                    "<itemGross>" + mItemGross + "</itemGross>\n" +
                    "<totalToPay>" + mTotalToPay + "</totalToPay>\n"

            mStringBuilder.append(mItemListString)

            //mTotalTax += mTaxAmount

            val mTotalTaxString = "<totalTax>" + (mTaxAmount * mYoYoList[i].mProductQuantity) + "</totalTax>" +
                    "</item>"

            mStringBuilder.append(mTotalTaxString)
        }

        e(javaClass.simpleName,"mTotalToPayAmount: $mTotalToPayAmount")

        mStringBuilder.append("</basket>\n")
        mStringBuilder.append("</detail>\n")
//        mStringBuilder.append("<customBundle></customBundle>\n")
        mStringBuilder.append("</service>")

        return mStringBuilder.toString()
    }


    fun yoyoBasketRegistration(
        context: Context, mPaymentAuthModel: YoyoPaymentAuthModel, mYoYoList: ArrayList<YoyoModel>,
        mBasketID: String, mInterface: CheckoutNetworkInterface
    ) {

        val mSharedPrefs = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        val mXMLData = getBasketRegistrationXML(context, mPaymentAuthModel, mYoYoList, mBasketID)
        e(javaClass.simpleName, "basket registration xml: $mXMLData")

        val mRequest = object : StringRequest(
            Method.POST, YOYO_WALLET_URL,
            Response.Listener {
                mInterface.onYoyoBasketRegistrationResponse(it.toString())
            }, Response.ErrorListener {
                mInterface.onYoyoBasketRegistrationResponse(it.toString())
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val credentials = mSharedPrefs.getString(RESTAURANT_USER_NAME, "")!! + ":" +
                        mSharedPrefs.getString(RESTAURANT_PASSWORD, "")
                val auth = AUTH_BASIC + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                headers[AUTH_CONTENT_TYPE] = AUTH_CONTENT_TYPE_VALUE
                headers["Authorization"] = auth
                return headers
            }

            override fun getBodyContentType(): String {
                return "application/xml"
            }

            override fun getBody(): ByteArray {
                return try {
                    mXMLData.toByteArray()
                } catch (uee: UnsupportedEncodingException) {
                    VolleyLog.wtf(
                        "Unsupported Encoding while trying to get the bytes of %s using %s",
                        mXMLData,
                        "utf-8"
                    )
                    byteArrayOf()
                }
            }
        }

        mRequest.retryPolicy = DefaultRetryPolicy(
            30000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        VolleySingleton.instance?.addToRequestQueue(mRequest)
    }

    private fun getBasketRegistrationXML(
        context: Context, mPaymentAuthModel: YoyoPaymentAuthModel, mYoYoList: ArrayList<YoyoModel>,
        mBasketID: String
    ): String {

        val mSharedPrefs = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)
        val mCollectionType: String

        var mNonYoyoAmount = BigDecimal(0)

        for (i in 0 until mPaymentAuthModel.mCheckoutTransaction.mPaymentTransaction.size) {
            if (mPaymentAuthModel.mCheckoutTransaction.mPaymentTransaction[i].mPaymentMethodType != TYPE_WALLET) {
                mNonYoyoAmount += mPaymentAuthModel.mCheckoutTransaction.mPaymentTransaction[i].mTransactionAmount
            }
        }

        var mNonYoyoAmountLeft = mNonYoyoAmount

        e(javaClass.simpleName, "mNonYoyoAmount: $mNonYoyoAmount")
        e(javaClass.simpleName, "mCheckoutTransactionAmount: ${mPaymentAuthModel.mCheckoutTransaction.mSeatCartTotal}")

        mCollectionType = if (mSharedPrefs.getInt(LOCATION_SERVICE_TYPE, 0) == SERVICE_DINE_IN) "eat_in"
        else "take_away"

        val mStringBuilder = StringBuilder()
        val mXMLString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "<service>\n" +
                "<action>basket_registration</action>" +
                "<apiKey>3642949908613951</apiKey>\n" +
                //"<apiKey>15642659</apiKey>\n" +       // Invalid API Testing
                "<clientId>114</clientId>\n" +
                "<transactionReferences>\n" +
                "<tillId></tillId>\n" +
                "<operatorID></operatorID>\n" +
                "<tillTransactionId>1234567890</tillTransactionId>\n" +
                "<tillTimestamp>" + mPaymentAuthModel.mDateTime + "</tillTimestamp>\n" +
                "<collectionType>" + mCollectionType + "</collectionType>\n" +
                "</transactionReferences>\n" + "<detail>\n" +
                "<basketAmountPaidNonYoyo>${String.format(
                    Locale.ENGLISH,
                    "%.1f",
                    mNonYoyoAmount
                )}</basketAmountPaidNonYoyo>\n" +
                "<currency>GBP</currency>\n" +
                "<basket>\n"
        mStringBuilder.append(mXMLString)

        var mTotalTax = BigDecimal(0)

        for (i in 0 until mYoYoList.size) {

            val mTotalAmount: BigDecimal =
                (mYoYoList[i].mProductPrice * mYoYoList[i].mProductQuantity) + mYoYoList[i].mSpecialInstructionPrice

            val mTaxAmount : BigDecimal = (mTotalAmount * mPaymentAuthModel.mTaxPercent) / BigDecimal(100)

            val mItemListString = "<item><description>" + mYoYoList[i].mProductName + "</description>\n" +
                    "<lineNo>1</lineNo>\n" +
                    "<productCode>PROD123</productCode>\n" +
                    "<quantity>" + mYoYoList[i].mProductQuantity + "</quantity>\n" +
                    "<categories>\n" +
                    "<category>" + mYoYoList[i].mProductCategory + "</category>\n" +
                    "</categories>\n" +
                    "<itemNet>" + String.format(Locale.ENGLISH, "%.1f", mTaxAmount + mTotalAmount) + "</itemNet>\n" +
                    "<itemTax>" + String.format(Locale.ENGLISH, "%.1f", mTaxAmount) + "</itemTax>\n" +
                    "<itemGross>" + String.format(Locale.ENGLISH, "%.1f", mTotalAmount) + "</itemGross>\n" +
                    "<totalToPay>" + String.format(
                Locale.ENGLISH,
                "%.1f",
                mTaxAmount + mTotalAmount
            ) + "</totalToPay>\n"

            mStringBuilder.append(mItemListString)

            var mProductNonYoyo = BigDecimal(0)

            if (mNonYoyoAmountLeft > BigDecimal(0)) {
                when {
                    (mTaxAmount + mTotalAmount) > BigDecimal(0) && (mTaxAmount + mTotalAmount) >= mNonYoyoAmountLeft -> {
                        mProductNonYoyo = mNonYoyoAmountLeft
                        mNonYoyoAmountLeft -= mProductNonYoyo
                    }
                    else -> {
                        mProductNonYoyo = mTaxAmount + mTotalAmount
                        mNonYoyoAmountLeft -= mProductNonYoyo
                    }
                }
            }

            mStringBuilder.append("<amountPaidNonYoyo>$mProductNonYoyo</amountPaidNonYoyo>\n")

            mTotalTax += mTaxAmount

            val mTotalTaxString = "<totalTax>" + String.format(Locale.ENGLISH, "%.1f", mTaxAmount) + "</totalTax>" +
                    "</item>"

            mStringBuilder.append(mTotalTaxString)
        }

        mStringBuilder.append("</basket>\n")

        val mPaymentString = "<paymentMethods>\n" +
                "<paymentMethod>\n"

        mStringBuilder.append(mPaymentString)

        var mPaymentType = ""

        if (mNonYoyoAmount > BigDecimal(0)) {
            mPaymentType = "Cash"
        }

        val mPaymentString2 =
            "<paymentType>$mPaymentType</paymentType>\n" +
                    "<detail></detail>\n" +
                    "<reference></reference>\n" +
                    "<expiryDate></expiryDate>\n" +
                    "</paymentMethod>\n" +
                    "</paymentMethods>\n" +
                    "<taxes>\n" +
                    "<tax>\n" +
                    "<taxPercentage>" + mPaymentAuthModel.mTaxPercent + "</taxPercentage>\n" +
                    "<totalTaxAmount>" + mTotalTax + "</totalTaxAmount>\n" +
                    "</tax>\n" +
                    "</taxes>\n"

        mStringBuilder.append(mPaymentString2)
        mStringBuilder.append("</detail>\n")

        val persistenceParam = "<persistentParam>\n" +
                "<basketId>" + mBasketID + "</basketId>\n" + "</persistentParam>"

        mStringBuilder.append(persistenceParam)
        mStringBuilder.append("</service>")

        return mStringBuilder.toString()

    }

    fun paymentSync(mContext: Context, mTableID: String, mCartID: String, mInterface: CheckoutNetworkInterface) {

        val mSharedPrefs = mContext.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE)

        val mURL = getAllTableUrl(mSharedPrefs.getString(RESTAURANT_ID, "")!!, mTableID, mCartID).toString()

        e(javaClass.simpleName, "paymentSync: $mURL")

        val mRequest = object : StringRequest(Method.GET, mURL,
            Response.Listener { response -> mInterface.onSyncPaymentResponse(response.toString()) },
            Response.ErrorListener { error -> mInterface.onSyncPaymentResponse(error.toString()) }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                val credentials = mSharedPrefs.getString(RESTAURANT_USER_NAME, "")!! + ":" +
                        mSharedPrefs.getString(RESTAURANT_PASSWORD, "")
                val auth = "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
                headers["Content-Type"] = "application/json"
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

    private fun getAllTableUrl(mRestaurantID: String, mTableID: String, mCartID: String): URL {
        return buildGetAllTableUrlWithCredentialsQuery(mRestaurantID, mTableID, mCartID)
    }

    private fun buildGetAllTableUrlWithCredentialsQuery(
        mRestaurantID: String,
        mTableID: String,
        mCartID: String
    ): URL {
        val loginQueryUri = Uri.parse(CART_PAYMENT).buildUpon()
            .appendQueryParameter(API_RESTAURANT_ID, mRestaurantID)
            .appendQueryParameter(API_TABLE_ID, mTableID)
            .appendQueryParameter(API_CART_ID, mCartID)
            .build()

        return URL(loginQueryUri.toString())
    }

    fun parsePayment(
        mJSONObject: JSONObject,
        mTableNO: Int,
        mCartNO: String,
        mTaxList: ArrayList<TaxModel>
    ): CheckoutModel {


        val mCheckoutTransactionList = ArrayList<CheckoutTransaction>()

        val mPaymentArray = mJSONObject.getJSONArray("payments")

        val mSeatList = ArrayList<String>()
        for (i in 0 until mPaymentArray.length()) {
            val mPaymentObject = mPaymentArray.getJSONObject(i)
            when {
                !mSeatList.contains(mPaymentObject.getString("seat")) -> mSeatList.add(mPaymentObject.getString("seat"))
            }
        }

        var mTipAmount = BigDecimal(0.0)
        val mTipPercent = BigDecimal(0.0)
        for (i in 0 until mSeatList.size) {

            var mOrderTotal = BigDecimal(0.0)

            val mPaymentTransactionList = ArrayList<PaymentTransaction>()
            for (j in 0 until mPaymentArray.length()) {
                val mPaymentObject = mPaymentArray.getJSONObject(j)

                when {
                    mSeatList[i] == mPaymentObject.getString("seat") -> {
                        val mPaymentTransaction = PaymentTransaction(
                            mPaymentObject.getString("transaction_id"),
                            mPaymentObject.getString("reference_no"),
                            mPaymentObject.getString("payment_method_id"),
                            mPaymentObject.getString("payment_method_name"),
                            mPaymentObject.getInt("payment_method_type"),
                            BigDecimal(mPaymentObject.getDouble("amount")),
                            BigDecimal(mPaymentObject.getDouble("cash_given")),
                            BigDecimal(mPaymentObject.getDouble("change_given")),
                            mPaymentObject.getString("card_no"),
                            mPaymentObject.getString("card_type"),
                            mPaymentObject.getString("card_provider"),
                            mPaymentObject.getString("entry_mode"),
                            mPaymentObject.getString("wallet_name"),
                            mPaymentObject.getString("wallet_name") // For QR Code
                        )

                        mPaymentTransactionList.add(mPaymentTransaction)
                        mOrderTotal += BigDecimal(mPaymentObject.getDouble("amount"))

                        if (mPaymentObject.getInt("payment_method_type") == TYPE_TIP) {
                            mTipAmount += BigDecimal(mPaymentObject.getDouble("amount"))
                        }
                    }
                }
            }

            val mSeats = mSeatList[i].split(",")

            val mIntSeatList = ArrayList<Int>()
            for (mSeat in mSeats) {
                if (mSeat.isNotEmpty())
                    mIntSeatList.add(mSeat.toInt())
            }

            val mCheckoutTransaction = CheckoutTransaction(
                mIntSeatList,
                true, true,
                BigDecimal(0.0), BigDecimal(0.0), BigDecimal(0.0), BigDecimal(0.0), BigDecimal(0.0), BigDecimal(0.0), BigDecimal(0.0), BigDecimal(0.0),
                "", BigDecimal(0.0), BigDecimal(0.0), mOrderTotal, mOrderTotal, BigDecimal(0.0),
                Utils.getDateFromString("dd/MM/yyyy HH:mm:ss", mJSONObject.getString("time")),
                true, mPaymentTransactionList
            )

            mCheckoutTransactionList.add(mCheckoutTransaction)
        }



        return CheckoutModel(
            mTableNO,
            mJSONObject.getString("table_id"),
            mJSONObject.getString("group_name"),
            mJSONObject.getString("cart_id"),
            mCartNO,
            mJSONObject.getString("cart_payment_id"),
            BigDecimal(mJSONObject.getDouble("cart_total")),
            BigDecimal(mJSONObject.getDouble("tax_amount")),
            BigDecimal(mJSONObject.getDouble("tax_percent")),
            mTaxList,
            mTipAmount, mTipPercent,
            BigDecimal(mJSONObject.getDouble("service_charge_amount")),
            BigDecimal(mJSONObject.getDouble("service_charge_percentage")),
            BigDecimal(mJSONObject.getDouble("discount_amount")),
            BigDecimal(mJSONObject.getDouble("discount_percent")),
            "", BigDecimal(0.0), BigDecimal(0.0),
            BigDecimal(mJSONObject.getDouble("order_total")),
            mJSONObject.getInt("order_type"),
            1,
            1,
            BigDecimal(mJSONObject.getDouble("amount_paid")),
            BigDecimal(mJSONObject.getDouble("order_total") - mJSONObject.getDouble("amount_paid")),
            Utils.getDateFromString("dd/MM/yyyy HH:mm:ss", mJSONObject.getString("time")),
            mCheckoutTransactionList
        )
    }

    interface CheckoutNetworkInterface {
        fun onPaymentResponse(mResponse: String, result: Boolean)
        fun onSyncPaymentResponse(mResponse: String)
        fun onOrderSubmitResponse(mResponse: String)
        fun onYoyoPaymentAuthResponse(mResponse: String)
        fun onYoyoBasketRegistrationResponse(mResponse: String)
    }
}