package com.indwealth.core

import androidx.multidex.MultiDexApplication
import com.indwealth.core.rest.data.api.RetrofitFactory
import com.indwealth.core.util.manager.TokenManager

abstract class BaseApplication : MultiDexApplication() {

    open lateinit var retrofitFactory: RetrofitFactory
    open lateinit var uploadFactory: RetrofitFactory

    override fun onCreate() {
        super.onCreate()
        initDependencies()
    }

    open fun initDependencies() {
        tokenManager = TokenManager(this)
        createRetrofitFactory()
    }

    open fun createRetrofitFactory() {
        retrofitFactory = RetrofitFactory(getBaseURL())
        uploadFactory = RetrofitFactory(getBaseURL(), 50)
    }

    abstract fun getBaseURL(): String

    open fun onLogout() {}

    companion object {
        lateinit var tokenManager: TokenManager
    }
}