package com.indwealth.core.util.manager

import android.content.Context
import android.net.*
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.indwealth.core.ui.CoreActivity

class NetworkConnectivityObserver(var activity: CoreActivity, networkConnectivityChangeListener: NetworkConnectivityChangeListener) : LifecycleObserver {

    private val connectivityManager: ConnectivityManager
    private val networkCallback: AppNetworkCallBack
    val request = NetworkRequest.Builder()

    private inner class AppNetworkCallBack(var networkConnectivityChangeListener: NetworkConnectivityChangeListener, var isInternetConnected: Boolean = false) : ConnectivityManager.NetworkCallback() {

        val handler = Handler(Looper.getMainLooper())

        override fun onUnavailable() {
            super.onUnavailable()
            if (isInternetConnected) {
                isInternetConnected = false
                handler.post {
                    networkConnectivityChangeListener.onInternetConnectivityChanged(isInternetConnected)
                }
            }
        }

        override fun onAvailable(network: Network?) {
            super.onAvailable(network)
            if (!isInternetConnected) {
                isInternetConnected = true
                handler.post {
                    networkConnectivityChangeListener.onInternetConnectivityChanged(isInternetConnected)
                }
            }
        }

        override fun onLost(network: Network?) {
            super.onLost(network)
            if (isInternetConnected) {
                isInternetConnected = false
                handler.post {
                    networkConnectivityChangeListener.onInternetConnectivityChanged(isInternetConnected)
                }
            }
        }
    }

    init {
        (activity as LifecycleOwner).lifecycle.addObserver(this)
        connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        networkCallback = AppNetworkCallBack(networkConnectivityChangeListener, isNetworkConnected())
        request.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        request.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        request.addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun start() {
        connectivityManager.requestNetwork(request.build(), networkCallback)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun stop() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    /**
     * @return true if Network is available on device, otherwise false.
     */
    fun isNetworkConnected(): Boolean {
        val ni: NetworkInfo? = connectivityManager.activeNetworkInfo
        return ni != null
    }

    interface NetworkConnectivityChangeListener {
        fun onInternetConnectivityChanged(internetAvailable: Boolean)
    }
}