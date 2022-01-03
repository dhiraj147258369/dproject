package com.rsl.youresto.ui.main_screen.checkout.payment_options.wallet.model

import com.rsl.youresto.data.checkout.model.CheckoutTransaction
import java.math.BigDecimal

class YoyoPaymentAuthModel(
    val mCheckoutTransaction: CheckoutTransaction,
    val mDateTime: String,
    val mQRCode: String,
    val mTableNO: Int,
    val mGroupName: String,
    val mTaxPercent: BigDecimal
)