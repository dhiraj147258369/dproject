package com.rsl.foodnairesto.data.checkout.model

import java.math.BigDecimal

class SeatPriceModel(
    val mSeatNo: Int,
    var mSeatTotal: BigDecimal,
    var isSelected: Boolean = false,
    var mPaid: Boolean = false
)