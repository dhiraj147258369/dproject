package com.rsl.youresto.ui.main_screen.pending_order.event

import com.rsl.youresto.data.cart.models.CartProductModel

class PendingOrderDeleteEvent(
    val mResult: Boolean, val mCart: CartProductModel
)