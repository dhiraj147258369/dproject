package com.rsl.youresto.utils

import com.rsl.youresto.BuildConfig.BASE_URL

object EndPoints {

    const val API_IP = "54.90.226.3"

    private const val OPENBRAVO_SERVER_URL = "http://54.90.226.3:8080/openbravo/ws/com.orda.mobileapps."

    const val APP_LOGIN = OPENBRAVO_SERVER_URL + "applogin"
    const val GET_ALL_DATA = OPENBRAVO_SERVER_URL + "getAllData"
    const val GET_ALL_TABLES = OPENBRAVO_SERVER_URL + "getAllTable"
    const val OCCUPY_TABLE = OPENBRAVO_SERVER_URL + "occupyTable"
    const val OCCUPY_TABLE_UPDATE = OPENBRAVO_SERVER_URL + "occupyTablesUpdate"

    const val SUBMIT_CART = OPENBRAVO_SERVER_URL + "submitCart"
    const val DELETE_CART_PRODUCT = OPENBRAVO_SERVER_URL + "deleteCartProduct"


    const val USER_LOGIN = OPENBRAVO_SERVER_URL + "userLogin"
    const val SHIFT_DETAILS = OPENBRAVO_SERVER_URL + "userShift"
    const val CLEAR_TABLE = OPENBRAVO_SERVER_URL + "clearTable"
    const val FAVORITE_ITEMS = BASE_URL + "saveFavoriteProduct"
    const val CART_PAYMENT = OPENBRAVO_SERVER_URL + "cartPayment"
    const val MOVE_TABLE = OPENBRAVO_SERVER_URL + "moveTable"
    const val SUBMIT_ORDER = OPENBRAVO_SERVER_URL + "submitOrder"
    const val CART_REPORT = OPENBRAVO_SERVER_URL + "cartReport"

    // YOYO WALLET
    const val YOYO_WALLET_URL = "https://commerce.staging1.yoyowallet.com/epos/v3"

}