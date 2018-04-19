package com.raizlabs.dbflow5.threading

expect class LinkedBlockingQueue<E : Any>() {

    fun take(): E

    fun remove(): E

    fun add(element: E): Boolean

    fun clear()

    fun contains(element: E): Boolean

    fun remove(element: E): Boolean

    fun iterator(): MutableIterator<E>
}
