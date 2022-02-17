package com.rsl.foodnairesto.ui.main_screen.cart.event

import com.rsl.foodnairesto.data.cart.models.CartProductModel

class DeleteCartProductEvent(var mCartModel: CartProductModel, val mPosition: Int, val mGroupName:String)