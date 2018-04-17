package com.raizlabs.dbflow5.sql

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

        private val typeMap = platformTypeMap

        /**
         * Returns the [SQLiteType] for this class
         *
         * @param className The fully qualified class name
         * @return The type from the class name
         */

        operator fun get(className: String): SQLiteType? = typeMap[className]

        fun containsClass(className: String): Boolean = typeMap.containsKey(className)
    }
}

/**
 * The type map that we add to the contained type map specific to a platform.
 */
expect val platformTypeMap: Map<String, SQLiteType>
