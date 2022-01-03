package com.rsl.youresto.ui.main_screen.checkout

import androidx.lifecycle.ViewModel
import com.rsl.youresto.data.checkout.model.PostCheckout

class SharedCheckoutViewModel: ViewModel() {

    var postCheckout = PostCheckout()

    var paidAmount = 0.0
}