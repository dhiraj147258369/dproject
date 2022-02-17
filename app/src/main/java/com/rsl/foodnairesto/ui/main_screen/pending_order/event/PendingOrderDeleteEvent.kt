package com.rsl.foodnairesto.ui.main_screen.pending_order.event

import com.rsl.foodnairesto.data.cart.models.CartProductModel

class PendingOrderDeleteEvent(
    val mResult: Boolean, val mCart: CartProductModel
)