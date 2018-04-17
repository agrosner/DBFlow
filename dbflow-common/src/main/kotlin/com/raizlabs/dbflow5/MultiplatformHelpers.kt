package com.raizlabs.dbflow5

expect class RunnableHandler() {

    fun post(runnable: Runnable): Boolean
}
