package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.data.Blob

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
                Byte::class.javaPrimitiveType!!.name to SQLiteType.INTEGER,
                Short::class.javaPrimitiveType!!.name to SQLiteType.INTEGER,
                Int::class.javaPrimitiveType!!.name to SQLiteType.INTEGER,
                Long::class.javaPrimitiveType!!.name to SQLiteType.INTEGER,
                Float::class.javaPrimitiveType!!.name to SQLiteType.REAL,
                Double::class.javaPrimitiveType!!.name to SQLiteType.REAL,
                Boolean::class.javaPrimitiveType!!.name to SQLiteType.INTEGER,
                Char::class.javaPrimitiveType!!.name to SQLiteType.TEXT,
                ByteArray::class.java.name to SQLiteType.BLOB,
                Byte::class.java.name to SQLiteType.INTEGER,
                Short::class.java.name to SQLiteType.INTEGER,
                Int::class.java.name to SQLiteType.INTEGER,
                Long::class.java.name to SQLiteType.INTEGER,
                Float::class.java.name to SQLiteType.REAL,
                Double::class.java.name to SQLiteType.REAL,
                Boolean::class.java.name to SQLiteType.INTEGER,
                Char::class.java.name to SQLiteType.TEXT,
                CharSequence::class.java.name to SQLiteType.TEXT,
                String::class.java.name to SQLiteType.TEXT,
                Array<Byte>::class.java.name to SQLiteType.BLOB,
                Blob::class.java.name to SQLiteType.BLOB)

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
