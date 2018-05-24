package com.dbflow5.sql

import com.dbflow5.data.Blob

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

        private val sTypeMap = hashMapOf<String, SQLiteType>(
                Byte::class.javaPrimitiveType!!.name to INTEGER,
                Short::class.javaPrimitiveType!!.name to INTEGER,
                Int::class.javaPrimitiveType!!.name to INTEGER,
                Long::class.javaPrimitiveType!!.name to INTEGER,
                Float::class.javaPrimitiveType!!.name to REAL,
                Double::class.javaPrimitiveType!!.name to REAL,
                Boolean::class.javaPrimitiveType!!.name to INTEGER,
                Char::class.javaPrimitiveType!!.name to TEXT,
                ByteArray::class.java.name to BLOB,
                Byte::class.java.name to INTEGER,
                Short::class.java.name to INTEGER,
                Int::class.java.name to INTEGER,
                Long::class.java.name to INTEGER,
                Float::class.java.name to REAL,
                Double::class.java.name to REAL,
                Boolean::class.java.name to INTEGER,
                Char::class.java.name to TEXT,
                CharSequence::class.java.name to TEXT,
                String::class.java.name to TEXT,
                Array<Byte>::class.java.name to BLOB,
                Blob::class.java.name to BLOB)

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
