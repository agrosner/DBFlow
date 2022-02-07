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

    fun bindNull(index: Int)

    fun bindLong(index: Int, aLong: Long)

    fun bindDouble(index: Int, aDouble: Double)

    fun bindBlob(index: Int, bytes: ByteArray)

    fun bindAllArgsAsStrings(selectionArgs: Array<String>?)

    fun bindStringOrNull(index: Int, s: String?) {
        if (s != null) {
            bindString(index, s)
        } else {
            bindNull(index)
        }
    }

    fun bindNumber(index: Int, number: Number?) {
        bindNumberOrNull(index, number)
    }

    fun bindNumberOrNull(index: Int, number: Number?) {
        if (number != null) {
            bindLong(index, number.toLong())
        } else {
            bindNull(index)
        }
    }

    fun bindLongOrNull(index: Int, aLong: Long?) {
        if (aLong != null) {
            bindLong(index, aLong)
        } else {
            bindNull(index)
        }
    }

    fun bindDoubleOrNull(index: Int, aDouble: Double?) {
        if (aDouble != null) {
            bindDouble(index, aDouble)
        } else {
            bindNull(index)
        }
    }

    fun bindFloatOrNull(index: Int, aFloat: Float?) {
        if (aFloat != null) {
            bindDouble(index, aFloat.toDouble())
        } else {
            bindNull(index)
        }
    }

    fun bindBlobOrNull(index: Int, bytes: ByteArray?) {
        if (bytes != null) {
            bindBlob(index, bytes)
        } else {
            bindNull(index)
        }
    }
}

inline fun DatabaseStatement.bind(index: Int, value: String) {
    bindString(index, value)
}

@JvmName("bindNullable")
inline fun DatabaseStatement.bind(index: Int, value: String?) {
    bindStringOrNull(index, value)
}

inline fun DatabaseStatement.bind(index: Int, value: Long) {
    bindLong(index, value)
}

@JvmName("bindNullable")
inline fun DatabaseStatement.bind(index: Int, value: Long?) {
    bindLongOrNull(index, value)
}

inline fun DatabaseStatement.bind(index: Int, value: Number) {
    bindNumber(index, value)
}

@JvmName("bindNullable")
inline fun DatabaseStatement.bind(index: Int, value: Number?) {
    bindNumberOrNull(index, value)
}

inline fun DatabaseStatement.bind(index: Int, value: Double) {
    bindDouble(index, value)
}

@JvmName("bindNullable")
inline fun DatabaseStatement.bind(index: Int, value: Double?) {
    bindDoubleOrNull(index, value)
}


inline fun DatabaseStatement.bind(index: Int, value: Float) {
    bindDouble(index, value.toDouble())
}

@JvmName("bindNullable")
inline fun DatabaseStatement.bind(index: Int, value: Float?) {
    bindFloatOrNull(index, value)
}

inline fun DatabaseStatement.bind(index: Int, value: ByteArray) {
    bindBlob(index, value)
}

@JvmName("bindNullable")
inline fun DatabaseStatement.bind(index: Int, value: ByteArray?) {
    bindBlobOrNull(index, value)
}

inline fun DatabaseStatement.bind(index: Int, value: Boolean) {
    bindLong(index, if (value) 1 else 0)
}

inline fun DatabaseStatement.bind(index: Int, value: Boolean?) {
    if (value != null) {
        bind(index, value)
    } else {
        bindNull(index)
    }
}

/**
 * Bind enum values
 */
inline fun DatabaseStatement.bind(index: Int, value: Enum<*>) {
    bindString(index, value.name)
}

@JvmName("bindNullable")
inline fun DatabaseStatement.bind(index: Int, value: Enum<*>?) {
    bindStringOrNull(index, value?.name)
}
