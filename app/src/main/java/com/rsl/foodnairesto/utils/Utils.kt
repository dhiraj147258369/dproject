package com.rsl.foodnairesto.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.Build
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.SuperscriptSpan
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("LogNotTimber")
object Utils {

    const val DATE_FORMAT_1 = "yyyy-MM-dd HH:mm:ss"

    fun dpToPx(dp: Int): Int {
        return (dp * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun round(value: Double, places: Int): Double {
        if (places < 0) throw IllegalArgumentException()

        var bd = BigDecimal(value)
        bd = bd.setScale(places, RoundingMode.DOWN)
        return bd.toDouble()
    }

    fun round(value: Double): Double {
        var bd = BigDecimal(value)
        bd = bd.setScale(2, RoundingMode.DOWN)
        return bd.toDouble()
    }

    fun roundUp(value: Double): Double {
        var bd = BigDecimal(value)
        bd = bd.setScale(2, RoundingMode.HALF_UP)
        return bd.toDouble()
    }

    fun getStringFromDate(mFormat: String, mDate: Date): String {
        val mDateFormat = SimpleDateFormat(mFormat, Locale.ENGLISH)
        return mDateFormat.format(mDate)
    }

    fun getDateFromString(mFormat: String, date: String) : Date{
        val mDateFormat = SimpleDateFormat(mFormat, Locale.ENGLISH)
        return mDateFormat.parse(date)
    }

    fun getDate(mFormat: String): Date? {
        val mDateFormat = SimpleDateFormat(mFormat, Locale.ENGLISH)
        val mToday = mDateFormat.format(Date())
        return mDateFormat.parse(mToday)
    }

    fun hideKeyboardFrom(context: Context, view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @SuppressLint("LogNotTimber")
    fun roundUsingString(value: Double) : Double{

        var mString = value.toString()
        var mPosition = mString.indexOf(".")
        mPosition += 3

        //remove
        if (mString.length > mPosition ) mString = mString.removeRange(mPosition, mString.length)

        return mString.toDouble()
    }


    fun fromHtml(html:String):String{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // FROM_HTML_MODE_LEGACY is the behaviour that was used for versions below android N
            // we are using this flag to give a consistent behaviour
            Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            Html.fromHtml(html).toString()
        }
    }


    fun getSuperScriptForDate(mDate: String): SpannableStringBuilder{

        val mDateSplit = mDate.split(" ")

        val mNumber = mDateSplit[0].split("")

        val mSuperScript: String

        mSuperScript = when {
            mNumber[2] == "1" && mDate != "11" ->"st"
            mNumber[2] == "2" && mDate != "12" -> "nd"
            mNumber[2] == "3" && mDate != "13" -> "rd"
            else -> "th"
        }

        val mDateString =  mDateSplit[0] + mSuperScript + " " +
                mDateSplit[1] + "\n" + mDateSplit[2] + " " + mDateSplit[3]

        val superscriptSpan = SuperscriptSpan()
        val builder = SpannableStringBuilder(mDateString)
        builder.setSpan(
            superscriptSpan,
            2,
            3 + 2,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        return builder
    }

    fun disableAllViews(vg: ViewGroup, enable: Boolean){
        for (i in 0 until vg.childCount) {
            val child = vg.getChildAt(i)
            child.isEnabled = !enable
            if (child is ViewGroup) {
                disableAllViews(child, enable)
            }
        }
    }
}