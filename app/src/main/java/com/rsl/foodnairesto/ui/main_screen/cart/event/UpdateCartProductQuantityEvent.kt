package com.rsl.foodnairesto.ui.main_screen.cart.event

import java.math.BigDecimal

class UpdateCartProductQuantityEvent(var mRowID: Int, var mQuantity: BigDecimal, var mTotalProductPrice: BigDecimal, val mGroupName: String)