package data


import android.content.Intent
import android.net.Uri
import com.indwealth.core.ui.CoreActivity

abstract class FeatureBaseActivity : CoreActivity() {


    override var shouldShowKeyGuard = {
        corePrefs.screenLockEnabled ?: false
    }

    fun openDeeplink(url: String) {
        if (url.isEmpty()) return

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.`package` = packageName
        startActivity(intent)
    }

}