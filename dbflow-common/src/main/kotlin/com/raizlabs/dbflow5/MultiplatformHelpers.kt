package com.raizlabs.dbflow5


interface RunnableHandler {
    fun post(runnable: Runnable): Boolean
}

expect class SameThreadRunnableHandler() : RunnableHandler

expect class MainThreadRunnableHandler() : RunnableHandler
