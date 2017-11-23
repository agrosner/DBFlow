package com.raizlabs.android.dbflow.structure.database

import android.content.ContentValues

operator fun ContentValues.set(key: String, value: String?) = put(key, value)

operator fun ContentValues.set(key: String, value: Byte?) = put(key, value)

operator fun ContentValues.set(key: String, value: Short?) = put(key, value)

operator fun ContentValues.set(key: String, value: Int?) = put(key, value)

operator fun ContentValues.set(key: String, value: Long?) = put(key, value)

operator fun ContentValues.set(key: String, value: Float?) = put(key, value)

operator fun ContentValues.set(key: String, value: Double?) = put(key, value)

operator fun ContentValues.set(key: String, value: Boolean?) = put(key, value)

operator fun ContentValues.set(key: String, value: ByteArray?) = put(key, value)



