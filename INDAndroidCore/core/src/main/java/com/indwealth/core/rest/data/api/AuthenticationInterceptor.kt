package com.indwealth.core.rest.data.api

import com.indwealth.core.util.manager.TokenManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

open class AuthenticationInterceptor(private val tokenManager: TokenManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        // Build new request
        val builder = request.newBuilder()
        builder.header("Accept", "application/json") // if necessary, say to consume JSON
        builder.header("PLATFORM", "Android")

        val token = getAccessToken() // save token of this request for future
        setAuthHeader(builder, token) // write current token to request

        request = builder.build() // overwrite old request
        val response = chain.proceed(request) // perform request, here original request will be executed

        if (response.code == 401) { // if unauthorized
            synchronized(this) {
                // perform all 401 in sync blocks, to avoid multiply token updates
                val currentToken = getAccessToken() // get currently stored token

                if (currentToken != null && currentToken == token) { // compare current token with token that was stored before, if it was not updated - do update
                    logout()
                }
            }
        }

        return response
    }

    private fun getAccessToken(): String? {
        return tokenManager.token
    }

    open fun setAuthHeader(builder: Request.Builder, token: String?) {
        if (!token.isNullOrEmpty())
            builder.header("Authorization", String.format("Bearer %s", token))
    }

    private fun refreshToken(): String {
        return tokenManager.refreshToken()
    }

    private fun logout() {
        tokenManager.reset()
        // logout your user
    }

    companion object
}