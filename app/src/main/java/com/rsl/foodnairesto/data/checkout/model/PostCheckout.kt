package com.rsl.foodnairesto.data.checkout.model


import com.google.gson.annotations.SerializedName

data class PostCheckout(
    @SerializedName("card_payment")
    var cardPayment: Double = 0.0,
    @SerializedName("cash_payment")
    var cashPayment: Double = 0.0,
    @SerializedName("cgst_per")
    var cgstPer: Double = 0.0,
    @SerializedName("dis_total")
    var disTotal: Double = 0.0,
    @SerializedName("dis_total_percentage")
    var disTotalPercentage: Double = 0.0,
    @SerializedName("discount_note")
    var discountNote: String = "",
    @SerializedName("invoice_id")
    var invoiceId: String = "",
    @SerializedName("net_banking")
    var netBanking: Double = 0.0,
    @SerializedName("net_total")
    var netTotal: Double = 0.0,
    @SerializedName("order_id")
    var orderId: String = "",


    var ids: ArrayList<String> = ArrayList(),

    @SerializedName("sgst_per")
    var sgstPer: Double = 0.0,
    @SerializedName("sub_total")
    var subTotal: Double = 0.0,
    @SerializedName("table_order_id")
    var tableOrderId: String = "",
    @SerializedName("upi_payment")
    var upiPayment: Double = 0.0
) {
    override fun toString(): String {
        return "PostCheckout(cardPayment=$cardPayment, cashPayment=$cashPayment, cgstPer=$cgstPer, disTotal=$disTotal, disTotalPercentage=$disTotalPercentage, discountNote='$discountNote', invoiceId='$invoiceId', netBanking=$netBanking, netTotal=$netTotal, orderId='$orderId', sgstPer=$sgstPer, subTotal=$subTotal, tableOrderId='$tableOrderId', upiPayment=$upiPayment)"
    }
}