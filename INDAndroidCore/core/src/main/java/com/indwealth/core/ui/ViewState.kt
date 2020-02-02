package com.indwealth.core.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

sealed class ViewState<out T : Any> {
    object Loading : ViewState<Nothing>()
    data class Data<out T : Any>(val data: T) : ViewState<T>()
    data class Error(val error: String) : ViewState<Nothing>()
}

fun <T : Any> observeForResponse(vararg listOfLiveData: LiveData<ViewState<*>>): LiveData<Boolean> {
    val finalLiveData: MediatorLiveData<Boolean> = MediatorLiveData()

    listOfLiveData.forEach { liveData ->
        finalLiveData.addSource(liveData) {
            if (it !is ViewState.Loading) {
                finalLiveData.value = true
            }
        }
    }
    return finalLiveData
}