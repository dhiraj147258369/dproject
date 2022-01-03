package com.rsl.youresto.ui.main_screen.cart.event

import com.rsl.youresto.data.cart.models.CartProductModel

class CheckNetworkCartEvent(val mPosition: Int, val mChangeType: Int, val mCartModel: CartProductModel)