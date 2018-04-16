package com.raizlabs.dbflow5

expect annotation class JvmStatic()
expect annotation class JvmOverloads()
expect interface KClass<T : Any>
expect annotation class Synchronized()

expect interface Closeable {
    fun close()
}

expect interface AutoCloseable : Closeable

expect inline fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R

expect interface Runnable {

    fun run()
}
