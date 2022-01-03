package com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.model

import java.math.BigDecimal

class YoyoModel(
    val mProductName: String,
    val mProductQuantity: BigDecimal,
    val mProductCategory: String,
    var mSpecialInstructionPrice: BigDecimal,
    var mProductPrice: BigDecimal,
    val mSeatNO: Int
)