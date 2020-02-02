package viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.indwealth.core.BaseApplication
import data.ZomatoRepository

class ZomatoViewModelFactory(
    private val application: Application
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        require(modelClass == ZomatoViewModel::class.java) { "Unknown ViewModel class" }
        return ZomatoViewModel(
            application,
            ZomatoRepository.getInstance((application as BaseApplication).retrofitFactory)
        ) as T
    }
}