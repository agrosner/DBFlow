package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.data.Blob
import java.util.*

/**
 * Description: Represents a type that SQLite understands.
 */
enum class SQLiteType {

    /**
     * Represents an integer number in the DB.
     */
    INTEGER,

    /**
     * Represents a floating-point, precise number.
     */
    REAL,

    /**
     * Represents text.
     */
    TEXT,

    /**
     * A column defined by [byte[]] data. Usually reserved for images or larger pieces of content.
     */
    BLOB;


    companion object {

        private val sTypeMap = object : HashMap<String, SQLiteType>() {
            init {
                put(Byte::class.javaPrimitiveType!!.name, SQLiteType.INTEGER)
                put(Short::class.javaPrimitiveType!!.name, SQLiteType.INTEGER)
                put(Int::class.javaPrimitiveType!!.name, SQLiteType.INTEGER)
                put(Long::class.javaPrimitiveType!!.name, SQLiteType.INTEGER)
                put(Float::class.javaPrimitiveType!!.name, SQLiteType.REAL)
                put(Double::class.javaPrimitiveType!!.name, SQLiteType.REAL)
                put(Boolean::class.javaPrimitiveType!!.name, SQLiteType.INTEGER)
                put(Char::class.javaPrimitiveType!!.name, SQLiteType.TEXT)
                put(ByteArray::class.java.name, SQLiteType.BLOB)
                put(Byte::class.java.name, SQLiteType.INTEGER)
                put(Short::class.java.name, SQLiteType.INTEGER)
                put(Int::class.java.name, SQLiteType.INTEGER)
                put(Long::class.java.name, SQLiteType.INTEGER)
                put(Float::class.java.name, SQLiteType.REAL)
                put(Double::class.java.name, SQLiteType.REAL)
                put(Boolean::class.java.name, SQLiteType.INTEGER)
                put(Char::class.java.name, SQLiteType.TEXT)
                put(CharSequence::class.java.name, SQLiteType.TEXT)
                put(String::class.java.name, SQLiteType.TEXT)
                put(Array<Byte>::class.java.name, SQLiteType.BLOB)
                put(Blob::class.java.name, SQLiteType.BLOB)
            }
        }

        /**
         * Returns the [SQLiteType] for this class
         *
         * @param className The fully qualified class name
         * @return The type from the class name
         */
        operator fun get(className: String): SQLiteType? = sTypeMap[className]

        fun containsClass(className: String): Boolean = sTypeMap.containsKey(className)
    }
}
