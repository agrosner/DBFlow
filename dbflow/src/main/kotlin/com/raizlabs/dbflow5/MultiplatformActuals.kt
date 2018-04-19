package com.raizlabs.dbflow5

import android.os.Handler
import android.os.Looper

actual class SameThreadRunnableHandler : Handler(Looper.myLooper()), RunnableHandler
actual class MainThreadRunnableHandler : Handler(Looper.getMainLooper()), RunnableHandler
