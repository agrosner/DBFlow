package com.raizlabs.dbflow5.threading

import com.raizlabs.dbflow5.Runnable


interface RunnableHandler {
    fun post(runnable: Runnable): Boolean
}

expect class SameThreadRunnableHandler() : RunnableHandler

expect class MainThreadRunnableHandler() : RunnableHandler
