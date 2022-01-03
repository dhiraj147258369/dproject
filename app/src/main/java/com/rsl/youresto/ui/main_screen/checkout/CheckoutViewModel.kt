package com.rsl.youresto.ui.main_screen.checkout

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.rsl.youresto.data.checkout.model.CheckoutModel
import com.rsl.youresto.data.database_download.models.PaymentMethodModel
import com.rsl.youresto.data.database_download.models.TaxModel
import com.rsl.youresto.data.tables.models.LocalTableGroupModel
import com.rsl.youresto.repositories.CheckoutRepository
import com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.model.YoyoPaymentAuthModel
import com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.model.YoyoResponseModel

class CheckoutViewModel constructor(val mRepository: CheckoutRepository): ViewModel() {

    fun insertCheckoutRow(mCheckoutModel: CheckoutModel) : LiveData<Int>{
        return mRepository.insertCheckoutRow(mCheckoutModel)
    }

    fun updateCheckout(mCheckoutModel: CheckoutModel) : LiveData<Int>{
        return mRepository.updateCheckout(mCheckoutModel)
    }

    fun getTableGroupsAndSeats(mTableNO: Int, mLocationID: String): LiveData<List<LocalTableGroupModel>> {
        return mRepository.getTableGroupsAndSeats(mTableNO, mLocationID)
    }

    fun getTableGroupAndSeats(mTableNO: Int, mGroupName: String): LiveData<LocalTableGroupModel> {
        return mRepository.getTableGroupAndSeats(mTableNO, mGroupName)
    }

    fun updateTableGroupAndSeats(mGroupTableGroupModel: LocalTableGroupModel){
        mRepository.updateTableGroupAndSeats(mGroupTableGroupModel)
    }

    fun getCheckoutDataByTableAndGroup(mTableID: String, mGroupName: String): LiveData<CheckoutModel> {
        return mRepository.getCheckoutDataByTableAndGroup(mTableID, mGroupName)
    }

    fun getCheckoutDataByTableAndGroupWithoutObserving(mTableID: String, mGroupName: String): LiveData<CheckoutModel> {
        return mRepository.getCheckoutDataByTableAndGroupWithoutObserving(mTableID, mGroupName)
    }

    fun getCheckoutDataByCartID(mCartID: String): LiveData<CheckoutModel> {
        return mRepository.getCheckoutDataByCartID(mCartID)
    }

    fun getCheckoutDataByTableAndCartID(mTableID: String, mCartID: String): LiveData<CheckoutModel> {
        return mRepository.getCheckoutDataByTableAndCartID(mTableID, mCartID)
    }

    fun getCheckoutDataByRowID(mCheckoutRowID: Int): LiveData<CheckoutModel> {
        return mRepository.getCheckoutDataByRowID(mCheckoutRowID)
    }

    fun getCheckoutDataByRowIDWithoutObserving(mCheckoutRowID: Int): LiveData<CheckoutModel> {
        return mRepository.getCheckoutDataByRowIDWithoutObserving(mCheckoutRowID)
    }

    fun getPaymentMethods(): LiveData<List<PaymentMethodModel>> {
        return mRepository.getPaymentMethods()
    }

    fun deleteCheckoutRow(mCheckoutRowID: Int) : LiveData<Int>{
        return mRepository.deleteCheckoutRow(mCheckoutRowID)
    }

    fun deleteQuickServiceCheckoutRow(mCheckoutRowID: Int) : LiveData<Int>{
        return mRepository.deleteQuickServiceCheckoutRow(mCheckoutRowID)
    }

    fun deleteWithoutSeatSelectionCheckoutRow(mCheckoutRowID: Int) : LiveData<Int>{
        return mRepository.deleteWithoutSeatSelectionCheckoutRow(mCheckoutRowID)
    }

    fun getTaxData(): LiveData<List<TaxModel>>{
        return mRepository.getTaxData()
    }

    fun updateDiscount(mCheckoutModel: CheckoutModel): LiveData<Int> {
        return mRepository.updateDiscount(mCheckoutModel)
    }

    fun updateServiceCharge(mCheckoutModel: CheckoutModel): LiveData<Int> {
        return mRepository.updateServiceCharge(mCheckoutModel)
    }

    fun updateTip(mCheckoutModel: CheckoutModel): LiveData<Int> {
        return mRepository.updateTip(mCheckoutModel)
    }

    fun updateCash(mCheckout: CheckoutModel, context: Context): LiveData<Int>{
        return mRepository.updatePayment(mCheckout, context)
    }

    fun updateCard(mCheckout: CheckoutModel, context: Context): LiveData<Int>{
        return mRepository.updatePayment(mCheckout, context)
    }

    fun updateWallet(mCheckout: CheckoutModel, context: Context): LiveData<Int>{
        return mRepository.updatePayment(mCheckout, context)
    }

    fun submitOrder(mCheckout: CheckoutModel) : LiveData<Int>{
        return mRepository.submitOrder(mCheckout)
    }

    fun callYoyoPaymentAuthAPI(mPaymentAuthModel : YoyoPaymentAuthModel): LiveData<YoyoResponseModel> {
        return mRepository.callYoyoPaymentAuthAPI(mPaymentAuthModel)
    }

    fun callYoyoBasketRegistrationAPI(mPaymentAuthModel : YoyoPaymentAuthModel, mBasketID: String): LiveData<YoyoResponseModel> {
        return mRepository.callYoyoBasketRegistrationAPI(mPaymentAuthModel, mBasketID)
    }

    fun paymentSync(mLocationID: String){
        mRepository.paymentSync(mLocationID)
    }

    fun checkInternet(mContext: Context): LiveData<Boolean> {
        return mRepository.checkInternet(mContext)
    }
}