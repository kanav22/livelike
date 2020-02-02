package com.indwealth.core.util.manager

import android.app.Activity
import android.app.Application
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class InAppUpdateManager private constructor(application: Application) {

    private val appUpdateManager: AppUpdateManager by lazy { AppUpdateManagerFactory.create(application) }
    private var activity: Activity? = null
    private var immediate: Boolean? = null
    private var notNowTapped: Boolean = false
    private val listener: InstallStateUpdatedListener by lazy {
        InstallStateUpdatedListener {
            onStateUpdate(it)
        }
    }

    fun attachActivity(activity: Activity) {
        this.activity = activity
    }

    fun detachActivity() {
        activity = null
    }

    fun isFlexibleUpdate(): Boolean {
        return immediate == false
    }

    fun checkForUpdate(updateAvailable: (updateAvailability: Int, installStatus: Int) -> Unit) {
        appUpdateManager.appUpdateInfo.addOnCompleteListener {
            if (it.isSuccessful && it.result != null) {
                updateAvailable.invoke(it.result.updateAvailability(), it.result.installStatus())
            } else {
                if (it.exception != null) {
                    it.exception.printStackTrace()
                }
                updateAvailable.invoke(UpdateAvailability.UNKNOWN, InstallStatus.UNKNOWN)
            }
        }
    }

    fun startUpdate(immediate: Boolean) {
        this.immediate = immediate
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(if (immediate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE)) {
                requestForUpdate(appUpdateInfo)
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                if (immediate) {
                    // If an in-app update is already running, resume the update.
                    requestForUpdate(appUpdateInfo)
                } else {
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        // If the update is downloaded but not installed,
                        // complete the update.
                        appUpdateManager.completeUpdate()
                    }
                }
            }
        }

        appUpdateManager.appUpdateInfo.addOnFailureListener {
            it.printStackTrace()
        }
    }

    fun startUpdate(immediate: Boolean, updatedListener: (started: Boolean) -> Unit) {
        Log.d("InAppUpdateManager", "startUpdate")
        this.immediate = immediate
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            Log.d("InAppUpdateManager", "appUpdateInfo = ${appUpdateInfo.updateAvailability()} ${appUpdateInfo.packageName()}")

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(if (immediate) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE)) {
                requestForUpdate(appUpdateInfo)
                updatedListener.invoke(true)
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                if (immediate) {
                    // If an in-app update is already running, resume the update.
                    requestForUpdate(appUpdateInfo)
                    updatedListener.invoke(true)
                } else {
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                        // If the update is downloaded but not installed,
                        // complete the update.
                        appUpdateManager.completeUpdate()
                        updatedListener.invoke(true)
                    } else {
                        updatedListener.invoke(false)
                    }
                }
            } else {
                updatedListener.invoke(false)
            }
        }

        appUpdateManager.appUpdateInfo.addOnFailureListener {
            updatedListener.invoke(false)
            it.printStackTrace()
        }
    }

    private fun requestForUpdate(appUpdateInfo: AppUpdateInfo) {
        if (activity == null) return
        if (immediate == false) {
            appUpdateManager.registerListener(listener)
        }
        val updateType = if (immediate == true) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
        appUpdateManager.startUpdateFlowForResult(
            appUpdateInfo,
            updateType,
            activity,
            REQUEST_CODE_UPDATE)
    }

    fun resume() {
        if (immediate != null) {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                    if (immediate == true) {
                        // If an in-app update is already running, resume the update.
                        requestForUpdate(appUpdateInfo)
                    } else {
                        if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED && !notNowTapped) {
                            // If the update is downloaded but not installed,
                            // notify the user to complete the update.
                            showDownloadedPopUp()
                        }
                    }
                }
            }
        }
    }

    private fun onStateUpdate(state: InstallState) {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // After the update is downloaded, request user confirmation to restart the app.
            showDownloadedPopUp()
            appUpdateManager.unregisterListener(listener)
        } else if (state.installStatus() == InstallStatus.CANCELED || state.installStatus() == InstallStatus.FAILED) {
            appUpdateManager.unregisterListener(listener)
        }
    }

    /**
     * Displays the snack bar notification and call to action.
     */
    private fun showDownloadedPopUp() {
        activity?.showInfoDialog(message = "An update has just been downloaded.",
            positiveButton = "Restart",
            negativeButton = "Not now",
            callback = {
                if (it == DialogCallback.Positive) {
                    appUpdateManager.completeUpdate()
                } else {
                    notNowTapped = true
                }
            })
    }

    companion object : SingletonHolder<InAppUpdateManager, Application>(::InAppUpdateManager)
}

const val REQUEST_CODE_UPDATE = 8002