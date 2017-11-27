package com.raizlabs.android.dbflow.database

import android.database.sqlite.SQLiteStatement

/**
 * Description: Abstracts out a [SQLiteStatement].
 */
interface DatabaseStatement {

    fun executeUpdateDelete(): Long

    fun execute()

    fun close()

    fun simpleQueryForLong(): Long

    fun simpleQueryForString(): String?

    fun executeInsert(): Long

    fun bindString(index: Int, s: String)

    fun bindStringOrNull(index: Int, s: String?)

    fun bindNull(index: Int)

    fun bindLong(index: Int, aLong: Long)

    fun bindNumber(index: Int, number: Number?)

    fun bindNumberOrNull(index: Int, number: Number?)

    fun bindDouble(index: Int, aDouble: Double)

    fun bindDoubleOrNull(index: Int, aDouble: Double?)

    fun bindFloatOrNull(index: Int, aFloat: Float?)

    fun bindBlob(index: Int, bytes: ByteArray)

    fun bindBlobOrNull(index: Int, bytes: ByteArray?)

}
