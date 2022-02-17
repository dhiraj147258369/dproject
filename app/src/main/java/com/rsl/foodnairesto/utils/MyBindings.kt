package com.rsl.foodnairesto.utils

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("android:layout_height")
fun setLayoutHeight(view: View, height: Float) {
    val layoutParams = view.layoutParams
    layoutParams.height = height.toInt()
    view.layoutParams = layoutParams
}