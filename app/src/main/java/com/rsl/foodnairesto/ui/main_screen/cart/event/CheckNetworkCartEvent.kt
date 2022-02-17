package com.rsl.foodnairesto.ui.main_screen.cart.event

import com.rsl.foodnairesto.data.cart.models.CartProductModel

class CheckNetworkCartEvent(val mPosition: Int, val mChangeType: Int, val mCartModel: CartProductModel)