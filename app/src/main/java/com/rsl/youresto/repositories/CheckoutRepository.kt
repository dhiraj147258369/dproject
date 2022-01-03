package com.rsl.youresto.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log.e
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.rsl.youresto.data.checkout.CheckoutDao
import com.rsl.youresto.data.checkout.CheckoutDataSource
import com.rsl.youresto.data.checkout.model.CheckoutModel
import com.rsl.youresto.data.checkout.model.CheckoutTransaction
import com.rsl.youresto.data.database_download.models.PaymentMethodModel
import com.rsl.youresto.data.database_download.models.TablesModel
import com.rsl.youresto.data.database_download.models.TaxModel
import com.rsl.youresto.data.tables.models.LocalTableGroupModel
import com.rsl.youresto.data.tables.models.LocalTableSeatModel
import com.rsl.youresto.data.tables.models.ServerTableSeatModel
import com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.model.YoyoModel
import com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.model.YoyoPaymentAuthModel
import com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.model.YoyoResponseModel
import com.rsl.youresto.utils.AppConstants.SERVICE_DINE_IN
import com.rsl.youresto.utils.AppConstants.TYPE_TIP
import com.rsl.youresto.utils.Network
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.coroutines.CoroutineContext

@SuppressLint("LogNotTimber")
class CheckoutRepository constructor(
    private val checkoutDao: CheckoutDao,
    private val checkoutDataSource: CheckoutDataSource
) {

    companion object {
        @Volatile
        private var sInstance: CheckoutRepository? = null

        fun getInstance(checkoutDao: CheckoutDao, checkoutDataSource: CheckoutDataSource) =
            sInstance ?: synchronized(this) {
                sInstance ?: CheckoutRepository(checkoutDao, checkoutDataSource)
            }
    }

    private var parentJob = Job()
    private val coRoutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main

    fun insertCheckoutRow(mCheckoutModel: CheckoutModel): LiveData<Int> {
        val mCheckoutData = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            val mInsert = checkoutDao.insertCheckoutRow(mCheckoutModel)
            if (mInsert > -1)
                mCheckoutData.postValue(checkoutDao.getLatestCheckoutRowID())
        }
        return mCheckoutData
    }

    private var mCheckoutObserver: Observer<CheckoutModel>? = null

    fun updateCheckout(mCheckoutModel: CheckoutModel): LiveData<Int> {
        val mUpdateCheckoutData = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            checkoutDao.updateCheckout(mCheckoutModel)
            mUpdateCheckoutData.postValue(mCheckoutModel.mID)
        }
        return mUpdateCheckoutData
    }

    fun getTableGroupsAndSeats(mTableNO: Int, mLocationID: String): LiveData<List<LocalTableGroupModel>> {
        val mMutableGroupData = MutableLiveData<List<LocalTableGroupModel>>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mMutableGroupData.postValue(checkoutDao.getTableGroupsAndSeats(mTableNO, mLocationID))
        }
        return mMutableGroupData
    }

    fun getTableGroupAndSeats(mTableNO: Int, mGroupName: String): LiveData<LocalTableGroupModel> {
        val mMutableGroupData = MutableLiveData<LocalTableGroupModel>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mMutableGroupData.postValue(checkoutDao.getTableGroupAndSeats(mTableNO, mGroupName))
        }
        return mMutableGroupData
    }

    fun updateTableGroupAndSeats(mGroupTableGroupModel: LocalTableGroupModel) {
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            checkoutDao.updateTableGroupAndSeats(mGroupTableGroupModel)
        }
    }

    fun getCheckoutDataByTableAndGroup(mTableID: String, mGroupName: String): LiveData<CheckoutModel> {
        return checkoutDao.getCheckoutDataByTableAndGroup(mTableID, mGroupName)
    }

    fun getCheckoutDataByTableAndGroupWithoutObserving(mTableID: String, mGroupName: String): LiveData<CheckoutModel> {
        val mTableGroupData = MutableLiveData<CheckoutModel>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) { mTableGroupData.postValue(checkoutDao.getCheckoutDataByTableAndGroupWithoutObserving(mTableID, mGroupName)) }
        return mTableGroupData
    }

    fun getCheckoutDataByCartID(mCartID: String): LiveData<CheckoutModel> {
        return checkoutDao.getCheckoutDataByCartID(mCartID)
    }

    fun getCheckoutDataByTableAndCartID(mTableID: String, mCartID: String): LiveData<CheckoutModel> {
        return checkoutDao.getCheckoutDataByTableAndCartID(mTableID, mCartID)
    }

    fun getCheckoutDataByRowID(mCheckoutRowID: Int): LiveData<CheckoutModel> {
        return checkoutDao.getCheckoutDataByRowID(mCheckoutRowID)
    }

    fun getCheckoutDataByRowIDWithoutObserving(mCheckoutRowID: Int): LiveData<CheckoutModel> {
        val mCartData = MutableLiveData<CheckoutModel>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mCartData.postValue(checkoutDao.getCheckoutDataByRowIDWithoutObserving(mCheckoutRowID))
        }
        return mCartData
    }

    fun getPaymentMethods(): LiveData<List<PaymentMethodModel>> {
        val mMutablePaymentMethodData = MutableLiveData<List<PaymentMethodModel>>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mMutablePaymentMethodData.postValue(checkoutDao.getPaymentMethods())
        }
        return mMutablePaymentMethodData
    }

    fun deleteQuickServiceCheckoutRow(mCheckoutRowID: Int): LiveData<Int> {
        val mDeleteData = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mDeleteData.postValue(checkoutDao.deleteCheckoutByRowID(mCheckoutRowID))
        }
        return mDeleteData
    }

    fun deleteWithoutSeatSelectionCheckoutRow(mCheckoutRowID: Int): LiveData<Int> {
        val mDeleteData = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mDeleteData.postValue(checkoutDao.deleteCheckoutByRowID(mCheckoutRowID))
        }
        return mDeleteData
    }

    fun deleteCheckoutRow(mCheckoutRowID: Int): LiveData<Int> {
        val mDeleteCheckoutData = MutableLiveData<Int>()


        val mCheckoutData = checkoutDao.getCheckoutDataByRowID(mCheckoutRowID)

        mCheckoutObserver = Observer {

            when {
                it != null -> {
                    val mTransactionList = it.mCheckoutTransaction

                    var mUnpaidTransaction: CheckoutTransaction? = null

                    val mFinalTransactionList = ArrayList<CheckoutTransaction>()
                    for (i in 0 until mTransactionList.size)
                        when {
                            mTransactionList[i].isFullPaid -> mFinalTransactionList.add(mTransactionList[i])
                            else -> mUnpaidTransaction = mTransactionList[i]
                        }

                    when {
                        mUnpaidTransaction != null -> {
                            it.mDiscountAmount = it.mDiscountAmount - mUnpaidTransaction.mDiscountAmount
                            it.mDiscountPercent = it.mDiscountPercent - mUnpaidTransaction.mDiscountPercent
                            it.mTipAmount = it.mTipAmount - mUnpaidTransaction.mTipAmount
                            it.mTipPercent = it.mTipPercent - mUnpaidTransaction.mTipPercent


                            when {
                                mUnpaidTransaction.mDiscountAmount > BigDecimal(0) -> {
                                    val subTotal = it.mCartTotal - it.mDiscountAmount

                                    e(javaClass.simpleName, "subTotal: $subTotal")
                                    e(javaClass.simpleName, "tax: ${it.mTaxPercent}")

                                    val mTax = subTotal.multiply(it.mTaxPercent.divide(BigDecimal(100)))

                                    e(javaClass.simpleName, "tax1: $mTax")

                                    it.mTaxAmount = mTax.setScale(2, RoundingMode.HALF_UP)

                                    e(javaClass.simpleName, "tax2: ${it.mTaxAmount}")

                                    val orderTotal = subTotal + mTax

                                    it.mOrderTotal = orderTotal.setScale(2, RoundingMode.HALF_UP)

                                    it.mAmountRemaining = it.mOrderTotal - it.mAmountPaid

                                }
                            }


                            if (mUnpaidTransaction.mTipAmount > BigDecimal(0)) {
                                it.mOrderTotal = it.mOrderTotal - mUnpaidTransaction.mTipAmount
                                it.mAmountRemaining = it.mOrderTotal - it.mAmountPaid
                            }
                        }
                    }

                    it.mCheckoutTransaction = mFinalTransactionList

                    val scope = CoroutineScope(coRoutineContext)
                    scope.launch(Dispatchers.IO) {
                        mDeleteCheckoutData.postValue(checkoutDao.updateCheckout(it))
                    }
                    mCheckoutData.removeObserver(mCheckoutObserver!!)
                }
                else -> mDeleteCheckoutData.postValue(1)
            }
        }
        mCheckoutData.observeForever(mCheckoutObserver!!)
        return mDeleteCheckoutData
    }

    fun getTaxData(): LiveData<List<TaxModel>> {
        val mTaxData = MutableLiveData<List<TaxModel>>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mTaxData.postValue(checkoutDao.getTaxData())
        }
        return mTaxData
    }

    fun updateDiscount(mCheckoutModel: CheckoutModel): LiveData<Int> {
        val mUpdateDiscountData = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mUpdateDiscountData.postValue(checkoutDao.updateCheckout(mCheckoutModel))
        }
        return mUpdateDiscountData
    }

    fun updateServiceCharge(mCheckoutModel: CheckoutModel): LiveData<Int> {
        val mUpdateServiceChargeData = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mUpdateServiceChargeData.postValue(checkoutDao.updateCheckout(mCheckoutModel))
        }
        return mUpdateServiceChargeData
    }

    fun updateTip(mCheckoutModel: CheckoutModel): LiveData<Int> {
        val mUpdateTipData = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            mUpdateTipData.postValue(checkoutDao.updateCheckout(mCheckoutModel))
        }
        return mUpdateTipData
    }

    fun updatePayment(
        mCheckout: CheckoutModel,
        context: Context
    ): LiveData<Int> {
        val mUpdatePaymentData = MutableLiveData<Int>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            val mUpdate = checkoutDao.updateCheckout(mCheckout)

            e(javaClass.simpleName, "mUpdate :$mUpdate")

            when {
                mUpdate > -1 -> {

                    val mCheckoutTransaction = mCheckout.mCheckoutTransaction

                    var mNeedToUpdateToServer = false
                    for (i in 0 until mCheckoutTransaction.size) {
                        when {
                            mCheckoutTransaction[i].isFullPaid && !mCheckoutTransaction[i].isSentToServer ->
                                mNeedToUpdateToServer = true
                        }
                    }

                    when {
                        !mNeedToUpdateToServer -> mUpdatePaymentData.postValue(mUpdate)
                        else -> {

                            //getting tip method info
                            val mPaymentMethodList = checkoutDao.getPaymentMethods()
                            var mTipPaymentMethod: PaymentMethodModel? = null
                            for (i in 0 until mPaymentMethodList.size) {
                                when (TYPE_TIP) {
                                    mPaymentMethodList[i].mPaymentMethodType -> mTipPaymentMethod =
                                        mPaymentMethodList[i]
                                }
                            }

                            Handler(Looper.getMainLooper()).post {
                                Network.isNetworkAvailableWithInternetAccess(context).observeForever {
                                    when {
                                        it != null ->
                                            when {
                                                it -> checkoutDataSource.updatePayment(mCheckout, mTipPaymentMethod!!)
                                                    .observeForever { paymentSuccess ->
                                                        when {
                                                            paymentSuccess != null ->
                                                                when {
                                                                    paymentSuccess != "Error" -> {
                                                                        for (i in 0 until mCheckoutTransaction.size)
                                                                            mCheckoutTransaction[i].isSentToServer = true
                                                                        mCheckout.mCartPaymentID = paymentSuccess
                                                                        scope.launch(Dispatchers.IO) {
                                                                            val mUpdate2 = checkoutDao.updateCheckout(mCheckout)
                                                                            e(javaClass.simpleName, "updatePayment: $mUpdate2")
                                                                            mUpdatePaymentData.postValue(mUpdate2)
                                                                        }
                                                                    }
                                                                    else -> mUpdatePaymentData.postValue(-1)
                                                                }
                                                        }
                                                    }
                                                else -> {
                                                    e(javaClass.simpleName, "network error: $it")
                                                    mUpdatePaymentData.postValue(-1)
                                                }
                                            }
                                    }

                                }
                            }
                        }
                    }

                }
            }
        }
        return mUpdatePaymentData
    }

    fun submitOrder(mCheckout: CheckoutModel): LiveData<Int> {
        val mSubmitOrderData = MutableLiveData<Int>()
        checkoutDataSource.submitOrder(mCheckout).observeForever {
            when {
                it != null ->
                    if (it != "Error") {
                        val scope = CoroutineScope(coRoutineContext)
                        scope.launch(Dispatchers.IO) {

                            val mCartDelete =
                                checkoutDao.deleteCartAfterSubmitOrder(mCheckout.mCartID, mCheckout.mTableID)
                            when {
                                mCartDelete > -1 -> {
                                    when (SERVICE_DINE_IN) {
                                        mCheckout.mOrderType -> checkoutDao.deleteCartTableGroup(
                                            mCheckout.mGroupName,
                                            mCheckout.mTableID
                                        )
                                    }
                                    mSubmitOrderData.postValue(checkoutDao.deleteCheckout(mCheckout))
                                }
                            }
                        }
                    }
                    else mSubmitOrderData.postValue(-1)
            }
        }
        return mSubmitOrderData
    }

    fun callYoyoPaymentAuthAPI(mPaymentAuthModel: YoyoPaymentAuthModel): LiveData<YoyoResponseModel> {
        val mPaymentAuthData = MutableLiveData<YoyoResponseModel>()
        var mSeatTotal = BigDecimal(0.0)

        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            val mCartList = checkoutDao.getCartData(mPaymentAuthModel.mTableNO, mPaymentAuthModel.mGroupName)

            val mYoYoList = ArrayList<YoyoModel>()

            for (j in mCartList.indices) {
                val mYoYoModel = YoyoModel(
                    mCartList[j].mProductName,
                    mCartList[j].mProductQuantity,
                    mCartList[j].mCourseType,
                    mCartList[j].mSpecialInstructionPrice,
                    mCartList[j].mProductTotalPrice,
                    0
                )
                mYoYoList.add(mYoYoModel)
            }

            val mYoYoFinalList = ArrayList<YoyoModel>()
            var mHasProduct = false
            for (l in mYoYoList.indices) {
                mSeatTotal += mYoYoList[l].mProductPrice
                when (mYoYoFinalList.size) {
                    0 -> mYoYoFinalList.add(mYoYoList[l])
                    else -> {
                        var mOldPrice: BigDecimal
                        loop@ for (m in mYoYoFinalList.indices) {
                            when (mYoYoList[l].mProductName) {
                                mYoYoFinalList[m].mProductName -> {
                                    mHasProduct = false
                                    mOldPrice = mYoYoList[l].mProductPrice
                                    mYoYoFinalList[m].mProductPrice = mOldPrice + mYoYoFinalList[m].mProductPrice
                                    break@loop
                                }
                                else -> mHasProduct = true
                            }
                        }
                        when {
                            mHasProduct -> mYoYoFinalList.add(mYoYoList[l])
                        }
                    }
                }
            }

            Handler(Looper.getMainLooper()).post {
                checkoutDataSource.yoyoPaymentAuth(mPaymentAuthModel, mYoYoFinalList).observeForever {
                    if (it != null) {
                        mPaymentAuthData.postValue(it)
                    }
                }
            }
        }

        return mPaymentAuthData
    }

    fun callYoyoBasketRegistrationAPI(
        mPaymentAuthModel: YoyoPaymentAuthModel,
        mBasketID: String
    ): LiveData<YoyoResponseModel> {
        val mBasketRegistrationData = MutableLiveData<YoyoResponseModel>()

        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            val mCartList = checkoutDao.getCartData(mPaymentAuthModel.mTableNO, mPaymentAuthModel.mGroupName)

            val mYoYoList = ArrayList<YoyoModel>()
            var mSeatTotal = BigDecimal(0.0)

            for (j in 0 until mCartList.size) {
                val mYoYoModel = YoyoModel(
                    mCartList[j].mProductName,
                    mCartList[j].mProductQuantity,
                    mCartList[j].mCourseType,
                    mCartList[j].mSpecialInstructionPrice,
                    mCartList[j].mProductTotalPrice,
                    0
                )
                mYoYoList.add(mYoYoModel)
            }

            val mYoYoFinalList = ArrayList<YoyoModel>()
            var mHasProduct = false
            for (l in mYoYoList.indices) {
                mSeatTotal += mYoYoList[l].mProductPrice
                when (mYoYoFinalList.size) {
                    0 -> mYoYoFinalList.add(mYoYoList[l])
                    else -> {
                        var mOldPrice: BigDecimal
                        loop@ for (m in mYoYoFinalList.indices) {
                            when (mYoYoList[l].mProductName) {
                                mYoYoFinalList[m].mProductName -> {
                                    mHasProduct = false
                                    mOldPrice = mYoYoList[l].mProductPrice
                                    mYoYoFinalList[m].mProductPrice = mOldPrice + mYoYoFinalList[m].mProductPrice
                                    break@loop
                                }
                                else -> mHasProduct = true
                            }
                        }
                        when {
                            mHasProduct -> mYoYoFinalList.add(mYoYoList[l])
                        }
                    }
                }
            }

            Handler(Looper.getMainLooper()).post {
                checkoutDataSource.yoyoBasketRegistration(mPaymentAuthModel, mYoYoFinalList, mBasketID)
                    .observeForever {
                        if (it != null) {
                            mBasketRegistrationData.postValue(it)
                        }
                    }
            }
        }

        return mBasketRegistrationData
    }

    private var mTableList: ArrayList<TablesModel>? = null
    private var mTaxList: ArrayList<TaxModel>? = null
    val scope = CoroutineScope(coRoutineContext)

    private var mPaymentSyncData = MutableLiveData<Int>()
    fun paymentSync(mLocationID: String): LiveData<Int> {
        mPaymentSyncData = MutableLiveData()

        scope.launch(Dispatchers.IO) {
            mTableList = ArrayList(checkoutDao.getOccupiedTable(mLocationID))
            mTaxList = ArrayList(checkoutDao.getTaxData())

            when {
                mTableList!!.size > 0 && mTableList!![0].mGroupList!!.size > 0 -> Handler(Looper.getMainLooper()).post {

                    checkoutDataSource.paymentSync(
                        mTableList!![0].mTableID,
                        mTableList!![0].mGroupList!![0].mCartID!!,
                        mTableList!![0].mTableNo,
                        mTableList!![0].mGroupList!![0].mCartNO!!,
                        mTaxList!!
                    ).observeForever {
                        if (it != null) {
                            scope.launch(Dispatchers.IO) {
                                val mDelete = checkoutDao.deleteCheckout(
                                    mTableList!![0].mTableID,
                                    mTableList!![0].mGroupList!![0].mGroupName
                                )

                                e(javaClass.simpleName, "DeleteCheckout ${mTableList!![0].mTableNo} delete: $mDelete")

                                when {
                                    mDelete > -1 -> {
                                        val mInsert = checkoutDao.insertCheckoutRow(it)

                                        when {
                                            mInsert > -1 -> {

                                                val mGroup = storeGroupsAndSeats(
                                                    it,
                                                    mTableList!![0].mGroupList!![0].mSeatList!!,
                                                    mLocationID
                                                )

                                                val mGroupDelete = checkoutDao.deleteTableGroupsByGroupName(
                                                    mTableList!![0].mTableID,
                                                    mTableList!![0].mGroupList!![0].mGroupName
                                                )

                                                when {
                                                    mGroupDelete > -1 -> {
                                                        val mGroupInsert =
                                                            checkoutDao.storeTableGroups(mGroup)

                                                        when {
                                                            mGroupInsert > -1 -> insertCheckouts(0, 0, mLocationID)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            e(javaClass.simpleName, "else ${mTableList!![0].mTableNo}")

                            if (mTableList!!.size == 1) {
                                e(javaClass.simpleName, "mPaymentSyncData 1 ")
                                mPaymentSyncData.postValue(1)
                            }
                            insertCheckouts(0, 0, mLocationID)
                        }

                    }
                }
                else -> checkoutDao.deleteAllCheckout()
            }
        }

        return mPaymentSyncData
    }

    private fun insertCheckouts(mTableCount: Int, mGroupCount: Int, mLocationID: String) {


        when {
            mTableList!![mTableCount].mGroupList!!.size > mGroupCount + 1 -> Handler(Looper.getMainLooper()).post {
                checkoutDataSource.paymentSync(
                    mTableList!![mTableCount].mTableID,
                    mTableList!![mTableCount].mGroupList!![mGroupCount + 1].mCartID!!,
                    mTableList!![mTableCount].mTableNo,
                    mTableList!![mTableCount].mGroupList!![mGroupCount + 1].mCartNO!!,
                    mTaxList!!
                ).observeForever {
                    if (it != null) {
                        scope.launch(Dispatchers.IO) {

                            val mDelete = checkoutDao.deleteCheckout(
                                mTableList!![mTableCount].mTableID,
                                mTableList!![mTableCount].mGroupList!![mGroupCount + 1].mGroupName
                            )

                            e(
                                javaClass.simpleName,
                                "DeleteCheckout ${mTableList!![mTableCount].mTableNo} delete: $mDelete"
                            )

                            when {
                                mDelete > -1 -> {
                                    val mInsert = checkoutDao.insertCheckoutRow(it)

                                    when {
                                        mInsert > -1 -> {

                                            val mGroup = storeGroupsAndSeats(
                                                it,
                                                mTableList!![mTableCount].mGroupList!![mGroupCount + 1].mSeatList!!,
                                                mLocationID
                                            )

                                            val mGroupDelete = checkoutDao.deleteTableGroupsByGroupName(
                                                mTableList!![mTableCount].mTableID,
                                                mTableList!![mTableCount].mGroupList!![mGroupCount + 1].mGroupName
                                            )

                                            when {
                                                mGroupDelete > -1 -> {
                                                    val mGroupInsert =
                                                        checkoutDao.storeTableGroups(mGroup)

                                                    when {
                                                        mGroupInsert > -1 -> insertCheckouts(
                                                            mTableCount,
                                                            mGroupCount + 1,
                                                            mLocationID
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        e(javaClass.simpleName, "else ${mTableList!![mTableCount].mTableNo}")
                        e(javaClass.simpleName, "else size: ${mTableList!!.size}")
                        when (mTableCount) {
                            mTableList!!.size -> {
                                e(javaClass.simpleName, "mPaymentSyncData 2 ")
                                mPaymentSyncData.postValue(1)
                            }
                        }
                        insertCheckouts(mTableCount, mGroupCount + 1, mLocationID)
                    }

                }
            }
            mTableList!!.size > mTableCount + 1 -> if (mTableList!![mTableCount + 1].mGroupList!!.size > 0)
                Handler(Looper.getMainLooper()).post {
                    checkoutDataSource.paymentSync(
                        mTableList!![mTableCount + 1].mTableID,
                        mTableList!![mTableCount + 1].mGroupList!![0].mCartID!!,
                        mTableList!![mTableCount + 1].mTableNo,
                        mTableList!![mTableCount + 1].mGroupList!![0].mCartNO!!,
                        mTaxList!!
                    ).observeForever {
                        if (it != null) {
                            scope.launch(Dispatchers.IO) {

                                val mDelete = checkoutDao.deleteCheckout(
                                    mTableList!![mTableCount + 1].mTableID,
                                    mTableList!![mTableCount].mGroupList!![0].mGroupName
                                )

                                e(
                                    javaClass.simpleName,
                                    "DeleteCheckout ${mTableList!![mTableCount + 1].mTableNo} delete: $mDelete"
                                )

                                when {
                                    mDelete > -1 -> {
                                        val mInsert = checkoutDao.insertCheckoutRow(it)

                                        when {
                                            mInsert > -1 -> {
                                                val mGroup = storeGroupsAndSeats(
                                                    it,
                                                    mTableList!![mTableCount + 1].mGroupList!![0].mSeatList!!,
                                                    mLocationID
                                                )

                                                val mGroupDelete =
                                                    checkoutDao.deleteTableGroupsByGroupName(
                                                        mTableList!![mTableCount + 1].mTableID,
                                                        mTableList!![mTableCount + 1].mGroupList!![0].mGroupName
                                                    )

                                                when {
                                                    mGroupDelete > -1 -> {
                                                        val mGroupInsert =
                                                            checkoutDao.storeTableGroups(mGroup)

                                                        when {
                                                            mGroupInsert > -1 -> insertCheckouts(
                                                                mTableCount + 1,
                                                                0,
                                                                mLocationID
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            e(javaClass.simpleName, "else ${mTableList!![mTableCount + 1].mTableNo}")
                            e(javaClass.simpleName, "else size: ${mTableList!!.size}")
                            when (mTableList!!.size) {
                                mTableCount + 1 -> mPaymentSyncData.postValue(1)
                            }
                            insertCheckouts(mTableCount + 1, 0, mLocationID)
                        }

                    }
                }
        }
    }

    private fun storeGroupsAndSeats(
        mCheckout: CheckoutModel,
        mTableSeatList: ArrayList<ServerTableSeatModel>,
        mLocationID: String
    ): LocalTableGroupModel {

        val mTransactionList = mCheckout.mCheckoutTransaction

        val mSeatList = ArrayList<LocalTableSeatModel>()

        for (i in 0 until mTransactionList.size) {
            val mServerSeatList = mTransactionList[i].mSeatList

            for (j in 0 until mServerSeatList.size) {

                val mLocalSeat = LocalTableSeatModel(
                    mServerSeatList[j],
                    mCheckout.mGroupName,
                    mCheckout.mTableNO,
                    mCheckout.mTableID,
                    true, isPaid = true
                )
                mSeatList.add(mLocalSeat)

            }
        }


        for (i in 0 until mTableSeatList.size) {
            var mHasSeat = false
            for (j in 0 until mSeatList.size) {
                if (mTableSeatList[i].mSeatNO == mSeatList[j].mSeatNO) {
                    mHasSeat = true
                    break
                }
            }

            if (!mHasSeat) {
                val mLocalSeat = LocalTableSeatModel(
                    mTableSeatList[i].mSeatNO,
                    mCheckout.mGroupName,
                    mCheckout.mTableNO,
                    mCheckout.mTableID,
                    true, isPaid = false
                )
                mSeatList.add(mLocalSeat)
            }
        }


        return LocalTableGroupModel(
            mCheckout.mGroupName,
            true,
            mCheckout.mTableNO,
            mCheckout.mTableID,
            mLocationID,
            mSeatList
        )
    }

    fun checkInternet(mContext: Context): LiveData<Boolean> {
        val mInternetData = MutableLiveData<Boolean>()
        val scope = CoroutineScope(coRoutineContext)
        scope.launch(Dispatchers.IO) {
            Handler(Looper.getMainLooper()).post {
                Network.isNetworkAvailableWithInternetAccess(mContext).observeForever {
                    mInternetData.postValue(it)
                }
            }
        }
        return mInternetData
    }
}