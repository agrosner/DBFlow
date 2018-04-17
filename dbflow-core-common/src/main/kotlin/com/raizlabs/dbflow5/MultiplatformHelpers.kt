package com.raizlabs.dbflow5

expect annotation class JvmStatic()
expect annotation class JvmOverloads()
expect annotation class Transient()
expect annotation class SafeVarargs()

expect interface KClass<T : Any>

expect annotation class Synchronized()

expect interface Closeable : AutoCloseable

expect interface AutoCloseable {
    fun close()
}

expect inline fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R

expect interface Runnable {

    fun run()
}

expect val <T : Any> T.kClass: KClass<out T>


expect annotation class CallSuper()
