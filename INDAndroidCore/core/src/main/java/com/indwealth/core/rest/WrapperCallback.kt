package com.indwealth.core.rest

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber

class WrapperCallback<T>(val handler: ResponseHandler<T>? = null) : Callback<T> {
    override fun onFailure(call: Call<T>, t: Throwable) {
        Timber.d("onFailure (${call.request().url}) $t")
        try {
            handler?.onFail?.invoke()
            handler?.onComplete?.invoke()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResponse(call: Call<T>, response: Response<T>?) {
        Timber.d("onResponse ${response?.body()}")
        try {
            if (response?.body() == null)
                handler?.onFail?.invoke()
            else
                handler?.onSuccess?.invoke(response.body()!!)

            handler?.onComplete?.invoke()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class WrapperCallbackWithCode<T>(val handler: ResponseHandlerWithResponseCode<T>? = null) : Callback<T> {
    override fun onFailure(call: Call<T>, t: Throwable) {
        Timber.d("onFailure $t")
        try {
            handler?.onFail?.invoke(400)
            handler?.onComplete?.invoke(400)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResponse(call: Call<T>, response: Response<T>?) {
        Timber.d("onResponse ${response?.body()}")
        try {
            if (response?.body() == null)
                handler?.onFail?.invoke(response?.code() ?: 400)
            else {
                handler?.onSuccess?.invoke(response.code(), response.body()!!)
            }
            handler?.onComplete?.invoke(response?.code() ?: 400)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}