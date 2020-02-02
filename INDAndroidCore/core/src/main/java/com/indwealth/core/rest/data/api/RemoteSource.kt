package com.indwealth.core.rest.data.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.crashlytics.android.core.CrashlyticsCore
import com.google.gson.Gson
import com.indwealth.core.rest.data.*
import com.indwealth.core.rest.data.Result.Error
import com.squareup.moshi.JsonDataException
import java.io.IOException
import java.net.HttpURLConnection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import retrofit2.Response

object RemoteSource {

    private const val DEFAULT_RETRY_COUNT = 3

    /*suspend fun <T : Any> safeApiCall(call: () -> Deferred<Response<T>>, defaultErrorMessage: String): Result<T> {
        return try {
            val response = call().await()
            if (response.isSuccessful) return Result.Success(response.body()!!)
            return handleResponseFailure(response)
        } catch (ioException: IOException) {
            // An exception was thrown when calling the API so we're converting this to an ErrorBody
            Error(ErrorBody("Unable to connect with the server. Please try again later.", ERROR_CODE_IO_EXCEPTION))
        } catch (exception: Exception) {
            // An exception was thrown when calling the API so we're converting this to an ErrorBody
            Error(ErrorBody(defaultErrorMessage))
        }
    }
*/
    suspend fun <T : Any> safeApiCall(needRetry: Boolean = true, call: suspend () -> Response<T>): Result<T> {
        var count = 0
        var backOffTime = 2_000L
        while (count < DEFAULT_RETRY_COUNT) {
            try {
                val response = call.invoke()
                count = DEFAULT_RETRY_COUNT + 1
                if (response.isSuccessful) {
                    return if (response.body() != null) {
                        Result.Success(response.body()!!)
                    } else {
                        Result.SuccessWithNoContent
                    }
                }
                return handleResponseFailure(response)
            } catch (ioException: IOException) {
                ioException.printStackTrace()
                if (needRetry) {
                    delay(backOffTime)
                    count++
                    backOffTime *= 2
                } else {
                    return Error(ErrorBody("Unable to connect with the server. Please check your internet connection", ERROR_CODE_TIMEOUT))
                }
            } catch (cancellationException: CancellationException) {
                cancellationException.printStackTrace()
                return Error(ErrorBody("", ERROR_CODE_CANCELLATION_JOB))
            } catch (moshiDataException: JsonDataException) {
                moshiDataException.printStackTrace()
                CrashlyticsCore.getInstance().logException(moshiDataException)
                Error(ErrorBody(DEFAULT_ERROR_MESSAGE))
            } catch (exception: Exception) {
                exception.printStackTrace()
                // An exception was thrown when calling the API so we're converting this to an ErrorBody
                CrashlyticsCore.getInstance().logException(exception)
                return Error(ErrorBody(DEFAULT_ERROR_MESSAGE))
            }
        }

        return Error(ErrorBody(DEFAULT_ERROR_MESSAGE))
    }

    fun <T : Any> apiLiveData(call: suspend () -> Response<T>): LiveData<Result<T>> {
        return liveData {
            emit(safeApiCall(false, call))
        }
    }

    private fun <T : Any> handleResponseFailure(response: Response<T>): Error {
        val err = response.errorBody()
        if (err != null) {
            // try getting first error detail
            return try {
                val errorDetail = err.string()
                Error(buildErrorModel(response, errorDetail))
                // Keeping old error flow intact
            } catch (ignore: Exception) {
                val responseCode = response.code()
                when {
                    responseCode >= HttpURLConnection.HTTP_BAD_REQUEST && responseCode < HttpURLConnection.HTTP_INTERNAL_ERROR -> Error(ErrorBody("Unable to process your request. Please try again later.", responseCode))
                    responseCode >= HttpURLConnection.HTTP_INTERNAL_ERROR && responseCode < HttpURLConnection.HTTP_INTERNAL_ERROR + 100 -> Error(ErrorBody("Unable to process your request. Please try again later.", responseCode))
                    else -> Error(ErrorBody("Something went wrong! Please try again later", responseCode))
                }
            }
        }

        return Error(ErrorBody("Something went wrong! Please try again later"))
    }

    /**
     * Handle response error, default common error view converted, override it to change implementation
     *
     * @param response raw retrofit response
     * @param errorDetail error string from error body
     */
    private fun <T : Any> buildErrorModel(response: Response<T>, errorDetail: String): ErrorBody {

        if (errorDetail.contains("msg")) {
            val error = errorDetail.replace("msg", "message")
            return Gson().fromJson(error, ErrorBody::class.java).copy(code = response.code())
        }

        if (!errorDetail.contains("message")) {
            throw IllegalArgumentException()
        }

        // parse the error body from the response, copy the object apply the response code
        return Gson().fromJson(errorDetail, ErrorBody::class.java).copy(code = response.code())
    }
}