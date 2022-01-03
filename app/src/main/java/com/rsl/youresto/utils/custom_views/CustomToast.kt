package com.rsl.youresto.utils.custom_views

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.rsl.youresto.R

object CustomToast {

    fun makeText(context: Activity, text: String, duration: Int): Toast {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // Inflate the Layout
        val layout = inflater.inflate(R.layout.custom_toast, null)

        val textView = layout.findViewById<TextView>(R.id.text_view_toast_text)
        // Set the Text to show in TextView
        textView.text = text

        val toast = Toast(context)
        toast.duration = duration
        toast.view = layout


        return toast
    }

    fun createToast(context: Context, message: String, duration: Int) :Toast{
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        /*first parameter is the layout you made
        second parameter is the root view in that xml
         */
        val layout = inflater.inflate(R.layout.custom_toast, null)

        layout.findViewById<TextView>(R.id.text_view_toast_text).text = message

        val toast = Toast(context)
        toast.duration = duration

        toast.view = layout

        return toast
    }

}