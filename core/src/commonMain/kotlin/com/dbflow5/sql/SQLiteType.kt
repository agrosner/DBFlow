package com.dbflow5.sql

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
}
