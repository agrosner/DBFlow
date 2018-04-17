package com.raizlabs.dbflow5

import android.os.Handler
import android.os.Looper

actual class RunnableHandler : Handler(Looper.myLooper())
