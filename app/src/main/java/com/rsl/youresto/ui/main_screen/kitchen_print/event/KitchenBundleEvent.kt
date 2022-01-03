package com.rsl.youresto.ui.main_screen.kitchen_print.event

import com.rsl.youresto.ui.main_screen.kitchen_print.model.SingleKOTModel

class KitchenBundleEvent(
    var mResult: Boolean,
    var mSerialNO: Int,
    var mGroupName: String,
    var mCartID: String,
    var mSingleKOTData: SingleKOTModel
)