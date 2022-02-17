package com.rsl.foodnairesto.utils

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun getDateForShift(mFutureDay: Boolean): Date {

        val calendar1 = Calendar.getInstance()
        calendar1.set(Calendar.HOUR_OF_DAY, 0)
        calendar1.set(Calendar.MINUTE, 0)
        calendar1.set(Calendar.SECOND, 0)
        calendar1.set(Calendar.MILLISECOND, 0)


        val calendar2 = Calendar.getInstance()
        calendar2.set(Calendar.HOUR_OF_DAY, 4)
        calendar2.set(Calendar.MINUTE, 0)
        calendar2.set(Calendar.SECOND, 0)
        calendar2.set(Calendar.MILLISECOND, 0)

        if (calendar1.timeInMillis < System.currentTimeMillis() && System.currentTimeMillis() < calendar2.timeInMillis) {
            val cal = Calendar.getInstance()

            if (!mFutureDay)
                cal.add(Calendar.DAY_OF_YEAR, -1)

            cal.set(Calendar.HOUR_OF_DAY, 4)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            return cal.time
        } else {
            val cal = Calendar.getInstance()

            if (mFutureDay)
                cal.add(Calendar.DAY_OF_YEAR, 1)

            cal.set(Calendar.HOUR_OF_DAY, 4)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)

            return cal.time
        }

    }

    fun getStringFromDate(mFormat: String, mDate: Date): String? {
        val mDateFormat = SimpleDateFormat(mFormat, Locale.ENGLISH)
        return mDateFormat.format(mDate)
    }
}