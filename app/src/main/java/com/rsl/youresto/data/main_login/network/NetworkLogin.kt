package com.rsl.youresto.data.main_login.network


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class NetworkLogin(
    var data: Login = Login(),
    var status: Boolean = false,
    var msg: String = ""
)

@Entity
data class Login(
    @PrimaryKey
    var id: String = "",
    @SerializedName("about_restaurant")
    var aboutRestaurant: String = "",
    var address: String = "",
    @SerializedName("api_key")
    var apiKey: String = "",
    @SerializedName("business_name")
    var businessName: String = "",
    var city: String = "",
    @SerializedName("close_time")
    var closeTime: String = "",
    @SerializedName("contact_number")
    var contactNumber: String = "",
    var country: String = "",
    var countrycode: String = "",
    var currency: String = "",
    @SerializedName("currency_symbol")
    var currencySymbol: String = "",
    var datetime: String = "",
    var email: String = "",
    @SerializedName("food_company_id")
    var foodCompanyId: String? = "",
    @SerializedName("forgot_link_used")
    var forgotLinkUsed: String = "",
    @SerializedName("group_seq")
    var groupSeq: String = "",
    @SerializedName("img_url")
    var imgUrl: String = "",
    @SerializedName("is_active")
    var active: String = "",
    @SerializedName("is_alacalc_recipe")
    var isAlacalcRecipe: String = "",
    @SerializedName("is_category_prices")
    var isCategoryPrices: String = "",
    @SerializedName("is_individual_reg")
    var isIndividualReg: String = "",
    @SerializedName("is_reg_payment")
    var isRegPayment: String = "",
    @SerializedName("is_tax")
    var isTax: Boolean = false,
    var latitude: String = "",
    var longitude: String = "",
    var name: String = "",
    @SerializedName("opening_time")
    var openingTime: String = "",
    @SerializedName("owner_address")
    var ownerAddress: String = "",
    @SerializedName("owner_contact_no")
    var ownerContactNo: String = "",
    var password: String = "",
    @SerializedName("payment_end_date")
    var paymentEndDate: String = "",
    var postcode: String = "",
    @SerializedName("profile_photo")
    var profilePhoto: String = "",
    @SerializedName("rest_img_1")
    var restImg1: String = "",
    @SerializedName("rest_img_2")
    var restImg2: String = "",
    @SerializedName("rest_img_3")
    var restImg3: String = "",
    @SerializedName("rest_img_4")
    var restImg4: String = "",
    @SerializedName("rest_img_5")
    var restImg5: String = "",
    @SerializedName("restaurant_name")
    var restaurantName: String? = "",
    var restauranttype: String = "",
    @SerializedName("subscription_id")
    var subscriptionId: String = "",
    var tax: String = "",
    @SerializedName("upline_id")
    var uplinkId: String? = "",
    var usertype: String = ""
)