package com.raizlabs.dbflow5.threading

/**
 * Handles [ITransactionQueue threading].
 */
class ThreadManager(private val name: String) {

    private val threadConfigurator = ThreadConfigurator()

    fun run() {
        threadConfigurator.configureForBackground()
    }

    fun quit() {

    }
}
