package com.indwealth.core.util

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.appsflyer.AppsFlyerLib
import com.crashlytics.android.Crashlytics
import com.segment.analytics.Analytics
import com.segment.analytics.Properties
import com.segment.analytics.Traits

/**
 * The track method is how you record any actions your users perform. Each action is known by a
 * name, like 'Purchased a T-Shirt'. You can also record properties specific to those actions. For
 * example a 'Purchased a Shirt' event might have properties like revenue or size.
 *
 * E.g: track("Goal deleted", "goalName" to goal.name, "isOnTrack" to goal.isOnTrack)
 *
 * @param event Name of the event. Must not be null or empty.
 * @param attributes to add extra information to this call.
 * @see <a href="https://segment.com/docs/spec/track/">Track Documentation</a>
 */
fun Context.track(event: String, vararg attributes: Pair<String, Any>, sendToAppsFlyer: Boolean = false) {
    val propertiesObj = Properties(attributes.size)
    attributes.forEach {
        propertiesObj[it.first] = it.second
    }

    Crashlytics.log(Log.DEBUG, "<Analytics>", "Event: $event\nAttributes: ${getAttrsString(*attributes)}")
    Analytics.with(this).track(event, propertiesObj)
    if (sendToAppsFlyer)
        AppsFlyerLib.getInstance().trackEvent(this, event, propertiesObj)
}

fun Fragment.track(event: String, vararg attributes: Pair<String, Any>, sendToAppsFlyer: Boolean = false) {
    val propertiesObj = Properties(attributes.size)
    attributes.forEach {
        propertiesObj[it.first] = it.second
    }
    Log.d("<Analytics>", "Event: $event\nAttributes: ${getAttrsString(*attributes)}")
    if (sendToAppsFlyer)
        AppsFlyerLib.getInstance().trackEvent(context, event, propertiesObj)
    Analytics.with(context).track(event, propertiesObj)
}

/**
 * Identify lets you tie one of your users and their actions to a recognizable userId. It
 * also lets you record traits about the user, like their email, name, account type, etc.
 *
 * <p>Traits and userId will be automatically cached and available on future sessions for the same
 * user. To update a trait on the server, call identify with the same user id (or null).
 *
 * @param traits Traits about the user.
 * @see <a href="https://segment.com/docs/spec/identify/">Identify Documentation</a>.
 */
fun Context.identify(vararg traits: Pair<String, Any>) {
    val traitsObj = Traits()
    traits.forEach { traitsObj[it.first] = it.second }
//    Analytics.with(this).identify(Prefs(this).userProfileBasic.id.toString(), traitsObj, null)
}

private fun getAttrsString(vararg traits: Pair<String, Any>): String {
    var s = ""
    traits.forEach { s += ", (${it.first}: ${it.second}) " }
    return s
}