package com.raizlabs.android.dbflow.annotation

/**
 * Represents a SQL Collate method for comparing string columns.
 */
enum class Collate {

    /**
     * Tells the table creation and condition that we wont use collation
     */
    NONE,

    /**
     * Compares string data normally
     */
    BINARY,

    /**
     * Ignores case for the 26 upper case characters of ASCII are folded to lower case equivalents.
     * Does not attempt to fold UTF so be careful!
     */
    NOCASE,

    /**
     * Trims trailing space characters before performing comparison.
     */
    RTRIM,

    /**
     * Takes the current locale data into account.
     */
    LOCALIZED,

    /**
     * The Unicode Collation Algorithm and not tailored to the current locale.
     */
    UNICODE
}
