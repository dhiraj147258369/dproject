package com.rsl.youresto.network.models


import com.google.gson.annotations.SerializedName

data class NetworkReportModel(
    var data: List<ReportData> = ArrayList(),
    var status: Boolean = false
)

data class ReportData(
    @SerializedName("applied_tax_details")
    var appliedTaxDetails: Any,
    @SerializedName("assigned_at")
    var assignedAt: Any,
    @SerializedName("cancel_note")
    var cancelNote: Any,
    @SerializedName("cgst_per")
    var cgstPer: String,
    @SerializedName("completed_at")
    var completedAt: String,
    @SerializedName("created_at")
    var createdAt: String,
    @SerializedName("customer_address_id")
    var customerAddressId: String,
    @SerializedName("customer_id")
    var customerId: String,
    @SerializedName("delivery_fee")
    var deliveryFee: String,
    @SerializedName("delivery_payment")
    var deliveryPayment: String,
    @SerializedName("dis_total_percentage")
    var disTotalPercentage: String,
    @SerializedName("disc_percentage_total")
    var discPercentageTotal: String,
    @SerializedName("disc_total")
    var discTotal: String,
    @SerializedName("discount_note")
    var discountNote: String,
    var id: String,
    @SerializedName("invoice_id")
    var invoiceId: Any,
    @SerializedName("is_invoiced")
    var isInvoiced: String,
    @SerializedName("loyalty_points")
    var loyaltyPoints: String,
    @SerializedName("net_total")
    var netTotal: String,
    @SerializedName("no_of_person")
    var noOfPerson: String,
    @SerializedName("order_by")
    var orderBy: String? = "",
    @SerializedName("order_items")
    var orderItems: List<OrderItem>,
    @SerializedName("order_no")
    var orderNo: String,
    @SerializedName("picked_at")
    var pickedAt: Any,
    @SerializedName("rest_id")
    var restId: String,
    @SerializedName("sgst_per")
    var sgstPer: String,
    var status: String,
    @SerializedName("sub_total")
    var subTotal: String,
    var suggetion: String,
    @SerializedName("supply_option")
    var supplyOption: String,
    @SerializedName("table_id")
    var tableId: String,
    @SerializedName("table_orders_id")
    var tableOrdersId: String,
    @SerializedName("updated_at")
    var updatedAt: String,
    var viewed: String
)

data class OrderItem(
    @SerializedName("created_at")
    var createdAt: String,
    var disc: String,
    @SerializedName("disc_amt")
    var discAmt: String,
    var id: String,
    @SerializedName("is_kot")
    var isKot: String,
    @SerializedName("order_id")
    var orderId: String,
    var price: String,
    var qty: String,
    @SerializedName("recipe_id")
    var recipeId: String,
    @SerializedName("special_notes")
    var specialNotes: String,
    @SerializedName("sub_total")
    var subTotal: String,
    var total: String,
    @SerializedName("updated_at")
    var updatedAt: Any?
)