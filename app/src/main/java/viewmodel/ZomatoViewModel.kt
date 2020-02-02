package viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.indwealth.core.rest.data.DEFAULT_ERROR_MESSAGE
import com.indwealth.core.rest.data.Result
import com.indwealth.core.ui.ViewState
import data.ZomatoRepository
import kotlinx.coroutines.launch
import model.SearchResponse

class ZomatoViewModel constructor(
    application: Application,
    private val zomatoRepo: ZomatoRepository
) : AndroidViewModel(application) {

    private val _liveDataRestaurantsList = MutableLiveData<ViewState<SearchResponse>>()

    val liveDataRestaurantsList: LiveData<ViewState<SearchResponse>> = _liveDataRestaurantsList
    private var searchTextSaved: String = ""


    init {
        getRestaurantsData()
    }

    fun getRestaurantsData(searchText: String = "", cost: String = "cost", count: Int = 15) {
        if (searchTextSaved.isEmpty()) {
            searchTextSaved = searchText
        }

        if (searchText.isNotEmpty()) {
            searchTextSaved = searchText
        }


        viewModelScope.launch {
            if (_liveDataRestaurantsList.value !is ViewState.Data) {
                _liveDataRestaurantsList.value = ViewState.Loading
            }

            _liveDataRestaurantsList.value =
                when (val result = zomatoRepo.getSummary(searchTextSaved, cost, count)) {
                    is Result.Success -> ViewState.Data(result.data)
                    is Result.Error -> ViewState.Error(result.error.message)
                    is Result.SuccessWithNoContent -> ViewState.Error(DEFAULT_ERROR_MESSAGE)
                }
        }
    }


}