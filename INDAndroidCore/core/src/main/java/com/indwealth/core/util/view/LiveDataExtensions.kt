package com.indwealth.core.util.view

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/**
 * zips both of the LiveData and emits a value after both of them have emitted their values,
 * after that, emits values whenever any of them emits a value.
 *
 * The difference between combineLatest and zip is that the zip only emits after all LiveData
 * objects have a new value, but combineLatest will emit after any of them has a new value.
 *
 *
 * @see https://github.com/adibfara/Lives/blob/master/lives/src/main/java/com/snakydesign/livedataextensions/Combining.kt
 */
fun <T, Y> zip(first: LiveData<T>, second: LiveData<Y>): LiveData<Pair<T?, Y?>> {
    return zip(first, second) { t, y -> Pair(t, y) }
}

fun <T, Y, Z> zip(first: LiveData<T>, second: LiveData<Y>, zipFunction: (T?, Y?) -> Z): LiveData<Z> {
    val finalLiveData: MediatorLiveData<Z> = MediatorLiveData()

    var firstEmitted = false
    var firstValue: T? = null

    var secondEmitted = false
    var secondValue: Y? = null
    finalLiveData.addSource(first) { value ->
        firstEmitted = true
        firstValue = value
        if (firstEmitted && secondEmitted) {
            finalLiveData.value = zipFunction(firstValue, secondValue)
            firstEmitted = false
            secondEmitted = false
        }
    }
    finalLiveData.addSource(second) { value ->
        secondEmitted = true
        secondValue = value
        if (firstEmitted && secondEmitted) {
            finalLiveData.value = zipFunction(firstValue, secondValue)
            firstEmitted = false
            secondEmitted = false
        }
    }
    return finalLiveData
}

/**
 * zips three LiveData and emits a value after all of them have emitted their values,
 * after that, emits values whenever any of them emits a value.
 *
 * The difference between combineLatest and zip is that the zip only emits after all LiveData
 * objects have a new value, but combineLatest will emit after any of them has a new value.
 */
fun <T, Y, X, Z> zip(first: LiveData<T>, second: LiveData<Y>, third: LiveData<X>, zipFunction: (T, Y, X) -> Z): LiveData<Z> {
    val finalLiveData: MediatorLiveData<Z> = MediatorLiveData()

    var firstEmitted = false
    var firstValue: T? = null

    var secondEmitted = false
    var secondValue: Y? = null

    var thirdEmitted = false
    var thirdValue: X? = null
    finalLiveData.addSource(first) { value ->
        firstEmitted = true
        firstValue = value
        if (firstEmitted && secondEmitted && thirdEmitted) {
            finalLiveData.value = zipFunction(firstValue!!, secondValue!!, thirdValue!!)
            firstEmitted = false
            secondEmitted = false
            thirdEmitted = false
        }
    }

    finalLiveData.addSource(second) { value ->
        secondEmitted = true
        secondValue = value
        if (firstEmitted && secondEmitted && thirdEmitted) {
            firstEmitted = false
            secondEmitted = false
            thirdEmitted = false
            finalLiveData.value = zipFunction(firstValue!!, secondValue!!, thirdValue!!)
        }
    }

    finalLiveData.addSource(third) { value ->
        thirdEmitted = true
        thirdValue = value
        if (firstEmitted && secondEmitted && thirdEmitted) {
            firstEmitted = false
            secondEmitted = false
            thirdEmitted = false
            finalLiveData.value = zipFunction(firstValue!!, secondValue!!, thirdValue!!)
        }
    }

    return finalLiveData
}

/**
 * Combines the latest values from two LiveData objects.
 * First emits after both LiveData objects have emitted a value, and will emit afterwards after any
 * of them emits a new value.
 *
 * The difference between combineLatest and zip is that the zip only emits after all LiveData
 * objects have a new value, but combineLatest will emit after any of them has a new value.
 */
fun <X, T, Z> combineLatest(first: LiveData<X>, second: LiveData<T>, combineFunction: (X?, T?) -> Z): LiveData<Z> {
    val finalLiveData: MediatorLiveData<Z> = MediatorLiveData()

    var firstEmitted = false
    var firstValue: X? = null

    var secondEmitted = false
    var secondValue: T? = null
    finalLiveData.addSource(first) { value ->
        firstEmitted = true
        firstValue = value
        if (firstEmitted && secondEmitted) {
            finalLiveData.value = combineFunction(firstValue, secondValue)
        }
    }
    finalLiveData.addSource(second) { value ->
        secondEmitted = true
        secondValue = value
        if (firstEmitted && secondEmitted) {
            finalLiveData.value = combineFunction(firstValue, secondValue)
        }
    }
    return finalLiveData
}

/**
 * Combines the latest values from two LiveData objects.
 * First emits after both LiveData objects have emitted a value, and will emit afterwards after any
 * of them emits a new value.
 *
 * The difference between combineLatest and zip is that the zip only emits after all LiveData
 * objects have a new value, but combineLatest will emit after any of them has a new value.
 */
fun <P, Q, R, S, T, Z> combineLatest(
    first: LiveData<P>,
    second: LiveData<Q>,
    third: LiveData<R>,
    fourth: LiveData<S>,
    fifth: LiveData<T>,
    combineFunction: (P, Q, R, S, T) -> Z
): LiveData<Z> {
    val finalLiveData: MediatorLiveData<Z> = MediatorLiveData()

    var firstEmitted = false
    var firstValue: P? = null

    var secondEmitted = false
    var secondValue: Q? = null

    var thirdEmitted = false
    var thirdValue: R? = null

    var fourthEmitted = false
    var fourthValue: S? = null

    var fifthEmitted = false
    var fifthValue: T? = null

    fun emitFinal() {
        if (firstEmitted && secondEmitted && thirdEmitted && fourthEmitted && fifthEmitted) {
            finalLiveData.value = combineFunction(firstValue!!, secondValue!!, thirdValue!!, fourthValue!!, fifthValue!!)
        }
    }

    finalLiveData.addSource(first) { value ->
        firstEmitted = true
        firstValue = value
        emitFinal()
    }
    finalLiveData.addSource(second) { value ->
        secondEmitted = true
        secondValue = value
        emitFinal()
    }
    finalLiveData.addSource(third) { value ->
        thirdEmitted = true
        thirdValue = value
        emitFinal()
    }

    finalLiveData.addSource(fourth) { value ->
        fourthEmitted = true
        fourthValue = value
        emitFinal()
    }

    finalLiveData.addSource(fifth) { value ->
        fifthEmitted = true
        fifthValue = value
        emitFinal()
    }

    return finalLiveData
}

/**
 * Emits the `startingValue` before any other value.
 */
fun <T> LiveData<T>.startWith(startingValue: T?): LiveData<T> {
    val finalLiveData: MediatorLiveData<T> = MediatorLiveData()
    finalLiveData.value = startingValue
    finalLiveData.addSource(this) { source ->
        finalLiveData.value = source
    }
    return finalLiveData
}