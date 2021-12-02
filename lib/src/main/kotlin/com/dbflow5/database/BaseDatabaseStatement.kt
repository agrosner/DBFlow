package com.dbflow5.database

/**
 * Description: Default implementation for some [DatabaseStatement] methods.
 */
abstract class BaseDatabaseStatement : DatabaseStatement {

    override fun bindStringOrNull(index: Int, s: String?) {
        if (s != null) {
            bindString(index, s)
        } else {
            bindNull(index)
        }
    }

    override fun bindNumber(index: Int, number: Number?) {
        bindNumberOrNull(index, number)
    }

    override fun bindNumberOrNull(index: Int, number: Number?) {
        if (number != null) {
            bindLong(index, number.toLong())
        } else {
            bindNull(index)
        }
    }

    override fun bindLongOrNull(index: Int, aLong: Long?) {
        if (aLong != null) {
            bindLong(index, aLong)
        } else {
            bindNull(index)
        }
    }

    override fun bindDoubleOrNull(index: Int, aDouble: Double?) {
        if (aDouble != null) {
            bindDouble(index, aDouble)
        } else {
            bindNull(index)
        }
    }

    override fun bindFloatOrNull(index: Int, aFloat: Float?) {
        if (aFloat != null) {
            bindDouble(index, aFloat.toDouble())
        } else {
            bindNull(index)
        }
    }

    override fun bindBlobOrNull(index: Int, bytes: ByteArray?) {
        if (bytes != null) {
            bindBlob(index, bytes)
        } else {
            bindNull(index)
        }
    }

}
