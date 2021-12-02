package com.dbflow5.database

import java.io.Closeable

/**
 * Description: Abstracts out an Android SQLiteStatement.
 */
interface DatabaseStatement : Closeable {

    fun executeUpdateDelete(): Long

    fun execute()

    override fun close()

    fun simpleQueryForLong(): Long

    fun simpleQueryForString(): String?

    fun executeInsert(): Long

    fun bindString(index: Int, s: String)

    fun bindStringOrNull(index: Int, s: String?)

    fun bindNull(index: Int)

    fun bindLong(index: Int, aLong: Long)

    fun bindLongOrNull(index: Int, aLong: Long?)

    fun bindNumber(index: Int, number: Number?)

    fun bindNumberOrNull(index: Int, number: Number?)

    fun bindDouble(index: Int, aDouble: Double)

    fun bindDoubleOrNull(index: Int, aDouble: Double?)

    fun bindFloatOrNull(index: Int, aFloat: Float?)

    fun bindBlob(index: Int, bytes: ByteArray)

    fun bindBlobOrNull(index: Int, bytes: ByteArray?)

    fun bindAllArgsAsStrings(selectionArgs: Array<String>?)
}

fun DatabaseStatement.bind(index: Int, value: String) {
    bindString(index, value)
}

@JvmName("bindNullable")
fun DatabaseStatement.bind(index: Int, value: String?) {
    bindStringOrNull(index, value)
}

fun DatabaseStatement.bind(index: Int, value: Long) {
    bindLong(index, value)
}

@JvmName("bindNullable")
fun DatabaseStatement.bind(index: Int, value: Long?) {
    bindLongOrNull(index, value)
}

fun DatabaseStatement.bind(index: Int, value: Number) {
    bindNumber(index, value)
}

@JvmName("bindNullable")
fun DatabaseStatement.bind(index: Int, value: Number?) {
    bindNumberOrNull(index, value)
}

fun DatabaseStatement.bind(index: Int, value: Double) {
    bindDouble(index, value)
}

@JvmName("bindNullable")
fun DatabaseStatement.bind(index: Int, value: Double?) {
    bindDoubleOrNull(index, value)
}


fun DatabaseStatement.bind(index: Int, value: Float) {
    bindDouble(index, value.toDouble())
}

@JvmName("bindNullable")
fun DatabaseStatement.bind(index: Int, value: Float?) {
    bindFloatOrNull(index, value)
}

fun DatabaseStatement.bind(index: Int, value: ByteArray) {
    bindBlob(index, value)
}

@JvmName("bindNullable")
fun DatabaseStatement.bind(index: Int, value: ByteArray?) {
    bindBlobOrNull(index, value)
}




