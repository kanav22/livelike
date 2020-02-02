@file:JvmName("DateExtensions")

package com.indwealth.core.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs
import timber.log.Timber

@Deprecated(message = "Use com.indwealth.core.util.toDate2", replaceWith = ReplaceWith("toDate2(format)"))
fun String.toDate(format: SimpleDateFormat? = null): Date {
    try {
        return when {
            format != null -> format.parse(this)
            indexOf("-") == 2 -> FORMAT_DDMMYYYY.parse(this)
            indexOf("-") == 4 -> FORMAT_YYYYMMDD.parse(this)
            else -> FORMAT_DDMMYYYY_SLASH.parse(this)
        }
    } catch (e: Exception) {
        Timber.d("toDate: $this")
        e.printStackTrace()
        throw e
    }
}

fun Double.toDate(): Date {
    return Date((this * 1000L).toLong())
}

/**
 * Converts the string to date bhy matching the string to regex patters
 * @param format: pass string of the date format
 */
fun String.toDate2(format: String? = null): Date? {
    val date: Date?

    if (format != null) {
        try {
            date = SimpleDateFormat(format, Locale.ENGLISH).parse(this)
            if (date != null) return date
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    try {
        return when {
            Pattern.matches(REGEX_DD_MM_YYYY_SLASH, this) -> SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).parse(this)
            Pattern.matches(REGEX_DD_MM_YYYY_HYPHEN, this) -> SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(this)
            Pattern.matches(REGEX_YYYY_MM_DD_HYPHEN, this) -> SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(this)
            Pattern.matches(REGEX_YYYY_MM_DD_SLASH, this) -> SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH).parse(this)
            Pattern.matches(DATE_FORMAT_TIME_STAMP, this) -> SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(this)
            else -> {
                Timber.e("toDate2 : DATE: $this")
                null
            }
        }
    } catch (e: Exception) {
        Timber.e("toDate2 no format found: DATE: $this")
        e.printStackTrace()
        return null
    }
}

fun Date.DMMMYYYY(): String = SimpleDateFormat(DATE_FORMAT_DMMMYYYY, Locale.ENGLISH).format(this)

fun Date.MMMyy(): String = SimpleDateFormat(DATE_FORMAT_MMMyy, Locale.ENGLISH).format(this)

fun Date.MMMyyyy(): String = SimpleDateFormat(DATE_FORMAT_MMMyyyy, Locale.ENGLISH).format(this)

fun Date.YYYY_MM_DD(): String = SimpleDateFormat(DATE_FORMAT_YYYYMMDD, Locale.ENGLISH).format(this)

fun Date.DD_MM_YYYY_SLASH(): String = SimpleDateFormat(DATE_FORMAT_DDMMYYYY_SLASH, Locale.ENGLISH).format(this)

fun Date.MMM_DD_YYYY(): String = SimpleDateFormat(DATE_FORMAT_MMM_DD_YYYY, Locale.ENGLISH).format(this)

fun Date.DD_MMM_YYYY() = SimpleDateFormat(DATE_FORMAT_DD_MMM_YYYY, Locale.ENGLISH).format(this)!!

fun Date.MMM_NEWLINE_YYYY(): String = SimpleDateFormat(DATE_FORMAT_MMM_YYYY, Locale.ENGLISH).format(this)

fun Date.YYYY(): String = SimpleDateFormat(DATE_FORMAT_YYYY, Locale.ENGLISH).format(this)

fun Date.MMM_COMMA_YYYY(): String = SimpleDateFormat(DATE_FORMAT_MMM_COMMA_YYYY, Locale.ENGLISH).format(this)

fun Date.string(format: String): String = SimpleDateFormat(format, Locale.ENGLISH).format(this)

fun Date.howManyMonthsAway(date: Date = Date()): Int {
    val startCalendar = GregorianCalendar()
    startCalendar.time = this
    val endCalendar = GregorianCalendar()
    endCalendar.time = date
    val diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)
    val diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH)
    return abs(diffMonth - 1)
}

fun Date.howManyMonthsAwayAbsolute(date: Date = Date()): Int {
    val startCalendar = GregorianCalendar()
    startCalendar.time = this
    val endCalendar = GregorianCalendar()
    endCalendar.time = date
    val diffYear = endCalendar.get(Calendar.YEAR) - startCalendar.get(Calendar.YEAR)
    val diffMonth = diffYear * 12 + endCalendar.get(Calendar.MONTH) - startCalendar.get(Calendar.MONTH)
    return abs(diffMonth)
}

fun Date.endingIn(appendEndingIn: Boolean = true): String {
    val text = StringBuilder(if (appendEndingIn) "Ending In: " else "")

    val months = this.howManyMonthsAwayAbsolute()
    if (months < 1) return text.apply { append("1m") }.toString()

    var y = months / 12
    if (y > 0) text.append("${y}y ")
    y = months % 12
    if (y > 0) text.append("${y}m")
    return text.toString()
}