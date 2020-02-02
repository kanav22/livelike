package com.indwealth.core.rest

import androidx.lifecycle.Observer
import com.indwealth.core.rest.data.Result

class ResponseObserver<T : Any>(
    val onLoading: (() -> Unit)? = null,
    var onSuccess: ((t: T) -> Unit)? = null,
    val onComplete: (() -> Unit)? = null,
    var onFail: ((String) -> Unit)? = null
) : Observer<Result<T>> {

    init {
        onLoading?.invoke()
    }

    override fun onChanged(it: Result<T>?) {
        when (it) {
            is Result.Success -> {
                onSuccess?.invoke(it.data)
                onComplete?.invoke()
            }
            is Result.Error -> {
                onFail?.invoke(it.error.message)
                onComplete?.invoke()
            }
        }
    }
}