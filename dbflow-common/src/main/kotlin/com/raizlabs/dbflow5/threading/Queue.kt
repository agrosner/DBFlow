package com.raizlabs.dbflow5.threading

expect interface Queue<T : Any> {

    fun take(): T

    fun remove(): T
}

expect abstract class AbstractQueue<T : Any>() : MutableCollection<T>, Queue<T>

expect class LinkedBlockingQueue<T : Any>() : AbstractQueue<T>
