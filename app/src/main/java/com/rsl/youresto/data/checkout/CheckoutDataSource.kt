package com.rsl.youresto.data.checkout

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log.e
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.rsl.youresto.data.checkout.model.CheckoutModel
import com.rsl.youresto.data.database_download.models.PaymentMethodModel
import com.rsl.youresto.data.database_download.models.TaxModel
import com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.model.YoyoModel
import com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.model.YoyoPaymentAuthModel
import com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.model.YoyoResponseModel
import org.json.JSONObject
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import kotlin.collections.ArrayList

@SuppressLint("LogNotTimber")
class CheckoutDataSource(val context: Context) : CheckoutNetworkUtils.CheckoutNetworkInterface {

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var sInstance: CheckoutDataSource? = null

        fun getInstance(context: Context): CheckoutDataSource? {
            val tempInstance = sInstance
            if (tempInstance != null) return sInstance

            sInstance ?: synchronized(this) {
                sInstance = CheckoutDataSource(context).also { sInstance = it }
            }

            return sInstance
        }

        const val ERROR = "Error: "
    }

    private var mUpdatePaymentData: MutableLiveData<String>? = null
    fun updatePayment(mCheckoutModel: CheckoutModel, mTipPaymentMethod: PaymentMethodModel): LiveData<String> {
        mUpdatePaymentData = MutableLiveData()
        CheckoutNetworkUtils.updatePaymentInfo(context, mCheckoutModel, mTipPaymentMethod, this)
        return mUpdatePaymentData!!
    }

    override fun onPaymentResponse(mResponse: String, result: Boolean) {
        e(javaClass.simpleName, "onPaymentResponse: $mResponse")

        if(result) {
            val mJSONObject = JSONObject(mResponse)

            if (mJSONObject.getString("status").lowercase(Locale.getDefault()) == "ok") {
                val mDataArray = mJSONObject.getJSONArray("data")
                val mDataObject = mDataArray.getJSONObject(0)

                val mCartPaymentID = mDataObject.getString("cart_payment_id")

                mUpdatePaymentData!!.postValue(mCartPaymentID)
            } else
                mUpdatePaymentData!!.postValue("Error")
        } else
            mUpdatePaymentData!!.postValue("Error")
    }

    private var mSubmitOrderData: MutableLiveData<String>? = null
    fun submitOrder(mCheckoutModel: CheckoutModel): LiveData<String> {
        mSubmitOrderData = MutableLiveData()
        CheckoutNetworkUtils.submitOrder(context, mCheckoutModel, this)
        return mSubmitOrderData!!
    }

    override fun onOrderSubmitResponse(mResponse: String) {
        e(javaClass.simpleName, "onOrderSubmitResponse: $mResponse")

        val mJSONObject = JSONObject(mResponse)

        if (mJSONObject.getString("status").lowercase(Locale.getDefault()) == "ok") {
            val mDataArray = mJSONObject.getJSONArray("data")
            val mDataObject = mDataArray.getJSONObject(0)

            val mOrderID = mDataObject.getString("order_no")

            mSubmitOrderData!!.postValue(mOrderID)
        } else
            mSubmitOrderData!!.postValue("Error")
    }

    private var mYoyoPaymentAuthData: MutableLiveData<YoyoResponseModel>? = null

    fun yoyoPaymentAuth(
        mPaymentAuthModel: YoyoPaymentAuthModel,
        mYoYoList: ArrayList<YoyoModel>
    ): LiveData<YoyoResponseModel> {
        mYoyoPaymentAuthData = MutableLiveData()
        CheckoutNetworkUtils.yoyoPaymentAuth(context, mPaymentAuthModel, mYoYoList, this)
        return mYoyoPaymentAuthData!!
    }

    @SuppressLint("LogNotTimber")
    override fun onYoyoPaymentAuthResponse(mResponse: String) {
        e(javaClass.simpleName,"mResponse: $mResponse")

        var mMessageID = ""
        var mStatus = ""
        var mStatusMessage = ""
        var mTransactionID = ""
        var mBasketID = ""

        val doc = getDomElement(mResponse)
        val nl = doc!!.getElementsByTagName("result")
        for (i in 0 until nl.length) {
            val e = nl.item(i) as Element
            e(javaClass.simpleName, "sendData: onResponse MessageID: " + getValue(e, "messageId"))
            e(javaClass.simpleName, "sendData: onResponse Status: " + getValue(e, "status"))
            e(javaClass.simpleName, "sendData: onResponse StatusMessage: " + getValue(e, "statusMessage"))

            mMessageID = getValue(e, "messageId")
            mStatus = getValue(e, "status")
            mStatusMessage = getValue(e, "statusMessage")
        }

        val n5 = doc.getElementsByTagName("persistentParam")
        for (i in 0 until n5.length) {
            val e = n5.item(i) as Element
            e(javaClass.simpleName, "sendData: onResponse: basketId " + getValue(e, "basketId"))
            e(javaClass.simpleName, "sendData: onResponse: transactionId " + getValue(e, "transactionId"))
            mTransactionID = getValue(e, "transactionId")
            mBasketID = getValue(e, "basketId")
        }

        val mYoyoResponse = YoyoResponseModel(mMessageID,mStatus,mStatusMessage,mBasketID,mTransactionID)
        mYoyoPaymentAuthData!!.postValue(mYoyoResponse)

    }

    private var mYoyoBasketRegistrationData: MutableLiveData<YoyoResponseModel>? = null

    fun yoyoBasketRegistration(mPaymentAuthModel: YoyoPaymentAuthModel, mYoYoList: ArrayList<YoyoModel>
                               , mBasketID: String): LiveData<YoyoResponseModel> {
        mYoyoBasketRegistrationData = MutableLiveData()
        CheckoutNetworkUtils.yoyoBasketRegistration(context, mPaymentAuthModel, mYoYoList, mBasketID, this)
        return mYoyoBasketRegistrationData!!
    }

    @SuppressLint("LogNotTimber")
    override fun onYoyoBasketRegistrationResponse(mResponse: String) {
        var mMessageID = ""
        var mStatus = ""
        var mStatusMessage = ""

        val doc = getDomElement(mResponse)
        val nl = doc!!.getElementsByTagName("result")
        for (i in 0 until nl.length) {
            val e = nl.item(i) as Element
            e(javaClass.simpleName, "sendData: onResponse MessageID: " + getValue(e, "messageId"))
            e(javaClass.simpleName, "sendData: onResponse Status: " + getValue(e, "status"))
            e(javaClass.simpleName, "sendData: onResponse StatusMessage: " + getValue(e, "statusMessage"))

            mMessageID = getValue(e, "messageId")
            mStatus = getValue(e, "status")
            mStatusMessage = getValue(e, "statusMessage")
        }

        val mYoyoResponse = YoyoResponseModel(mMessageID,mStatus,mStatusMessage,"NULL","NULL")
        mYoyoBasketRegistrationData!!.postValue(mYoyoResponse)
    }


    private fun getDomElement(xml: String): Document? {
        val doc: Document?
        val dbf = DocumentBuilderFactory.newInstance()
        try {

            val db = dbf.newDocumentBuilder()

            val inputSource = InputSource()
            inputSource.characterStream = StringReader(xml)
            doc = db.parse(inputSource)

        } catch (e: ParserConfigurationException) {
            e.message?.let { e(ERROR, it) }
            return null
        } catch (e: SAXException) {
            e.message?.let { e(ERROR, it) }
            return null
        } catch (e: IOException) {
            e.message?.let { e(ERROR, it) }
            return null
        }

        return doc
    }

    fun getValue(item: Element, str: String): String {
        val n = item.getElementsByTagName(str)
        return this.getElementValue(n.item(0))
    }

    private fun getElementValue(elem: Node?): String {
        var child: Node?
        if (elem != null && elem.hasChildNodes()) {
            child = elem.firstChild
            while (child != null) {
                if (child.nodeType == Node.TEXT_NODE) {
                    return child.nodeValue
                }
                child = child.nextSibling
            }
        }
        return ""
    }

    private var mPaymentSyncData : MutableLiveData<CheckoutModel>? = null
    private var mTableNO : Int? = null
    private var mCartNO : String? = null
    private var mTaxList : ArrayList<TaxModel>? = null
    fun paymentSync(mTableID: String, mCartID: String, mTableNO: Int, mCartNO: String, mTaxList: ArrayList<TaxModel>): LiveData<CheckoutModel>{
        mPaymentSyncData = MutableLiveData()
        this.mTableNO = mTableNO
        this.mCartNO = mCartNO
        this.mTaxList = mTaxList
        CheckoutNetworkUtils.paymentSync(context, mTableID, mCartID, this)
        return mPaymentSyncData!!
    }

    override fun onSyncPaymentResponse(mResponse: String) {
        e(javaClass.simpleName, "Response: $mResponse")

        try{
            val mJSONObject = JSONObject(mResponse)

            val mJSONArray = mJSONObject.getJSONArray("data")

            if(mJSONArray.length() > 0){
                val mDataJSONObject = mJSONArray.getJSONObject(0)
                mPaymentSyncData!!.postValue(
                    CheckoutNetworkUtils.parsePayment(mDataJSONObject, mTableNO!!, mCartNO!!, mTaxList!!)
                )
            }else{
                mPaymentSyncData!!.postValue(null)
            }
        }catch (e: Exception){
            e.printStackTrace()
            mPaymentSyncData!!.postValue(null)
        }





    }
}