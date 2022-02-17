package com.rsl.foodnairesto.repositories

import com.rsl.foodnairesto.data.database_download.models.NewPaymentMethodModel
import com.rsl.foodnairesto.data.database_download.models.ReportModel
import com.rsl.foodnairesto.data.database_download.models.ReportProductIngredientModel
import com.rsl.foodnairesto.data.database_download.models.ReportProductModel
import com.rsl.foodnairesto.data.order_history.OrderHistoryDao
import com.rsl.foodnairesto.data.order_history.OrderHistoryRemoteSource
import com.rsl.foodnairesto.network.Resource
import com.rsl.foodnairesto.network.models.NetworkReportModel
import com.rsl.foodnairesto.network.models.ReportData
import com.rsl.foodnairesto.utils.Utils.DATE_FORMAT_1
import com.rsl.foodnairesto.utils.Utils.getDateFromString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class NewOrdersRepository(private val remoteSource: OrderHistoryRemoteSource, val dao: OrderHistoryDao) {

    suspend fun getCartReports(): Resource<NetworkReportModel> {
        val resource = withContext(Dispatchers.IO){
            remoteSource.getCartReports()
        }

        if (resource.status == Resource.Status.SUCCESS){
            resource.data?.let { data ->
                if (data.status) {
                    val reports = getReportsModel(ArrayList(data.data))
                    withContext(Dispatchers.IO){
                        dao.insertReports(reports)
                    }
                }
            }
        }

        return resource
    }

    private suspend fun getReportsModel(data: ArrayList<ReportData>): ArrayList<ReportModel>{
        val reports = ArrayList<ReportModel>()
        data.map { report ->
            val reportModel = ReportModel()
            val productList = ArrayList<ReportProductModel>()
            val paymentMethodList=ArrayList<NewPaymentMethodModel>()
            report.invPayment.map { method->
                paymentMethodList.add(NewPaymentMethodModel(method.id,method.paymentType,method.paymentAmount.toString().toDouble()))
            }
            report.orderItems.map { item ->

                val product = withContext(Dispatchers.IO){
                    dao.getProduct(item.recipeId)
                }

                if (product != null) {


                    val ingredientsList = ArrayList<ReportProductIngredientModel>()
                    var addOnPrice = BigDecimal(0)
                    if (item.addon != null){
                        if (item.addon.isNotEmpty()) {
                            val addOnIdList = ArrayList<String>()
                            item.addon.map { addOn -> addOnIdList.add(addOn.addonId.toString()) }
                            val localAddOns = dao.getAddOnsByIds(addOnIdList)
                            localAddOns.map { localAddOn ->
                                ingredientsList.add(
                                    ReportProductIngredientModel(
                                        localAddOn.mIngredientID,
                                        localAddOn.mIngredientName,
                                        localAddOn.mIngredientQuantity.toInt(),
                                        localAddOn.mCartIngredientID ?: "",
                                        localAddOn.mIngredientPrice.toDouble()
                                    )
                                )
                                addOnPrice += localAddOn.mIngredientPrice
                            }
                        }
                }

                    val totalPrice = (BigDecimal(item.price) + addOnPrice) * BigDecimal(item.qty)



                    productList.add(
                        ReportProductModel(
                            product.mGroupID.toString(),
                            product.mCategoryID.toString(),
                            item.recipeId,
                            product.mProductName,
                            item.qty.toInt(),
                            "",
                            item.price.toDouble(),
                            totalPrice.toDouble(),
                            product.mProductType,
                            "",
                            0.0,
                            item.createdAt,
                            "",
                            "",
                            "",
                            ingredientsList
                        )
                    )
                }


            }


            if (report.orderBy != null){
                val server = withContext(Dispatchers.IO){
                    dao.getServerById(report.orderBy ?: "")
                }
                reportModel.mServerName = server?.mServerName ?: ""
            }

            reportModel.mProductList = productList
            reportModel.paymentMethodsList=paymentMethodList

            var deliveryCharge = 0.0
            try {
                deliveryCharge = report.deliveryFee.toDouble()
            }catch (e: java.lang.Exception){
                e.printStackTrace()
            }


            reportModel.apply {
                id = report.id
                mRestaurantID = report.restId
                mTableID = report.tableId
                mCartID = report.orderNo
                mCartNO = report.orderNo
                mCartTotal = report.subTotal.toDouble()
                mDiscountType = report.discountNote
                try {
                    mDiscountPercent = report.discPercentageTotal.toDouble()
                    mDiscountAmount = report.discTotal.toDouble()

                }catch (e: Exception) {
                    e.printStackTrace()
                }
                mOrderTotal = report.netTotal.toDouble()
                mDateTime = report.createdAt
                mDateTimeInTimeStamp = getDateFromString(DATE_FORMAT_1, report.createdAt)
                deliverCharges = deliveryCharge




            }

            reports.add(reportModel)
        }

        return reports
    }
}