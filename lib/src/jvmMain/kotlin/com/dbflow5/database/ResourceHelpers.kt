package com.dbflow5.database

import java.io.File

internal inline fun <reified T> resourceFile(name: String) = T::class.java.classLoader
    ?.getResource(name)?.let {
        File(it.toURI())
    }
