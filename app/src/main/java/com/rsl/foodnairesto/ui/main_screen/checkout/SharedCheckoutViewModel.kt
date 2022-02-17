package com.rsl.foodnairesto.ui.main_screen.checkout

import androidx.lifecycle.ViewModel
import com.rsl.foodnairesto.data.checkout.model.PostCheckout

class SharedCheckoutViewModel: ViewModel() {

    var postCheckout = PostCheckout()

    var paidAmount = 0.0
}