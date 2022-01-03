package com.rsl.youresto.ui.main_screen.cart.event

import com.rsl.youresto.data.cart.models.CartProductModel

class DeleteCartProductEvent(var mCartModel: CartProductModel, val mPosition: Int, val mGroupName:String)