package com.indwealth.core.rest

class ResponseHandler<T>(
    var onSuccess: ((t: T) -> Unit)? = null,
    val onComplete: (() -> Unit)? = null,
    var onFail: (() -> Unit)? = null
)

class ResponseHandlerWithResponseCode<T>(
    var onSuccess: ((code: Int, t: T) -> Unit)? = null,
    val onComplete: ((code: Int) -> Unit)? = null,
    var onFail: ((code: Int) -> Unit)? = null
)