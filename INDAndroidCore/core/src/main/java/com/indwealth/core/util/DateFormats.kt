package com.indwealth.core.util

import java.text.SimpleDateFormat
import java.util.*

// const val REGEX_MM_DD_YYYY_SLASH = "^(0[1-9]|1[0-2])[\\/](0[1-9]|[12]\\d|3[01])[\\/](19|20)\\d{2}\$"
// const val REGEX_MM_DD_YYYY_HYPHEN = "^(0[1-9]|1[0-2])[-](0[1-9]|[12]\\d|3[01])[-](19|20)\\d{2}\$"

const val REGEX_DD_MM_YYYY_SLASH = "^(0[1-9]|[12]\\d|3[01])[\\/](0[1-9]|1[0-2])[\\/](19|20)\\d{2}\$"
const val REGEX_DD_MM_YYYY_HYPHEN = "^(0[1-9]|[12]\\d|3[01])[-](0[1-9]|1[0-2])[-](19|20)\\d{2}\$"

const val REGEX_YYYY_MM_DD_HYPHEN = "^(19|20)\\d\\d[-]([1-9]|0[1-9]|1[012])[-]([1-9]|0[1-9]|[12][0-9]|3[01])\$"
const val REGEX_YYYY_MM_DD_SLASH = "^(19|20)\\d\\d[-]([1-9]|0[1-9]|1[012])[-]([1-9]|0[1-9]|[12][0-9]|3[01])\$"

const val DATE_FORMAT_DMMMYYYY = "d MMM yyyy"
const val DATE_FORMAT_MMMyy = "MMM yy"
const val DATE_FORMAT_DDMMYYYY_SLASH = "dd/MM/yyyy"
const val DATE_FORMAT_YYYYMMDD = "yyyy-MM-dd"
const val DATE_FORMAT_MMM_DD_YYYY = "MMM dd, yyyy"
const val DATE_FORMAT_DD_MMM_YYYY = "dd MMM yyyy"
const val DATE_FORMAT_MMMyyyy = "MMM yyyy"
const val DATE_FORMAT_MMM_YYYY = "MMM\nyyyy"
const val DATE_FORMAT_YYYY = "yyyy"
const val DATE_FORMAT_MMM_COMMA_YYYY = "MMM, yyyy"
const val DATE_FORMAT_TIME_STAMP = "yyyy-MM-dd'T'HH:mm:ss"
const val DATE_FORMAT_TIME = "yyyy-MM-dd'T'HH:mm:ss"
const val DATE_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss"
// const val DATE_FORMAT_TIME_STAMP = "yyyy-MM-dd'T'HH:mm:ss.ssssss'Z'"

@Deprecated("use a new Instance every time as SimpleDateFormat is not thread safe")
val FORMAT_DDMMYYYY = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)

@Deprecated("use a new Instance every time as SimpleDateFormat is not thread safe")
val FORMAT_DDMMYYYY_SLASH = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)

@Deprecated("use a new Instance every time as SimpleDateFormat is not thread safe")
val FORMAT_YYYYMMDD = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

@Deprecated("use a new Instance every time as SimpleDateFormat is not thread safe")
val FORMAT_YYYYMMDD_SLASH = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)

@Deprecated("use a new Instance every time as SimpleDateFormat is not thread safe", replaceWith = ReplaceWith("SimpleDateFormat(\"MM/dd/yyyy\", Locale.ENGLISH)", "java.text.SimpleDateFormat", "java.util.*"))
val FORMAT_MMDDYYYY = SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH)

@Deprecated("use a new Instance every time as SimpleDateFormat is not thread safe")
val FORMAT_MMM_DD_YYYY = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)

@Deprecated("use a new Instance every time as SimpleDateFormat is not thread safe")
val FORMAT_DD_MMM_YYYY = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

@Deprecated("use a new Instance every time as SimpleDateFormat is not thread safe")
val FORMAT_YYYYMMDDHHMMSS = SimpleDateFormat("yyyymmddhhmmss", Locale.ENGLISH)

@Deprecated("use a new Instance every time as SimpleDateFormat is not thread safe")
val FORMAT_YYYYMMDDTHHMMSS = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)

@Deprecated("use a new Instance every time as SimpleDateFormat is not thread safe")
val FORMAT_MMMyyyy = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)

@Deprecated("use a new Instance every time as SimpleDateFormat is not thread safe")
val FORMAT_DMMMYYYY = SimpleDateFormat(DATE_FORMAT_DMMMYYYY, Locale.ENGLISH)

@Deprecated("use a new Instance every time as SimpleDateFormat is not thread safe")
val FORMAT_MMMyy = SimpleDateFormat("MMM yy", Locale.ENGLISH)

@Deprecated("use a new Instance every time as SimpleDateFormat is not thread safe")
val FORMAT_MMM_YYYY = SimpleDateFormat("MMM\nyyyy", Locale.ENGLISH)

@Deprecated("use a new Instance every time as SimpleDateFormat is not thread safe")
val FORMAT_YYYY = SimpleDateFormat("yyyy", Locale.ENGLISH)