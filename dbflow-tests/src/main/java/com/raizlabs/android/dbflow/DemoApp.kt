package com.raizlabs.android.dbflow

import android.app.Application
import android.content.Context

class DemoApp : Application() {

    companion object {
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
    }
}