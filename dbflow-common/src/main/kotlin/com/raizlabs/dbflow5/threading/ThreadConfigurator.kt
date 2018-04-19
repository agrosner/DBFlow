package com.raizlabs.dbflow5.threading

expect class ThreadConfigurator() {

    /**
     * Sets the thread priority and handles setting state here.
     */
    fun configureForBackground()

    fun sleep(timeInMillis: Long)

    fun isInterrupted(exception: Exception) : Boolean
}
