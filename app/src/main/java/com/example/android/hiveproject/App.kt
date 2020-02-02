package com.example.android.hiveproject

import com.google.android.play.core.missingsplits.MissingSplitsManagerFactory
import com.indwealth.core.BaseApplication

class App : BaseApplication() {
    // base url constant of the zomato website
     private val baseUrl="https://developers.zomato.com/"

    @Deprecated(
        "Use retrofitFactory and use RemoteSource to call the apis (does not show toast on error)",
        replaceWith = ReplaceWith("retrofitFactory")
    )


    override fun getBaseURL(): String {
        return baseUrl
    }

    override fun onCreate() {
        // checks if the app installed has all the components in the bundle that are required to
        // run the app on the device
        if (MissingSplitsManagerFactory.create(this).disableAppIfMissingRequiredSplits()) {
            return
        }

        super.onCreate()

    }

    override fun createRetrofitFactory() {
        retrofitFactory = ServiceGenerator(this, false)
        uploadFactory = ServiceGenerator(this, false, 60)
    }


}