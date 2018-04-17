package com.raizlabs.dbflow5

actual typealias JvmStatic = kotlin.jvm.JvmStatic
actual typealias JvmOverloads = kotlin.jvm.JvmOverloads
actual typealias KClass<T> = kotlin.reflect.KClass<T>
actual typealias Synchronized = kotlin.jvm.Synchronized
actual typealias SafeVarargs = java.lang.SafeVarargs

actual typealias Closeable = java.io.Closeable
actual typealias AutoCloseable = java.lang.AutoCloseable

actual inline fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R {
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        this.closeFinally(exception)
    }
}

fun AutoCloseable?.closeFinally(cause: Throwable?) = when {
    this == null -> {
    }
    cause == null -> close()
    else ->
        try {
            close()
        } catch (closeException: Throwable) {
            cause.addSuppressed(closeException)
        }
}

actual typealias Runnable = java.lang.Runnable

actual val <T : Any> T.kClass: KClass<out T>
    get() = this::class

actual typealias Transient = kotlin.jvm.Transient

actual typealias CallSuper = android.support.annotation.CallSuper
