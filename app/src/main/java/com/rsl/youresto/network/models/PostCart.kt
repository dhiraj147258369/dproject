package com.rsl.youresto.network.models


import com.google.gson.annotations.SerializedName

data class PostCart(
    var appliedTaxDetails: ArrayList<AppliedTaxDetail> = ArrayList(),
    @SerializedName("cart_details")
    var cartDetails: ArrayList<CartDetail> = ArrayList(),
    @SerializedName("customer_id")
    var customerId: Int = 1,
    @SerializedName("loyalty_points")
    var loyaltyPoints: Int = 0,
    @SerializedName("net_total")
    var netTotal: Double = 0.0,
    @SerializedName("no_of_person")
    var noOfPerson: Int = 0,
    var orderBy: Int = 0,
    @SerializedName("restaurant_id")
    var restaurantId: Int = 0,
    @SerializedName("sub_total")
    var subTotal: Double = 0.0,
    var suggetion: String = "",
    @SerializedName("table_id")
    var tableId: Int = 0,

    var orderId: String = "",
)

data class AppliedTaxDetail(
    @SerializedName("tx_name")
    var txName: String = "",
    @SerializedName("tx_per")
    var txPer: String = "",
    @SerializedName("tx_total")
    var txTotal: String = ""
)

data class CartDetail(
    var addon: List<Addon> = listOf(),
    var price: Double = 0.0,
    var qty: Int = 0,
    @SerializedName("recipe_id")
    var recipeId: Int = 0,
    @SerializedName("special_notes")
    var specialNotes: String = "",
    var subtotal: Double = 0.0,
) {
    data class Addon(
        @SerializedName("addon_id")
        var addonId: Int = 0
    )

    override fun toString(): String {
        return super.toString()
    }
}