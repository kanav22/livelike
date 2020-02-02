package com.example.android.hiveproject

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.indwealth.core.rest.data.api.RetrofitFactory
import okhttp3.OkHttpClient

class ServiceGenerator(
    private val context: Application,
    private val showToastOnError: Boolean,
    defaultTimeOut: Long = 20
) : RetrofitFactory("https://developers.zomato.com/", defaultTimeOut) {

    private  val userKey = "a5da29e9d258f037bf7e16e1bc972193"

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun getOkHttpClientBuilder(): OkHttpClient.Builder {
        val okHttpBuilder = super.getOkHttpClientBuilder()

        okHttpBuilder.addInterceptor { chain ->
            val request = chain.request().newBuilder()
            request.addHeader("user_key", userKey)
            val response =
                chain.proceed(request.build()) // perform request, here original request will be executed
            if (response.code == 400) {
                mainHandler.post {
                }
            }
            return@addInterceptor response
        }

        return okHttpBuilder
    }
}