package com.indwealth.core.util.manager

/**
 * Utility class to create Singletons which need any argument in the constructors as object keyword
 * does not allow to have constructors with arguments.
 *
 * Most common usage for this classes would be repositories, database classes, remote sources.
 *
 * Example :-
 *  class Manager private constructor(context: Context) {
 *      init {
 *          // Init using context argument
 *      }
 *
 *      companion object : SingletonHolder<Manager, Context>(::Manager)
 *  }
 *
 */
open class SingletonHolder<out T : Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile
    private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}