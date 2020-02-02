package com.indwealth.core.rest.data

import java.net.HttpURLConnection

data class ErrorBody(val message: String, val code: Int = -1)

const val DEFAULT_ERROR_MESSAGE = "Something went wrong. Please try again later."

const val ERROR_CODE_UNKNOWN_HOST = 450
const val ERROR_CODE_CANCELLATION_JOB = 451
const val ERROR_CODE_TIMEOUT = HttpURLConnection.HTTP_CLIENT_TIMEOUT
const val ERROR_CODE_IO_EXCEPTION = 452