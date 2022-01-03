package com.rsl.youresto.data.checkout

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rsl.youresto.data.cart.models.CartProductModel
import com.rsl.youresto.data.checkout.model.CheckoutModel
import com.rsl.youresto.data.checkout.model.CheckoutTransaction
import com.rsl.youresto.data.database_download.models.PaymentMethodModel
import com.rsl.youresto.data.database_download.models.TablesModel
import com.rsl.youresto.data.database_download.models.TaxModel
import com.rsl.youresto.data.tables.models.LocalTableGroupModel

@Dao
interface CheckoutDao {

    @Transaction
    @Insert
    fun insertCheckoutRow(mCheckoutModel: CheckoutModel) : Long

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun storeTableGroups(mGroup: LocalTableGroupModel): Long

    @Transaction
    @Query("DELETE FROM LocalTableGroupModel WHERE mTableID =:mTableID")
    fun deleteTableGroups(mTableID: String): Int

    @Transaction
    @Query("DELETE FROM LocalTableGroupModel WHERE mGroupName =:mGroupName AND mTableID =:mTableID")
    fun deleteTableGroupsByGroupName(mTableID: String, mGroupName: String): Int

    @Transaction
    @Query("UPDATE CheckoutModel SET mCheckoutTransaction =:mCheckoutTransaction WHERE mTableID =:mTableID AND mGroupName =:mGroupName")
    fun updateCheckoutTransactionIntoCheckout(mCheckoutTransaction: ArrayList<CheckoutTransaction>, mTableID: String, mGroupName: String ): Int

    @Transaction
    @Query("SELECT mID FROM CheckoutModel ORDER BY mID DESC LIMIT 1")
    fun getLatestCheckoutRowID() :Int

    @Transaction
    @Query("SELECT mID FROM CheckoutModel WHERE mTableID =:mTableID AND mGroupName =:mGroupName")
    fun getCheckoutRowIDByTableID(mTableID: String, mGroupName: String) :Int

    @Transaction
    @Query("SELECT * FROM LocalTableGroupModel WHERE mTableNO =:mTableNO AND mLocationID =:mLocationID")
    fun getTableGroupsAndSeats(mTableNO: Int, mLocationID: String): List<LocalTableGroupModel>

    @Transaction
    @Query("SELECT * FROM LocalTableGroupModel WHERE mTableNO =:mTableNO AND mGroupName =:mGroupName")
    fun getTableGroupAndSeats(mTableNO: Int, mGroupName: String): LocalTableGroupModel

    @Update
    fun updateTableGroupAndSeats(mGroupTableGroupModel: LocalTableGroupModel)

    @Transaction
    @Query("SELECT * FROM CheckoutModel WHERE mTableID =:mTableID AND mGroupName =:mGroupName")
    fun getCheckoutDataByTableAndGroup(mTableID: String, mGroupName: String): LiveData<CheckoutModel>

    @Transaction
    @Query("SELECT * FROM CheckoutModel WHERE mTableID =:mTableID AND mGroupName =:mGroupName")
    fun getCheckoutDataByTableAndGroupWithoutObserving(mTableID: String, mGroupName: String): CheckoutModel

    @Query("SELECT * FROM CheckoutModel WHERE mCartID =:mCartID")
    fun getCheckoutDataByCartID(mCartID: String): LiveData<CheckoutModel>

    @Transaction
    @Query("SELECT * FROM CheckoutModel WHERE mTableID =:mTableID AND mCartID =:mCartID")
    fun getCheckoutDataByTableAndCartID(mTableID: String, mCartID: String): LiveData<CheckoutModel>

    @Transaction
    @Query("SELECT * FROM CheckoutModel WHERE mID =:mCheckoutRowID")
    fun getCheckoutDataByRowID(mCheckoutRowID: Int): LiveData<CheckoutModel>

    @Transaction
    @Query("SELECT * FROM CheckoutModel WHERE mID =:mCheckoutRowID")
    fun getCheckoutDataByRowIDWithoutObserving(mCheckoutRowID: Int): CheckoutModel

    @Transaction
    @Query("SELECT * FROM PaymentMethodModel")
    fun getPaymentMethods(): List<PaymentMethodModel>

    @Transaction
    @Update
    fun updateCheckout(mCheckout: CheckoutModel) : Int

    @Delete
    fun deleteCheckout(mCheckout: CheckoutModel) : Int

    @Query("DELETE FROM CheckoutModel WHERE mID =:mCheckoutRowID")
    fun deleteCheckoutByRowID(mCheckoutRowID: Int) : Int

    @Query("DELETE FROM CheckoutModel WHERE mCartID =:mCartID AND mTableID =:mTableID")
    fun deleteCheckoutAfterSubmitOrder(mCartID: String, mTableID: String) : Int

    @Query("DELETE FROM CartProductModel WHERE mCartID =:mCartID AND mTableID =:mTableID")
    fun deleteCartAfterSubmitOrder(mCartID: String, mTableID: String) : Int

    @Query("DELETE FROM LocalTableGroupModel WHERE mGroupName =:mGroupName AND mTableID =:mTableID")
    fun deleteCartTableGroup(mGroupName: String, mTableID: String) : Int

    @Transaction
    @Query("SELECT * FROM TaxModel")
    fun getTaxData(): List<TaxModel>

    /*@Query("UPDATE CheckoutModel SET mTipAmount =:mTipAmount, mTipPercent =:mTipPercent, mAmountRemaining =:mRemainingAmount, mOrderTotal =:mOrderTotal, mTaxAmount =:mUpdatedTaxAmount WHERE mID =:mID")
    fun updateDiscount(
        mDiscountAmount: Double, mUpdatedTaxAmount: Double, mDiscountPercent: Double, mRemainingAmount: Double,
        mOrderTotal: Double, mID: Int
    ): Int*/

    @Transaction
    @Query("SELECT * FROM CartProductModel WHERE mTableNO =:mTableNO AND mGroupName =:mGroupName")
    fun getCartData(mTableNO: Int, mGroupName: String): List<CartProductModel>

    @Transaction
    @Query("SELECT * FROM TablesModel WHERE mLocationID =:mLocationID AND mTableNoOfOccupiedChairs > 0")
    fun getOccupiedTable(mLocationID: String) : List<TablesModel>

    @Query("DELETE FROM CheckoutModel WHERE mTableID =:mTableID AND mGroupName =:mGroupName")
    fun deleteCheckout(mTableID: String, mGroupName: String): Int

    @Query("DELETE FROM CheckoutModel")
    fun deleteAllCheckout(): Int
}